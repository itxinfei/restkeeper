package com.restkeeper.store.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 桌台记录
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "桌台记录")
@TableName(value="t_table_log")
public class TableLog extends BaseStoreEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "开桌记录id")
    @TableId(type = IdType.ASSIGN_ID)
    private String logId;

    @Schema(description = "桌台id")
    private String tableId;

    @Schema(description = "操作人")
    private String userId;

    @Schema(description = "桌台操作记录 1 开桌 2 锁桌 ")
    private Integer tableStatus;

    @Schema(description = "用餐人数")
    private Integer userNumbers;

    @Schema(description = "开桌时间")
    private LocalDateTime createTime;


}

