package com.restkeeper.vo.store;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 挂账公司用户VO
 */
@Data
public class CreditCompanyUserVO{

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "手机号码")
    private String phone;
}
