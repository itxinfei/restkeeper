package com.restkeeper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.entity.OrderDetailAllEntity;
import com.restkeeper.entity.ReportDish;
import com.restkeeper.mapper.OrderDetailAllMapper;
import com.restkeeper.mapper.ReportDishMapper;
import com.restkeeper.service.ReportDishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportDishServiceImpl implements ReportDishService {

    @Autowired
    private OrderDetailAllMapper orderDetailAllMapper;

    @Autowired
    private ReportDishMapper reportDishMapper;

    @Override
    @Transactional
    public void generateData() {

        LocalDate end = LocalDate.now();
        LocalDate start = end.plusDays(-1);

        //生成前先清掉同一统计日的历史汇总，保证任务可重复执行不会主键冲突或数据翻倍
        reportDishMapper.delete(new LambdaQueryWrapper<ReportDish>().eq(ReportDish::getPayDate, start));

        QueryWrapper<OrderDetailAllEntity> wrapper = new QueryWrapper<>();
        wrapper.select("dish_name","dish_category_name","SUM(dish_number) as dish_number","SUM(dish_amount) as dish_price","store_id","shop_id")
                .lambda().ge(OrderDetailAllEntity::getLastUpdateTime,start).lt(OrderDetailAllEntity::getLastUpdateTime,end)
                //用括号包住明细状态条件，避免 or 把日期范围过滤绕过
                .and(w -> w.eq(OrderDetailAllEntity::getDetailStatus,1).or().eq(OrderDetailAllEntity::getDetailStatus,4))
                //只统计已付款订单的菜品销量
                .eq(OrderDetailAllEntity::getPayStatus,SystemCode.ORDER_STATUS_PAYED)
                .groupBy(OrderDetailAllEntity::getDishName,OrderDetailAllEntity::getDishCategoryName,OrderDetailAllEntity::getShopId,OrderDetailAllEntity::getStoreId);
        List<OrderDetailAllEntity> detailAllEntityList = orderDetailAllMapper.selectList(wrapper);

        detailAllEntityList.forEach(d->{
            ReportDish reportDish = new ReportDish();
            reportDish.setCategory(d.getDishCategoryName());
            reportDish.setDishMoney(d.getDishPrice());
            reportDish.setDishName(d.getDishName());
            reportDish.setDishNumber(d.getDishNumber());
            reportDish.setPayDate(start);
            reportDish.setShopId(d.getShopId());
            reportDish.setStoreId(d.getStoreId());
            reportDish.setIsDeleted(0);
            reportDish.setLastUpdateTime(LocalDateTime.now());
            reportDishMapper.insert(reportDish);
        });
    }
}
