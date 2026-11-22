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
import java.util.List;

/**
 * 挂账单位管理
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "挂账管理表")
@TableName(value = "t_credit")
public class Credit extends BaseStoreEntity implements Serializable{
    @TableId(value = "credit_id",type = IdType.ASSIGN_ID)
    @Schema(description = "主键id")
    private String creditId;

    @Schema(description = "挂账类型 1 公司 2 个人")
    private Integer creditType;

    @Schema(description = "公司名称,如果个人可以为空")
    private String companyName;

    @Schema(description = "用户名称")
    private String userName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "状态 1 开启 0 停用")
    private Integer status;

    @Schema(description = "挂账金额")
    private Integer creditAmount;

    @Schema(description = "总还款金额")
    private Integer totalRepaymentAmount;

    @Schema(description = "单位公司用户列表")
    @TableField(exist = false)
    private List<CreditCompanyUser> users;

}
