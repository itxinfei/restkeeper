package com.restkeeper.controller;

import cn.dev33.satoken.stp.StpLogic;
import com.restkeeper.dto.DetailDTO;
import com.restkeeper.entity.OrderEntity;
import com.restkeeper.service.IOrderService;
import com.restkeeper.utils.Result;
import com.restkeeper.utils.ResultCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端下单 / 加菜 / 退菜 / 订单详情（需登录）。
 * <p>复用 order provider 的 {@code /order/add}（下单与加菜同端点）、{@code /order/returnDish}、{@code /order/{id}}。
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    private final IOrderService orderService;
    private final StpLogic microappStpLogic;

    public OrderController(IOrderService orderService, StpLogic microappStpLogic) {
        this.orderService = orderService;
        this.microappStpLogic = microappStpLogic;
    }

    /**
     * 提交订单（购物车结算）；返回订单 id
     */
    @PostMapping("/submit")
    public Result submit(@RequestBody OrderEntity order) {
        microappStpLogic.checkLogin();
        String orderId = orderService.addOrder(order);
        return success(orderId);
    }

    /**
     * 加菜（复用下单端点，body 带 orderId）
     */
    @PostMapping("/add")
    public Result add(@RequestBody OrderEntity order) {
        microappStpLogic.checkLogin();
        orderService.addOrder(order);
        return success(null);
    }

    @PostMapping("/returnDish")
    public Result returnDish(@RequestBody DetailDTO detailDTO) {
        microappStpLogic.checkLogin();
        orderService.returnDish(detailDTO);
        return success(null);
    }

    @GetMapping("/{id}")
    public OrderEntity detail(@PathVariable("id") String id) {
        return orderService.getById(id);
    }

    private Result success(Object data) {
        Result result = new Result();
        result.setStatus(ResultCode.success);
        result.setData(data);
        return result;
    }
}
