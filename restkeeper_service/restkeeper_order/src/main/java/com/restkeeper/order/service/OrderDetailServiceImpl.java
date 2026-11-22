package com.restkeeper.order.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.restkeeper.entity.OrderDetailAllView;
import com.restkeeper.entity.OrderDetailEntity;
import com.restkeeper.order.mapper.OrderDetailAllMapper;
import com.restkeeper.order.mapper.OrderDetailMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 订单明细本地服务；报表查询走 OrderDetailAllMapper 视图
 */
@Service("orderDetailService")
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetailEntity> {

    @Autowired
    private OrderDetailAllMapper orderDetailAllMapper;

    //销售额
    public List<OrderDetailAllView> getCurrentCategoryAmountCollect(LocalDate start, LocalDate end) {
        QueryWrapper<OrderDetailAllView> wrapper = new QueryWrapper<>();
        wrapper.select("dish_category_name","SUM(dish_amount) as dish_amount")
                .lambda().ge(OrderDetailAllView::getLastUpdateTime,start)
                .lt(OrderDetailAllView::getLastUpdateTime,end)
                .eq(OrderDetailAllView::getDishType,1)
                .groupBy(OrderDetailAllView::getDishCategoryName);
        return orderDetailAllMapper.selectList(wrapper);
    }

    //销量
    public List<OrderDetailAllView> getCurrentCategoryCountCollect(LocalDate start, LocalDate end) {
        QueryWrapper<OrderDetailAllView> wrapper = new QueryWrapper<>();
        wrapper.select("dish_category_name","count(dish_category_name) as total_count")
                .lambda().ge(OrderDetailAllView::getLastUpdateTime,start)
                .lt(OrderDetailAllView::getLastUpdateTime,end)
                .eq(OrderDetailAllView::getDishType,1)
                .groupBy(OrderDetailAllView::getDishCategoryName);
        return orderDetailAllMapper.selectList(wrapper);
    }

    //菜品销售排行
    public List<OrderDetailAllView> getCurrentDishRank(LocalDate start, LocalDate end) {
        QueryWrapper<OrderDetailAllView> wrapper = new QueryWrapper<>();

        wrapper.select("dish_name","SUM(dish_number) as total_count")
                .lambda().ge(OrderDetailAllView::getLastUpdateTime,start)
                .lt(OrderDetailAllView::getLastUpdateTime,end)
                .eq(OrderDetailAllView::getDishType,1)
                .groupBy(OrderDetailAllView::getDishName);
        wrapper.orderByDesc("total_count");

        return orderDetailAllMapper.selectList(wrapper);
    }
}
