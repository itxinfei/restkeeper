package com.restkeeper.store.controller;

import com.restkeeper.store.entity.TableArea;
import com.restkeeper.store.service.ITableAreaService;
import com.restkeeper.store.service.TableAreaServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 桌台区域 HTTP 端点：契约映射由 {@link ITableAreaService} 上的 @HttpExchange 提供
 */
@RestController
public class TableAreaController implements ITableAreaService {

    @Autowired
    private TableAreaServiceImpl tableAreaService;

    @Override
    public boolean add(TableArea tableArea) {
        return tableAreaService.save(tableArea);
    }

    @Override
    public List<Map<String, Object>> listTableArea() {
        return tableAreaService.listTableArea();
    }
}
