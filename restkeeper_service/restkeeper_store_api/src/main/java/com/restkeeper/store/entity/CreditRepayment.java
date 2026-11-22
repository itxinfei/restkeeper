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
@Schema(description = "还款记录")
@TableName(value = "t_credit_repayment")
public class CreditRepayment extends BaseStoreEntity implements Serializable {

    @Schema(description = "日志Id")
    @TableId(value = "log_id",type = IdType.ASSIGN_ID)
    private String logId;

    @Schema(description = "挂账类型,1公司;2个人")
    @TableField(value = "credit_type")
    private String creditType;

    @Schema(description = "挂账管理id")
    @TableField(value = "credit_id")
    private String creditId;

    @Schema(description = "挂账公司名称")
    @TableField(value = "company_name")
    private String companyName;

    @Schema(description = "还款人名称")
    @TableField(value = "user_name")
    private String userName;

    @Schema(description = "还款方式")
    @TableField(value = "repayment_type")
    private String repaymentType;

    @Schema(description = "还款金额")
    @TableField(value = "repayment_amount")
    private Integer repaymentAmount;
}

