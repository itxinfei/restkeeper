package com.restkeeper.store.controller;

import com.restkeeper.http.PageResult;
import com.restkeeper.store.dto.SetMealWithDishes;
import com.restkeeper.store.entity.SetMeal;
import com.restkeeper.store.service.ISetMealService;
import com.restkeeper.store.service.SetMealServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 套餐 HTTP 端点：契约映射由 {@link ISetMealService} 上的 @HttpExchange 提供
 */
@RestController
public class SetMealController implements ISetMealService {

    @Autowired
    private SetMealServiceImpl setMealService;

    @Override
    public PageResult<SetMeal> queryPage(int pageNum, int pageSize, String name) {
        return setMealService.queryPage(pageNum, pageSize, name);
    }

    @Override
    public boolean add(SetMealWithDishes request) {
        return setMealService.add(request.getSetMeal(), request.getDishList());
    }

    @Override
    public boolean update(SetMealWithDishes request) {
        return setMealService.update(request.getSetMeal(), request.getDishList());
    }

    @Override
    public SetMeal getById(String id) {
        return setMealService.getById(id);
    }

    @Override
    public boolean forbiddenSetMeals(List<String> ids) {
        return setMealService.forbiddenSetMeals(ids);
    }

    @Override
    public boolean enableSetMeals(List<String> ids) {
        return setMealService.enableSetMeals(ids);
    }

    @Override
    public boolean removeByIds(List<String> ids) {
        return setMealService.removeByIds(ids);
    }
}
