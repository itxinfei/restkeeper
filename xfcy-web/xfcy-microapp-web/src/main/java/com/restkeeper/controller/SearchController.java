package com.restkeeper.controller;

import com.restkeeper.entity.DishEs;
import com.restkeeper.entity.SearchResult;
import com.restkeeper.service.IDishSearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * C 端菜品/套餐检索（匿名可访问，走 ES 索引）。
 */
@RestController
@RequestMapping("/search")
public class SearchController {

    private final IDishSearchService dishSearchService;

    public SearchController(IDishSearchService dishSearchService) {
        this.dishSearchService = dishSearchService;
    }

    @GetMapping
    public Map<String, Object> search(@RequestParam("keyword") String keyword,
                                      @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                      @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        //1 菜品、2 套餐，合并返回
        SearchResult<DishEs> dish = dishSearchService.searchDishByName(keyword, 1, pageNum, pageSize);
        SearchResult<DishEs> setMeal = dishSearchService.searchDishByName(keyword, 2, pageNum, pageSize);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("dish", dish);
        data.put("setMeal", setMeal);
        return data;
    }
}
