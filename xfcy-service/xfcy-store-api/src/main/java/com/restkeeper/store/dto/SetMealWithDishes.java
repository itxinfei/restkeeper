package com.restkeeper.store.dto;

import com.restkeeper.store.entity.SetMeal;
import com.restkeeper.store.entity.SetMealDish;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 套餐新增/修改请求体：套餐主体与套餐菜品列表
 */
@Data
@Schema(description = "套餐及套餐菜品提交体")
public class SetMealWithDishes implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "套餐主体")
    private SetMeal setMeal;

    @Schema(description = "套餐菜品列表")
    private List<SetMealDish> dishList;
}
