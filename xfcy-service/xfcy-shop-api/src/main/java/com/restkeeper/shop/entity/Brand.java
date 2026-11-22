package com.restkeeper.shop.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 品牌管理
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "t_brand",resultMap = "BaseResultMap")
@Schema(description = "品牌管理")
public class Brand extends BaseShopEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "商户id")
    @TableId(type = IdType.ASSIGN_ID)
    private String brandId;

    @Schema(description = "餐饮类别")
    private String brandName;

    @Schema(description = "分类名称")
    private String category;

    @Schema(description = "图片地址")
    private String logo;

    @Schema(description = "联系人")
    private String contact;

    @Schema(description = "联系人电话")
    private String contactPhone;

    @TableField(exist = false)
    private int storeCount; //门店总数

    @TableField(exist = false)
    private int cityCount; //城市总数

    @TableField(exist = false)
    private String info; //页面显示信息

    public String getInfo(){
        return "当前品牌有"+this.getStoreCount()+"家门店分布于"+ this.getCityCount() +"城市";
    }
}

