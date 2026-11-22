package com.restkeeper.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收银端桌台对象
 */
@Data
public class TableVO {
    @Schema(description = "桌台id")
    private String tableId;

    @Schema(description = "桌台名称")
    private String tableName;

    @Schema(description = "开桌时间")
    private LocalDateTime createTime;

    @Schema(description = "用餐人数")
    private Integer userNumbers;
}

