package com.restkeeper.store.service;

import com.restkeeper.store.entity.CreditCompanyUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@Tag(name = "挂账单位用餐人服务")
@HttpExchange("/credit/companyUser")
public interface ICreditCompanyUserService {

    //按挂账 id 查询关联用餐人列表
    @GetExchange("/list/{creditId}")
    List<CreditCompanyUser> getInfoList(@PathVariable("creditId") String creditId);
}
