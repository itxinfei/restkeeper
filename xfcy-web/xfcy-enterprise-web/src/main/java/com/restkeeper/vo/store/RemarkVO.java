package com.restkeeper.vo.store;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 备注vo
 */
@Data
public class RemarkVO {

    @Schema(description = "备注名称")
    private String remarkName;

    @Schema(description = "备注集合")
    private List<String> remarkValue;

}
