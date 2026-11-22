package com.restkeeper.store.service;

import com.restkeeper.http.PageResult;
import com.restkeeper.store.dto.CreditWithCompanyUsers;
import com.restkeeper.store.entity.Credit;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

@Tag(name = "挂账服务")
@HttpExchange("/credit")
public interface ICreditService {

    //新增挂账（含关联单位用餐人）
    @PostExchange("/add")
    boolean add(@RequestBody CreditWithCompanyUsers request);

    //挂账分页列表（按用餐人名称模糊）
    @GetExchange("/page/{pageNum}/{pageSize}")
    PageResult<Credit> queryPage(@PathVariable("pageNum") int pageNum,
                                 @PathVariable("pageSize") int pageSize,
                                 @RequestParam(value = "userName", required = false) String userName);

    //挂账详情回显（含关联单位用餐人）
    @GetExchange("/{id}")
    Credit queryById(@PathVariable("id") String id);

    //修改挂账（含关联单位用餐人）
    @PutExchange
    boolean updateInfo(@RequestBody CreditWithCompanyUsers request);

    //按主键查询挂账主体（挂账结账流程内部调用）
    @GetExchange("/get/{id}")
    Credit getById(@PathVariable("id") String id);

    //保存或更新挂账主体（挂账结账流程内部调用）
    @PostExchange("/saveOrUpdate")
    boolean saveOrUpdate(@RequestBody Credit credit);
}
