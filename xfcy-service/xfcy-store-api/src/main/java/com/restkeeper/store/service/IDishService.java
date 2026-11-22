package com.restkeeper.store.service;

import com.restkeeper.http.PageResult;
import com.restkeeper.store.dto.DishWithFlavors;
import com.restkeeper.store.entity.Dish;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PutExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;
import java.util.Map;

@Tag(name = "菜品服务")
@HttpExchange("/dish")
public interface IDishService {

    //新增菜品（含口味）
    @PostExchange
    boolean save(@RequestBody DishWithFlavors request);

    //修改菜品（含口味）
    @PutExchange
    boolean update(@RequestBody DishWithFlavors request);

    //按主键查询菜品
    @GetExchange("/{id}")
    Dish getById(@PathVariable("id") String id);

    //根据分类与菜品名称查询可售菜品列表
    @GetExchange("/enable/list/{categoryId}")
    List<Map<String, Object>> findEnableDishListInfo(@PathVariable("categoryId") String categoryId,
                                                     @RequestParam(value = "name", required = false) String name);

    //管理端菜品分页（名称模糊、分类精确、status 可空=含停售全部；经类级 resultMap 带出分类与口味）
    @GetExchange("/manage/page/{pageNum}/{pageSize}")
    PageResult<Dish> queryManagePage(@PathVariable("pageNum") int pageNum,
                                     @PathVariable("pageSize") int pageSize,
                                     @RequestParam(value = "name", required = false) String name,
                                     @RequestParam(value = "categoryId", required = false) String categoryId,
                                     @RequestParam(value = "status", required = false) Integer status);

    //停售菜品
    @PutExchange("/forbidden")
    boolean forbiddenDishes(@RequestBody List<String> ids);

    //起售菜品
    @PutExchange("/enable")
    boolean enableDishes(@RequestBody List<String> ids);

    //删除菜品
    @DeleteExchange
    boolean deleteDishes(@RequestBody List<String> ids);
}
