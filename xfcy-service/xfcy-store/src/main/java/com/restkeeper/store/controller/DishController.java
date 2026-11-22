package com.restkeeper.store.controller;

import com.restkeeper.http.PageResult;
import com.restkeeper.store.dto.DishWithFlavors;
import com.restkeeper.store.entity.Dish;
import com.restkeeper.store.service.IDishService;
import com.restkeeper.store.service.DishServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 菜品 HTTP 端点：契约映射由 {@link IDishService} 上的 @HttpExchange 提供
 */
@RestController
public class DishController implements IDishService {

    @Autowired
    private DishServiceImpl dishService;

    @Override
    public boolean save(DishWithFlavors request) {
        return dishService.save(request.getDish(), request.getFlavorList());
    }

    @Override
    public boolean update(DishWithFlavors request) {
        return dishService.update(request.getDish(), request.getFlavorList());
    }

    @Override
    public Dish getById(String id) {
        return dishService.getById(id);
    }

    @Override
    public List<Map<String, Object>> findEnableDishListInfo(String categoryId, String name) {
        return dishService.findEnableDishListInfo(categoryId, name);
    }

    @Override
    public PageResult<Dish> queryManagePage(int pageNum, int pageSize, String name, String categoryId, Integer status) {
        return dishService.queryManagePage(pageNum, pageSize, name, categoryId, status);
    }

    @Override
    public boolean forbiddenDishes(List<String> ids) {
        return dishService.forbiddenDishes(ids);
    }

    @Override
    public boolean enableDishes(List<String> ids) {
        return dishService.enableDishes(ids);
    }

    @Override
    public boolean deleteDishes(List<String> ids) {
        return dishService.deleteDishes(ids);
    }
}
