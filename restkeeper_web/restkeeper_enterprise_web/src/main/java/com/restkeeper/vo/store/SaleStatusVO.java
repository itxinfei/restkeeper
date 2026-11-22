package com.restkeeper.vo.store;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class SaleStatusVO{

    @Schema(description = "售卖状态")
    private Integer status; // 0 停售 1 启售

    @Schema(description = "菜品id列表")
    private List<String> ids;
}
