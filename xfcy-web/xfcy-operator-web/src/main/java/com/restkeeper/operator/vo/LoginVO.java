package com.restkeeper.operator.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LoginVO {

    @Schema(description = "登录账号")
    private String loginName;
    @Schema(description = "密码")
    private String loginPass;
}
