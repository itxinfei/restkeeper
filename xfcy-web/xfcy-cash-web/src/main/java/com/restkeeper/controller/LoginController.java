package com.restkeeper.controller;

import com.restkeeper.store.service.IStaffService;
import com.restkeeper.utils.Result;
import com.restkeeper.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 收银端登录（阶段 4：Dubbo @Reference → HTTP Interface 契约代理）。
 */
@Tag(name = "收银端登录")
@RestController
public class LoginController {

    private final IStaffService staffService;

    public LoginController(IStaffService staffService) {
        this.staffService = staffService;
    }

    @Operation(summary = "员工登录")
    @PostMapping("/staff/login")
    public Result login(@RequestParam("shopId") String shopId,
                        @RequestParam("loginName") String loginName,
                        @RequestParam("password") String password) {
        return staffService.login(shopId, loginName, password);
    }
}
