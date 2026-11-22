package com.restkeeper.store.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 员工信息
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(value = "t_staff")
@Schema(description = "员工信息")
public class Staff extends BaseStoreEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "员工id")
    @TableId(type = IdType.ASSIGN_UUID)
    private String staffId;


    @Schema(description = "员工姓名")
    private String staffName;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "性别")
    private String sex;

    @Schema(description = "身份证号")
    private String idNumber;

    @Schema(description = "状态：1 在职启用，0 停用")
    private Integer status;
}

