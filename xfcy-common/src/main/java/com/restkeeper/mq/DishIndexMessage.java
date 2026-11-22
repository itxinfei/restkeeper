package com.restkeeper.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 菜品/套餐索引变更消息（K16）：store provider 写操作后发送，es provider 消费并双写 ES 索引。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DishIndexMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 动作：upsert 更新/新增，delete 删除 */
    public static final String ACTION_UPSERT = "upsert";
    public static final String ACTION_DELETE = "delete";

    /** 类型：1 菜品，2 套餐（见 SystemCode.DISH_TYPE_*） */
    private Integer type;

    /** 菜品/套餐 id */
    private String id;

    /** 动作 */
    private String action;
}
