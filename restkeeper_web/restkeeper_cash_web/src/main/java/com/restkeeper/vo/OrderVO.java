package com.restkeeper.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 下单订单vo
 */
@Data
public class OrderVO {
    @Schema(description = "订单id（加菜时传入，下单不传）")
    private String orderId;

    @Schema(description = "桌台号")
    private String tableId;
    @Schema(description = "就餐人数")
    private int personNumbers;
    @Schema(description = "总价")
    private Integer totalAmount;
    @Schema(description = "整单备注")
    private String orderRemark;
    @Schema(description = "订单菜品列表")
    private List<OrderDetailVO> dishes;

}

