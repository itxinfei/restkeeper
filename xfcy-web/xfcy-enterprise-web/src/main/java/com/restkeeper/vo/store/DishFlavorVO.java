package com.restkeeper.vo.store;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class DishFlavorVO {

    @Schema(description = "口味")
    private String flavor;
	
    @Schema(description = "口味标签")
    private List<String> flavorData;
}
