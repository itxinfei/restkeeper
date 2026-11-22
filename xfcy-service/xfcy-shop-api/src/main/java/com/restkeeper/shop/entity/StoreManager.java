package com.restkeeper.shop.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 门店管理员信息
 * </p>
 * @since 2019-11-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(value = "t_store_manager",resultMap = "BaseResultMap")
@Schema(description = "门店管理员信息")
public class StoreManager extends BaseShopEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    //@TableId(type = IdType.ASSIGN_ID)
    @TableId(type = IdType.ASSIGN_ID)
    private String storeManagerId;

    @Schema(description = "门店管理员姓名")
    private String storeManagerName;

    @Schema(description = "门店管理员电话")
    private String storeManagerPhone;

    @Schema(description = "状态")
    private Integer status;

    @JsonIgnore
    @Schema(description = "密码")
    private String password;

    @Schema(description = "关联的门店集合")
    @TableField(exist = false)
    private List<Store> stores;

}
