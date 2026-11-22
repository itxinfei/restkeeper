package com.restkeeper.controller;

import com.restkeeper.constants.SystemCode;
import com.restkeeper.http.PageResult;
import com.restkeeper.store.entity.Table;
import com.restkeeper.store.entity.TableLog;
import com.restkeeper.store.service.ITableAreaService;
import com.restkeeper.store.service.ITableLogService;
import com.restkeeper.store.service.ITableService;
import com.restkeeper.vo.TablePanelVO;
import com.restkeeper.vo.TableVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 收银端区域桌台（阶段 4：Dubbo @Reference → HTTP Interface 契约代理）。
 */
@Tag(name = "收银端区域桌台")
@RestController
public class TableController {

    private final ITableAreaService tableAreaService;
    private final ITableLogService tableLogService;
    private final ITableService tableService;

    public TableController(ITableAreaService tableAreaService,
                           ITableLogService tableLogService,
                           ITableService tableService) {
        this.tableAreaService = tableAreaService;
        this.tableLogService = tableLogService;
        this.tableService = tableService;
    }

    @Operation(summary = "区域列表")
    @GetMapping("/table/area/list")
    public List<Map<String, Object>> listTableArea() {
        return tableAreaService.listTableArea();
    }

    @Operation(summary = "开台")
    @PostMapping("/table/log/open")
    public boolean openTable(@RequestBody TableLog tableLog) {
        return tableLogService.openTable(tableLog);
    }

    @Operation(summary = "桌台面板")
    @GetMapping("/table/area/{areaId}/page/{pageNum}/{pageSize}")
    public TablePanelVO queryByArea(@PathVariable("areaId") String areaId,
                                    @PathVariable("pageNum") int pageNum,
                                    @PathVariable("pageSize") int pageSize) {
        TablePanelVO tablePanelVO = new TablePanelVO();

        // 桌台统计信息
        tablePanelVO.setFreeNumbers(tableService.countTableByStatus(areaId, SystemCode.TABLE_STATUS_FREE));
        tablePanelVO.setLockedNumbers(tableService.countTableByStatus(areaId, SystemCode.TABLE_STATUS_LOCKED));
        tablePanelVO.setOpenedNumbers(tableService.countTableByStatus(areaId, SystemCode.TABLE_STATUS_OPENED));

        // 桌台面板详情，支持分页
        PageResult<Table> page = tableService.queryPageByAreaId(areaId, pageNum, pageSize);

        List<TableVO> tableVOList = new ArrayList<>();
        page.getRecords().forEach(table -> {
            TableVO tableVO = new TableVO();
            tableVO.setTableId(table.getTableId());
            tableVO.setTableName(table.getTableName());

            // 开台状态：从 TableLog 取开台时间与人数
            if (table.getStatus() != null && table.getStatus() == SystemCode.TABLE_STATUS_OPENED) {
                TableLog tableLog = tableLogService.getOpenTableLog(table.getTableId());
                if (tableLog != null) {
                    tableVO.setCreateTime(tableLog.getCreateTime());
                    tableVO.setUserNumbers(tableLog.getUserNumbers());
                }
            }
            tableVOList.add(tableVO);
        });

        PageResult<TableVO> tablePage = new PageResult<>();
        tablePage.setRecords(tableVOList);
        tablePage.setTotal(page.getTotal());
        tablePage.setPage(page.getPage());
        tablePage.setSize(page.getSize());
        tablePage.setPages(page.getPages());
        tablePanelVO.setTablePage(tablePage);

        return tablePanelVO;
    }
}
