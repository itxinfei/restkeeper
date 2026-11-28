package com.restkeeper.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.restkeeper.constants.OrderDetailType;
import com.restkeeper.constants.OrderPayType;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.dto.CurrentAmountCollectDTO;
import com.restkeeper.dto.CurrentHourCollectDTO;
import com.restkeeper.dto.CreditDTO;
import com.restkeeper.dto.CreditPayRequest;
import com.restkeeper.dto.DetailDTO;
import com.restkeeper.dto.PayType;
import com.restkeeper.dto.PayTypeCollectDTO;
import com.restkeeper.dto.PrivilegeDTO;
import com.restkeeper.entity.OrderDetailEntity;
import com.restkeeper.entity.OrderDetailMealEntity;
import com.restkeeper.entity.OrderEntity;
import com.restkeeper.exception.BussinessException;
import com.restkeeper.http.PageResult;
import com.restkeeper.order.mapper.OrderMapper;
import com.restkeeper.store.entity.Credit;
import com.restkeeper.store.entity.CreditCompanyUser;
import com.restkeeper.store.entity.CreditLogs;
import com.restkeeper.store.entity.Dish;
import com.restkeeper.store.entity.SetMeal;
import com.restkeeper.store.entity.Table;
import com.restkeeper.store.entity.TableLog;
import com.restkeeper.store.service.ICreditCompanyUserService;
import com.restkeeper.store.service.ICreditLogService;
import com.restkeeper.store.service.ICreditService;
import com.restkeeper.store.service.IDishService;
import com.restkeeper.store.service.ISellCalculationService;
import com.restkeeper.store.service.ISetMealDishService;
import com.restkeeper.store.service.ISetMealService;
import com.restkeeper.store.service.ITableLogService;
import com.restkeeper.store.service.ITableService;
import com.restkeeper.tenant.TenantContext;
import com.restkeeper.utils.SequenceUtils;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 订单本地服务：订单库写入走 MyBatis-Plus；沽清、桌台、挂账等门店库操作经 HTTP Interface 代理。
 * 跨库写操作（下单、挂账结账）由 Apache Seata 2.5 {@link GlobalTransactional} 保证最终一致。
 */
@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderEntity> {

    @Autowired
    private OrderDetailServiceImpl orderDetailService;

    @Autowired
    private OrderDetailMealServiceImpl orderDetailMealService;

    @Autowired
    private ISellCalculationService sellCalculationService;

    @Autowired
    private ITableService tableService;

    @Autowired
    private ICreditService creditService;

    @Autowired
    private ICreditCompanyUserService creditCompanyUserService;

    @Autowired
    private ICreditLogService creditLogService;

    @Autowired
    private ITableLogService tableLogService;

    @Autowired
    private ISetMealDishService setMealDishService;

    @Autowired
    private IDishService dishService;

    @Autowired
    private ISetMealService setMealService;

    //下单（跨订单/门店两库，Seata 全局事务）
    @GlobalTransactional
    public String addOrder(OrderEntity orderEntity) {

        //生成订单流水号
        if (!StringUtils.hasText(orderEntity.getOrderNumber())){
            String storeId = TenantContext.getStoreId();
            orderEntity.setOrderNumber(SequenceUtils.getSequence(storeId));
        }
        //必须在入库前判断：saveOrUpdate 会用雪花 ID 回填 orderId，之后新单也会变成非 null
        boolean isNewOrder = orderEntity.getOrderId() == null;

        //t_order.create_time 无数据库默认值，服务端必须补：否则订单时间为 NULL，
        //按起止时间筛选（cash-web /order/page?start=&end=）会漏掉这些单
        if (isNewOrder) {
            orderEntity.setCreateTime(LocalDateTime.now());
        }

        //操作订单详情
        List<OrderDetailEntity> orderDetailEntities = orderEntity.getOrderDetails();
        //明细缺失时给业务提示：microapp 的 submit 不传 orderDetails，
        //直接 forEach 会 NPE 并被全局异常兜成 500
        if (orderDetailEntities == null || orderDetailEntities.isEmpty()) {
            throw new BussinessException("订单缺少菜品明细");
        }

        //单价与总额一律以服务端为准：按菜品/套餐表重算每条明细单价与小计。
        //否则调用方只要传一个不存在的 dishId + 任意低价，就能伪造订单金额（资金漏洞）。
        int totalAmount = recalcDetailAmount(orderDetailEntities);
        if (!isNewOrder) {
            //加菜在库中订单总额基础上累加本次明细，避免逐条回查明细
            OrderEntity exists = this.getById(orderEntity.getOrderId());
            if (exists == null) {
                throw new BussinessException("订单不存在");
            }
            totalAmount += exists.getTotalAmount() == null ? 0 : exists.getTotalAmount();
        }
        orderEntity.setTotalAmount(totalAmount);

        this.saveOrUpdate(orderEntity);

        // 明细状态区分「正常下单」与「加菜」：入参不带 orderId 表示新开订单
        Integer detailStatus = isNewOrder
                ? OrderDetailType.NORMAL_DISH.getType()
                : OrderDetailType.PLUS_DISH.getType();
        orderDetailEntities.forEach(orderDetailEntity -> {
            orderDetailEntity.setOrderId(orderEntity.getOrderId());
            orderDetailEntity.setOrderNumber(SequenceUtils.getSequenceWithPrefix(orderEntity.getOrderNumber()));
            orderDetailEntity.setDetailStatus(detailStatus);

            //沽清检查（租户上下文由 TenantContextHeaderFilter 从请求头还原，HTTP 客户端拦截器透传）
            Integer remainder = sellCalculationService.getRemainderCount(orderDetailEntity.getDishId());
            if (remainder != -1){
                if (remainder < orderDetailEntity.getDishNumber()){
                    throw new BussinessException(orderDetailEntity.getDishName()+"超过沽清设置数目");
                }
            }

            //沽清扣减
            sellCalculationService.decrease(orderDetailEntity.getDishId(),orderDetailEntity.getDishNumber());
        });
        orderDetailService.saveBatch(orderDetailEntities);


        return orderEntity.getOrderId();
    }

    /**
     * 下单/加菜明细入参校验：
     * 缺菜品 ID、类型非法、份数缺失时抛业务异常，
     * 避免 null 进入跨服务路径变量（沽清查询）或触发拆箱 NPE。
     * 单价不在此校验：金额由 {@link #recalcDetailAmount} 按菜品表重算。
     */
    private void checkOrderDetailParam(OrderDetailEntity detail) {
        String name = detail.getDishName() == null ? "" : detail.getDishName();
        if (detail.getDishId() == null || detail.getDishId().isEmpty()) {
            throw new BussinessException("菜品[" + name + "]缺少菜品ID");
        }
        Integer type = detail.getDishType();
        if (type == null || (type != SystemCode.DISH_TYPE_MORMAL && type != SystemCode.DISH_TYPE_SETMEAL)) {
            throw new BussinessException("菜品[" + name + "]类型缺失或非法（1 菜品 / 2 套餐）");
        }
        if (detail.getDishNumber() == null || detail.getDishNumber() <= 0) {
            throw new BussinessException("菜品[" + name + "]份数必须大于 0");
        }
    }

    /**
     * 按菜品/套餐表重算每条明细的单价与小计，返回本次明细合计。
     * 同一单内重复菜品只查一次价，减少跨服务往返。
     */
    private int recalcDetailAmount(List<OrderDetailEntity> details) {
        Map<String, Integer> priceCache = new HashMap<>();
        int sum = 0;
        for (OrderDetailEntity detail : details) {
            checkOrderDetailParam(detail);
            int price = resolveDishPrice(detail, priceCache);
            detail.setDishPrice(price);
            detail.setDishAmount(price * detail.getDishNumber());
            sum += detail.getDishAmount();
        }
        return sum;
    }

    /**
     * 查询菜品/套餐表单价：查不到或已停售直接报业务错误，避免伪造低价单。
     */
    private int resolveDishPrice(OrderDetailEntity detail, Map<String, Integer> priceCache) {
        String dishId = detail.getDishId();
        String name = detail.getDishName() == null ? dishId : detail.getDishName();
        Integer cached = priceCache.get(dishId);
        if (cached != null) {
            return cached;
        }
        Integer price;
        if (SystemCode.DISH_TYPE_SETMEAL == detail.getDishType()) {
            SetMeal setMeal = setMealService.getById(dishId);
            if (setMeal == null) {
                throw new BussinessException("套餐[" + name + "]不存在或已删除");
            }
            if (setMeal.getStatus() == null || setMeal.getStatus() != SystemCode.ENABLED) {
                throw new BussinessException("套餐[" + name + "]已停售");
            }
            //套餐未配置子菜时，原流程要到结账拆明细才报「未配置菜品」，
            //订单已生成却结不了账。这里提前到下单拦截，避免单子卡死。
            List<Dish> subDishes = setMealDishService.getAllDishBySetMealId(dishId);
            if (subDishes == null || subDishes.isEmpty()) {
                throw new BussinessException("套餐[" + name + "]未配置菜品，无法下单");
            }
            price = setMeal.getPrice();
        } else {
            Dish dish = dishService.getById(dishId);
            if (dish == null) {
                throw new BussinessException("菜品[" + name + "]不存在或已删除");
            }
            if (dish.getStatus() == null || dish.getStatus() != SystemCode.ENABLED) {
                throw new BussinessException("菜品[" + name + "]已停售");
            }
            price = dish.getPrice();
        }
        if (price == null || price < 0) {
            throw new BussinessException("菜品[" + name + "]价格未维护，无法下单");
        }
        priceCache.put(dishId, price);
        return price;
    }

    /**
     * 按库内有效明细汇总订单总额（正常下单 + 加菜口径，与销量报表一致）。
     */
    private int sumValidDetailAmount(String orderId) {
        List<OrderDetailEntity> details = orderDetailService.lambdaQuery()
                .eq(OrderDetailEntity::getOrderId, orderId)
                .in(OrderDetailEntity::getDetailStatus,
                        OrderDetailType.NORMAL_DISH.getType(), OrderDetailType.PLUS_DISH.getType())
                .list();
        int sum = 0;
        for (OrderDetailEntity detail : details) {
            if (detail.getDishAmount() != null) {
                sum += detail.getDishAmount();
            }
        }
        return sum;
    }

    /**
     * 抹零 / 免单范围校验：不能为负，合计不能超过订单总额。
     * 这两个金额此前完全由调用方透传，并直接 SUM 进营收报表。
     */
    private void validateDiscountAmount(int totalAmount, OrderEntity orderEntity) {
        int small = orderEntity.getSmallAmount() == null ? 0 : orderEntity.getSmallAmount();
        int free = orderEntity.getFreeAmount() == null ? 0 : orderEntity.getFreeAmount();
        if (small < 0 || free < 0) {
            throw new BussinessException("抹零/免单金额不能为负");
        }
        if (small + free > totalAmount) {
            throw new BussinessException("抹零/免单金额合计不能超过订单总额");
        }
    }

    private int payableAmount(int totalAmount, OrderEntity orderEntity) {
        int small = orderEntity.getSmallAmount() == null ? 0 : orderEntity.getSmallAmount();
        int free = orderEntity.getFreeAmount() == null ? 0 : orderEntity.getFreeAmount();
        return totalAmount - small - free;
    }

    /**
     * 现金类结账实收兜底：未传按应付金额补齐；少收直接拒付（多收走找零，不设上限）。
     */
    private void applyCashPayAmount(OrderEntity orderEntity, int totalAmount) {
        validateDiscountAmount(totalAmount, orderEntity);
        int payable = payableAmount(totalAmount, orderEntity);
        Integer payAmount = orderEntity.getPayAmount();
        if (payAmount == null) {
            orderEntity.setPayAmount(payable);
        } else if (payAmount < payable) {
            throw new BussinessException("实收金额小于应付金额" + (payable / 100.0) + "元");
        }
    }

    /**
     * 挂账结账金额兜底：未填按应付记账；空值/超额挂账拒绝。
     */
    private void applyCreditPayAmount(OrderEntity orderEntity, CreditDTO creditDTO, int totalAmount) {
        validateDiscountAmount(totalAmount, orderEntity);
        int payable = payableAmount(totalAmount, orderEntity);
        Integer creditAmount = creditDTO.getCreditAmount();
        if (creditAmount == null || creditAmount <= 0) {
            creditAmount = payable;
        }
        if (creditAmount > payable) {
            throw new BussinessException("挂账金额超过应付金额" + (payable / 100.0) + "元");
        }
        creditDTO.setCreditAmount(creditAmount);
    }

    //退菜
    @Transactional
    public boolean returnDish(DetailDTO detailDTO) {

        //获取到原有的订单详情信息
        OrderDetailEntity detailEntity = orderDetailService.getById(detailDTO.getDetailId());

        Integer detailStatus = detailEntity.getDetailStatus();
        // 先判空再比较，避免历史数据 detail_status 为 NULL 时拆箱 NPE；用 equals 规避 Integer 缓存陷阱
        boolean normalOrPlus = detailStatus != null
                && (detailStatus.equals(OrderDetailType.PLUS_DISH.getType())
                    || detailStatus.equals(OrderDetailType.NORMAL_DISH.getType()));
        if (normalOrPlus){

            if (detailEntity.getDishNumber() <= 0){
                throw new BussinessException(detailEntity.getDishName()+"已经被退完");
            }

            //产生新的订单详情记录（退菜）
            OrderDetailEntity return_detailEntity = new OrderDetailEntity();
            BeanUtils.copyProperties(detailEntity,return_detailEntity);
            //去掉多余copy字段
            return_detailEntity.setDetailId(null);
            return_detailEntity.setStoreId(null);
            return_detailEntity.setShopId(null);

            return_detailEntity.setOrderNumber(SequenceUtils.getSequenceWithPrefix(detailEntity.getOrderNumber()));
            return_detailEntity.setDetailStatus(OrderDetailType.RETURN_DISH.getType());
            return_detailEntity.setDishNumber(1);
            return_detailEntity.setReturnRemark(detailDTO.getRemarks() == null ? "" : detailDTO.getRemarks().toString());
            orderDetailService.save(return_detailEntity);

            //修改原有订单详情中的信息
            detailEntity.setDishNumber(detailEntity.getDishNumber()-1);
            detailEntity.setDishAmount(detailEntity.getDishNumber()*detailEntity.getDishPrice());
            orderDetailService.updateById(detailEntity);

            //修改订单主表信息
            OrderEntity orderEntity = this.getById(detailEntity.getOrderId());
            orderEntity.setTotalAmount(orderEntity.getTotalAmount()-detailEntity.getDishPrice());
            this.updateById(orderEntity);

            //退菜回补沽清
            Integer remainderCount = sellCalculationService.getRemainderCount(detailEntity.getDishId());
            // getRemainderCount 约定：-1 表示未设置沽清（不限量），无需回补；
            // 其余取值都要回补，含「已售罄 0」——否则售罄菜退菜后永远是 0，再也卖不出去
            if (remainderCount != null && remainderCount >= 0) {
                sellCalculationService.add(detailEntity.getDishId(),1);
            }

        }else {

            throw new BussinessException("不支持退菜操作");
        }

        return true;
    }

    //现金结账
    @Transactional
    public boolean pay(OrderEntity orderEntity) {

        //先确认订单存在：updateById 对不存在的 id 返回 false 且不报错，
        //若不拦住，前端会看到"结账成功"而订单实际未被核销
        String orderId = orderEntity.getOrderId();
        if (!StringUtils.hasText(orderId)) {
            throw new BussinessException("订单不存在");
        }
        OrderEntity dbOrder = this.getById(orderId);
        if (dbOrder == null) {
            throw new BussinessException("订单不存在");
        }

        //桌台等关键字段以库中为准：调用方通常只传 orderId / 支付方式 / 金额，
        //缺 tableId 会让后续跨服务查桌台报路径变量缺失（实测 "Missing path variable value 'id'"）
        if (!StringUtils.hasText(orderEntity.getTableId())) {
            orderEntity.setTableId(dbOrder.getTableId());
        }

        //支付完成由服务端强制置为已支付，避免依赖调用方透传导致"只收钱不核销"
        orderEntity.setPayStatus(SystemCode.ORDER_STATUS_PAYED);
        //订单总额按库内有效明细汇总重算，不信任调用方透传的金额
        int totalAmount = sumValidDetailAmount(orderId);
        orderEntity.setTotalAmount(totalAmount);
        //实收金额服务端兜底并校验下限，防止少收、抹零/免单超过总额
        applyCashPayAmount(orderEntity, totalAmount);

        //修改订单主表的信息
        this.updateById(orderEntity);

        //修改桌台的状态
        if (!StringUtils.hasText(orderEntity.getTableId())) {
            throw new BussinessException("订单缺少桌台信息，无法完成结账");
        }
        Table table = tableService.getById(orderEntity.getTableId());
        if (table == null) {
            throw new BussinessException("订单关联的桌台不存在，无法完成结账");
        }
        table.setStatus(SystemCode.TABLE_STATUS_FREE);
        tableService.updateById(table);

        saveOrderDetailMealInfo(orderEntity.getOrderId());

        return true;
    }

    //挂账结账（跨 order/store 两库，Seata 全局事务）
    @GlobalTransactional
    public boolean payByCredit(CreditPayRequest request) {

        OrderEntity orderEntity = request.getOrder();
        CreditDTO creditDTO = request.getCredit();

        //入参兜底：挂账端点必须带订单与挂账支付方式，否则会把单误标成"已付"而不记挂账
        String orderId = orderEntity.getOrderId();
        if (!StringUtils.hasText(orderId)) {
            throw new BussinessException("订单不存在");
        }
        OrderEntity dbOrder = this.getById(orderId);
        if (dbOrder == null) {
            throw new BussinessException("订单不存在");
        }
        //桌台等关键字段以库中为准，原因同 pay：缺 tableId 会导致跨服务查桌台失败
        if (!StringUtils.hasText(orderEntity.getTableId())) {
            orderEntity.setTableId(dbOrder.getTableId());
        }
        //库中该单本身无桌台信息（历史脏数据）时，直接给可理解的业务提示
        if (!StringUtils.hasText(orderEntity.getTableId())) {
            throw new BussinessException("订单缺少桌台信息，无法完成挂账结账");
        }
        //挂账信息必须完整：creditDTO 为空或缺主体 ID 时不能继续，
        //否则请求已把订单置为已付却记不上挂账，且下游会报 Missing path variable
        if (null == creditDTO || !StringUtils.hasText(creditDTO.getCreditId())) {
            throw new BussinessException("挂账信息不完整，请填写挂账主体");
        }
        Integer payType = orderEntity.getPayType();
        if (payType == null || payType != OrderPayType.CREDIT.getType()) {
            throw new BussinessException("当前支付方式不是挂账，请走对应结账接口");
        }

        //挂账结账同样视为已结，服务端强制置为已支付，不依赖调用方透传
        orderEntity.setPayStatus(SystemCode.ORDER_STATUS_PAYED);
        //订单总额同样按库内有效明细重算，避免挂账金额随入参被改写
        int totalAmount = sumValidDetailAmount(orderId);
        orderEntity.setTotalAmount(totalAmount);
        //挂账金额服务端兜底并校验范围：未填按应付记账，超额挂账直接拒
        applyCreditPayAmount(orderEntity, creditDTO, totalAmount);

        this.updateById(orderEntity);

        //设置挂账信息
        if (payType == OrderPayType.CREDIT.getType()){

            String creditId = creditDTO.getCreditId();
            Credit credit = creditService.getById(creditId);

            // K11：挂账主体必须存在且启用，本次挂账金额不得超过剩余欠款额度
            if (credit == null) {
                throw new BussinessException("挂账主体不存在，请先在管家端维护");
            }
            // Credit.status：1 开启 / 0 停用
            if (credit.getStatus() == null || credit.getStatus() != 1) {
                throw new BussinessException("挂账主体已停用，不允许挂账");
            }
            Integer creditAmount = creditDTO.getCreditAmount() == null ? 0 : creditDTO.getCreditAmount();
            if (creditAmount <= 0) {
                throw new BussinessException("挂账金额必须大于 0");
            }
            if (credit.getCreditAmount() == null || credit.getCreditAmount() < creditAmount) {
                throw new BussinessException("挂账金额超过剩余欠款额度");
            }

            //挂账主体类型：null 时不能直接与 int 比较（拆箱 NPE）
            Integer creditType = credit.getCreditType();
            if (creditType == null) {
                throw new BussinessException("挂账主体类型缺失，请联系管家端维护");
            }

            //个人用户
            if (creditType.equals(SystemCode.CREDIT_TYPE_USER)){

                //判断挂账人信息是否正确（用 Objects.equals 规避任一侧为 null 的 NPE）
                if (!Objects.equals(credit.getUserName(), creditDTO.getCreditUserName())){
                    throw new BussinessException("挂账人信息不同，不允许挂账");
                }

                credit.setCreditAmount(credit.getCreditAmount() + creditDTO.getCreditAmount());
                creditService.saveOrUpdate(credit);

            }

            //公司用户
            List<CreditCompanyUser> companyUsers = null;
            if (creditType.equals(SystemCode.CREDIT_TYPE_COMPANY)){

                List<CreditCompanyUser> companyUserList = creditCompanyUserService.getInfoList(creditId);
                if (companyUserList == null || companyUserList.isEmpty()) {
                    throw new BussinessException("该公司未配置挂账人，请联系管家端进行设置");
                }

                //判断当前挂账人在集合中是否存在
                Optional<CreditCompanyUser> resultInfo = companyUserList.stream()
                        .filter(user -> Objects.equals(user.getUserName(), creditDTO.getCreditUserName()))
                        .findFirst();
                if (!resultInfo.isPresent()){
                    //不存在，不允许挂账
                    throw new BussinessException("当前用户不在该公司中，请联系管家端进行设置");
                }
                companyUsers=companyUserList;

                credit.setCreditAmount(credit.getCreditAmount() + creditDTO.getCreditAmount());
                creditService.saveOrUpdate(credit);
            }

            //挂账明细信息
            CreditLogs creditLogs = new CreditLogs();
            creditLogs.setCreditId(creditId);
            creditLogs.setOrderId(orderEntity.getOrderId());
            creditLogs.setType(credit.getCreditType());
            // K14：creditAmount 只赋值一次；orderAmount=订单总额，receivedAmount=本次实收（payAmount，未记录则回退总额）
            creditLogs.setCreditAmount(creditDTO.getCreditAmount());
            //金额以库中订单为准：调用方通常只传 orderId + 支付方式，不带金额，否则日志金额恒为 0
            Integer orderAmount = totalAmount;
            creditLogs.setOrderAmount(orderAmount);
            Integer receivedAmount = orderEntity.getPayAmount();
            creditLogs.setReceivedAmount(receivedAmount == null || receivedAmount <= 0
                    ? orderAmount : receivedAmount);

            if (creditType.equals(SystemCode.CREDIT_TYPE_COMPANY)){

                creditLogs.setUserName(creditDTO.getCreditUserName());
                creditLogs.setCompanyName(credit.getCompanyName());
                Optional<CreditCompanyUser> optional = companyUsers.stream()
                        .filter(user -> Objects.equals(user.getUserName(), creditDTO.getCreditUserName()))
                        .findFirst();
                String phone = optional.get().getPhone();
                creditLogs.setPhone(phone);

            }else if (creditType.equals(SystemCode.CREDIT_TYPE_USER)){

                creditLogs.setUserName(creditDTO.getCreditUserName());
                creditLogs.setPhone(credit.getPhone());
            }
            creditLogService.save(creditLogs);

            //修改桌台状态为空闲
            Table table = tableService.getById(orderEntity.getTableId());
            if (table == null) {
                throw new BussinessException("订单关联的桌台不存在，无法完成挂账结账");
            }
            table.setStatus(SystemCode.TABLE_STATUS_FREE);
            tableService.updateById(table);

        }

        saveOrderDetailMealInfo(orderEntity.getOrderId());

        return true;
    }

    //换台
    @Transactional
    public boolean changeTable(String orderId, String targetTableId) {

        String loginUserName = TenantContext.getLoginUserName();

        //获取目标桌台信息
        Table targetTable = tableService.getById(targetTableId);
        if (targetTable == null){
            throw new BussinessException("桌台不存在");
        }

        //获取订单对象
        OrderEntity orderEntity = this.getById(orderId);
        if (orderEntity == null) {
            throw new BussinessException("订单不存在");
        }

        //获取原有的桌台对象
        if (!StringUtils.hasText(orderEntity.getTableId())) {
            throw new BussinessException("订单缺少桌台信息，无法换台");
        }
        Table sourceTable = tableService.getById(orderEntity.getTableId());
        if (sourceTable == null) {
            throw new BussinessException("订单关联的原桌台不存在，无法换台");
        }
        //先判同一桌再判目标状态：否则同牌桌会报「非空闲」，提示与实际不符
        if (Objects.equals(targetTableId, sourceTable.getTableId())) {
            throw new BussinessException("目标桌台与原桌台相同，无需换台");
        }

        //Integer 与 int 直接比较会拆箱，历史数据 status 为 NULL 时 NPE
        if (!Integer.valueOf(SystemCode.TABLE_STATUS_FREE).equals(targetTable.getStatus())){
            throw new BussinessException("桌台处于非空闲状态，不能换桌");
        }

        //将原有的桌台对象状态设置为空闲
        sourceTable.setStatus(SystemCode.TABLE_STATUS_FREE);
        tableService.updateById(sourceTable);

        //将目标桌台的状态设置为开桌
        targetTable.setStatus(SystemCode.TABLE_STATUS_OPENED);
        tableService.updateById(targetTable);

        //新增桌台日志信息
        TableLog tableLog =new TableLog();
        tableLog.setTableStatus(SystemCode.TABLE_STATUS_OPENED);
        tableLog.setCreateTime(LocalDateTime.now());
        tableLog.setTableId(targetTableId);
        tableLog.setUserNumbers(orderEntity.getPersonNumbers());
        tableLog.setUserId(loginUserName);
        tableLogService.save(tableLog);

        //重新设置订单与桌台的关联
        orderEntity.setTableId(targetTableId);

        return this.updateById(orderEntity);
    }

    /**
     * 按订单流水号查询已结账订单并装载明细（管家端反结账入口）。
     * 只查 pay_status=1 的订单；明细必须按 order_id 关联——
     * 明细自身的 orderNumber 是带前缀的派生流水号，不能用于关联主单。
     */
    public OrderEntity getByOrderNumber(String orderNumber) {
        QueryWrapper<OrderEntity> orderWrapper = new QueryWrapper<>();
        orderWrapper.lambda()
                .eq(OrderEntity::getOrderNumber, orderNumber)
                .eq(OrderEntity::getPayStatus, SystemCode.ORDER_STATUS_PAYED);
        OrderEntity orderEntity = this.getOne(orderWrapper);
        if (orderEntity == null) {
            return null;
        }

        QueryWrapper<OrderDetailEntity> detailWrapper = new QueryWrapper<>();
        detailWrapper.lambda().eq(OrderDetailEntity::getOrderId, orderEntity.getOrderId());
        orderEntity.setOrderDetails(orderDetailService.list(detailWrapper));
        return orderEntity;
    }

    /**
     * 订单分页（只读）。
     * <p>与报表口径一致：时间过滤用 lastUpdateTime（自动填充，createTime 未填充）；区间左闭右开。
     */
    public PageResult<OrderEntity> queryPage(int pageNum, int pageSize, LocalDate start, LocalDate end,
                                             Integer payStatus, Integer payType, String orderNumber) {
        Page<OrderEntity> page = new Page<>(pageNum, pageSize);
        // 条件重载会先对取值表达式求值，必须用三元兜住 null，否则 start/end 为空时 NPE
        LambdaQueryWrapper<OrderEntity> wrapper = new QueryWrapper<OrderEntity>().lambda()
                .ge(start != null, OrderEntity::getLastUpdateTime,
                        start == null ? null : start.atStartOfDay())
                .lt(end != null, OrderEntity::getLastUpdateTime,
                        end == null ? null : end.plusDays(1).atStartOfDay())
                .eq(payStatus != null, OrderEntity::getPayStatus, payStatus)
                .eq(payType != null, OrderEntity::getPayType, payType)
                .like(StringUtils.hasText(orderNumber), OrderEntity::getOrderNumber, orderNumber)
                .orderByDesc(OrderEntity::getLastUpdateTime);
        return PageResult.of(this.page(page, wrapper));
    }

    //指定日期区间经营数据统计
    public CurrentAmountCollectDTO getCurrentAmount(LocalDate start, LocalDate end) {
        CurrentAmountCollectDTO result  = new CurrentAmountCollectDTO();

        //查询并设置已付款总金额
        QueryWrapper<OrderEntity> totalPayQueryWrapper = new QueryWrapper<>();
        totalPayQueryWrapper.select("SUM(pay_amount) as total_amount").lambda()
                .ge(OrderEntity::getLastUpdateTime,start)
                .lt(OrderEntity::getLastUpdateTime,end)
                .eq(OrderEntity::getPayStatus,SystemCode.ORDER_STATUS_PAYED);
        OrderEntity totalPayAmount = this.getOne(totalPayQueryWrapper);
        result.setPayTotal(totalPayAmount!=null && totalPayAmount.getTotalAmount()!=null ? totalPayAmount.getTotalAmount():0);

        //查询并设置已付款总单数
        QueryWrapper<OrderEntity> totalPayCountWrapper = new QueryWrapper<>();
        totalPayCountWrapper.lambda()
                .ge(OrderEntity::getLastUpdateTime,start)
                .lt(OrderEntity::getLastUpdateTime,end)
                .eq(OrderEntity::getPayStatus,SystemCode.ORDER_STATUS_PAYED);
        result.setPayTotalCount(Math.toIntExact(this.count(totalPayCountWrapper)));

        //未付款总金额
        QueryWrapper<OrderEntity> noPayQueryWrapper = new QueryWrapper<>();
        noPayQueryWrapper.select("SUM(total_amount) as total_amount").lambda()
                .ge(OrderEntity::getLastUpdateTime,start)
                .lt(OrderEntity::getLastUpdateTime,end)
                .eq(OrderEntity::getPayStatus,SystemCode.ORDER_STATUS_NOTPAY);
        OrderEntity noPayTotalAmount = this.getOne(noPayQueryWrapper);
        result.setNoPayTotal(noPayTotalAmount!=null && noPayTotalAmount.getTotalAmount()!=null ? noPayTotalAmount.getTotalAmount():0);


        //未付款总单数
        QueryWrapper<OrderEntity> noPayCountWrapper = new QueryWrapper<>();
        noPayCountWrapper.lambda()
                .ge(OrderEntity::getLastUpdateTime,start)
                .lt(OrderEntity::getLastUpdateTime,end)
                .eq(OrderEntity::getPayStatus,SystemCode.ORDER_STATUS_NOTPAY);
        result.setNoPayTotalCount(Math.toIntExact(this.count(noPayCountWrapper)));

        //当日已结账就餐总人数
        QueryWrapper<OrderEntity> payedTotalPersionWrapper = new QueryWrapper<>();
        payedTotalPersionWrapper.select("SUM(person_numbers) as person_numbers").lambda()
                .ge(OrderEntity::getLastUpdateTime,start)
                .lt(OrderEntity::getLastUpdateTime,end)
                .eq(OrderEntity::getPayStatus,SystemCode.ORDER_STATUS_PAYED);
        OrderEntity payedTotalPersion = this.getOne(payedTotalPersionWrapper);
        result.setTotalPerson(payedTotalPersion!=null && payedTotalPersion.getPersonNumbers()!=null ? payedTotalPersion.getPersonNumbers():0);


        //未结账就餐总人数
        QueryWrapper<OrderEntity> noPayedTotalPersionWrapper = new QueryWrapper<>();
        noPayedTotalPersionWrapper.select("SUM(person_numbers) as person_numbers").lambda()
                .ge(OrderEntity::getLastUpdateTime,start)
                .lt(OrderEntity::getLastUpdateTime,end)
                .eq(OrderEntity::getPayStatus,SystemCode.ORDER_STATUS_NOTPAY);
        OrderEntity noPayedTotalPersion = this.getOne(noPayedTotalPersionWrapper);
        result.setCurrentPerson(noPayedTotalPersion!=null && noPayedTotalPersion.getPersonNumbers()!=null ? noPayedTotalPersion.getPersonNumbers():0);


        return result;
    }

    //各时间段销售数据，type：1 销售额金额统计，2 销售总单数统计
    public List<CurrentHourCollectDTO> getCurrentHourCollect(LocalDate start, LocalDate end, Integer type) {

        QueryWrapper<OrderEntity> wrapper = new QueryWrapper<>();

        if (type == 1){
            //针对销售额求和
            wrapper.select("SUM(total_amount) as total_amount","HOUR(last_update_time) as current_date_hour")
                    .lambda().ge(OrderEntity::getLastUpdateTime,start)
                    .lt(OrderEntity::getLastUpdateTime,end);
        }

        if (type ==2){
            //针对销售量（单数）求和
            wrapper.select("COUNT(total_amount) as total_amount","HOUR(last_update_time) as current_date_hour")
                    .lambda().ge(OrderEntity::getLastUpdateTime,start)
                    .lt(OrderEntity::getLastUpdateTime,end);

        }

        //针对时间分组汇总
        wrapper.groupBy("current_date_hour").orderByAsc("current_date_hour");
        List<CurrentHourCollectDTO> result = new ArrayList<>();

        this.getBaseMapper().selectList(wrapper).forEach(o->{

            CurrentHourCollectDTO item = new CurrentHourCollectDTO();
            item.setTotalAmount(o.getTotalAmount());
            item.setCurrentDateHour(o.getCurrentDateHour());
            result.add(item);
        });

        //当时间为null，设置值为0
        for(int i=0;i<=23;i++){

            int hour =i;

            if (!result.stream().anyMatch(r->r.getCurrentDateHour() == hour)){

                CurrentHourCollectDTO item = new CurrentHourCollectDTO();
                item.setTotalAmount(0);
                item.setCurrentDateHour(hour);
                result.add(item);
            }
        }

        //对result根据小时从小到大排序
        result.sort((a,b)->Integer.compare(a.getCurrentDateHour(),b.getCurrentDateHour()));

        return result;
    }

    //收款方式构成
    public List<PayTypeCollectDTO> getPayTypeCollect(LocalDate start, LocalDate end) {

        List<PayTypeCollectDTO> result = new ArrayList<>();

        QueryWrapper<OrderEntity> wrapper = new QueryWrapper<>();

        wrapper.select("pay_type","SUM(pay_amount) as total_amount")
                .lambda().ge(OrderEntity::getLastUpdateTime,start)
                .lt(OrderEntity::getLastUpdateTime,end)
                .eq(OrderEntity::getPayStatus,SystemCode.ORDER_STATUS_PAYED)
                .groupBy(OrderEntity::getPayType);

        List<OrderEntity> orderEntityList = this.getBaseMapper().selectList(wrapper);

        orderEntityList.forEach(orderEntity -> {

            PayTypeCollectDTO dto = new PayTypeCollectDTO();

            dto.setPayType(orderEntity.getPayType());
            dto.setPayName(PayType.getName(orderEntity.getPayType()));
            dto.setTotalCount(orderEntity.getTotalAmount());
            result.add(dto);
        });

        return result;
    }

    //优惠数据统计
    public PrivilegeDTO getPrivilegeCollect(LocalDate start, LocalDate end) {

        PrivilegeDTO dto = new PrivilegeDTO();

        QueryWrapper<OrderEntity> wrapper = new QueryWrapper<>();

        wrapper.select("SUM(present_amount) as present_amount","SUM(free_amount) as free_amount","SUM(small_amount) AS small_amount")
                .lambda().ge(OrderEntity::getLastUpdateTime,start)
                .lt(OrderEntity::getLastUpdateTime,end)
                .eq(OrderEntity::getPayStatus,SystemCode.ORDER_STATUS_PAYED);

        OrderEntity orderEntity = this.getBaseMapper().selectOne(wrapper);

        //区间内无已付款订单时聚合结果为 null，按 0 处理避免拆箱 NPE
        dto.setPresentAmount(orderEntity != null && orderEntity.getPresentAmount() != null ? orderEntity.getPresentAmount() : 0);
        dto.setSmallAmount(orderEntity != null && orderEntity.getSmallAmount() != null ? orderEntity.getSmallAmount() : 0);
        dto.setFreeAmount(orderEntity != null && orderEntity.getFreeAmount() != null ? orderEntity.getFreeAmount() : 0);

        return dto;
    }

    //向t_order_detail_meal中添加数据（结账时把套餐明细拆成单品，供报表按菜品维度统计）
    private void saveOrderDetailMealInfo(String orderId){

        //反结账后再次结账会重复拆明细，先按订单清掉旧快照（物理删，避免逻辑删对 job 历史统计不可见）
        orderDetailMealService.getBaseMapper().physicalDeleteByOrderId(orderId);

        //获取订单明细表中的套餐信息
        QueryWrapper<OrderDetailEntity> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(OrderDetailEntity::getOrderId,orderId).eq(OrderDetailEntity::getDishType,2);
        List<OrderDetailEntity> orderDetailSetMealList = orderDetailService.list(wrapper);

        //获取套餐下的相关菜品信息  orderSetMeal->套餐
        orderDetailSetMealList.forEach(orderSetMeal->{

            //通过套餐id获取该套餐下的菜品信息
            List<Dish> dishList = setMealDishService.getAllDishBySetMealId(orderSetMeal.getDishId());

            //套餐未配置菜品时，原实现 reduce(...).get() 会抛 NoSuchElementException
            if (dishList == null || dishList.isEmpty()) {
                throw new BussinessException("套餐[" + orderSetMeal.getDishName() + "]未配置菜品，无法完成结账");
            }

            int setMealNumber = orderSetMeal.getDishNumber() == null ? 0 : orderSetMeal.getDishNumber();
            //当前套餐的优惠比率 = 套餐支付金额 / 套餐中所有菜品的原价总和
            int allDishPriceInSetMeal = dishList.stream()
                    .mapToInt(d -> {
                        Integer price = d.getPrice();
                        Integer copies = setMealDishService.getDishCopiesInSetMeal(d.getId(), orderSetMeal.getDishId());
                        if (price == null || copies == null) {
                            throw new BussinessException("套餐[" + orderSetMeal.getDishName() + "]内菜品缺少价格或份数，无法完成结账");
                        }
                        return price * copies;
                    }).sum() * setMealNumber;
            //原价合计为 0 会让比率为 Infinity/NaN，污染报表数据，直接拦下
            if (allDishPriceInSetMeal <= 0) {
                throw new BussinessException("套餐[" + orderSetMeal.getDishName() + "]原价合计为 0，无法计算优惠比率");
            }
            int payAmount = orderSetMeal.getDishAmount() == null ? 0 : orderSetMeal.getDishAmount();
            float rate = payAmount / (float) allDishPriceInSetMeal;


            //循环补充其他信息
            dishList.forEach(d->{
                OrderDetailMealEntity orderDetailMealEntity = new OrderDetailMealEntity();

                //复制共有信息
                BeanUtils.copyProperties(orderSetMeal,orderDetailMealEntity);

                //去除相关重复的信息
                orderDetailMealEntity.setDetailId(null);
                orderDetailMealEntity.setShopId(null);
                orderDetailMealEntity.setStoreId(null);

                orderDetailMealEntity.setDishId(d.getId());
                orderDetailMealEntity.setDishName(d.getName());
                orderDetailMealEntity.setDishPrice(d.getPrice());
                orderDetailMealEntity.setDishType(1);
                orderDetailMealEntity.setDishCategoryName(d.getDishCategory().getName());
                //获取套餐中这个菜品的数量
                Integer dishCopies = setMealDishService.getDishCopiesInSetMeal(d.getId(),orderSetMeal.getDishId());

                //菜品数量：订单中套餐的数量 * 套餐中这个菜品的数量；份数缺失按 0，避免拆箱 NPE
                orderDetailMealEntity.setDishNumber(setMealNumber * dishCopies);

                orderDetailMealEntity.setDishAmount((int)(d.getPrice()*dishCopies*setMealNumber*rate));

                orderDetailMealService.save(orderDetailMealEntity);
            });

        });
    }
}
