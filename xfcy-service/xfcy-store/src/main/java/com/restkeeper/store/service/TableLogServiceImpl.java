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
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

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
        if (tableLog == null || !StringUtils.hasText(tableLog.getTableId())) {
            throw new BussinessException("缺少桌台信息，不能开桌");
        }
        Table table = tableService.getById(tableLog.getTableId());
        if (table == null) {
            throw new BussinessException("不存在该桌台");
        }
        //状态为 NULL 时不能直接与 int 常量比较，否则拆箱 NPE
        if (table.getStatus() == null || SystemCode.TABLE_STATUS_FREE != table.getStatus()) {
            throw new BussinessException("非空闲桌台，不能开桌");
        }
        //人数缺省/座位数为空时同样要避免拆箱 NPE
        Integer userNumbers = tableLog.getUserNumbers();
        Integer seatNumber = table.getTableSeatNumber();
        if (userNumbers == null || userNumbers <= 0) {
            throw new BussinessException("请填写开台人数");
        }
        if (seatNumber != null && userNumbers > seatNumber) {
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
     * 查询桌台最近一条开台记录；无记录时返回 null（调用方按未开台处理）
     */
    public TableLog getOpenTableLog(String tableId) {
        QueryWrapper<TableLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TableLog::getTableId, tableId).orderByDesc(TableLog::getCreateTime);
        List<TableLog> list = this.list(queryWrapper);
        return list.isEmpty() ? null : list.get(0);
    }
}
