package com.restkeeper.operator.controller;

import com.restkeeper.http.PageResult;
import com.restkeeper.operator.entity.OperatorUser;
import com.restkeeper.operator.service.IOperatorUserService;
import com.restkeeper.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 运营人员接口（阶段 4：Dubbo @Reference → HTTP Interface 契约代理）。
 * 端点路径对齐 provider 契约 {@link IOperatorUserService}。
 */
@Tag(name = "运营人员")
@RestController
@Slf4j
public class UserController {

    @Value("${server.port}")
    private String port;

    @Value("${key:''}")
    private String key;

    @Value("${commonKey:''}")
    private String commonKey;

    @Value("${shareInfo:''}")
    private String shareInfo;

    private final IOperatorUserService operatorUserService;

    public UserController(IOperatorUserService operatorUserService) {
        this.operatorUserService = operatorUserService;
    }

    @GetMapping("/echo/{message}")
    public String echo(@PathVariable("message") String message) {
        return "Hello Nacos Discovery " + message + ", i am from port " + port;
    }

    @GetMapping("/config")
    public String config() {
        return "Hello Nacos Config get " + key + "      commonValue:" + commonKey;
    }

    @GetMapping("/demo")
    public String demo() {
        return "shareInfo:  " + shareInfo;
    }

    @Operation(summary = "运营人员分页")
    @GetMapping("/operator/user/page/{pageNum}/{pageSize}")
    public PageResult<OperatorUser> queryPageByName(@PathVariable("pageNum") int pageNum,
                                                    @PathVariable("pageSize") int pageSize,
                                                    @RequestParam(value = "name", required = false) String name) {
        return operatorUserService.queryPageByName(pageNum, pageSize, name);
    }

    @Operation(summary = "运营人员登录")
    @PostMapping("/operator/user/login")
    public Result login(@RequestParam("loginName") String loginName,
                        @RequestParam("loginPass") String loginPass) {
        return operatorUserService.login(loginName, loginPass);
    }
}
