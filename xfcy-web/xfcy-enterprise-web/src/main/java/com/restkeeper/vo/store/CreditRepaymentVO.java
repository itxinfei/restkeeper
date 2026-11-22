package com.restkeeper.vo.store;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 还款vo
 */
@Data
public class CreditRepaymentVO {

    @Schema(description = "挂账类型,1公司;2个人")
    private String creditType;

    @Schema(description = "挂账管理id")
    private String creditId;

    @Schema(description = "挂账公司名称")
    private String companyName;

    @Schema(description = "还款人名称")
    private String userName;

    @Schema(description = "还款方式")
    private String repaymentType;

    @Schema(description = "还款金额")
    private Integer repaymentAmount;
}

