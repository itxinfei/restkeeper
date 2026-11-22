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
import java.util.List;

/**
 * 菜品
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "菜品")
@TableName(value = "t_dish", resultMap = "BaseResultMap")
public class Dish extends BaseStoreEntity implements Serializable{

    @Schema(description = "菜品id")
    @TableId(value = "dish_id",type = IdType.ASSIGN_ID)
    private String id;

    @Schema(description = "菜品名称")
    @TableField(value = "dish_name")
    private String name;

    @Schema(description = "分类id")
    @TableField(value = "category_id")
    private String categoryId;

    @Schema(description = "价格")
    @TableField(value = "price")
    private Integer price;

    @Schema(description = "菜品code")
    @TableField(value = "code")
    private String code;

    @Schema(description = "图片路径")
    @TableField(value = "image")
    private String image;

    @TableField(value = "description")
    @Schema(description = "备注")
    private String description;

    @Schema(description = "状态")
    @TableField(value = "status")
    private Integer status;

    @Schema(description = "所属分类对象")
    @TableField(exist = false)
    private DishCategory dishCategory;

    @Schema(description = "口味列表")
    @TableField(exist = false)
    private List<DishFlavor> flavorList;
}
