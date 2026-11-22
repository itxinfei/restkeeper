package com.restkeeper.store.entity;

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
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "挂账日志")
@TableName(value = "t_credit_logs")
public class CreditLogs extends BaseStoreEntity implements Serializable{
    @Schema(description = "日志Id")
    @TableId(value = "log_id",type = IdType.ASSIGN_ID)
    private String logId;
    @Schema(description = "订单Id")
    @TableField(value = "order_id")
    private String orderId;
    @Schema(description = "挂账管理Id")
    @TableField(value = "credit_id")
    private String creditId;
    @TableField(value = "credit_type")
    private Integer type;
    @Schema(description = "挂账公司名称")
    @TableField(value = "company_name")
    private String companyName;
    @Schema(description = "挂账人名称")
    @TableField(value = "user_name")
    private String userName;
    @TableField(value = "phone")
    private String phone;
    @Schema(description = "订单金额")
    @TableField(value = "order_amount")
    private Integer orderAmount;
    @Schema(description = "应收金额")
    @TableField(value = "received_amount")
    private Integer receivedAmount;
    @Schema(description = "应收金额")
    @TableField(value = "credit_amount")
    private Integer creditAmount;
}
