package com.restkeeper.order.service;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.entity.OrderEntity;
import com.restkeeper.entity.ReverseOrder;
import com.restkeeper.exception.BussinessException;
import com.restkeeper.order.mapper.ReverseOrderMapper;
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


        //向反结账表插入一条记录
        reverseOrder.setCreateTime(LocalDateTime.now());
        reverseOrder.setOrderNumber(orderEntity.getOrderNumber());
        reverseOrder.setTableId(orderEntity.getTableId());
        reverseOrder.setAmount(orderEntity.getPayAmount());
        reverseOrder.setOperatorName(TenantContext.getLoginUserName());


        return this.save(reverseOrder);
    }
}
