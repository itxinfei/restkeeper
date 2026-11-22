package com.restkeeper.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 估清VO
 */
@Data
public class SellCalculationVO {
    @Schema(description = "菜品或者套餐id")
    private String dishId;

    @Schema(description = "类型 1 菜品 2 套餐")
    private Integer dishType;

    @Schema(description = "名称")
    private String dishName;

    @Schema(description = "估清数目")
    private Integer numbers;

}
