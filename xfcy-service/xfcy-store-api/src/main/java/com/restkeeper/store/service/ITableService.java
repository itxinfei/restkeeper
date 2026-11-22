package com.restkeeper.store.service;

import com.restkeeper.http.PageResult;
import com.restkeeper.store.entity.Table;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

@Tag(name = "桌台服务")
@HttpExchange("/table")
public interface ITableService {

    //新增桌台
    @PostExchange("/add")
    boolean add(@RequestBody Table table);

    //按区域分页查询桌台
    @GetExchange("/area/{areaId}/page/{pageNum}/{pageSize}")
    PageResult<Table> queryPageByAreaId(@PathVariable("areaId") String areaId,
                                        @PathVariable("pageNum") int pageNum,
                                        @PathVariable("pageSize") int pageSize);

    //按区域与状态统计桌台数量
    @GetExchange("/count/{areaId}/{status}")
    Integer countTableByStatus(@PathVariable("areaId") String areaId,
                               @PathVariable("status") Integer status);

    //按主键查询桌台
    @GetExchange("/{id}")
    Table getById(@PathVariable("id") String id);

    //按主键更新桌台
    @PutExchange
    boolean updateById(@RequestBody Table table);
}
