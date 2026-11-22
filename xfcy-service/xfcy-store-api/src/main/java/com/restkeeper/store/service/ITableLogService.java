package com.restkeeper.store.service;

import com.restkeeper.store.entity.TableLog;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@Tag(name = "桌台开台记录服务")
@HttpExchange("/table/log")
public interface ITableLogService {

    //开桌
    @PostExchange("/open")
    boolean openTable(@RequestBody TableLog tableLog);

    //按 tableId 查询当前开台记录
    @GetExchange("/open/{tableId}")
    TableLog getOpenTableLog(@PathVariable("tableId") String tableId);

    //保存开台记录（订单流程内部调用）
    @PostExchange
    boolean save(@RequestBody TableLog tableLog);
}
