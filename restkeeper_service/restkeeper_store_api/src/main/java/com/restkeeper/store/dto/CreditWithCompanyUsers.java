package com.restkeeper.store.dto;

import com.restkeeper.store.entity.Credit;
import com.restkeeper.store.entity.CreditCompanyUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 挂账新增/修改请求体：挂账主体与关联单位用餐人列表
 */
@Data
@Schema(description = "挂账及关联用餐人提交体")
public class CreditWithCompanyUsers implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "挂账主体")
    private Credit credit;

    @Schema(description = "关联单位用餐人列表")
    private List<CreditCompanyUser> users;
}
