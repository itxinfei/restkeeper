package com.restkeeper.controller;

import cn.dev33.satoken.stp.StpLogic;
import com.restkeeper.utils.Result;
import com.restkeeper.utils.ResultCode;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * C 端登录（阶段 6）。
 * <p>dev 打桩：未接入微信 code2session，用 code 派生 mock openid；会话写入 shopId/storeId 供下游透传租户。
 */
@RestController
public class LoginController {

    private final StpLogic microappStpLogic;

    public LoginController(StpLogic microappStpLogic) {
        this.microappStpLogic = microappStpLogic;
    }

    @PostMapping("/login")
    public Result login(@RequestBody Map<String, Object> body) {
        String code = (String) body.get("code");
        String shopId = (String) body.get("shopId");
        String storeId = (String) body.get("storeId");
        String openid = "mock-openid-" + (code == null ? "0" : Math.abs(code.hashCode()));

        microappStpLogic.login(openid);
        microappStpLogic.getSession().set("shopId", shopId);
        microappStpLogic.getSession().set("storeId", storeId);
        microappStpLogic.getSession().set("userType", "microapp");

        Result result = new Result();
        result.setStatus(ResultCode.success);
        result.setDesc("登录成功");
        result.setToken(microappStpLogic.getTokenValue());
        return result;
    }
}
