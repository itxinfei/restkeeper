package com.restkeeper.operator.service;

import com.restkeeper.http.PageResult;
import com.restkeeper.operator.entity.OperatorUser;
import com.restkeeper.utils.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@Tag(name = "运营人员服务")
@HttpExchange("/operator/user")
public interface IOperatorUserService {

    //按名称分页查询
    @GetExchange("/page/{pageNum}/{pageSize}")
    PageResult<OperatorUser> queryPageByName(@PathVariable("pageNum") int pageNum,
                                             @PathVariable("pageSize") int pageSize,
                                             @RequestParam(value = "name", required = false) String name);

    //登录
    @PostExchange("/login")
    Result login(@RequestParam("loginName") String loginName,
                 @RequestParam("loginPass") String loginPass);
}
