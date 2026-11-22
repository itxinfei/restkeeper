package com.restkeeper.store.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 挂账单位人员
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "挂账公司人员")
@TableName(value = "t_credit_company_user")
public class CreditCompanyUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键id")
    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private String id;

    @Schema(description = "挂账公司关联id")
    private String creditId;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "最后更新时间")
    @TableField(value = "last_update_time",fill = FieldFill.INSERT_UPDATE)
    protected LocalDateTime lastUpdateTime;

    @Schema(description = "所属商户id")
    @TableField(value = "shop_id")
    protected String shopId;

    @Schema(description = "门店id")
    protected String storeId;
}
