package com.restkeeper.store.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "菜品及套餐分类")
@TableName(value = "t_dish_flavor",resultMap = "BaseResultMap")
public class DishFlavor implements Serializable{
	
    @Schema(description = "主键id")
    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private String id;
	
    @Schema(description = "菜品id")
    @TableField(value = "dish_id")
    private String dishId;
	
    @Schema(description = "口味名")
    @TableField(value = "flavor_name")
    private String flavorName;
	
    @Schema(description = "口味标签")
    @TableField(value = "flavor_value")
    private String flavorValue;
	
    @Schema(description = "所属商户id")
    @TableField(value = "shop_id")
    protected String shopId;
	
    @Schema(description = "门店id")
    protected String storeId;
	
    @Schema(description = "最后更新时间")
    @TableField(value = "last_update_time",fill = FieldFill.INSERT_UPDATE)
    protected LocalDateTime lastUpdateTime;
}
