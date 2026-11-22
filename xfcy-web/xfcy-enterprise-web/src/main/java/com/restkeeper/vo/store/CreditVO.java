package com.restkeeper.vo.store;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 挂账管理VO
 */
@Data
public class CreditVO {

    @Schema(description = "挂账id（修改时必填）")
    private String id;

    @Schema(description = "挂账类型 1 企业 2 个人")
    private int creditType;

    @Schema(description = "挂账公司名")
    private String companyName;

    @Schema(description = "挂账公司用户列表")
    private List<CreditCompanyUserVO> users;

    //挂账类型个人时候使用
    @Schema(description = "个人类型 挂账人")
    private String userName;

    @Schema(description = "个人类型 联系电话")
    private String phone;

    //回显使用
    @Schema(description = "挂账用户列表")
    private Integer creditAmount; //挂账金额

    @Schema(description = "挂账用户列表")
    private Integer status=1; // 状态 0停用， 1启用
}