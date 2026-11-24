package com.restkeeper.operator.service;

import com.restkeeper.http.PageResult;
import com.restkeeper.operator.entity.EnterpriseAccount;
import com.restkeeper.utils.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

@Tag(name = "企业账号服务")
@HttpExchange("/enterprise/account")
public interface IEnterpriseAccountService {

    //分页查询（按企业名称模糊 + 可选状态精确，仅未删除账号）
    @GetExchange("/page/{pageNum}/{pageSize}")
    PageResult<EnterpriseAccount> queryPageByName(@PathVariable("pageNum") int pageNum,
                                                  @PathVariable("pageSize") int pageSize,
                                                  @RequestParam(value = "enterpriseName", required = false) String enterpriseName,
                                                  @RequestParam(value = "status", required = false) Integer status);

    //回收站分页（仅已逻辑删除账号，按企业名称模糊）
    @GetExchange("/removed/page/{pageNum}/{pageSize}")
    PageResult<EnterpriseAccount> queryRemovedPage(@PathVariable("pageNum") int pageNum,
                                                   @PathVariable("pageSize") int pageSize,
                                                   @RequestParam(value = "enterpriseName", required = false) String enterpriseName);

    //新增企业账号（validityDay：正式期有效天数；试用固定 7 天，由 provider 计算到期时间）
    @PostExchange("/add")
    boolean add(@RequestBody EnterpriseAccount enterpriseAccount,
                @RequestParam(value = "validityDay", required = false) Integer validityDay);

    //按主键查询
    @GetExchange("/{id}")
    EnterpriseAccount getById(@PathVariable("id") String id);

    //按主键更新（validityDay：正式期有效天数；试用转正式/续费以 now 为基准重算到期，正式禁改试用）
    @PutExchange
    boolean updateById(@RequestBody EnterpriseAccount enterpriseAccount,
                       @RequestParam(value = "validityDay", required = false) Integer validityDay);

    //按主键删除（逻辑删除）
    @DeleteExchange("/{id}")
    boolean removeById(@PathVariable("id") String id);

    //账号还原（回收站恢复）
    @PutExchange("/recovery/{id}")
    boolean recovery(@PathVariable("id") String id);

    //停用账号（置为 -1；启用走编辑接口选择试用/正式并重算到期时间，不设独立启用端点）
    @PutExchange("/forbidden/{id}")
    boolean forbidden(@PathVariable("id") String id);

    //重置密码
    @PutExchange("/resetPwd")
    boolean restPwd(@RequestParam("id") String id,
                    @RequestParam("password") String password);

    //登录
    @PostExchange("/login")
    Result login(@RequestParam("shopId") String shopId,
                 @RequestParam("phone") String phone,
                 @RequestParam("loginPass") String loginPass);
}
