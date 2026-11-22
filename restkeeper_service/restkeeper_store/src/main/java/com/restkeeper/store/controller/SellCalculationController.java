package com.restkeeper.store.controller;

import com.restkeeper.http.PageResult;
import com.restkeeper.store.entity.SellCalculation;
import com.restkeeper.store.service.ISellCalculationService;
import com.restkeeper.store.service.SellCalculationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 沽清 HTTP 端点：契约映射由 {@link ISellCalculationService} 上的 @HttpExchange 提供
 */
@RestController
public class SellCalculationController implements ISellCalculationService {

    @Autowired
    private SellCalculationServiceImpl sellCalculationService;

    @Override
    public PageResult<SellCalculation> queryPage(int pageNum, int pageSize, String dishName, Integer dishType) {
        return sellCalculationService.queryPage(pageNum, pageSize, dishName, dishType);
    }

    @Override
    public Integer getRemainderCount(String dishId) {
        return sellCalculationService.getRemainderCount(dishId);
    }

    @Override
    public void decrease(String dishId, Integer dishNumber) {
        sellCalculationService.decrease(dishId, dishNumber);
    }

    @Override
    public void add(String dishId, int dishNum) {
        sellCalculationService.add(dishId, dishNum);
    }

    @Override
    public boolean save(SellCalculation sellCalculation) {
        return sellCalculationService.save(sellCalculation);
    }

    @Override
    public boolean removeByIds(List<String> ids) {
        return sellCalculationService.removeByIds(ids);
    }
}
