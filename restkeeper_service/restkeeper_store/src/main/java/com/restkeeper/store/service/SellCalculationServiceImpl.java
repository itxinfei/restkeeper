package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.restkeeper.http.PageResult;
import com.restkeeper.store.entity.SellCalculation;
import com.restkeeper.store.mapper.SellCalculationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 沽清本地服务
 */
@Service("sellCalculationService")
public class SellCalculationServiceImpl extends ServiceImpl<SellCalculationMapper, SellCalculation> {

    /**
     * 沽清分页：dish_name 为本表冗余快照，直接 like 无需关联菜品表
     */
    public PageResult<SellCalculation> queryPage(int pageNum, int pageSize, String dishName, Integer dishType) {
        Page<SellCalculation> page = new Page<>(pageNum, pageSize);
        QueryWrapper<SellCalculation> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(dishName)) {
            queryWrapper.lambda().like(SellCalculation::getDishName, dishName);
        }
        if (dishType != null) {
            queryWrapper.lambda().eq(SellCalculation::getDishType, dishType);
        }
        return PageResult.of(this.page(page, queryWrapper));
    }

    /**
     * 查询菜品剩余沽清数量，无记录表示不限量返回 -1
     */
    public Integer getRemainderCount(String dishId) {
        QueryWrapper<SellCalculation> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().select(SellCalculation::getRemainder).eq(SellCalculation::getDishId, dishId);
        SellCalculation sellCalculation = this.getOne(queryWrapper);
        if (sellCalculation == null) {
            return -1;
        }
        return sellCalculation.getRemainder();
    }

    /**
     * 扣减沽清数量（不小于 0）
     */
    @Transactional
    public void decrease(String dishId, Integer dishNumber) {
        SellCalculation sellCalculation = getByDishId(dishId);
        if (sellCalculation != null) {
            int remainder = sellCalculation.getRemainder() - dishNumber;
            sellCalculation.setRemainder(Math.max(remainder, 0));
            this.updateById(sellCalculation);
        }
    }

    /**
     * 回加沽清数量（退菜）
     */
    @Transactional
    public void add(String dishId, int dishNum) {
        SellCalculation sellCalculation = getByDishId(dishId);
        sellCalculation.setRemainder(sellCalculation.getRemainder() + dishNum);
        this.updateById(sellCalculation);
    }

    private SellCalculation getByDishId(String dishId) {
        QueryWrapper<SellCalculation> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SellCalculation::getDishId, dishId);
        return this.getOne(queryWrapper);
    }
}
