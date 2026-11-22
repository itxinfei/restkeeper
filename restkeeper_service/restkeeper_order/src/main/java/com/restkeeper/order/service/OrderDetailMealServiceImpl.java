package com.restkeeper.order.service;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.restkeeper.entity.OrderDetailMealEntity;
import com.restkeeper.order.mapper.OrderDetailMealMapper;
import org.springframework.stereotype.Service;

/**
 * 订单套餐拆分明细本地服务（无独立 HTTP 端点，供订单结账流程内部调用）
 */
@Service("orderDetailMealService")
public class OrderDetailMealServiceImpl extends ServiceImpl<OrderDetailMealMapper, OrderDetailMealEntity> {
}
