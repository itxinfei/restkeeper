package com.restkeeper.operator.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.restkeeper.http.PageResult;
import com.restkeeper.operator.entity.OperatorUser;
import com.restkeeper.operator.mapper.OperatorUserMapper;
import com.restkeeper.utils.PasswordUtil;
import com.restkeeper.utils.Result;
import com.restkeeper.utils.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 运营人员本地业务服务（去 Dubbo 后不再实现 api 契约；HTTP 入口在 Controller 层）
 */
@Slf4j
@Service
@RefreshScope
public class OperatorUserServiceImpl extends ServiceImpl<OperatorUserMapper, OperatorUser> {

    //按名称分页查询
    public PageResult<OperatorUser> queryPageByName(int pageNum, int pageSize, String name) {
        Page<OperatorUser> page = new Page<>(pageNum, pageSize);
        QueryWrapper<OperatorUser> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(name)) {
            queryWrapper.like("loginname", name);
        }
        return PageResult.of(this.page(page, queryWrapper));
    }

    //登录
    public Result login(String loginName, String loginPass) {
        Result result = new Result();

        if (!StringUtils.hasText(loginName)) {
            result.setStatus(ResultCode.error);
            result.setDesc("用户名不存在");
            return result;
        }
        if (!StringUtils.hasText(loginPass)) {
            result.setStatus(ResultCode.error);
            result.setDesc("密码不存在");
            return result;
        }

        QueryWrapper<OperatorUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("loginname", loginName);
        OperatorUser operatorUser = this.getOne(queryWrapper);
        if (operatorUser == null) {
            result.setStatus(ResultCode.error);
            result.setDesc("用户不存在");
            return result;
        }

        //BCrypt 密码校验
        if (!PasswordUtil.matches(loginPass, operatorUser.getLoginpass())) {
            result.setStatus(ResultCode.error);
            result.setDesc("密码不正确");
            return result;
        }

        // Sa-Token 签发（阶段 5：替换 JWT）
        StpUtil.login(operatorUser.getUid());
        StpUtil.getSession().set("loginUserId", operatorUser.getUid());
        StpUtil.getSession().set("loginUserName", operatorUser.getLoginname());
        String token = StpUtil.getTokenValue();

        result.setStatus(ResultCode.success);
        result.setDesc("ok");
        result.setData(operatorUser);
        result.setToken(token);
        return result;
    }
}
