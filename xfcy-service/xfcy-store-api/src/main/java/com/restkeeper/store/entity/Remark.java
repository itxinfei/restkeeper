package com.restkeeper.store.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 备注信息
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "备注信息")
@TableName(value = "t_remark")
public class Remark implements Serializable {

    private static final long serialVersionUID = 1L;
    @Schema(description = "备注id")
    @TableId(type = IdType.ASSIGN_ID)
    private String remarkId;

    @Schema(description = "备注名称 退菜原因 退单原因。。。")
    private String remarkName;

    @Schema(description = "备注")
    private String remarkValue;

    @Schema(description = "最后更新时间")
    @TableField(value = "last_update_time",fill = FieldFill.INSERT_UPDATE)
    protected LocalDateTime lastUpdateTime;

    @Schema(description = "所属商户id")
    @TableField(value = "shop_id")
    protected String shopId;

    @Schema(description = "门店id")
    protected String storeId;
}

