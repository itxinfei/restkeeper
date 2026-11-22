package com.restkeeper.vo.shop;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AddTBrandVO {

    @Schema(description = "品牌名称")
    private String brandName;

    @Schema(description = "所属业态")
    private String category;

    @Schema(description = "品牌Logo")
    private String logo;

    @Schema(description = "负责人")
    private String contact;

    @Schema(description = "联系方式")
    private String contactPhone;

    @Schema(description = "描述信息")
    private String info;
}
