package com.restkeeper.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 菜品面板展现对象
 */
@Data
public class DishPanelVO {
    @Schema(description = "菜品或者套餐id")
    private  String dishId;
    @Schema(description = "名称")
    private  String dishName;
    @Schema(description = "类型 1 菜品 2 套餐")
    private  Integer type;
    @Schema(description = "单价")
    private  Integer price;
    @Schema(description = "图片地址")
    private  String image;
    @Schema(description = "剩余份数")
    private  Integer remainder;

}
