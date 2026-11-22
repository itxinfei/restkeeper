package com.restkeeper.shop.controller;

import com.restkeeper.http.PageResult;
import com.restkeeper.shop.entity.Brand;
import com.restkeeper.shop.service.BrandServiceImpl;
import com.restkeeper.shop.service.IBrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 品牌 HTTP 端点：契约映射由 {@link IBrandService} 上的 @HttpExchange 提供
 */
@RestController
public class BrandController implements IBrandService {

    @Autowired
    private BrandServiceImpl brandService;

    @Override
    public PageResult<Brand> queryPage(int pageNo, int pageSize) {
        return brandService.queryPage(pageNo, pageSize);
    }

    @Override
    public boolean save(Brand brand) {
        return brandService.save(brand);
    }

    @Override
    public List<Map<String, Object>> getBrandList() {
        return brandService.getBrandList();
    }
}
