package com.restkeeper.service;

import com.restkeeper.dto.CurrentAmountCollectDTO;
import com.restkeeper.dto.CurrentHourCollectDTO;
import com.restkeeper.dto.CreditPayRequest;
import com.restkeeper.dto.DetailDTO;
import com.restkeeper.dto.PayTypeCollectDTO;
import com.restkeeper.dto.PrivilegeDTO;
import com.restkeeper.entity.OrderEntity;
import com.restkeeper.http.PageResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import java.time.LocalDate;
import java.util.List;

/**
 * 订单服务契约：provider 端 Controller implements 本接口，web 端经 HttpServiceProxyFactory 生成代理
 */
@Tag(name = "订单服务")
@HttpExchange("/order")
public interface IOrderService {

    //下单
    @PostExchange("/add")
    String addOrder(@RequestBody OrderEntity orderEntity);

    //退菜
    @PostExchange("/returnDish")
    boolean returnDish(@RequestBody DetailDTO detailDTO);

    //现金结账
    @PostExchange("/pay")
    boolean pay(@RequestBody OrderEntity orderEntity);

    //挂账结账（跨 order/store 两库，Seata 全局事务）
    @PostExchange("/pay/credit")
    boolean payByCredit(@RequestBody CreditPayRequest request);

    //换台
    @PutExchange("/changeTable")
    boolean changeTable(@RequestParam("orderId") String orderId,
                        @RequestParam("targetTableId") String targetTableId);

    //获取指定日期区间的经营数据统计
    @GetExchange("/report/currentAmount")
    CurrentAmountCollectDTO getCurrentAmount(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end);

    //获取各时间段的销售数据分析，type：1 销售额金额统计，2 销售总单数统计
    @GetExchange("/report/hourCollect")
    List<CurrentHourCollectDTO> getCurrentHourCollect(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam("type") Integer type);

    //指定日期区间内的收款方式构成
    @GetExchange("/report/payType")
    List<PayTypeCollectDTO> getPayTypeCollect(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end);

    //指定日期区间内的优惠数据统计
    @GetExchange("/report/privilege")
    PrivilegeDTO getPrivilegeCollect(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end);

    //订单分页（只读；按最后更新时间倒序，可选时间区间/支付状态/支付方式/流水号）
    @GetExchange("/page/{pageNum}/{pageSize}")
    PageResult<OrderEntity> queryPage(@PathVariable("pageNum") int pageNum,
                                      @PathVariable("pageSize") int pageSize,
                                      @RequestParam(value = "start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                      @RequestParam(value = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
                                      @RequestParam(value = "payStatus", required = false) Integer payStatus,
                                      @RequestParam(value = "payType", required = false) Integer payType,
                                      @RequestParam(value = "orderNumber", required = false) String orderNumber);

    //按主键查询订单
    @GetExchange("/{id}")
    OrderEntity getById(@PathVariable("id") String id);

    //按订单流水号查询已结账订单（反结账入口），返回结果含订单明细；非已付单或查无此号返回 null
    @GetExchange("/number/{orderNumber}")
    OrderEntity getByOrderNumber(@PathVariable("orderNumber") String orderNumber);
}
