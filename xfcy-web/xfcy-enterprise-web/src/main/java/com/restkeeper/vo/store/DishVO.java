package com.restkeeper.vo.store;

import com.restkeeper.constants.SystemCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class DishVO {

    @Schema(description = "菜品id")
    private String id;  //添加时候可以不传
	
    @Schema(description = "菜品名称")
    private String name;
	
    @Schema(description = "分类id")
    private String categoryId;
	
    @Schema(description = "价格")
    private Integer price;
	
    @Schema(description = "菜品code")
    private String code;
	
    @Schema(description = "图片路径")
    private String image;
	
    @Schema(description = "备注")
    private String description;
	
    @Schema(description = "状态 0 停售，1起售")
    private Integer status = SystemCode.ENABLED; //默认起售
	
    @Schema(description = "状态")
    private List<DishFlavorVO> dishFlavors;

}
