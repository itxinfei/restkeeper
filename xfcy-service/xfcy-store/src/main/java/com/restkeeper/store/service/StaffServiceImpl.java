package com.restkeeper.store.service;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.http.PageResult;
import com.restkeeper.sms.SmsObject;
import com.restkeeper.store.entity.Staff;
import com.restkeeper.store.mapper.StaffMapper;
import com.restkeeper.utils.PasswordUtil;
import com.restkeeper.utils.RandomCodeUtil;
import com.restkeeper.utils.Result;
import com.restkeeper.utils.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 门店员工本地服务
 */
@Slf4j
@Service
public class StaffServiceImpl extends ServiceImpl<StaffMapper, Staff> {

    @Value("${sms.operator.signName:}")
    private String signName;

    @Value("${sms.operator.templateCode:}")
    private String templateCode;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 员工分页：姓名、手机号均为可选模糊条件；shop_id/store_id 由双租户插件自动追加
     */
    public PageResult<Staff> queryPage(int pageNum, int pageSize, String staffName, String phone) {
        Page<Staff> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Staff> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(staffName)) {
            queryWrapper.lambda().like(Staff::getStaffName, staffName);
        }
        if (StringUtils.hasText(phone)) {
            queryWrapper.lambda().like(Staff::getPhone, phone);
        }
        return PageResult.of(this.page(page, queryWrapper));
    }

    /**
     * 停用员工（status 置 0）；重新启用走通用 updateById
     */
    @Transactional
    public boolean pause(String id) {
        Staff staff = this.getById(id);
        if (staff == null) {
            return false;
        }
        staff.setStatus(SystemCode.FORBIDDEN);
        return this.updateById(staff);
    }

    /**
     * 新增员工：未指定密码时生成 8 位随机初始密码，BCrypt 加密入库
     */
    @Transactional
    public String addStaff(Staff staff) {
        String rawPassword = StringUtils.hasText(staff.getPassword())
                ? staff.getPassword()
                : RandomCodeUtil.randomNumeric(8);
        staff.setPassword(rawPassword);
        return super.save(encodePassword(staff)) ? rawPassword : null;
    }

    /**
     * 保存员工：统一 BCrypt 加密（原裸 save 直通 MP 不加密，明文入库风险）
     */
    @Override
    @Transactional
    public boolean save(Staff staff) {
        return super.save(encodePassword(staff));
    }

    private Staff encodePassword(Staff staff) {
        String password = staff.getPassword();
        if (!StringUtils.hasText(password)) {
            password = RandomCodeUtil.randomNumeric(8);
        }
        staff.setPassword(PasswordUtil.encode(password));
        return staff;
    }

    /**
     * 员工登录
     */
    public Result login(String shopId, String loginName, String password) {
        Result result = new Result();

        //自定义登录 SQL 已豁免租户插件并显式带 shop_id
        Staff staff = this.getBaseMapper().login(shopId, loginName);
        if (staff == null) {
            result.setStatus(ResultCode.error);
            result.setDesc("员工信息不存在");
            return result;
        }

        if (!PasswordUtil.matches(password, staff.getPassword())) {
            result.setStatus(ResultCode.error);
            result.setDesc("密码不正确");
            return result;
        }

        // Sa-Token 签发（阶段 5：替换 JWT）
        StpUtil.login(staff.getStaffId());
        StpUtil.getSession().set("shopId", shopId);
        StpUtil.getSession().set("storeId", staff.getStoreId());
        StpUtil.getSession().set("loginUserId", staff.getStaffId());
        StpUtil.getSession().set("loginUserName", staff.getStaffName());
        StpUtil.getSession().set("userType", SystemCode.USER_TYPE_STAFF);
        String token = StpUtil.getTokenValue();

        //登录响应脱敏：BCrypt 密码哈希不下发前端
        staff.setPassword(null);
        result.setStatus(ResultCode.success);
        result.setDesc("ok");
        result.setData(staff);
        result.setToken(token);
        return result;
    }

    /**
     * 发送账号下发短信（预留，开通员工短信通知时在 addStaff 事务提交后调用）
     */
    private void sendMessage(String phone, String shopId, String pwd) {
        SmsObject smsObject = new SmsObject();
        smsObject.setPhoneNumber(phone);
        smsObject.setSignName(signName);
        smsObject.setTemplateCode(templateCode);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("shopId", shopId);
        jsonObject.put("password", pwd);
        smsObject.setTemplateJsonParam(jsonObject.toJSONString());
        rabbitTemplate.convertAndSend(SystemCode.SMS_ACCOUNT_QUEUE, JSON.toJSONString(smsObject));
    }
}
