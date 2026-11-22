package com.restkeeper.store.service;

import com.restkeeper.store.entity.Dish;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@Tag(name = "套餐菜品服务")
@HttpExchange("/setMeal/dish")
public interface ISetMealDishService {

    //按套餐 id 查询其包含的全部菜品
    @GetExchange("/list/{setMealId}")
    List<Dish> getAllDishBySetMealId(@PathVariable("setMealId") String setMealId);

    //查询某菜品在套餐中的份数
    @GetExchange("/copies")
    Integer getDishCopiesInSetMeal(@RequestParam("dishId") String dishId,
                                   @RequestParam("setMealId") String setMealId);
}
