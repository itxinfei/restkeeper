package com.restkeeper.vo.store;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SetMealDishVO {


    /**
     * 菜品Id
     */
    @Schema(description = "菜品id")
    private String dishId;

    /**
     * 菜品名称
     */
    @Schema(description = "菜品名称")
    private String dishName;
    /**
     * 份数
     */
    @Schema(description = "份数")
    private int copies;


}
