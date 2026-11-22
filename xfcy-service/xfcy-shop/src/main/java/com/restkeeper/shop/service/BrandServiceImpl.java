package com.restkeeper.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.restkeeper.http.PageResult;
import com.restkeeper.shop.entity.Brand;
import com.restkeeper.shop.mapper.BrandMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 品牌本地服务：不再实现 api 契约接口，HTTP 端点由 BrandController 薄转发。
 */
@Service
@Slf4j
public class BrandServiceImpl extends ServiceImpl<BrandMapper, Brand> {

    /**
     * 分页查询（按最后更新时间降序）
     */
    public PageResult<Brand> queryPage(int pageNo, int pageSize) {
        Page<Brand> page = new Page<>(pageNo, pageSize);
        QueryWrapper<Brand> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().orderByDesc(Brand::getLastUpdateTime);
        return PageResult.of(this.page(page, queryWrapper));
    }

    /**
     * 品牌下拉列表（只取 brand_id、brand_name）
     */
    public List<Map<String, Object>> getBrandList() {
        QueryWrapper<Brand> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().select(Brand::getBrandId, Brand::getBrandName);
        return this.listMaps(queryWrapper);
    }
}
