package com.restkeeper.controller;

import com.restkeeper.constants.SystemCode;
import com.restkeeper.operator.service.IEnterpriseAccountService;
import com.restkeeper.shop.service.IStoreManagerService;
import com.restkeeper.utils.Result;
import com.restkeeper.utils.ResultCode;
import com.restkeeper.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管家端登录入口：按用户类型分流到企业账号 / 门店经理 provider 端点。
 * <p>阶段 4：Dubbo @Reference → HTTP Interface 契约代理；
 * K13：旧 BFF VO 字段为 password，契约参数名为 loginPass，此处统一映射。
 */
@RestController
@Slf4j
@Tag(name = "登录接口")
public class LoginController {

    private final IEnterpriseAccountService enterpriseAccountService;
    private final IStoreManagerService storeManagerService;

    public LoginController(IEnterpriseAccountService enterpriseAccountService,
                           IStoreManagerService storeManagerService) {
        this.enterpriseAccountService = enterpriseAccountService;
        this.storeManagerService = storeManagerService;
    }

    @Operation(summary = "登录入口（按用户类型分流）")
    @PostMapping("/login")
    public Result login(@RequestBody LoginVO loginVO) {
        if (SystemCode.USER_TYPE_SHOP.equals(loginVO.getType())) {
            // 集团用户
            return enterpriseAccountService.login(loginVO.getShopId(), loginVO.getPhone(), loginVO.getPassword());
        }
        if (SystemCode.USER_TYPE_STORE_MANAGER.equals(loginVO.getType())) {
            // 店长
            return storeManagerService.login(loginVO.getShopId(), loginVO.getPhone(), loginVO.getPassword());
        }

        Result result = new Result();
        result.setStatus(ResultCode.error);
        result.setDesc("不支持当前用户类型登录");
        return result;
    }

    /**
     * 门店经理登录直连入口：与 {@code IStoreManagerService} 契约
     * （{@code POST /store/manager/login}，query: shopId/phone/loginPass）一致，
     * 供管家端前端直接调用（网关 excludedUrls 已放行该路径）。
     */
    @Operation(summary = "门店经理登录（直连契约入口）")
    @PostMapping("/store/manager/login")
    public Result loginStoreManager(@RequestParam("shopId") String shopId,
                                    @RequestParam("phone") String phone,
                                    @RequestParam("loginPass") String loginPass) {
        return storeManagerService.login(shopId, phone, loginPass);
    }
}
