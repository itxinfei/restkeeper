package com.restkeeper.service;

import com.restkeeper.entity.OrderDetailAllView;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "订单明细服务")
@HttpExchange("/order/detail")
public interface IOrderDetailService {

    //按销售额汇总当天菜品分类数据
    @GetExchange("/report/categoryAmount")
    List<OrderDetailAllView> getCurrentCategoryAmountCollect(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end);

    //按销量汇总当天菜品分类数据
    @GetExchange("/report/categoryCount")
    List<OrderDetailAllView> getCurrentCategoryCountCollect(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end);

    //统计当日菜品销售排行
    @GetExchange("/report/dishRank")
    List<OrderDetailAllView> getCurrentDishRank(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end);
}
