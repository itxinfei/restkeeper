package com.restkeeper.service;

import com.restkeeper.entity.ReportDishEntity;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "菜品报表服务")
@HttpExchange("/report/dish")
public interface IReportDishService {

    //获取一定时期内的菜品分类销售额汇总（饼图展示）
    @GetExchange("/categoryAmount")
    List<ReportDishEntity> getCategoryAmountCollect(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end);

    //获取一定日期内的菜品销量排行
    @GetExchange("/dishRank")
    List<ReportDishEntity> getDishRank(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end);
}
