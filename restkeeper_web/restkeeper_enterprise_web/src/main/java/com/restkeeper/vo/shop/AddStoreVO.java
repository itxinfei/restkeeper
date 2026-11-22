package com.restkeeper.vo.shop;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AddStoreVO {

    @Schema(description = "门店名称")
    private String storeName;

    @Schema(description = "所属品牌")
    private String brandId;

    @Schema(description = "省")
    private String province;

    @Schema(description = "市")
    private String city;

    @Schema(description = "区")
    private String area;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "联系人")
    private String contact;

    @Schema(description = "联系人电话")
    private String contactPhone;
}
