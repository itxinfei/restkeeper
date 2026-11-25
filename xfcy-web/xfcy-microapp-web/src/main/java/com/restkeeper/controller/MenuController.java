package com.restkeeper.controller;

import com.restkeeper.store.entity.SetMeal;
import com.restkeeper.store.service.IDishCategoryService;
import com.restkeeper.store.service.IDishService;
import com.restkeeper.store.service.ISetMealService;
import com.restkeeper.store.service.ITableAreaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * C 端菜单浏览：分类、可售菜品、套餐详情、桌台区域（匿名可访问）。
 */
@RestController
@RequestMapping("/menu")
public class MenuController {

    private final IDishCategoryService dishCategoryService;
    private final IDishService dishService;
    private final ISetMealService setMealService;
    private final ITableAreaService tableAreaService;

    public MenuController(IDishCategoryService dishCategoryService,
                          IDishService dishService,
                          ISetMealService setMealService,
                          ITableAreaService tableAreaService) {
        this.dishCategoryService = dishCategoryService;
        this.dishService = dishService;
        this.setMealService = setMealService;
        this.tableAreaService = tableAreaService;
    }

    @GetMapping("/category")
    public List<Map<String, Object>> category(@RequestParam(value = "type", defaultValue = "1") Integer type,
                                              @RequestParam(value = "shopId", required = false) String shopId,
                                              @RequestParam(value = "storeId", required = false) String storeId) {
        TenantQuerySupport.apply(shopId, storeId);
        return dishCategoryService.findCategoryList(type);
    }

    @GetMapping("/dish/{categoryId}")
    public List<Map<String, Object>> dish(@PathVariable("categoryId") String categoryId,
                                          @RequestParam(value = "shopId", required = false) String shopId,
                                          @RequestParam(value = "storeId", required = false) String storeId) {
        TenantQuerySupport.apply(shopId, storeId);
        return dishService.findEnableDishListInfo(categoryId, null);
    }

    @GetMapping("/setMeal/{id}")
    public SetMeal setMeal(@PathVariable("id") String id,
                           @RequestParam(value = "shopId", required = false) String shopId,
                           @RequestParam(value = "storeId", required = false) String storeId) {
        TenantQuerySupport.apply(shopId, storeId);
        return setMealService.getById(id);
    }

    @GetMapping("/area")
    public List<Map<String, Object>> area(@RequestParam(value = "shopId", required = false) String shopId,
                                          @RequestParam(value = "storeId", required = false) String storeId) {
        TenantQuerySupport.apply(shopId, storeId);
        return tableAreaService.listTableArea();
    }
}
