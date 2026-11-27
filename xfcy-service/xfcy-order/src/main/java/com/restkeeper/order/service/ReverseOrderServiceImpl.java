package com.restkeeper.order.service;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.restkeeper.constants.OrderPayType;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.entity.OrderEntity;
import com.restkeeper.entity.ReverseOrder;
import com.restkeeper.exception.BussinessException;
import com.restkeeper.order.mapper.ReverseOrderMapper;
import com.restkeeper.store.entity.Credit;
import com.restkeeper.store.entity.CreditLogs;
import com.restkeeper.store.entity.Table;
import com.restkeeper.store.service.ICreditLogService;
import com.restkeeper.store.service.ICreditService;
import com.restkeeper.store.service.ITableService;
import com.restkeeper.tenant.TenantContext;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 反结账本地服务（操作均在订单库内，本地事务即可）
 */
@Service("reverseOrderService")
public class ReverseOrderServiceImpl extends ServiceImpl<ReverseOrderMapper, ReverseOrder> {

    @Autowired
    private OrderServiceImpl orderService;

    @Autowired
    private ITableService tableService;

    @Autowired
    private ICreditLogService creditLogService;

    @Autowired
    private ICreditService creditService;

    //跨 order/store 两库写（撤销挂账要回写挂账主体与流水），必须走 Seata 全局事务
    @GlobalTransactional
    public boolean reverse(ReverseOrder reverseOrder) {

        //K7：反结账前校验订单存在且已支付，避免裸调端点反结未付订单
        String orderId = reverseOrder.getOrderId();
        OrderEntity orderEntity = orderService.getById(orderId);
        if (orderEntity == null) {
            throw new BussinessException("订单不存在");
        }
        if (orderEntity.getPayStatus() == null
                || orderEntity.getPayStatus() != SystemCode.ORDER_STATUS_PAYED) {
            throw new BussinessException("订单未支付，不能反结账");
        }
        //修改原有订单的支付状态为未支付
        orderEntity.setPayStatus(SystemCode.ORDER_STATUS_NOTPAY);
        orderService.updateById(orderEntity);

        //反结账撤销挂账：订单已回到未付而挂账留存，会让挂账主体欠款虚高、挂账报表多计
        if (orderEntity.getPayType() != null
                && orderEntity.getPayType() == OrderPayType.CREDIT.getType()) {
            cancelCreditOfOrder(orderId);
        }

        // 反结账后订单回到未付，桌台必须恢复为已开桌：否则桌台空闲却压着一张未付单，
        // 而 openTable 只看桌台状态，会导致同一桌台被重复开台、旧单永久悬挂。
        Table table = tableService.getById(orderEntity.getTableId());
        if (table != null && !Integer.valueOf(SystemCode.TABLE_STATUS_OPENED).equals(table.getStatus())) {
            table.setStatus(SystemCode.TABLE_STATUS_OPENED);
            tableService.updateById(table);
        }

        //向反结账表插入一条记录
        reverseOrder.setCreateTime(LocalDateTime.now());
        reverseOrder.setOrderNumber(orderEntity.getOrderNumber());
        reverseOrder.setTableId(orderEntity.getTableId());
        //反结账金额：优先实付金额，未记录（null 或 0）时回退订单总额（否则历史单会恒为 0）
        Integer payAmount = orderEntity.getPayAmount();
        reverseOrder.setAmount(payAmount == null || payAmount <= 0
                ? orderEntity.getTotalAmount() : payAmount);
        reverseOrder.setOperatorName(TenantContext.getLoginUserName());
        //status：0 失败 1 成功；走到这里说明校验与订单状态修改已完成，固定记成功
        reverseOrder.setStatus(1);


        return this.save(reverseOrder);
    }

    /**
     * 撤销该订单产生的挂账：回退挂账主体剩余欠款并删除挂账流水。
     * 回退金额不超过主体当前欠款，避免历史已还款数据被反向撑大。
     */
    private void cancelCreditOfOrder(String orderId) {
        List<CreditLogs> creditLogsList = creditLogService.listByOrderId(orderId);
        if (creditLogsList == null || creditLogsList.isEmpty()) {
            return;
        }
        for (CreditLogs creditLogs : creditLogsList) {
            Integer amount = creditLogs.getCreditAmount();
            if (amount == null || amount <= 0) {
                continue;
            }
            Credit credit = creditService.getById(creditLogs.getCreditId());
            if (credit == null || credit.getCreditAmount() == null) {
                continue;
            }
            if (credit.getCreditAmount() >= amount) {
                credit.setCreditAmount(credit.getCreditAmount() - amount);
                creditService.saveOrUpdate(credit);
            }
        }
        creditLogService.deleteByOrderId(orderId);
    }
}
