package com.restkeeper.vo.store;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PieVo {
    @Schema(description = "支付类型")
    private String name;
    @Schema(description = "支付总额")
    private Integer value;

    @Schema(description = "占比")
    private Double percent;
}
