package com.restkeeper.shop.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.http.PageResult;
import com.restkeeper.shop.config.RabbitMQConfig;
import com.restkeeper.shop.entity.Store;
import com.restkeeper.shop.entity.StoreManager;
import com.restkeeper.shop.mapper.StoreManagerMapper;
import com.restkeeper.sms.SmsObject;
import com.restkeeper.tenant.TenantContext;
import com.restkeeper.utils.JWTUtil;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 门店管理员本地服务：不再实现 api 契约接口，HTTP 端点由 StoreManagerController 薄转发。
 */
@Service
@Slf4j
public class StoreManagerServiceImpl extends ServiceImpl<StoreManagerMapper, StoreManager> {

    @Autowired
    private StoreServiceImpl storeService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${sms.operator.signName:}")
    private String signName;

    @Value("${sms.operator.templateCode:}")
    private String templateCode;

    @Value("${gateway.secret:restkeeper}")
    private String secret;

    /**
     * 按手机号或姓名分页查询
     */
    public PageResult<StoreManager> queryPageByCriteria(int pageNo, int pageSize, String criteria) {
        Page<StoreManager> page = new Page<>(pageNo, pageSize);
        QueryWrapper<StoreManager> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(criteria)) {
            queryWrapper.lambda()
                    .eq(StoreManager::getStoreManagerPhone, criteria)
                    .or()
                    .eq(StoreManager::getStoreManagerName, criteria);
        }
        return PageResult.of(this.page(page, queryWrapper));
    }

    /**
     * 新增店长并重建门店归属，初始密码经短信下发
     */
    @Transactional
    public boolean addStoreManager(String name, String phone, List<String> storeIds) {
        //新增店长基础信息，8 位随机初始密码 BCrypt 加密入库
        StoreManager storeManager = new StoreManager();
        storeManager.setStoreManagerName(name);
        storeManager.setStoreManagerPhone(phone);
        String pwd = RandomCodeUtil.randomNumeric(8);
        storeManager.setPassword(PasswordUtil.encode(pwd));
        this.save(storeManager);

        //一个门店只能归属一个门店管理员
        String storeManagerId = storeManager.getStoreManagerId();
        UpdateWrapper<Store> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .in(Store::getStoreId, storeIds)
                .set(Store::getStoreManagerId, storeManagerId);
        boolean flag = storeService.update(updateWrapper);

        if (flag) {
            sendMessage(phone, storeManager.getShopId(), pwd);
        }
        return flag;
    }

    /**
     * 修改店长并重建门店归属
     */
    @Transactional
    public boolean updateStoreManager(String storeManagerId, String name, String phone, List<String> storeIds) {
        StoreManager storeManager = this.getById(storeManagerId);

        if (StringUtils.hasText(name)) {
            storeManager.setStoreManagerName(name);
        }
        if (StringUtils.hasText(phone)) {
            storeManager.setStoreManagerPhone(phone);
        }
        this.updateById(storeManager);

        //先清除原归属关系
        UpdateWrapper<Store> clearWrapper = new UpdateWrapper<>();
        clearWrapper.lambda()
                .set(Store::getStoreManagerId, null)
                .eq(Store::getStoreManagerId, storeManagerId);
        storeService.update(clearWrapper);

        //再建立最新归属关系
        UpdateWrapper<Store> bindWrapper = new UpdateWrapper<>();
        bindWrapper.lambda()
                .in(Store::getStoreId, storeIds)
                .set(Store::getStoreManagerId, storeManagerId);
        return storeService.update(bindWrapper);
    }

    /**
     * 逻辑删除店长并解除门店归属
     */
    @Transactional
    public boolean deleteStoreManager(String storeManagerId) {
        this.removeById(storeManagerId);

        UpdateWrapper<Store> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .set(Store::getStoreManagerId, null)
                .eq(Store::getStoreManagerId, storeManagerId);
        return storeService.update(updateWrapper);
    }

    /**
     * 停用店长
     */
    public boolean pauseStoreManager(String storeManagerId) {
        UpdateWrapper<StoreManager> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .set(StoreManager::getStatus, SystemCode.FORBIDDEN)
                .eq(StoreManager::getStoreManagerId, storeManagerId);
        return this.update(updateWrapper);
    }

    /**
     * 门店管理员登录
     */
    public Result login(String shopId, String phone, String loginPass) {
        Result result = new Result();

        if (!StringUtils.hasText(shopId)) {
            result.setStatus(ResultCode.error);
            result.setDesc("商铺号为空");
            return result;
        }
        if (!StringUtils.hasText(phone)) {
            result.setStatus(ResultCode.error);
            result.setDesc("手机号为空");
            return result;
        }
        if (!StringUtils.hasText(loginPass)) {
            result.setStatus(ResultCode.error);
            result.setDesc("密码为空");
            return result;
        }

        //登录请求本身不带租户头，把表单中的 shopId 放入上下文，供多租户插件拼接查询条件
        TenantContext.addAttachment("shopId", shopId);

        QueryWrapper<StoreManager> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(StoreManager::getShopId, shopId)
                .eq(StoreManager::getStoreManagerPhone, phone);
        StoreManager storeManager = this.getOne(queryWrapper);
        if (storeManager == null) {
            result.setStatus(ResultCode.error);
            result.setDesc("门店管理员不存在");
            return result;
        }

        List<Store> stores = storeManager.getStores();
        if (stores == null || stores.isEmpty()) {
            result.setStatus(ResultCode.error);
            result.setDesc("没有相关联的门店信息");
            return result;
        }
        Store store = stores.get(0);

        if (!PasswordUtil.matches(loginPass, storeManager.getPassword())) {
            result.setStatus(ResultCode.error);
            result.setDesc("密码错误");
            return result;
        }

        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("shopId", shopId);
        tokenMap.put("storeId", store.getStoreId());
        tokenMap.put("loginUserId", storeManager.getStoreManagerId());
        tokenMap.put("loginUserName", storeManager.getStoreManagerName());
        tokenMap.put("userType", SystemCode.USER_TYPE_STORE_MANAGER);

        try {
            String tokenInfo = JWTUtil.createJWTByObj(tokenMap, secret);
            result.setStatus(ResultCode.success);
            result.setDesc("ok");
            result.setData(storeManager);
            result.setToken(tokenInfo);
        } catch (Exception e) {
            log.error("门店管理员登录令牌生成失败, phone={}", phone, e);
            result.setStatus(ResultCode.error);
            result.setDesc("令牌生成失败");
        }
        return result;
    }

    /**
     * 发送账号下发短信
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
        rabbitTemplate.convertAndSend(RabbitMQConfig.ACCOUNT_QUEUE, JSON.toJSONString(smsObject));
    }
}
