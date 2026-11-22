package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.restkeeper.http.PageResult;
import com.restkeeper.store.entity.CreditLogs;
import com.restkeeper.store.mapper.CreditLogMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 挂账流水本地服务
 */
@Service("creditLogService")
public class CreditLogServiceImpl extends ServiceImpl<CreditLogMapper, CreditLogs> {

    /**
     * 挂账流水分页（按最后更新时间倒序）
     */
    public PageResult<CreditLogs> queryPage(int pageNum, int pageSize, String creditId) {
        Page<CreditLogs> page = new Page<>(pageNum, pageSize);
        QueryWrapper<CreditLogs> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(CreditLogs::getCreditId, creditId).orderByDesc(CreditLogs::getLastUpdateTime);
        return PageResult.of(this.page(page, queryWrapper));
    }

    /**
     * 按挂账 id 与时间区间查询流水
     */
    public List<CreditLogs> list(String creditId, LocalDateTime start, LocalDateTime end) {
        QueryWrapper<CreditLogs> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(CreditLogs::getCreditId, creditId);
        if (start != null && end != null) {
            queryWrapper.lambda().between(CreditLogs::getLastUpdateTime, start, end);
        }
        queryWrapper.lambda().orderByDesc(CreditLogs::getLastUpdateTime);
        return this.list(queryWrapper);
    }
}
