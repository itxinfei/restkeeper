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

/**
 * <p>
 * 门店信息账号
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "门店信息账号")
@TableName(value = "t_store",resultMap = "BaseResultMap")
public class Store extends BaseShopEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "门店主键id")
    @TableId(type = IdType.ASSIGN_ID)
    private String storeId;

    @Schema(description = "门店名称")
    private String storeName;

    @JsonIgnore
    @Schema(description = "所属品牌")
    private String brandId;

    @Schema(description = "省")
    private String province;

    @Schema(description = "市")
    private String city;

    @Schema(description = "区")
    private String area;

    @Schema(description = "联系人")
    private String contact;

    @Schema(description = "联系人电话")
    private String contactPhone;

    @Schema(description = "门店管理员id")
    private String storeManagerId;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "状态(正式运营1，停业0)")
    private Integer status;

    @Schema(description = "所属品牌对象")
    @TableField(exist = false)
    private Brand brand;


}
