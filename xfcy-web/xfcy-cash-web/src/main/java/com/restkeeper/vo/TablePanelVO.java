package com.restkeeper.vo;

import com.restkeeper.http.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 收银端桌台面板
 */
@Data
public class TablePanelVO {
    @Schema(description = "空闲桌台")
    private Integer freeNumbers; //table状态0

    @Schema(description = "已开台")
    private int openedNumbers; //table状态2

    @Schema(description = "锁定")
    private int lockedNumbers; //table状态1

    @Schema(description = "桌台分页信息")
    PageResult<TableVO> tablePage;
}

