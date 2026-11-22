package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.exception.BussinessException;
import com.restkeeper.store.entity.Table;
import com.restkeeper.store.entity.TableLog;
import com.restkeeper.store.mapper.TableLogMapper;
import com.restkeeper.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 开台记录本地服务
 */
@Service("tableLogService")
public class TableLogServiceImpl extends ServiceImpl<TableLogMapper, TableLog> {

    @Autowired
    private TableServiceImpl tableService;

    /**
     * 开桌：校验桌台状态与人数，更新桌台并写入开台记录
     */
    @Transactional
    public boolean openTable(TableLog tableLog) {
        Table table = tableService.getById(tableLog.getTableId());

        if (SystemCode.TABLE_STATUS_FREE != table.getStatus()) {
            throw new BussinessException("非空闲桌台，不能开桌");
        }
        if (tableLog.getUserNumbers() > table.getTableSeatNumber()) {
            throw new BussinessException("超过桌台的限定人数，不能开桌");
        }

        table.setStatus(SystemCode.TABLE_STATUS_OPENED);
        tableService.updateById(table);

        tableLog.setUserId(TenantContext.getLoginUserName());
        tableLog.setCreateTime(LocalDateTime.now());
        tableLog.setTableStatus(SystemCode.TABLE_STATUS_OPENED);
        return this.save(tableLog);
    }

    /**
     * 查询桌台最近一条开台记录
     */
    public TableLog getOpenTableLog(String tableId) {
        QueryWrapper<TableLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TableLog::getTableId, tableId).orderByDesc(TableLog::getCreateTime);
        return this.list(queryWrapper).get(0);
    }
}
