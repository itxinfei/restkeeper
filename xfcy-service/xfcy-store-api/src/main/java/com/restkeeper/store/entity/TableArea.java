package com.restkeeper.store.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "区域")
@TableName(value = "t_table_area")
public class TableArea extends BaseStoreEntity implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private String areaId;

    @TableField(value = "area_name")
    private String areaName;

    @TableField(value = "t_order")
    private int indexNumber;
}

