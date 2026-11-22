package com.restkeeper.store.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 沽清
 * </p>
 */
@Data
@Accessors(chain = true)
@Schema(description = "沽清")
@TableName(value = "t_sell_calculation")
public class SellCalculation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "菜品Id")
    private String dishId;

    @Schema(description = "菜品名称")
    private String dishName;

    @Schema(description = "1 普通菜品2 套餐")
    private Integer dishType;

    @Schema(description = "估清总数")
    private Integer sellLimitTotal;

    @Schema(description = "剩余数")
    private Integer remainder;

    @Schema(description = "最后更新时间")
    @TableField(value = "last_update_time",fill = FieldFill.INSERT_UPDATE)
    protected LocalDateTime lastUpdateTime;

    @Schema(description = "所属商户id")
    @TableField(value = "shop_id")
    protected String shopId;

    @Schema(description = "门店id")
    protected String storeId;

}

