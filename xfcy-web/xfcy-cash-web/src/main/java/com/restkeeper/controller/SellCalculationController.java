package com.restkeeper.controller;

import com.restkeeper.http.PageResult;
import com.restkeeper.store.entity.SellCalculation;
import com.restkeeper.store.service.ISellCalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 沽清（阶段 4：Dubbo @Reference → HTTP Interface 契约代理）。
 * 端点路径对齐 provider 契约 {@link ISellCalculationService}。
 */
@Tag(name = "沽清")
@RestController
public class SellCalculationController {

    private final ISellCalculationService sellCalculationService;

    public SellCalculationController(ISellCalculationService sellCalculationService) {
        this.sellCalculationService = sellCalculationService;
    }

    @Operation(summary = "沽清分页")
    @GetMapping("/sellCalculation/page/{pageNum}/{pageSize}")
    public PageResult<SellCalculation> page(@PathVariable("pageNum") int pageNum,
                                            @PathVariable("pageSize") int pageSize,
                                            @RequestParam(value = "dishName", required = false) String dishName,
                                            @RequestParam(value = "dishType", required = false) Integer dishType) {
        return sellCalculationService.queryPage(pageNum, pageSize, dishName, dishType);
    }

    @Operation(summary = "按菜品查剩余沽清数量")
    @GetMapping("/sellCalculation/remainder/{dishId}")
    public Integer remainder(@PathVariable("dishId") String dishId) {
        return sellCalculationService.getRemainderCount(dishId);
    }

    @Operation(summary = "扣减沽清数量")
    @PutMapping("/sellCalculation/decrease")
    public void decrease(@RequestParam("dishId") String dishId,
                         @RequestParam("dishNumber") Integer dishNumber) {
        sellCalculationService.decrease(dishId, dishNumber);
    }

    @Operation(summary = "增加沽清数量")
    @PostMapping("/sellCalculation/add")
    public void add(@RequestParam("dishId") String dishId,
                   @RequestParam("dishNumber") int dishNumber) {
        sellCalculationService.add(dishId, dishNumber);
    }

    @Operation(summary = "保存沽清")
    @PostMapping("/sellCalculation")
    public boolean save(@RequestBody SellCalculation sellCalculation) {
        return sellCalculationService.save(sellCalculation);
    }

    @Operation(summary = "批量删除沽清")
    @DeleteMapping("/sellCalculation")
    public boolean removeByIds(@RequestBody List<String> ids) {
        return sellCalculationService.removeByIds(ids);
    }
}
