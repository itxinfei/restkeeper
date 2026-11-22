package com.restkeeper.service;

import com.restkeeper.entity.DishEs;
import com.restkeeper.entity.SearchResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@Tag(name = "菜品检索服务")
@HttpExchange("/search/dish")
public interface IDishSearchService {

    //根据商品码与类型检索
    @GetExchange("/code/{code}/type/{type}/page/{pageNum}/{pageSize}")
    SearchResult<DishEs> searchAllByCode(@PathVariable("code") String code,
                                         @PathVariable("type") int type,
                                         @PathVariable("pageNum") int pageNum,
                                         @PathVariable("pageSize") int pageSize);

    //根据商品码检索菜品
    @GetExchange("/code/{code}/page/{pageNum}/{pageSize}")
    SearchResult<DishEs> searchDishByCode(@PathVariable("code") String code,
                                          @PathVariable("pageNum") int pageNum,
                                          @PathVariable("pageSize") int pageSize);

    //根据商品名称与类型检索
    @GetExchange("/name/{name}/type/{type}/page/{pageNum}/{pageSize}")
    SearchResult<DishEs> searchDishByName(@PathVariable("name") String name,
                                          @PathVariable("type") int type,
                                          @PathVariable("pageNum") int pageNum,
                                          @PathVariable("pageSize") int pageSize);
}
