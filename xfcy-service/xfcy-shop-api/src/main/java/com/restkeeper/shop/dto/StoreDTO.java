package com.restkeeper.shop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class StoreDTO {

    @Schema(description = "门店id")
    private String storeId;

    @Schema(description = "门店名称")
    private String storeName;

    @Schema(description = "门店管理员id")
    private String storeManagerId;

    @Schema(description = "门店地址")
    private String address;
}

