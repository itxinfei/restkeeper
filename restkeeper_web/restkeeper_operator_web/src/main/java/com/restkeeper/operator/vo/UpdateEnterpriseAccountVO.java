package com.restkeeper.operator.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class UpdateEnterpriseAccountVO extends AddEnterpriseAccountVO {

    @Schema(description = "企业id")
    private String enterpriseId;
}
