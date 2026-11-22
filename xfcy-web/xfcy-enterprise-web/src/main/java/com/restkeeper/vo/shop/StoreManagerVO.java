package com.restkeeper.vo.shop;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class StoreManagerVO {

    @Schema(description = "门店管理员id")
    private String storeManagerId;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "门店id列表")
    private List<String> storeIds;

}
