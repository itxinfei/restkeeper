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
import com.restkeeper.store.entity.DishFlavor;
import com.restkeeper.store.entity.SetMeal;
import com.restkeeper.store.entity.SetMealDish;
import com.restkeeper.store.mapper.DishMapper;
import com.restkeeper.store.mq.DishIndexEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜品本地服务
 */
@Service("dishService")
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> {

    @Autowired
    private DishFlavorServiceImpl dishFlavorService;

    @Autowired
    private SetMealDishServiceImpl setMealDishService;

    @Autowired
    private SetMealServiceImpl setMealService;

    @Autowired
    private DishIndexEventPublisher dishIndexEventPublisher;

    /**
     * 新增菜品及口味
     */
    @Transactional
    public boolean save(Dish dish, List<DishFlavor> dishFlavorList) {
        this.save(dish);

        dishFlavorList.forEach(dishFlavor -> dishFlavor.setDishId(dish.getId()));
        dishFlavorService.saveBatch(dishFlavorList);
        dishIndexEventPublisher.publishDish(dish.getId(), DishIndexMessage.ACTION_UPSERT);
        return true;
    }

    /**
     * 修改菜品及口味（先删后建）
     */
    @Transactional
    public boolean update(Dish dish, List<DishFlavor> dishFlavorList) {
        this.updateById(dish);

        QueryWrapper<DishFlavor> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DishFlavor::getDishId, dish.getId());
        dishFlavorService.remove(queryWrapper);

        dishFlavorList.forEach(dishFlavor -> dishFlavor.setDishId(dish.getId()));
        dishFlavorService.saveBatch(dishFlavorList);
        dishIndexEventPublisher.publishDish(dish.getId(), DishIndexMessage.ACTION_UPSERT);
        return true;
    }

    /**
     * 根据分类与名称查询可售菜品（下拉用精简列）
     */
    public List<Map<String, Object>> findEnableDishListInfo(String categoryId, String name) {
        QueryWrapper<Dish> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().select(Dish::getId, Dish::getName, Dish::getStatus, Dish::getPrice);

        if (StringUtils.hasText(categoryId)) {
            queryWrapper.lambda().eq(Dish::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(name)) {
            queryWrapper.lambda().eq(Dish::getName, name);
        }
        queryWrapper.lambda().eq(Dish::getStatus, SystemCode.ENABLED);
        return this.listMaps(queryWrapper);
    }

    /**
     * 管理端菜品分页：与点餐端 findEnableDishListInfo 不同，不强制 status=1，name 用 like。
     * 经 Dish 类级 resultMap 以嵌套 select 装配分类与口味，每页少量数据，N+1 在教学项目可接受。
     */
    public PageResult<Dish> queryManagePage(int pageNum, int pageSize, String name, String categoryId, Integer status) {
        Page<Dish> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Dish> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(name)) {
            queryWrapper.lambda().like(Dish::getName, name);
        }
        if (StringUtils.hasText(categoryId)) {
            queryWrapper.lambda().eq(Dish::getCategoryId, categoryId);
        }
        if (status != null) {
            queryWrapper.lambda().eq(Dish::getStatus, status);
        }
        return PageResult.of(this.page(page, queryWrapper));
    }

    /**
     * 批量停售菜品，并连带停售关联套餐
     */
    @Transactional
    public boolean forbiddenDishes(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BussinessException("校验参数失败");
        }

        UpdateWrapper<Dish> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().set(Dish::getStatus, SystemCode.FORBIDDEN).in(Dish::getId, ids);
        this.update(updateWrapper);
        ids.forEach(id -> dishIndexEventPublisher.publishDish(id, DishIndexMessage.ACTION_UPSERT));

        List<String> setMealIds = listRelatedSetMealIds(ids);
        if (!setMealIds.isEmpty()) {
            UpdateWrapper<SetMeal> setMealUpdateWrapper = new UpdateWrapper<>();
            setMealUpdateWrapper.lambda().set(SetMeal::getStatus, SystemCode.FORBIDDEN).in(SetMeal::getId, setMealIds);
            setMealService.update(setMealUpdateWrapper);
        }
        return true;
    }

    /**
     * 批量起售菜品
     */
    @Transactional
    public boolean enableDishes(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BussinessException("校验参数失败");
        }

        UpdateWrapper<Dish> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().set(Dish::getStatus, SystemCode.ENABLED).in(Dish::getId, ids);
        boolean result = this.update(updateWrapper);
        ids.forEach(id -> dishIndexEventPublisher.publishDish(id, DishIndexMessage.ACTION_UPSERT));
        return result;
    }

    /**
     * 批量逻辑删除菜品，关联套餐先停售
     */
    @Transactional
    public boolean deleteDishes(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BussinessException("校验参数失败");
        }

        List<String> setMealIds = listRelatedSetMealIds(ids);
        if (!setMealIds.isEmpty()) {
            UpdateWrapper<SetMeal> updateWrapper = new UpdateWrapper<>();
            updateWrapper.lambda().set(SetMeal::getStatus, SystemCode.FORBIDDEN).in(SetMeal::getId, setMealIds);
            setMealService.update(updateWrapper);
        }

        this.removeByIds(ids);
        ids.forEach(id -> dishIndexEventPublisher.publishDish(id, DishIndexMessage.ACTION_DELETE));
        return true;
    }

    /**
     * 查询与给定菜品关联的去重套餐 id
     */
    private List<String> listRelatedSetMealIds(List<String> dishIds) {
        QueryWrapper<SetMealDish> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DISTINCT setmeal_id").lambda().in(SetMealDish::getDishId, dishIds);
        List<SetMealDish> setMealDishList = setMealDishService.list(queryWrapper);
        return setMealDishList.stream().map(SetMealDish::getSetMealId).collect(Collectors.toList());
    }
}
