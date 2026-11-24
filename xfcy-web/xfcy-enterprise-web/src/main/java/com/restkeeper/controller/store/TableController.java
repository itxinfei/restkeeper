package com.restkeeper.controller.store;

import com.restkeeper.http.PageResult;
import com.restkeeper.store.entity.Table;
import com.restkeeper.store.entity.TableArea;
import com.restkeeper.store.entity.TableLog;
import com.restkeeper.store.service.ITableAreaService;
import com.restkeeper.store.service.ITableLogService;
import com.restkeeper.store.service.ITableService;
import com.restkeeper.vo.store.TableVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 区域 / 桌台管理。端点路径对齐 provider 契约 {@link ITableAreaService} / {@link ITableService}。
 */
@Slf4j
@Tag(name = "区域管理")
@RestController
@RequestMapping("/table")
public class TableController {

    private final ITableAreaService tableAreaService;
    private final ITableService tableService;
    private final ITableLogService tableLogService;

    public TableController(ITableAreaService tableAreaService,
                           ITableService tableService,
                           ITableLogService tableLogService) {
        this.tableAreaService = tableAreaService;
        this.tableService = tableService;
        this.tableLogService = tableLogService;
    }

    @Operation(summary = "添加区域")
    @PostMapping("/area/add")
    public boolean addArea(@RequestParam("name") String name) {
        TableArea tableArea = new TableArea();
        tableArea.setAreaName(name);
        return tableAreaService.add(tableArea);
    }

    @Operation(summary = "区域列表（含桌台数量等聚合信息）")
    @GetMapping("/area/list")
    public List<Map<String, Object>> listArea() {
        return tableAreaService.listTableArea();
    }

    @Operation(summary = "添加桌台")
    @PostMapping("/add")
    public boolean addTable(@RequestBody TableVO tableVO) {
        Table table = new Table();
        BeanUtils.copyProperties(tableVO, table);
        return tableService.add(table);
    }

    @Operation(summary = "根据区域id检索桌台")
    @GetMapping("/area/{areaId}/page/{pageNum}/{pageSize}")
    public PageResult<Table> queryByArea(@PathVariable("areaId") String areaId,
                                         @PathVariable("pageNum") int pageNum,
                                         @PathVariable("pageSize") int pageSize) {
        return tableService.queryPageByAreaId(areaId, pageNum, pageSize);
    }

    @Operation(summary = "按区域与状态统计桌台数量")
    @GetMapping("/count/{areaId}/{status}")
    public Integer countTableByStatus(@PathVariable("areaId") String areaId,
                                      @PathVariable("status") Integer status) {
        return tableService.countTableByStatus(areaId, status);
    }

    @Operation(summary = "按主键查询桌台")
    @GetMapping("/{id}")
    public Table getById(@PathVariable("id") String id) {
        return tableService.getById(id);
    }

    @Operation(summary = "按主键更新桌台")
    @PutMapping
    public boolean updateById(@RequestBody Table table) {
        return tableService.updateById(table);
    }

    @Operation(summary = "按桌台查当前开台记录")
    @GetMapping("/log/open/{tableId}")
    public TableLog getOpenTableLog(@PathVariable("tableId") String tableId) {
        return tableLogService.getOpenTableLog(tableId);
    }
}
