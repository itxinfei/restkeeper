package com.restkeeper.operator.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 企业账号管理
 * </p>
 */
@Data
@Accessors(chain = true)
@TableName(value="t_enterprise_account")
@Schema(description = "企业账号管理对象")
public class EnterpriseAccount implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "企业id")
    @TableId(type= IdType.ASSIGN_ID)
    private String enterpriseId;

    @Schema(description = "企业名称")
    private String enterpriseName;

    @JsonIgnore
    @Schema(description = "密码（后台自动下发）")
    private String password;

    @JsonIgnore
    @Schema(description = "商户号（下发生成）")
    private String shopId;

    @Schema(description = "申请人")
    private String applicant;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "省")
    private String province;

    @Schema(description = "市")
    private String city;

    @Schema(description = "区")
    private String area;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "申请时间（当前时间，精准到分）")
    private LocalDateTime applicationTime;

    @Schema(description = "到期时间 (试用下是默认七天后到期，状态改成停用)")
    private LocalDateTime expireTime;

    @Schema(description = "状态：0 试用，1 正式，-1 停用")
    private Integer status;

    @Schema(description = "最后更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime lastUpdateTime;

    @Schema(description = "是否删除 1 删除 0 未删除")
    /**
     * value: 默认的原值
     * delval: 删除后的值
     */
    @TableLogic(value = "0",delval = "1")
    private Integer isDeleted;
}
