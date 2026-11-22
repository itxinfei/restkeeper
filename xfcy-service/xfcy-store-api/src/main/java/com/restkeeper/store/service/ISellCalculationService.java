package com.restkeeper.store.service;

import com.restkeeper.http.PageResult;
import com.restkeeper.store.entity.SellCalculation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import java.util.List;

@Tag(name = "沽清服务")
@HttpExchange("/sellCalculation")
public interface ISellCalculationService {

    //分页查询（菜品名称模糊、菜品类型 1普通/2套餐 精确，双租户插件自动隔离门店）
    @GetExchange("/page/{pageNum}/{pageSize}")
    PageResult<SellCalculation> queryPage(@PathVariable("pageNum") int pageNum,
                                          @PathVariable("pageSize") int pageSize,
                                          @RequestParam(value = "dishName", required = false) String dishName,
                                          @RequestParam(value = "dishType", required = false) Integer dishType);

    //按菜品 id 查询剩余沽清数量
    @GetExchange("/remainder/{dishId}")
    Integer getRemainderCount(@PathVariable("dishId") String dishId);

    //扣减沽清数量（下单流程内部调用）
    @PutExchange("/decrease")
    void decrease(@RequestParam("dishId") String dishId,
                  @RequestParam("dishNumber") Integer dishNumber);

    //增加沽清数量（退菜流程内部调用；无记录则新建）
    @PostExchange("/add")
    void add(@RequestParam("dishId") String dishId,
             @RequestParam("dishNumber") int dishNumber);

    //保存沽清记录
    @PostExchange
    boolean save(@RequestBody SellCalculation sellCalculation);

    //批量删除沽清记录
    @DeleteExchange
    boolean removeByIds(@RequestBody List<String> ids);
}
