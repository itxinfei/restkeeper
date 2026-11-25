package com.restkeeper.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serial;
import java.io.Serializable;

/**
 * 菜品、套餐与 Elasticsearch 文档的映射对象（索引 dish 由外部预置，本应用只读写不建索引）。
 * {@link Field#name()} 供 Spring Data Elasticsearch 映射下划线字段，
 * {@link JsonProperty} 保证 Controller 返回 JSON 时字段名同样为下划线。
 */
@Data
@Document(indexName = "dish")
@Schema(description = "菜品检索文档")
public class DishEs implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Schema(description = "菜品/套餐 id")
    private String id;

    @Field(name = "code", type = FieldType.Keyword)
    @Schema(description = "商品码")
    private String code;

    @Field(name = "dish_name", type = FieldType.Wildcard)
    @Schema(description = "菜品名称")
    private String dishName;

    @Field(name = "image")
    @Schema(description = "图片地址")
    private String image;

    @Field(name = "type", type = FieldType.Integer)
    @Schema(description = "类型：1 菜品，2 套餐")
    private int type;

    @Field(name = "price", type = FieldType.Integer)
    @Schema(description = "单价（分）")
    private Integer price;

    //索引中为 date 类型，实体保持字符串接收，避免时区转换影响展示
    @JsonProperty("last_update_time")
    @Field(name = "last_update_time")
    @Schema(description = "最后更新时间")
    private String lastUpdateTime;

    @JsonProperty("store_id")
    @Field(name = "store_id", type = FieldType.Keyword)
    @Schema(description = "门店 id")
    private String storeId;

    @JsonProperty("shop_id")
    @Field(name = "shop_id", type = FieldType.Keyword)
    @Schema(description = "所属商户 id")
    private String shopId;
}
