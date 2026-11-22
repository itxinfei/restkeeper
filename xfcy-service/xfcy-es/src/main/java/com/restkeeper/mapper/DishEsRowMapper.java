package com.restkeeper.mapper;

import com.restkeeper.constants.SystemCode;
import com.restkeeper.entity.DishEs;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * 菜品/套餐库行 → ES 文档映射（K16）。
 * 菜品取 dish_id/dish_name，套餐取 setmeal_id/setmeal_name，其余字段同名。
 */
public class DishEsRowMapper implements RowMapper<DishEs> {

    private final int type;

    public DishEsRowMapper(int type) {
        this.type = type;
    }

    @Override
    public DishEs mapRow(ResultSet rs, int rowNum) throws SQLException {
        DishEs doc = new DishEs();
        if (SystemCode.DISH_TYPE_SETMEAL == type) {
            doc.setId(rs.getString("setmeal_id"));
            doc.setDishName(rs.getString("setmeal_name"));
        } else {
            doc.setId(rs.getString("dish_id"));
            doc.setDishName(rs.getString("dish_name"));
        }
        doc.setCode(rs.getString("code"));
        doc.setImage(rs.getString("image"));
        doc.setPrice(rs.getObject("price") != null ? rs.getInt("price") : null);
        doc.setType(type);
        doc.setShopId(rs.getString("shop_id"));
        doc.setStoreId(rs.getString("store_id"));
        Timestamp timestamp = rs.getTimestamp("last_update_time");
        doc.setLastUpdateTime(timestamp != null ? timestamp.toLocalDateTime().toString() : null);
        return doc;
    }
}
