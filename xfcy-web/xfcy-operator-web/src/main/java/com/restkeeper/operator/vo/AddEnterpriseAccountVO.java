package com.restkeeper.operator.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AddEnterpriseAccountVO {

    @Schema(description = "企业名称")
    private String enterpriseName;

    @Schema(description = "申请人")
    private String applicant;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "省")
    private String province;

    @Schema(description = "市")
    private String city;

    @Schema(description = "区")
    private String area;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "状态(试用中0,正式1,停用-1)")
    private Integer status;

    @Schema(description = "当正式期时候，需要设置有效期 前端转换成（天）传给后端")
    private int validityDay;
}
