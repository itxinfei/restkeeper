package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.restkeeper.store.entity.CreditCompanyUser;
import com.restkeeper.store.mapper.CreditCompanyUserMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 挂账单位用餐人本地服务
 */
@Service("creditCompanyUserService")
public class CreditCompanyUserServiceImpl extends ServiceImpl<CreditCompanyUserMapper, CreditCompanyUser> {

    /**
     * 按挂账 id 查询关联用餐人
     */
    public List<CreditCompanyUser> getInfoList(String creditId) {
        QueryWrapper<CreditCompanyUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(CreditCompanyUser::getCreditId, creditId);
        return this.list(queryWrapper);
    }
}
