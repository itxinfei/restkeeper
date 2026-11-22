package com.restkeeper.controller;

import com.restkeeper.constants.SystemCode;
import com.restkeeper.entity.DishEs;
import com.restkeeper.mapper.DishEsRowMapper;
import com.restkeeper.utils.Result;
import com.restkeeper.utils.ResultCode;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * ES 索引运维端点（K16）：初始化索引 mapping、全量重建索引。
 * <p>仅用于本地/运维，不对外暴露于网关路由。
 */
@RestController
@RequestMapping("/es/index")
public class DishIndexController {

    private static final String DISH_COLUMNS = "dish_id,dish_name,code,image,price,shop_id,store_id,last_update_time";
    private static final String MEAL_COLUMNS = "setmeal_id,setmeal_name,code,image,price,shop_id,store_id,last_update_time";

    private final JdbcTemplate jdbcTemplate;
    private final ElasticsearchOperations elasticsearchOperations;

    public DishIndexController(JdbcTemplate jdbcTemplate, ElasticsearchOperations elasticsearchOperations) {
        this.jdbcTemplate = jdbcTemplate;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    /**
     * 初始化 dish 索引（不存在则按实体注解创建 mapping）
     */
    @PostMapping("/init")
    public Result init() {
        IndexOperations indexOps = elasticsearchOperations.indexOps(DishEs.class);
        if (!indexOps.exists()) {
            indexOps.create();
            indexOps.putMapping(indexOps.createMapping());
        }
        return result(ResultCode.success, "索引初始化完成");
    }

    /**
     * 全量重建：扫描 store 库未删除的菜品与套餐，覆盖写入索引
     */
    @PostMapping("/rebuild")
    public Result rebuild() {
        List<DishEs> docs = new ArrayList<>();
        docs.addAll(jdbcTemplate.query(
                "SELECT " + DISH_COLUMNS + " FROM t_dish WHERE is_deleted=0",
                new DishEsRowMapper(SystemCode.DISH_TYPE_MORMAL)));
        docs.addAll(jdbcTemplate.query(
                "SELECT " + MEAL_COLUMNS + " FROM t_setmeal WHERE is_deleted=0",
                new DishEsRowMapper(SystemCode.DISH_TYPE_SETMEAL)));
        if (!docs.isEmpty()) {
            elasticsearchOperations.save(docs);
        }
        return result(ResultCode.success, "重建完成，共 " + docs.size() + " 条");
    }

    private Result result(int status, String desc) {
        Result result = new Result();
        result.setStatus(status);
        result.setDesc(desc);
        return result;
    }
}
