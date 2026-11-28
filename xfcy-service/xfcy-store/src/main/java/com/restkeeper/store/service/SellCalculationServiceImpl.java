package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.restkeeper.exception.BussinessException;
import com.restkeeper.http.PageResult;
import com.restkeeper.store.entity.SellCalculation;
import com.restkeeper.store.mapper.SellCalculationMapper;
import com.restkeeper.tenant.TenantContext;
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
     * 扣减沽清数量：改为单条原子 UPDATE（remainder >= n 才扣），
     * 并发下不再出现两个订单读到同一余量、后写的覆盖前者导致超卖。
     */
    @Transactional
    public void decrease(String dishId, Integer dishNumber) {
        if (dishId == null || dishNumber == null || dishNumber <= 0) {
            return;
        }
        String shopId = TenantContext.getShopId();
        String storeId = TenantContext.getStoreId();
        if (shopId == null || storeId == null) {
            throw new BussinessException("租户上下文缺失，无法扣减沽清");
        }
        //无记录表示不限量，无需扣减
        if (getByDishId(dishId) == null) {
            return;
        }
        int affected = this.getBaseMapper().decreaseAtomic(dishId, dishNumber, shopId, storeId);
        if (affected == 0) {
            //并发下余量已被其它请求扣到不足，明确报业务错误
            SellCalculation current = getByDishId(dishId);
            throw new BussinessException((current == null ? dishId : current.getDishName()) + "库存不足");
        }
    }

    /**
     * 回加沽清数量（退菜）：无记录则新建，避免 NPE（K6）
     */
    @Transactional
    public void add(String dishId, int dishNumber) {
        SellCalculation sellCalculation = getByDishId(dishId);
        if (sellCalculation == null) {
            sellCalculation = new SellCalculation();
            sellCalculation.setDishId(dishId);
            sellCalculation.setRemainder(dishNumber);
            this.save(sellCalculation);
            return;
        }
        sellCalculation.setRemainder(sellCalculation.getRemainder() + dishNumber);
        this.updateById(sellCalculation);
    }

    private SellCalculation getByDishId(String dishId) {
        QueryWrapper<SellCalculation> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SellCalculation::getDishId, dishId);
        return this.getOne(queryWrapper);
    }
}
