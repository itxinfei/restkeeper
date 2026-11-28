package com.restkeeper.shop.service;

import com.restkeeper.http.PageResult;
import com.restkeeper.shop.entity.StoreManager;
import com.restkeeper.utils.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import java.util.List;

@Tag(name = "门店经理服务")
@HttpExchange("/store/manager")
public interface IStoreManagerService {

    //按条件分页查询
    @GetExchange("/page/{pageNo}/{pageSize}")
    PageResult<StoreManager> queryPageByCriteria(@PathVariable("pageNo") int pageNo,
                                                 @PathVariable("pageSize") int pageSize,
                                                 @RequestParam(value = "criteria", required = false) String criteria);

    //增加门店经理
    @PostExchange("/add")
    boolean addStoreManager(@RequestParam("name") String name,
                            @RequestParam("phone") String phone,
                            //非必填：缺失时由 provider 统一给「请选择店长归属门店」业务提示
                            @RequestParam(value = "storeIds", required = false) List<String> storeIds);

    //修改门店经理
    @PutExchange("/update")
    boolean updateStoreManager(@RequestParam("storeManagerId") String storeManagerId,
                               @RequestParam("name") String name,
                               @RequestParam("phone") String phone,
                               //非必填：缺失时由 provider 统一给「请选择店长归属门店」业务提示
                               @RequestParam(value = "storeIds", required = false) List<String> storeIds);

    //逻辑删除门店经理
    @DeleteExchange("/{storeManagerId}")
    boolean deleteStoreManager(@PathVariable("storeManagerId") String storeManagerId);

    //停用门店经理
    @PutExchange("/pause/{storeManagerId}")
    boolean pauseStoreManager(@PathVariable("storeManagerId") String storeManagerId);

    //登录
    @PostExchange("/login")
    Result login(@RequestParam("shopId") String shopId,
                 @RequestParam("phone") String phone,
                 @RequestParam("loginPass") String loginPass);
}
