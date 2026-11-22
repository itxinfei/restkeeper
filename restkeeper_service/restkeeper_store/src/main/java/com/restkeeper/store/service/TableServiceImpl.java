package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.restkeeper.exception.BussinessException;
import com.restkeeper.http.PageResult;
import com.restkeeper.store.entity.Table;
import com.restkeeper.store.mapper.TableMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 桌台本地服务
 */
@Service("tableService")
public class TableServiceImpl extends ServiceImpl<TableMapper, Table> {

    /**
     * 新增桌台（名称防重）
     */
    @Override
    @Transactional
    public boolean save(Table table) {
        checkNameExist(table.getTableName());
        return super.save(table);
    }

    /**
     * 按区域分页查询桌台
     */
    public PageResult<Table> queryPageByAreaId(String areaId, int pageNum, int pageSize) {
        Page<Table> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Table> queryWrapper = new QueryWrapper<>();
        if (!"all".equals(areaId)) {
            queryWrapper.lambda().eq(Table::getAreaId, areaId);
        }
        queryWrapper.lambda().orderByDesc(Table::getLastUpdateTime);
        return PageResult.of(this.page(page, queryWrapper));
    }

    /**
     * 按区域与状态统计桌台数量
     */
    public Integer countTableByStatus(String areaId, Integer status) {
        QueryWrapper<Table> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Table::getAreaId, areaId).eq(Table::getStatus, status);
        return Math.toIntExact(this.count(queryWrapper));
    }

    /**
     * 桌台名称防重
     */
    private void checkNameExist(String tableName) {
        QueryWrapper<Table> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().select(Table::getTableId).eq(Table::getTableName, tableName);
        long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BussinessException("该桌台已存在");
        }
    }
}
