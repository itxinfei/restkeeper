package com.restkeeper.operator.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.http.PageResult;
import com.restkeeper.operator.config.RabbitMQConfig;
import com.restkeeper.operator.entity.EnterpriseAccount;
import com.restkeeper.operator.mapper.EnterpriseAccountMapper;
import com.restkeeper.sms.SmsObject;
import com.restkeeper.utils.AccountStatus;
import com.restkeeper.utils.JWTUtil;
import com.restkeeper.utils.PasswordUtil;
import com.restkeeper.utils.Result;
import com.restkeeper.utils.ResultCode;
import lombok.extern.slf4j.Slf4j;
import com.restkeeper.utils.RandomCodeUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 企业账号本地业务服务（去 Dubbo 后不再实现 api 契约；HTTP 入口在 Controller 层）
 */
@Slf4j
@Service
@RefreshScope
public class EnterpriseAccountServiceImpl extends ServiceImpl<EnterpriseAccountMapper, EnterpriseAccount> {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${sms.operator.signName:}")
    private String signName;

    @Value("${sms.operator.templateCode:}")
    private String templateCode;

    @Value("${gateway.secret:restkeeper}")
    private String secret;

    //消息发送
    private void sendMessage(String phone, String shopId, String pwd) {
        SmsObject smsObject = new SmsObject();
        smsObject.setPhoneNumber(phone);
        smsObject.setSignName(signName);
        smsObject.setTemplateCode(templateCode);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("shopId", shopId);
        jsonObject.put("password", pwd);
        smsObject.setTemplateJsonParam(jsonObject.toJSONString());
        rabbitTemplate.convertAndSend(RabbitMQConfig.ACCOUNT_QUEUE, JSON.toJSONString(smsObject));
    }

    /**
     * 按企业名称模糊分页查询
     */
    public PageResult<EnterpriseAccount> queryPageByName(int pageNum, int pageSize, String enterpriseName) {
        Page<EnterpriseAccount> page = new Page<>(pageNum, pageSize);
        QueryWrapper<EnterpriseAccount> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(enterpriseName)) {
            queryWrapper.like("enterprise_name", enterpriseName);
        }
        return PageResult.of(this.page(page, queryWrapper));
    }

    /**
     * 回收站分页：只查 is_deleted=1 的账号。
     * MP 内置分页会被 @TableLogic 追加 is_deleted=0，故走 Mapper 手写 SQL。
     */
    public PageResult<EnterpriseAccount> queryRemovedPage(int pageNum, int pageSize, String enterpriseName) {
        Page<EnterpriseAccount> page = new Page<>(pageNum, pageSize);
        return PageResult.of(this.getBaseMapper().queryRemovedPage(page, enterpriseName));
    }

    /** 试用期天数（K9：7 天试用规则单点在 provider） */
    private static final int TRIAL_DAYS = 7;

    //增加帐号（K9：申请时间 / 到期时间 / 状态校验由 provider 统一计算）
    @Transactional
    public boolean add(EnterpriseAccount enterpriseAccount, Integer validityDay) {
        //帐号、密码特殊处理
        String shopId = getShopId();
        enterpriseAccount.setShopId(shopId);

        LocalDateTime now = LocalDateTime.now();
        enterpriseAccount.setApplicationTime(now);

        Integer status = enterpriseAccount.getStatus();
        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("帐号类型信息设置有误");
        }
        if (status == 0) {
            enterpriseAccount.setExpireTime(now.plusDays(TRIAL_DAYS));
        } else {
            if (validityDay == null || validityDay <= 0) {
                throw new IllegalArgumentException("正式帐号必须设置有效期");
            }
            enterpriseAccount.setExpireTime(now.plusDays(validityDay));
        }

        //生成 6 位随机初始密码，BCrypt 加密入库，明文仅通过短信下发
        String pwd = RandomCodeUtil.randomNumeric(6);
        enterpriseAccount.setPassword(PasswordUtil.encode(pwd));

        this.save(enterpriseAccount);
        sendMessage(enterpriseAccount.getPhone(), shopId, pwd);
        return true;
    }

    //按主键更新（K9：正式禁改试用；试用转正式/续费以 now 为基准重算到期）
    @Transactional
    public boolean updateById(EnterpriseAccount enterpriseAccount, Integer validityDay) {
        EnterpriseAccount existing = this.getById(enterpriseAccount.getEnterpriseId());
        if (existing == null) {
            throw new IllegalArgumentException("修改账户不存在");
        }
        Integer targetStatus = enterpriseAccount.getStatus();
        if (targetStatus != null) {
            // 正式期不能修改为试用期
            if (targetStatus == 0 && Objects.equals(existing.getStatus(), 1)) {
                throw new IllegalArgumentException("不能将正式帐号修改为试用帐号");
            }
            // 试用转正式 / 正式续费：均以当前时间为基准重算到期
            if (targetStatus == 1) {
                LocalDateTime now = LocalDateTime.now();
                enterpriseAccount.setApplicationTime(now);
                enterpriseAccount.setExpireTime(now.plusDays(validityDay == null ? 0 : validityDay));
            }
        }
        return this.updateById(enterpriseAccount);
    }

    //帐号还原
    @Transactional
    public boolean recovery(String id) {
        return this.getBaseMapper().recovery(id);
    }

    //停用账号（置为 -1；启用需在编辑接口重新选择试用/正式并重算到期时间）
    @Transactional
    public boolean forbidden(String id) {
        EnterpriseAccount enterpriseAccount = this.getById(id);
        if (enterpriseAccount == null) {
            return false;
        }
        enterpriseAccount.setStatus(AccountStatus.Forbidden.getStatus());
        return this.updateById(enterpriseAccount);
    }

    //重置密码
    @Transactional
    public boolean restPwd(String id, String password) {
        EnterpriseAccount enterpriseAccount = this.getById(id);
        if (enterpriseAccount == null) {
            return false;
        }

        String newPwd = StringUtils.hasText(password) ? password : RandomCodeUtil.randomNumeric(6);
        enterpriseAccount.setPassword(PasswordUtil.encode(newPwd));
        this.updateById(enterpriseAccount);

        sendMessage(enterpriseAccount.getPhone(), enterpriseAccount.getShopId(), newPwd);
        return true;
    }

    //登录
    public Result login(String shopId, String phone, String loginPass) {
        Result result = new Result();

        if (!StringUtils.hasText(shopId)) {
            result.setStatus(ResultCode.error);
            result.setDesc("商户号未输入");
            return result;
        }
        if (!StringUtils.hasText(phone)) {
            result.setStatus(ResultCode.error);
            result.setDesc("手机号未输入");
            return result;
        }
        if (!StringUtils.hasText(loginPass)) {
            result.setStatus(ResultCode.error);
            result.setDesc("密码未输入");
            return result;
        }

        QueryWrapper<EnterpriseAccount> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(EnterpriseAccount::getPhone, phone)
                .eq(EnterpriseAccount::getShopId, shopId)
                .notIn(EnterpriseAccount::getStatus, AccountStatus.Forbidden.getStatus());
        EnterpriseAccount enterpriseAccount = this.getOne(queryWrapper);
        if (enterpriseAccount == null) {
            result.setStatus(ResultCode.error);
            result.setDesc("帐号不存在");
            return result;
        }

        //BCrypt 密码校验
        if (!PasswordUtil.matches(loginPass, enterpriseAccount.getPassword())) {
            result.setStatus(ResultCode.error);
            result.setDesc("密码不正确");
            return result;
        }

        //令牌生成（阶段 5 切换 Sa-Token 后移除 JWTUtil）
        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("shopId", enterpriseAccount.getShopId());
        tokenInfo.put("loginName", enterpriseAccount.getEnterpriseName());
        tokenInfo.put("userType", SystemCode.USER_TYPE_SHOP);
        String token = JWTUtil.createJWTByObj(tokenInfo, secret);

        result.setStatus(ResultCode.success);
        result.setDesc("ok");
        result.setData(enterpriseAccount);
        result.setToken(token);
        return result;
    }

    //获取不重复的 shopId（8 位随机数字）
    private String getShopId() {
        String shopId = RandomCodeUtil.randomNumeric(8);
        QueryWrapper<EnterpriseAccount> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("shop_id", shopId);
        if (this.getOne(queryWrapper) != null) {
            return getShopId();
        }
        return shopId;
    }
}
