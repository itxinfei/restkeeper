package com.restkeeper.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 订单菜品列表
 */
@Data
public class OrderDetailVO {

    @Schema(description = "菜品或者套餐id")
    private  String dishId;
    @Schema(description = "名称")
    private  String dishName;
    @Schema(description = "类型 1 菜品 2 套餐")
    private  Integer type;
    @Schema(description = "单价")
    private  Integer price;
    @Schema(description = "份数")
    private Integer dishNumber;
    @Schema(description = "菜品备注")
    private String dishRemark;
    @Schema(description = "状态")
    private Integer status=1; //状态 1正常 2 赠菜 3 退菜  4 加菜
    @Schema(description = "口味")
    private List<String> flavorList;
}

