package com.restkeeper.controller;

import com.restkeeper.constants.SystemCode;
import com.restkeeper.dto.CreditPayRequest;
import com.restkeeper.dto.DetailDTO;
import com.restkeeper.entity.OrderDetailEntity;
import com.restkeeper.entity.OrderEntity;
import com.restkeeper.entity.ReverseOrder;
import com.restkeeper.http.PageResult;
import com.restkeeper.service.IOrderService;
import com.restkeeper.service.IReverseOrderService;
import com.restkeeper.store.entity.Dish;
import com.restkeeper.store.entity.DishCategory;
import com.restkeeper.store.service.IDishService;
import com.restkeeper.store.service.ISetMealService;
import com.restkeeper.tenant.TenantContext;
import com.restkeeper.utils.Result;
import com.restkeeper.utils.ResultCode;
import com.restkeeper.utils.SequenceUtils;
import com.restkeeper.vo.OrderDetailVO;
import com.restkeeper.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单（阶段 4：Dubbo @Reference → HTTP Interface 契约代理）。
 * 下单与加菜合并为同一端点 {@code POST /order/add}：body 带 orderId 为加菜，不带为下单。
 */
@Tag(name = "订单")
@RestController
public class OrderController {

    private final IOrderService orderService;
    private final IDishService dishService;
    private final ISetMealService setMealService;
    private final IReverseOrderService reverseOrderService;

    public OrderController(IOrderService orderService,
                           IDishService dishService,
                           ISetMealService setMealService,
                           IReverseOrderService reverseOrderService) {
        this.orderService = orderService;
        this.dishService = dishService;
        this.setMealService = setMealService;
        this.reverseOrderService = reverseOrderService;
    }

    @Operation(summary = "下单 / 加菜（带 orderId 为加菜）")
    @PostMapping("/order/add")
    public Result addOrder(@RequestBody OrderVO orderVO) {
        if (orderVO.getOrderId() != null && !orderVO.getOrderId().isEmpty()) {
            return plusDish(orderVO);
        }

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setTableId(orderVO.getTableId());
        orderEntity.setPayStatus(SystemCode.ORDER_STATUS_NOTPAY);
        orderEntity.setOperatorName(TenantContext.getLoginUserName());
        orderEntity.setOrderSource(SystemCode.ORDER_SOURCE_STORE);
        orderEntity.setTotalAmount(orderVO.getTotalAmount());
        orderEntity.setPersonNumbers(orderVO.getPersonNumbers());
        orderEntity.setOrderRemark(orderVO.getOrderRemark());
        orderEntity.setCreateTime(LocalDateTime.now());

        List<OrderDetailEntity> orderDetails = new ArrayList<>();
        orderVO.getDishes().forEach(dishVO -> {
            OrderDetailEntity orderDetailEntity = new OrderDetailEntity();
            orderDetailEntity.setDishName(dishVO.getDishName());
            orderDetailEntity.setDishType(dishVO.getType());
            orderDetailEntity.setDishId(dishVO.getDishId());
            orderDetailEntity.setDishAmount(dishVO.getDishNumber() * dishVO.getPrice());
            orderDetailEntity.setDishNumber(dishVO.getDishNumber());
            orderDetailEntity.setFlavorRemark(dishVO.getFlavorList().toString());
            orderDetailEntity.setTableId(orderVO.getTableId());
            orderDetailEntity.setDishPrice(dishVO.getPrice());

            DishCategory dishCategory = null;
            if (dishVO.getType() != null && dishVO.getType() == 1) {
                Dish dish = dishService.getById(dishVO.getDishId());
                if (dish != null) {
                    dishCategory = dish.getDishCategory();
                }
            } else {
                var setMeal = setMealService.getById(dishVO.getDishId());
                if (setMeal != null && setMeal.getCategory() != null) {
                    orderDetailEntity.setDishCategoryName(setMeal.getCategory().getName());
                }
            }
            if (dishCategory != null) {
                orderDetailEntity.setDishCategoryName(dishCategory.getName());
            }
            orderDetails.add(orderDetailEntity);
        });
        orderEntity.setOrderDetails(orderDetails);

        String orderId = orderService.addOrder(orderEntity);

        Result result = new Result();
        result.setStatus(ResultCode.success);
        result.setData(orderId);
        return result;
    }

    private Result plusDish(OrderVO orderVO) {
        OrderEntity orderEntity = orderService.getById(orderVO.getOrderId());

        List<OrderDetailEntity> orderDetailEntities = new ArrayList<>();
        int amount = 0;

        for (OrderDetailVO detail : orderVO.getDishes()) {
            OrderDetailEntity orderDetailEntity = new OrderDetailEntity();
            orderDetailEntity.setOrderId(orderEntity.getOrderId());
            orderDetailEntity.setOrderNumber(SequenceUtils.getSequenceWithPrefix(orderEntity.getOrderNumber()));
            orderDetailEntity.setTableId(orderEntity.getTableId());
            orderDetailEntity.setDetailStatus(detail.getStatus());
            orderDetailEntity.setAddRemark(detail.getDishRemark());
            orderDetailEntity.setDishId(detail.getDishId());
            orderDetailEntity.setDishType(detail.getType());
            orderDetailEntity.setDishName(detail.getDishName());
            orderDetailEntity.setDishPrice(detail.getPrice());
            orderDetailEntity.setDishNumber(detail.getDishNumber());
            orderDetailEntity.setDishAmount(detail.getDishNumber() * detail.getPrice());
            orderDetailEntity.setDishRemark(detail.getDishRemark());
            orderDetailEntity.setFlavorRemark(detail.getFlavorList().toString());

            Dish dish = dishService.getById(detail.getDishId());
            if (dish != null && dish.getDishCategory() != null) {
                orderDetailEntity.setDishCategoryName(dish.getDishCategory().getName());
            }

            amount += orderDetailEntity.getDishAmount();
            orderDetailEntities.add(orderDetailEntity);
        }

        orderEntity.setOrderDetails(orderDetailEntities);
        orderEntity.setTotalAmount(amount + orderEntity.getTotalAmount());

        String orderId = orderService.addOrder(orderEntity);

        Result result = new Result();
        result.setStatus(ResultCode.success);
        result.setData(orderId);
        result.setDesc("ok");
        return result;
    }

    @Operation(summary = "退菜")
    @PostMapping("/order/returnDish")
    public boolean returnDish(@RequestBody DetailDTO detailDTO) {
        return orderService.returnDish(detailDTO);
    }

    @Operation(summary = "现金结账")
    @PostMapping("/order/pay")
    public boolean pay(@RequestBody OrderEntity orderEntity) {
        return orderService.pay(orderEntity);
    }

    @Operation(summary = "挂账结账")
    @PostMapping("/order/pay/credit")
    public boolean payByCredit(@RequestBody CreditPayRequest request) {
        return orderService.payByCredit(request);
    }

    @Operation(summary = "换台")
    @PutMapping("/order/changeTable")
    public boolean changeTable(@RequestParam("orderId") String orderId,
                               @RequestParam("targetTableId") String targetTableId) {
        return orderService.changeTable(orderId, targetTableId);
    }

    @Operation(summary = "订单分页")
    @GetMapping("/order/page/{pageNum}/{pageSize}")
    public PageResult<OrderEntity> page(@PathVariable("pageNum") int pageNum,
                                        @PathVariable("pageSize") int pageSize,
                                        @RequestParam(value = "start", required = false)
                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                        @RequestParam(value = "end", required = false)
                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
                                        @RequestParam(value = "payStatus", required = false) Integer payStatus,
                                        @RequestParam(value = "payType", required = false) Integer payType,
                                        @RequestParam(value = "orderNumber", required = false) String orderNumber) {
        return orderService.queryPage(pageNum, pageSize, start, end, payStatus, payType, orderNumber);
    }

    @Operation(summary = "按流水号查已付订单（反结账入口）")
    @GetMapping("/order/number/{orderNumber}")
    public OrderEntity getByOrderNumber(@PathVariable("orderNumber") String orderNumber) {
        return orderService.getByOrderNumber(orderNumber);
    }

    @Operation(summary = "反结账")
    @PostMapping("/order/reverse")
    public boolean reverse(@RequestBody ReverseOrder reverseOrder) {
        return reverseOrderService.reverse(reverseOrder);
    }
}
