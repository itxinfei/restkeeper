package com.restkeeper.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BaseEntity {

    @Schema(description = "最后更新时间")
    @TableField(value = "last_update_time",fill = FieldFill.INSERT_UPDATE)
    protected LocalDateTime lastUpdateTime;

    @JsonIgnore
    @Schema(description = "是否删除 1是删除 0 未删除")
    @TableLogic
    @TableField(value = "is_deleted")
    protected Integer isDeleted;

    @Schema(description = "所属商户id")
    @TableField(value = "shop_id")
    protected String shopId;

    @Schema(description = "门店id")
    protected String storeId;

}

