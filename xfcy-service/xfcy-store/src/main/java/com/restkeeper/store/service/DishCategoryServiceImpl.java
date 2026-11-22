package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.restkeeper.exception.BussinessException;
import com.restkeeper.http.PageResult;
import com.restkeeper.store.entity.Dish;
import com.restkeeper.store.entity.DishCategory;
import com.restkeeper.store.entity.SetMeal;
import com.restkeeper.store.mapper.DishCategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 菜品分类本地服务
 */
@Service("dishCategoryService")
public class DishCategoryServiceImpl extends ServiceImpl<DishCategoryMapper, DishCategory> {

    @Autowired
    private DishServiceImpl dishService;

    @Autowired
    private SetMealServiceImpl setMealService;

    /**
     * 新增分类（同名校验）
     */
    @Transactional
    public boolean add(String name, int type) {
        checkNameExist(name);

        DishCategory dishCategory = new DishCategory();
        dishCategory.setName(name);
        dishCategory.setType(type);
        dishCategory.setTorder(0);
        return this.save(dishCategory);
    }

    /**
     * 修改分类名称（同名校验）
     */
    @Transactional
    public boolean update(String id, String name) {
        checkNameExist(name);

        UpdateWrapper<DishCategory> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().set(DishCategory::getName, name).eq(DishCategory::getCategoryId, id);
        return this.update(updateWrapper);
    }

    /**
     * 分类分页（按最后更新时间降序）
     */
    public PageResult<DishCategory> queryPage(int pageNum, int pageSize) {
        Page<DishCategory> page = new Page<>(pageNum, pageSize);
        QueryWrapper<DishCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().orderByDesc(DishCategory::getLastUpdateTime);
        return PageResult.of(this.page(page, queryWrapper));
    }

    /**
     * 按分类类别查询下拉列表
     */
    public List<Map<String, Object>> findCategoryList(Integer type) {
        QueryWrapper<DishCategory> queryWrapper = new QueryWrapper<>();
        if (type != null) {
            queryWrapper.lambda().eq(DishCategory::getType, type);
        }
        queryWrapper.lambda().select(DishCategory::getCategoryId, DishCategory::getName);
        return this.listMaps(queryWrapper);
    }

    /**
     * 删除分类：有关联菜品/套餐时拒绝
     */
    @Transactional
    public boolean delete(String id) {
        DishCategory dishCategory = this.getById(id);
        if (dishCategory == null) {
            throw new BussinessException("分类不存在");
        }

        if (dishCategory.getType() == 1) {
            QueryWrapper<Dish> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(Dish::getCategoryId, id);
            long count = dishService.count(queryWrapper);
            if (count > 0) {
                throw new BussinessException("当前分类下已经关联的菜品信息，不允许删除");
            }
        }

        if (dishCategory.getType() == 2) {
            QueryWrapper<SetMeal> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(SetMeal::getCategoryId, id);
            long count = setMealService.count(queryWrapper);
            if (count > 0) {
                throw new BussinessException("当前分类下已经关联的套餐信息，不允许删除");
            }
        }

        return this.removeById(id);
    }

    /**
     * 分类名称重复校验
     */
    private void checkNameExist(String name) {
        QueryWrapper<DishCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().select(DishCategory::getCategoryId).eq(DishCategory::getName, name);
        long count = this.getBaseMapper().selectCount(queryWrapper);
        if (count > 0) {
            throw new BussinessException("该分类已经存在");
        }
    }
}
