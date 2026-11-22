package com.restkeeper.shop.service;

import com.restkeeper.http.PageResult;
import com.restkeeper.shop.entity.Brand;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;
import java.util.Map;

@Tag(name = "品牌服务")
@HttpExchange("/brand")
public interface IBrandService {

    //分页查询（降序）
    @GetExchange("/page/{pageNo}/{pageSize}")
    PageResult<Brand> queryPage(@PathVariable("pageNo") int pageNo,
                                @PathVariable("pageSize") int pageSize);

    //保存品牌
    @PostExchange
    boolean save(@RequestBody Brand brand);

    //查询品牌下拉列表
    @GetExchange("/list")
    List<Map<String, Object>> getBrandList();
}
