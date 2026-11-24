package com.restkeeper.operator.controller;

import com.restkeeper.http.PageResult;
import com.restkeeper.operator.entity.EnterpriseAccount;
import com.restkeeper.operator.service.EnterpriseAccountServiceImpl;
import com.restkeeper.operator.service.IEnterpriseAccountService;
import com.restkeeper.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 * 企业账号 HTTP 端点：契约映射由 {@link IEnterpriseAccountService} 上的 @HttpExchange 提供
 */
@RestController
public class EnterpriseAccountController implements IEnterpriseAccountService {

    @Autowired
    private EnterpriseAccountServiceImpl enterpriseAccountService;

    @Override
    public PageResult<EnterpriseAccount> queryPageByName(int pageNum, int pageSize, String enterpriseName, Integer status) {
        return enterpriseAccountService.queryPageByName(pageNum, pageSize, enterpriseName, status);
    }

    @Override
    public PageResult<EnterpriseAccount> queryRemovedPage(int pageNum, int pageSize, String enterpriseName) {
        return enterpriseAccountService.queryRemovedPage(pageNum, pageSize, enterpriseName);
    }

    @Override
    public boolean add(EnterpriseAccount enterpriseAccount, Integer validityDay) {
        return enterpriseAccountService.add(enterpriseAccount, validityDay);
    }

    @Override
    public EnterpriseAccount getById(String id) {
        return enterpriseAccountService.getById(id);
    }

    @Override
    public boolean updateById(EnterpriseAccount enterpriseAccount, Integer validityDay) {
        return enterpriseAccountService.updateById(enterpriseAccount, validityDay);
    }

    @Override
    public boolean removeById(String id) {
        return enterpriseAccountService.removeById(id);
    }

    @Override
    public boolean recovery(String id) {
        return enterpriseAccountService.recovery(id);
    }

    @Override
    public boolean forbidden(String id) {
        return enterpriseAccountService.forbidden(id);
    }

    @Override
    public boolean restPwd(String id, String password) {
        return enterpriseAccountService.restPwd(id, password);
    }

    @Override
    public Result login(String shopId, String phone, String loginPass) {
        return enterpriseAccountService.login(shopId, phone, loginPass);
    }
}
