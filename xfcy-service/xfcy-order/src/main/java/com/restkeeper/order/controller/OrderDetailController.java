package com.restkeeper.order.controller;

import com.restkeeper.entity.OrderDetailAllView;
import com.restkeeper.order.service.OrderDetailServiceImpl;
import com.restkeeper.service.IOrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 订单明细报表 HTTP 端点：契约映射由 {@link IOrderDetailService} 上的 @HttpExchange 提供
 */
@RestController
public class OrderDetailController implements IOrderDetailService {

    @Autowired
    private OrderDetailServiceImpl orderDetailService;

    @Override
    public List<OrderDetailAllView> getCurrentCategoryAmountCollect(LocalDate start, LocalDate end) {
        return orderDetailService.getCurrentCategoryAmountCollect(start, end);
    }

    @Override
    public List<OrderDetailAllView> getCurrentCategoryCountCollect(LocalDate start, LocalDate end) {
        return orderDetailService.getCurrentCategoryCountCollect(start, end);
    }

    @Override
    public List<OrderDetailAllView> getCurrentDishRank(LocalDate start, LocalDate end) {
        return orderDetailService.getCurrentDishRank(start, end);
    }
}
