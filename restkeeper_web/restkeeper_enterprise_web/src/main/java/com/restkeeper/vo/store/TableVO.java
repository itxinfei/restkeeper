package com.restkeeper.vo.store;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TableVO {

    @Schema(description = "区域id")
    private String areaId;

    @Schema(description = "桌台名称")
    private String tableName;

    @Schema(description = "座位数")
    private int tableSeatNumber;

}
