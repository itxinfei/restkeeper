package com.restkeeper.order.controller;

import com.restkeeper.entity.ReportDishEntity;
import com.restkeeper.order.service.ReportDishServiceImpl;
import com.restkeeper.service.IReportDishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 菜品报表 HTTP 端点：契约映射由 {@link IReportDishService} 上的 @HttpExchange 提供
 */
@RestController
public class ReportDishController implements IReportDishService {

    @Autowired
    private ReportDishServiceImpl reportDishService;

    @Override
    public List<ReportDishEntity> getCategoryAmountCollect(LocalDate start, LocalDate end) {
        return reportDishService.getCategoryAmountCollect(start, end);
    }

    @Override
    public List<ReportDishEntity> getDishRank(LocalDate start, LocalDate end) {
        return reportDishService.getDishRank(start, end);
    }
}
