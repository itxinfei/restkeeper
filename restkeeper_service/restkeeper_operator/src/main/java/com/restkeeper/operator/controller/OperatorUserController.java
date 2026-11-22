package com.restkeeper.operator.controller;

import com.restkeeper.http.PageResult;
import com.restkeeper.operator.entity.OperatorUser;
import com.restkeeper.operator.service.IOperatorUserService;
import com.restkeeper.operator.service.OperatorUserServiceImpl;
import com.restkeeper.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 * 运营人员 HTTP 端点：契约映射由 {@link IOperatorUserService} 上的 @HttpExchange 提供
 */
@RestController
public class OperatorUserController implements IOperatorUserService {

    @Autowired
    private OperatorUserServiceImpl operatorUserService;

    @Override
    public PageResult<OperatorUser> queryPageByName(int pageNum, int pageSize, String name) {
        return operatorUserService.queryPageByName(pageNum, pageSize, name);
    }

    @Override
    public Result login(String loginName, String loginPass) {
        return operatorUserService.login(loginName, loginPass);
    }
}
