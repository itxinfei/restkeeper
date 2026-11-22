package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.exception.BussinessException;
import com.restkeeper.http.PageResult;
import com.restkeeper.mq.DishIndexMessage;
import com.restkeeper.store.entity.Dish;
import com.restkeeper.store.entity.SetMeal;
import com.restkeeper.store.entity.SetMealDish;
import com.restkeeper.store.mapper.SetMealMapper;
import com.restkeeper.store.mq.DishIndexEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐本地服务
 */
@Service("setMealService")
public class SetMealServiceImpl extends ServiceImpl<SetMealMapper, SetMeal> {

    @Autowired
    private SetMealDishServiceImpl setMealDishService;

    @Autowired
    private DishServiceImpl dishService;

    @Autowired
    private DishIndexEventPublisher dishIndexEventPublisher;

    /**
     * 套餐分页（按名称模糊）
     */
    public PageResult<SetMeal> queryPage(int pageNum, int pageSize, String name) {
        Page<SetMeal> page = new Page<>(pageNum, pageSize);
        QueryWrapper<SetMeal> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(name)) {
            queryWrapper.lambda().like(SetMeal::getName, name);
        }
        return PageResult.of(this.page(page, queryWrapper));
    }

    /**
     * 新增套餐及套餐菜品关联
     */
    @Transactional
    public boolean add(SetMeal setMeal, List<SetMealDish> setMealDishes) {
        this.save(setMeal);

        setMealDishes.forEach(setMealDish -> {
            setMealDish.setSetMealId(setMeal.getId());
            setMealDish.setIndex(0);
        });
        setMealDishService.saveBatch(setMealDishes);
        dishIndexEventPublisher.publishSetMeal(setMeal.getId(), DishIndexMessage.ACTION_UPSERT);
        return true;
    }

    /**
     * 修改套餐及套餐菜品关联（先删后建）
     */
    @Transactional
    public boolean update(SetMeal setMeal, List<SetMealDish> setMealDishes) {
        this.updateById(setMeal);

        if (setMealDishes != null && !setMealDishes.isEmpty()) {
            QueryWrapper<SetMealDish> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(SetMealDish::getSetMealId, setMeal.getId());
            setMealDishService.remove(queryWrapper);

            setMealDishes.forEach(setMealDish -> setMealDish.setSetMealId(setMeal.getId()));
            setMealDishService.saveBatch(setMealDishes);
        }
        dishIndexEventPublisher.publishSetMeal(setMeal.getId(), DishIndexMessage.ACTION_UPSERT);
        return true;
    }

    /**
     * 批量停售套餐
     */
    @Transactional
    public boolean forbiddenSetMeals(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BussinessException("没有需要修改的套餐信息");
        }

        UpdateWrapper<SetMeal> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().set(SetMeal::getStatus, SystemCode.FORBIDDEN).in(SetMeal::getId, ids);
        boolean result = this.update(updateWrapper);
        ids.forEach(id -> dishIndexEventPublisher.publishSetMeal(id, DishIndexMessage.ACTION_UPSERT));
        return result;
    }

    /**
     * 批量起售套餐：套餐下存在停售菜品时拒绝
     */
    @Transactional
    public boolean enableSetMeals(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BussinessException("没有需要修改的套餐信息");
        }

        QueryWrapper<SetMealDish> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DISTINCT dish_id").lambda().in(SetMealDish::getSetMealId, ids);
        List<SetMealDish> setMealDishList = setMealDishService.list(queryWrapper);

        List<String> dishIds = setMealDishList.stream().map(SetMealDish::getDishId).collect(Collectors.toList());
        if (!dishIds.isEmpty()) {
            QueryWrapper<Dish> queryDishWrapper = new QueryWrapper<>();
            queryDishWrapper.lambda().eq(Dish::getStatus, SystemCode.FORBIDDEN).in(Dish::getId, dishIds);
            List<Dish> dishes = dishService.list(queryDishWrapper);
            if (!dishes.isEmpty()) {
                throw new BussinessException("套餐下有停售的菜品，不能起售该套餐");
            }
        }

        UpdateWrapper<SetMeal> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().set(SetMeal::getStatus, SystemCode.ENABLED).in(SetMeal::getId, ids);
        boolean result = this.update(updateWrapper);
        ids.forEach(id -> dishIndexEventPublisher.publishSetMeal(id, DishIndexMessage.ACTION_UPSERT));
        return result;
    }

    /**
     * 批量逻辑删除套餐（K16：同步删除 ES 索引）
     */
    @Override
    @Transactional
    public boolean removeByIds(Collection<?> ids) {
        boolean result = super.removeByIds(ids);
        ids.forEach(id -> dishIndexEventPublisher.publishSetMeal(String.valueOf(id), DishIndexMessage.ACTION_DELETE));
        return result;
    }
}
