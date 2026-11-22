package com.restkeeper.order.controller;

import com.restkeeper.entity.ReverseOrder;
import com.restkeeper.order.service.ReverseOrderServiceImpl;
import com.restkeeper.service.IReverseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 * 反结账 HTTP 端点：契约映射由 {@link IReverseOrderService} 上的 @HttpExchange 提供
 */
@RestController
public class ReverseOrderController implements IReverseOrderService {

    @Autowired
    private ReverseOrderServiceImpl reverseOrderService;

    @Override
    public boolean reverse(ReverseOrder reverseOrder) {
        return reverseOrderService.reverse(reverseOrder);
    }
}
