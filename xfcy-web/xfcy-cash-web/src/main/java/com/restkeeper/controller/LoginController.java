package com.restkeeper.controller;

import com.restkeeper.store.dto.StaffLoginDTO;
import com.restkeeper.store.service.IStaffService;
import com.restkeeper.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    public Result login(@RequestBody StaffLoginDTO dto) {
        // body 传参：规避 Boot4/Tomcat11 query 中文按 ISO-8859-1 解码导致的 Character decoding failed
        return staffService.login(dto);
    }
}
