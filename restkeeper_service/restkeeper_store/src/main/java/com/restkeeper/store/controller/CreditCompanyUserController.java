package com.restkeeper.store.controller;

import com.restkeeper.store.entity.CreditCompanyUser;
import com.restkeeper.store.service.CreditCompanyUserServiceImpl;
import com.restkeeper.store.service.ICreditCompanyUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 挂账单位用餐人 HTTP 端点：契约映射由 {@link ICreditCompanyUserService} 上的 @HttpExchange 提供
 */
@RestController
public class CreditCompanyUserController implements ICreditCompanyUserService {

    @Autowired
    private CreditCompanyUserServiceImpl creditCompanyUserService;

    @Override
    public List<CreditCompanyUser> getInfoList(String creditId) {
        return creditCompanyUserService.getInfoList(creditId);
    }
}
