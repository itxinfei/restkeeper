package com.restkeeper.vo.store;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AddDishCategoryVO {

     @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "分类类型")
    private int type;

}
