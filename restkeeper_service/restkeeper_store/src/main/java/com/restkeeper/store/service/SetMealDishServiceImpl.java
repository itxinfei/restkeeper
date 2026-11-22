package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.restkeeper.store.entity.Dish;
import com.restkeeper.store.entity.SetMealDish;
import com.restkeeper.store.mapper.SetMealDishMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 套餐菜品关联本地服务
 */
@Service("setMealDishService")
public class SetMealDishServiceImpl extends ServiceImpl<SetMealDishMapper, SetMealDish> {

    @Autowired
    private DishServiceImpl dishService;

    /**
     * 按套餐 id 查询其包含的全部菜品
     */
    public List<Dish> getAllDishBySetMealId(String setMealId) {
        List<SetMealDish> setMealDishList = this.getBaseMapper().selectDishes(setMealId);

        List<String> dishIds = new ArrayList<>();
        setMealDishList.forEach(setMealDish -> dishIds.add(setMealDish.getDishId()));

        return dishService.listByIds(dishIds);
    }

    /**
     * 查询某菜品在套餐中的份数
     */
    public Integer getDishCopiesInSetMeal(String dishId, String setMealId) {
        QueryWrapper<SetMealDish> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(SetMealDish::getDishId, dishId).eq(SetMealDish::getSetMealId, setMealId);
        SetMealDish setMealDish = this.getOne(wrapper);
        return setMealDish == null ? 0 : setMealDish.getDishCopies();
    }
}
