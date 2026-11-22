package com.restkeeper.controller;

import com.restkeeper.store.entity.SellCalculation;
import com.restkeeper.store.service.ISellCalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 沽清（阶段 4：Dubbo @Reference → HTTP Interface 契约代理）。
 */
@Tag(name = "沽清")
@RestController
public class SellCalculationController {

    private final ISellCalculationService sellCalculationService;

    public SellCalculationController(ISellCalculationService sellCalculationService) {
        this.sellCalculationService = sellCalculationService;
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
