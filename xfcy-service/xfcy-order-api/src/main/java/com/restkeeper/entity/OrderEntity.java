package com.restkeeper.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 订单主表
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "订单主表")
@TableName(value = "t_order")
@NoArgsConstructor
public class OrderEntity extends  BaseEntity  implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "order_id",type = IdType.ASSIGN_ID)
    private String orderId;

    @Schema(description = "流水号")
    private String orderNumber;

    @Schema(description = "桌台id")
    private String tableId;

    @Schema(description = "付款方式 0 免单 1 现金 2 微信 3 支付宝  4 银行卡 5 挂账")
    private Integer payType;

    @Schema(description = "支付状态 0 未付 1 已付 ")
    private Integer payStatus;

    @Schema(description = "付款金额")
    private Integer payAmount;

    @Schema(description = "就餐人数")
    private Integer smallAmount; //抹零金额

    @Schema(description = "就餐人数")
    private Integer personNumbers; //就餐人数

    @Schema(description = "应收金额")
    private Integer totalAmount;

    @Schema(description = "赠菜金额")
    private Integer presentAmount;

    @Schema(description = "免单金额")
    private Integer freeAmount;

    @Schema(description = "整单备注")
    private String orderRemark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "操作人")
    private String operatorName;

    @Schema(description = "购买人id")
    private String buyId;

    @Schema(description = "订单来源")
    private Integer orderSource; //订单来源 0 收银端 1 app

    @Schema(description = "订单详情列表")
    @TableField(exist = false)
    private List<OrderDetailEntity> orderDetails;

    @TableField(exist = false)
    private Integer currentDateHour;
}

