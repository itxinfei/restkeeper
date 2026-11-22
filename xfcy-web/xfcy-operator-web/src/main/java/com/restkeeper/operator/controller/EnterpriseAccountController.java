package com.restkeeper.operator.controller;

import com.restkeeper.http.PageResult;
import com.restkeeper.operator.entity.EnterpriseAccount;
import com.restkeeper.operator.service.IEnterpriseAccountService;
import com.restkeeper.operator.vo.AddEnterpriseAccountVO;
import com.restkeeper.operator.vo.UpdateEnterpriseAccountVO;
import com.restkeeper.utils.AccountStatus;
import com.restkeeper.utils.Result;
import com.restkeeper.utils.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientResponseException;

/**
 * 企业账号接口（阶段 4：Dubbo @Reference → HTTP Interface 契约代理）。
 * 端点路径对齐 provider 契约 {@link IEnterpriseAccountService}。
 * K9：7 天试用 / 到期重算（基准 now）/ 正式禁改试用已下沉 provider。
 */
@Tag(name = "企业账号")
@RestController
public class EnterpriseAccountController {

    private final IEnterpriseAccountService enterpriseAccountService;

    public EnterpriseAccountController(IEnterpriseAccountService enterpriseAccountService) {
        this.enterpriseAccountService = enterpriseAccountService;
    }

    @Operation(summary = "企业账号分页")
    @GetMapping("/enterprise/account/page/{pageNum}/{pageSize}")
    public PageResult<EnterpriseAccount> queryPageByName(@PathVariable("pageNum") int pageNum,
                                                         @PathVariable("pageSize") int pageSize,
                                                         @RequestParam(value = "enterpriseName", required = false) String enterpriseName) {
        return enterpriseAccountService.queryPageByName(pageNum, pageSize, enterpriseName);
    }

    @Operation(summary = "回收站分页")
    @GetMapping("/enterprise/account/removed/page/{pageNum}/{pageSize}")
    public PageResult<EnterpriseAccount> queryRemovedPage(@PathVariable("pageNum") int pageNum,
                                                          @PathVariable("pageSize") int pageSize,
                                                          @RequestParam(value = "enterpriseName", required = false) String enterpriseName) {
        return enterpriseAccountService.queryRemovedPage(pageNum, pageSize, enterpriseName);
    }

    @Operation(summary = "开通企业")
    @PostMapping("/enterprise/account/add")
    public boolean add(@RequestBody AddEnterpriseAccountVO addEnterpriseAccountVO) {
        EnterpriseAccount enterpriseAccount = new EnterpriseAccount();
        BeanUtils.copyProperties(addEnterpriseAccountVO, enterpriseAccount);
        // K9：申请时间、7 天试用或正式 validityDay 的到期时间由 provider 统一计算
        return enterpriseAccountService.add(enterpriseAccount, addEnterpriseAccountVO.getValidityDay());
    }

    @Operation(summary = "按主键查询")
    @GetMapping("/enterprise/account/{id}")
    public EnterpriseAccount getById(@PathVariable("id") String id) {
        return enterpriseAccountService.getById(id);
    }

    @Operation(summary = "按主键更新")
    @PutMapping("/enterprise/account")
    public Result update(@RequestBody UpdateEnterpriseAccountVO updateEnterpriseAccountVO) {
        Result result = new Result();

        EnterpriseAccount enterpriseAccount = new EnterpriseAccount();
        BeanUtils.copyProperties(updateEnterpriseAccountVO, enterpriseAccount);

        try {
            // K9：正式禁改试用、到期以 now 重算均由 provider 校验/计算
            boolean flag = enterpriseAccountService.updateById(enterpriseAccount, updateEnterpriseAccountVO.getValidityDay());
            result.setStatus(flag ? ResultCode.success : ResultCode.error);
            result.setDesc(flag ? "修改成功" : "修改失败");
        } catch (RestClientResponseException e) {
            result.setStatus(ResultCode.error);
            result.setDesc("修改失败，请检查帐号状态");
        }
        return result;
    }

    @Operation(summary = "逻辑删除")
    @DeleteMapping("/enterprise/account/{id}")
    public boolean removeById(@PathVariable("id") String id) {
        return enterpriseAccountService.removeById(id);
    }

    @Operation(summary = "账号还原")
    @PutMapping("/enterprise/account/recovery/{id}")
    public boolean recovery(@PathVariable("id") String id) {
        return enterpriseAccountService.recovery(id);
    }

    @Operation(summary = "停用账号")
    @PutMapping("/enterprise/account/forbidden/{id}")
    public boolean forbidden(@PathVariable("id") String id) {
        EnterpriseAccount enterpriseAccount = enterpriseAccountService.getById(id);
        enterpriseAccount.setStatus(AccountStatus.Forbidden.getStatus());
        // 停用不涉及到期重算，validityDay 传 null
        return enterpriseAccountService.updateById(enterpriseAccount, null);
    }

    @Operation(summary = "重置密码")
    @PutMapping("/enterprise/account/resetPwd")
    public boolean restPwd(@RequestParam("id") String id,
                           @RequestParam("password") String password) {
        return enterpriseAccountService.restPwd(id, password);
    }

    @Operation(summary = "企业账号登录")
    @PostMapping("/enterprise/account/login")
    public Result login(@RequestParam("shopId") String shopId,
                        @RequestParam("phone") String phone,
                        @RequestParam("loginPass") String loginPass) {
        return enterpriseAccountService.login(shopId, phone, loginPass);
    }
}
