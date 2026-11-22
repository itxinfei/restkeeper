package com.restkeeper.store.service;

import com.restkeeper.http.PageResult;
import com.restkeeper.store.entity.DishCategory;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import java.util.List;
import java.util.Map;

@Tag(name = "菜品分类服务")
@HttpExchange("/dish/category")
public interface IDishCategoryService {

    //新增分类
    @PostExchange("/add")
    boolean add(@RequestParam("name") String name, @RequestParam("type") int type);

    //修改分类
    @PutExchange("/{id}")
    boolean update(@PathVariable("id") String id, @RequestParam("name") String name);

    //分类分页查询
    @GetExchange("/page/{pageNum}/{pageSize}")
    PageResult<DishCategory> queryPage(@PathVariable("pageNum") int pageNum,
                                       @PathVariable("pageSize") int pageSize);

    //根据分类类别查询列表
    @GetExchange("/type/{type}")
    List<Map<String, Object>> findCategoryList(@PathVariable("type") Integer type);

    //删除分类
    @DeleteExchange("/{id}")
    boolean delete(@PathVariable("id") String id);
}
