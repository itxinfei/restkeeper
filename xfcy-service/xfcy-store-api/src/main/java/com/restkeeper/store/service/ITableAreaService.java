package com.restkeeper.store.service;

import com.restkeeper.store.entity.TableArea;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;
import java.util.Map;

@Tag(name = "桌台区域服务")
@HttpExchange("/table/area")
public interface ITableAreaService {

    //新增区域
    @PostExchange("/add")
    boolean add(@RequestBody TableArea tableArea);

    //查询区域列表（含桌台数量等聚合信息）
    @GetExchange("/list")
    List<Map<String, Object>> listTableArea();
}
