package com.restkeeper.store.controller;

import com.restkeeper.http.PageResult;
import com.restkeeper.store.entity.Table;
import com.restkeeper.store.service.ITableService;
import com.restkeeper.store.service.TableServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 * 桌台 HTTP 端点：契约映射由 {@link ITableService} 上的 @HttpExchange 提供
 */
@RestController
public class TableController implements ITableService {

    @Autowired
    private TableServiceImpl tableService;

    @Override
    public boolean add(Table table) {
        return tableService.save(table);
    }

    @Override
    public PageResult<Table> queryPageByAreaId(String areaId, int pageNum, int pageSize) {
        return tableService.queryPageByAreaId(areaId, pageNum, pageSize);
    }

    @Override
    public Integer countTableByStatus(String areaId, Integer status) {
        return tableService.countTableByStatus(areaId, status);
    }

    @Override
    public Table getById(String id) {
        return tableService.getById(id);
    }

    @Override
    public boolean updateById(Table table) {
        return tableService.updateById(table);
    }
}
