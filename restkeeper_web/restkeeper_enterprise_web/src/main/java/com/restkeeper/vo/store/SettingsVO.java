package com.restkeeper.vo.store;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 门店设置vo
 */
@Data
public class SettingsVO {
    @Schema(description = "备注列表")
    private List<RemarkVO> remarks;

}
