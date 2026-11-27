package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.restkeeper.store.entity.Dish;
import com.restkeeper.store.entity.DishCategory;
import com.restkeeper.store.entity.SetMealDish;
import com.restkeeper.store.mapper.SetMealDishMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 套餐菜品关联本地服务
 */
@Service("setMealDishService")
public class SetMealDishServiceImpl extends ServiceImpl<SetMealDishMapper, SetMealDish> {

    @Autowired
    @Lazy
    private DishServiceImpl dishService;

    @Autowired
    @Lazy
    private DishCategoryServiceImpl dishCategoryService;

    /**
     * 按套餐 id 查询其包含的全部菜品
     */
    public List<Dish> getAllDishBySetMealId(String setMealId) {
        List<SetMealDish> setMealDishList = this.getBaseMapper().selectDishes(setMealId);

        //套餐未配菜时直接返回空列表：listByIds(空集合) 会生成 "IN ()" 非法 SQL 直接抛语法错误
        if (setMealDishList == null || setMealDishList.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> dishIds = setMealDishList.stream()
                .map(SetMealDish::getDishId)
                .filter(Objects::nonNull)
                .toList();

        List<Dish> dishes = dishIds.isEmpty() ? new ArrayList<>() : dishService.listByIds(dishIds);
        //listByIds 不联表：Dish.dishCategory 是 exist=false 字段，必须按 category_id 回填，
        //否则下游结账拆套餐明细取分类名必然 NPE
        if (dishes != null && !dishes.isEmpty()) {
            List<String> categoryIds = dishes.stream()
                    .map(Dish::getCategoryId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            Map<String, String> nameMap = categoryIds.isEmpty() ? Map.of()
                    : dishCategoryService.lambdaQuery()
                        .in(DishCategory::getCategoryId, categoryIds)
                        .list().stream()
                        .collect(Collectors.toMap(DishCategory::getCategoryId,
                                c -> c.getName() == null ? "" : c.getName(), (a, b) -> a));
            dishes.forEach(d -> d.setDishCategory(new DishCategory()
                    .setCategoryId(d.getCategoryId())
                    .setName(nameMap.getOrDefault(d.getCategoryId(), ""))));
        }
        return dishes;
    }

    /**
     * 查询某菜品在套餐中的份数
     */
    public Integer getDishCopiesInSetMeal(String dishId, String setMealId) {
        QueryWrapper<SetMealDish> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(SetMealDish::getDishId, dishId).eq(SetMealDish::getSetMealId, setMealId);
        SetMealDish setMealDish = this.getOne(wrapper);
        //返回类型 Integer：不能写 "x == null ? 0 : getDishCopies()"，否则自动拆箱在份数为空时 NPE；
        //份数为空按 null 返回，由调用方（结账拆明细）给出可理解的业务提示
        if (setMealDish == null || setMealDish.getDishCopies() == null) {
            return null;
        }
        return setMealDish.getDishCopies();
    }
}
