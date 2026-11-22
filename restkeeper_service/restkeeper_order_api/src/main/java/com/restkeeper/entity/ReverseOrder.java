package com.restkeeper.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 反结账主表
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "反结账主表")
@TableName(value = "t_reverse_order")
public class ReverseOrder extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "关联订单号")
    private String orderId;

    @Schema(description = "流水号")
    private String orderNumber;

    @Schema(description = "桌台id")
    private String tableId;

    @Schema(description = "反结账状态0 失败 1 成功")
    private Integer status;

    @Schema(description = "反结账金额")
    private Integer amount;

    @Schema(description = "反结账备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "操作人名称")
    private String operatorName;


}
