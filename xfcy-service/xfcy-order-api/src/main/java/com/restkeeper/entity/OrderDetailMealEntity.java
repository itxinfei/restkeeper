package com.restkeeper.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "t_order_detail_meal")
public class OrderDetailMealEntity extends  BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value="detail_id",type = IdType.ASSIGN_ID)
    private String detailId;

    @Schema(description = "订单id")
    @TableField("order_id")
    private String orderId;

    @Schema(description = "订单流水号")
    private String orderNumber;

    @Schema(description = "所属桌台")
    private String tableId;

    @Schema(description = "状态 1 赠菜 2 退菜 ")
    private Integer detailStatus;

    @Schema(description = "菜品备注 ")
    private String dishRemark;

    @Schema(description = "菜品id")
    private String dishId;

    @Schema(description = "类型 1 菜品 2 套餐")
    private Integer dishType;

    @Schema(description = "名称")
    private String dishName;

    @Schema(description = "单价")
    private Integer dishPrice;

    @Schema(description = "份数")
    private Integer dishNumber;

    @Schema(description = "口味备注")
    private String flavorRemark;

    @Schema(description = "赠菜备注")
    private String presentRemark;

    @Schema(description = "退菜备注")
    private String returnRemark;

    @Schema(description = "金额")
    private Integer dishAmount;

    @Schema(description = "菜品分类名称")
    private String dishCategoryName;
}
