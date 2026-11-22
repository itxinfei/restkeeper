package com.restkeeper.store.controller;

import com.restkeeper.http.PageResult;
import com.restkeeper.store.entity.CreditLogs;
import com.restkeeper.store.service.CreditLogServiceImpl;
import com.restkeeper.store.service.ICreditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 挂账流水 HTTP 端点：契约映射由 {@link ICreditLogService} 上的 @HttpExchange 提供
 */
@RestController
public class CreditLogController implements ICreditLogService {

    @Autowired
    private CreditLogServiceImpl creditLogService;

    @Override
    public PageResult<CreditLogs> queryPage(int pageNum, int pageSize, String creditId) {
        return creditLogService.queryPage(pageNum, pageSize, creditId);
    }

    @Override
    public List<CreditLogs> list(String creditId, LocalDateTime start, LocalDateTime end) {
        return creditLogService.list(creditId, start, end);
    }

    @Override
    public boolean save(CreditLogs creditLogs) {
        return creditLogService.save(creditLogs);
    }
}
