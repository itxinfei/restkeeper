package com.restkeeper.shop.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.http.PageResult;
import com.restkeeper.shop.dto.StoreDTO;
import com.restkeeper.shop.entity.Store;
import com.restkeeper.shop.mapper.StoreMapper;
import com.restkeeper.tenant.TenantContext;
import com.restkeeper.utils.BeanListUtils;
import com.restkeeper.utils.Result;
import com.restkeeper.utils.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 门店本地服务：不再实现 api 契约接口，HTTP 端点由 StoreController 薄转发。
 */
@Service("storeService")
@Slf4j
public class StoreServiceImpl extends ServiceImpl<StoreMapper, Store> {

    /**
     * 分页查询（按门店名称模糊）
     */
    public PageResult<Store> queryPageByName(int pageNo, int pageSize, String name) {
        Page<Store> page = new Page<>(pageNo, pageSize);
        QueryWrapper<Store> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(name)) {
            queryWrapper.lambda().like(Store::getStoreName, name);
        }
        return PageResult.of(this.page(page, queryWrapper));
    }

    /**
     * 查询所有有效省份
     */
    public List<String> getAllProvince() {
        return getBaseMapper().getAllProvince();
    }

    /**
     * 按省份查询营业中门店
     */
    public List<StoreDTO> getStoreByProvince(String province) {
        QueryWrapper<Store> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Store::getStatus, 1);
        if (StringUtils.hasText(province) && !"all".equals(province)) {
            queryWrapper.lambda().eq(Store::getProvince, province);
        }
        List<Store> list = this.list(queryWrapper);
        return toDtoList(list);
    }

    /**
     * 查询当前登录店长管理的营业中门店
     */
    public List<StoreDTO> getStoreListByManagerId() {
        QueryWrapper<Store> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Store::getStatus, 1)
                .eq(Store::getStoreManagerId, TenantContext.getLoginUserId());
        List<Store> list = this.list(queryWrapper);
        return toDtoList(list);
    }

    /**
     * 停用门店（置为停业 0）
     */
    public boolean disable(String id) {
        return changeStatus(id, SystemCode.FORBIDDEN);
    }

    /**
     * 启用门店（恢复营业 1）
     */
    public boolean enable(String id) {
        return changeStatus(id, SystemCode.ENABLED);
    }

    //变更门店营业状态：查不到门店返回 false
    private boolean changeStatus(String id, int status) {
        Store store = this.getById(id);
        if (store == null) {
            return false;
        }
        store.setStatus(status);
        return this.updateById(store);
    }

    /**
     * 切换当前操作门店，重新签发令牌
     */
    public Result switchStore(String storeId) {
        Result result = new Result();

        // Sa-Token 重新签发（阶段 5：替换 JWT），会话更新当前门店
        StpUtil.login(TenantContext.getLoginUserId());
        StpUtil.getSession().set("shopId", TenantContext.getShopId());
        StpUtil.getSession().set("storeId", storeId);
        StpUtil.getSession().set("loginUserId", TenantContext.getLoginUserId());
        StpUtil.getSession().set("loginUserName", TenantContext.getLoginUserName());
        StpUtil.getSession().set("userType", SystemCode.USER_TYPE_STORE_MANAGER);
        String token = StpUtil.getTokenValue();

        result.setStatus(ResultCode.success);
        result.setDesc("ok");
        result.setData(storeId);
        result.setToken(token);
        return result;
    }

    /**
     * 实体集合转 DTO 集合，转换失败返回空集合
     */
    private List<StoreDTO> toDtoList(List<Store> list) {
        try {
            return BeanListUtils.copy(list, StoreDTO.class);
        } catch (Exception e) {
            log.error("门店集合转换失败", e);
            return new ArrayList<>();
        }
    }
}
