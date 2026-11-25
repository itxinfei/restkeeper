package com.restkeeper.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 门店员工登录请求体：POST /staff/login（body 传参，规避 query 中文编码问题）
 */
@Data
@Schema(description = "门店员工登录提交体")
public class StaffLoginDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "企业 ID")
    private String shopId;

    @Schema(description = "员工姓名")
    private String loginName;

    @Schema(description = "密码（明文，BCrypt 校验）")
    private String password;
}
