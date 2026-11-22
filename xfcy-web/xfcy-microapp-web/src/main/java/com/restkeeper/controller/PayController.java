package com.restkeeper.controller;

import cn.dev33.satoken.stp.StpLogic;
import com.restkeeper.entity.OrderEntity;
import com.restkeeper.service.IOrderService;
import com.restkeeper.utils.Result;
import com.restkeeper.utils.ResultCode;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端支付（需登录）。
 * <p>dev 打桩：跳过微信 JSAPI / 支付宝，直接调用 order provider 的现金结账端点完成账单流转。
 */
@RestController
@RequestMapping("/pay")
public class PayController {

    private final IOrderService orderService;
    private final StpLogic microappStpLogic;

    public PayController(IOrderService orderService, StpLogic microappStpLogic) {
        this.orderService = orderService;
        this.microappStpLogic = microappStpLogic;
    }

    @PostMapping("/mock")
    public Result mockPay(@RequestBody OrderEntity order) {
        microappStpLogic.checkLogin();
        orderService.pay(order);
        Result result = new Result();
        result.setStatus(ResultCode.success);
        result.setDesc("支付成功（dev 打桩）");
        return result;
    }
}
