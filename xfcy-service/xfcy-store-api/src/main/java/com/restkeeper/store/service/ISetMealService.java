package com.restkeeper.store.service;

import com.restkeeper.http.PageResult;
import com.restkeeper.store.dto.SetMealWithDishes;
import com.restkeeper.store.entity.SetMeal;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import java.util.List;

@Tag(name = "套餐服务")
@HttpExchange("/setMeal")
public interface ISetMealService {

    //分页查询（按套餐名称模糊）
    @GetExchange("/page/{pageNum}/{pageSize}")
    PageResult<SetMeal> queryPage(@PathVariable("pageNum") int pageNum,
                                  @PathVariable("pageSize") int pageSize,
                                  @RequestParam(value = "name", required = false) String name);

    //新增套餐（含套餐菜品）
    @PostExchange
    boolean add(@RequestBody SetMealWithDishes request);

    //修改套餐（含套餐菜品）
    @PutExchange
    boolean update(@RequestBody SetMealWithDishes request);

    //按主键查询套餐
    @GetExchange("/{id}")
    SetMeal getById(@PathVariable("id") String id);

    //停售套餐
    @PutExchange("/forbidden")
    boolean forbiddenSetMeals(@RequestBody List<String> ids);

    //起售套餐
    @PutExchange("/enable")
    boolean enableSetMeals(@RequestBody List<String> ids);

    //批量删除套餐
    @DeleteExchange
    boolean removeByIds(@RequestBody List<String> ids);
}
