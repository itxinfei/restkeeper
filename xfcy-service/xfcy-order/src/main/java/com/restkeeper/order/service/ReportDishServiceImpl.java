package com.restkeeper.order.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.restkeeper.entity.ReportDishEntity;
import com.restkeeper.order.mapper.ReportDishMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 菜品报表本地服务
 */
@Service("reportDishService")
public class ReportDishServiceImpl extends ServiceImpl<ReportDishMapper, ReportDishEntity> {

    //菜品分类销售额汇总（饼图）
    public List<ReportDishEntity> getCategoryAmountCollect(LocalDate start, LocalDate end) {

        QueryWrapper<ReportDishEntity> wrapper = new QueryWrapper<>();
        wrapper.select("category","sum(dish_number) as dish_number","sum(dish_money) as dish_money")
                .lambda().ge(ReportDishEntity::getPayDate, start).lt(ReportDishEntity::getPayDate, end)
                .groupBy(ReportDishEntity::getCategory);

        return this.list(wrapper);
    }

    //菜品销量排行
    public List<ReportDishEntity> getDishRank(LocalDate start, LocalDate end) {

        QueryWrapper<ReportDishEntity> wrapper = new QueryWrapper<>();

        wrapper.select("dish_name","sum(dish_number) as dish_number")
                .lambda().ge(ReportDishEntity::getPayDate, start).lt(ReportDishEntity::getPayDate, end)
                .groupBy(ReportDishEntity::getDishName);
        return this.list(wrapper);
    }

}
