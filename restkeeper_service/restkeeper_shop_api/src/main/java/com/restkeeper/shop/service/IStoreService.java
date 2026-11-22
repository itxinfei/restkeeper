package com.restkeeper.shop.service;

import com.restkeeper.http.PageResult;
import com.restkeeper.shop.dto.StoreDTO;
import com.restkeeper.shop.entity.Store;
import com.restkeeper.utils.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import java.util.List;

@Tag(name = "门店服务")
@HttpExchange("/store")
public interface IStoreService {

    //分页查询（按门店名称模糊）
    @GetExchange("/page/{pageNo}/{pageSize}")
    PageResult<Store> queryPageByName(@PathVariable("pageNo") int pageNo,
                                      @PathVariable("pageSize") int pageSize,
                                      @RequestParam(value = "name", required = false) String name);

    //保存门店
    @PostExchange
    boolean save(@RequestBody Store store);

    //按主键查询门店
    @GetExchange("/{id}")
    Store getById(@PathVariable("id") String id);

    //按主键更新门店
    @PutExchange
    boolean updateById(@RequestBody Store store);

    //停用门店（置为停业 0）
    @PutExchange("/disable/{id}")
    boolean disable(@PathVariable("id") String id);

    //启用门店（恢复营业 1）
    @PutExchange("/enable/{id}")
    boolean enable(@PathVariable("id") String id);

    //查询省份列表
    @GetExchange("/province/list")
    List<String> getAllProvince();

    //按省份查询门店列表
    @GetExchange("/province/{province}")
    List<StoreDTO> getStoreByProvince(@PathVariable("province") String province);

    //查询当前登录经理管理的门店列表
    @GetExchange("/manager/list")
    List<StoreDTO> getStoreListByManagerId();

    //切换当前操作门店
    @PutExchange("/switch/{storeId}")
    Result switchStore(@PathVariable("storeId") String storeId);
}
