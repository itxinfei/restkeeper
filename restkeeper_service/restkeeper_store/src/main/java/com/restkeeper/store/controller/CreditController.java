package com.restkeeper.store.controller;

import com.restkeeper.http.PageResult;
import com.restkeeper.store.dto.CreditWithCompanyUsers;
import com.restkeeper.store.entity.Credit;
import com.restkeeper.store.service.CreditServiceImpl;
import com.restkeeper.store.service.ICreditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 * 挂账主体 HTTP 端点：契约映射由 {@link ICreditService} 上的 @HttpExchange 提供
 */
@RestController
public class CreditController implements ICreditService {

    @Autowired
    private CreditServiceImpl creditService;

    @Override
    public boolean add(CreditWithCompanyUsers request) {
        return creditService.add(request.getCredit(), request.getUsers());
    }

    @Override
    public PageResult<Credit> queryPage(int pageNum, int pageSize, String userName) {
        return creditService.queryPage(pageNum, pageSize, userName);
    }

    @Override
    public Credit queryById(String id) {
        return creditService.queryById(id);
    }

    @Override
    public boolean updateInfo(CreditWithCompanyUsers request) {
        return creditService.updateInfo(request.getCredit(), request.getUsers());
    }

    @Override
    public Credit getById(String id) {
        return creditService.getById(id);
    }

    @Override
    public boolean saveOrUpdate(Credit credit) {
        return creditService.saveOrUpdate(credit);
    }
}
