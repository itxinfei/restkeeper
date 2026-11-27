package com.restkeeper.order.service;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.entity.OrderEntity;
import com.restkeeper.entity.ReverseOrder;
import com.restkeeper.exception.BussinessException;
import com.restkeeper.order.mapper.ReverseOrderMapper;
import com.restkeeper.store.entity.Table;
import com.restkeeper.store.service.ITableService;
import com.restkeeper.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 反结账本地服务（操作均在订单库内，本地事务即可）
 */
@Service("reverseOrderService")
public class ReverseOrderServiceImpl extends ServiceImpl<ReverseOrderMapper, ReverseOrder> {

    @Autowired
    private OrderServiceImpl orderService;

    @Autowired
    private ITableService tableService;

    @Transactional
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
}
