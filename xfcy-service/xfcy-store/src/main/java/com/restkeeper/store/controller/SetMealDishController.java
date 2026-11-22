package com.restkeeper.store.controller;

import com.restkeeper.store.entity.Dish;
import com.restkeeper.store.service.ISetMealDishService;
import com.restkeeper.store.service.SetMealDishServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 套餐菜品关联 HTTP 端点：契约映射由 {@link ISetMealDishService} 上的 @HttpExchange 提供
 */
@RestController
public class SetMealDishController implements ISetMealDishService {

    @Autowired
    private SetMealDishServiceImpl setMealDishService;

    @Override
    public List<Dish> getAllDishBySetMealId(String setMealId) {
        return setMealDishService.getAllDishBySetMealId(setMealId);
    }

    @Override
    public Integer getDishCopiesInSetMeal(String dishId, String setMealId) {
        return setMealDishService.getDishCopiesInSetMeal(dishId, setMealId);
    }
}
