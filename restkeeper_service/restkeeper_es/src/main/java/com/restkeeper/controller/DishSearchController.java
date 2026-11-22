package com.restkeeper.controller;

import com.restkeeper.entity.DishEs;
import com.restkeeper.entity.SearchResult;
import com.restkeeper.service.DishSearchServiceImpl;
import com.restkeeper.service.IDishSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 * 菜品检索 HTTP 端点：契约映射由 {@link IDishSearchService} 上的 @HttpExchange 提供
 */
@RestController
public class DishSearchController implements IDishSearchService {

    @Autowired
    private DishSearchServiceImpl dishSearchService;

    @Override
    public SearchResult<DishEs> searchAllByCode(String code, int type, int pageNum, int pageSize) {
        return dishSearchService.searchAllByCode(code, type, pageNum, pageSize);
    }

    @Override
    public SearchResult<DishEs> searchDishByCode(String code, int pageNum, int pageSize) {
        return dishSearchService.searchDishByCode(code, pageNum, pageSize);
    }

    @Override
    public SearchResult<DishEs> searchDishByName(String name, int type, int pageNum, int pageSize) {
        return dishSearchService.searchDishByName(name, type, pageNum, pageSize);
    }
}
