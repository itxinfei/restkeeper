package com.restkeeper.operator.entity;

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
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "字典数据")
@TableName(value = "t_sys_dict")
public class SysDictionary implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "字典id")
    @TableId(value = "dict_id",type = IdType.ASSIGN_ID)
    private String dictId;

    @Schema(description = "名称")
    @TableField(value = "dict_name")
    private String dictName;

    @Schema(description = "字典数据")
    @TableField(value = "dict_data")
    private String dictData;

    @Schema(description = "分类")
    @TableField(value = "category")
    private String category;

    @TableField(value = "info")
    private String info;

}
