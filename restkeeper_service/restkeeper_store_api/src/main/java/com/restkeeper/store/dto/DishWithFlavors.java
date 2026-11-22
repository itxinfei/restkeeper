package com.restkeeper.store.dto;

import com.restkeeper.store.entity.Dish;
import com.restkeeper.store.entity.DishFlavor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 菜品保存/修改请求体：HTTP 单 body 承载菜品主体与口味列表
 */
@Data
@Schema(description = "菜品及口味提交体")
public class DishWithFlavors implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "菜品主体")
    private Dish dish;

    @Schema(description = "口味列表")
    private List<DishFlavor> flavorList;
}
