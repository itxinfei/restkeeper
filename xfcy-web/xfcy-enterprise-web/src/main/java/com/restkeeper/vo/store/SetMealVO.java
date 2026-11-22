package com.restkeeper.vo.store;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class SetMealVO {

    @Schema(description = "套餐id")
    private String id; //添加时候不传

    @Schema(description = "分类id")
    private String categoryId;

    @Schema(description = "套餐名称")
    private String name;

    @Schema(description = "套餐价格")
    private Integer price;

    @Schema(description = "状态 0停售，1起售")
    private Integer status;

    @Schema(description = "商品码")
    private String code;

    @Schema(description = "描述信息")
    private String description;

    @Schema(description = "图片")
    private String image;

    @Schema(description = "菜品")
    private List<SetMealDishVO> dishList;

}
