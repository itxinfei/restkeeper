package com.restkeeper.store.controller;

import com.restkeeper.http.PageResult;
import com.restkeeper.store.entity.DishCategory;
import com.restkeeper.store.service.DishCategoryServiceImpl;
import com.restkeeper.store.service.IDishCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 菜品分类 HTTP 端点：契约映射由 {@link IDishCategoryService} 上的 @HttpExchange 提供
 */
@RestController
public class DishCategoryController implements IDishCategoryService {

    @Autowired
    private DishCategoryServiceImpl dishCategoryService;

    @Override
    public boolean add(String name, int type) {
        return dishCategoryService.add(name, type);
    }

    @Override
    public boolean update(String id, String name) {
        return dishCategoryService.update(id, name);
    }

    @Override
    public PageResult<DishCategory> queryPage(int pageNum, int pageSize) {
        return dishCategoryService.queryPage(pageNum, pageSize);
    }

    @Override
    public List<Map<String, Object>> findCategoryList(Integer type) {
        return dishCategoryService.findCategoryList(type);
    }

    @Override
    public boolean delete(String id) {
        return dishCategoryService.delete(id);
    }
}
