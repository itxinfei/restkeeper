package com.restkeeper.store.controller;

import com.restkeeper.store.entity.TableLog;
import com.restkeeper.store.service.ITableLogService;
import com.restkeeper.store.service.TableLogServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 * 开台记录 HTTP 端点：契约映射由 {@link ITableLogService} 上的 @HttpExchange 提供
 */
@RestController
public class TableLogController implements ITableLogService {

    @Autowired
    private TableLogServiceImpl tableLogService;

    @Override
    public boolean openTable(TableLog tableLog) {
        return tableLogService.openTable(tableLog);
    }

    @Override
    public TableLog getOpenTableLog(String tableId) {
        return tableLogService.getOpenTableLog(tableId);
    }

    @Override
    public boolean save(TableLog tableLog) {
        return tableLogService.save(tableLog);
    }
}
