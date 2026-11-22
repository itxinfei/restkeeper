package com.restkeeper.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.entity.DishEs;
import com.restkeeper.mapper.DishEsRowMapper;
import com.restkeeper.mq.DishIndexMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 菜品/套餐索引变更消费者（K16）：消费 store 经默认交换机直投的 dish_index_queue，
 * 从 store 库只读回填文档并双写 ES 索引；删除事件则移除索引。
 */
@Slf4j
@Component
public class DishIndexConsumer {

    private final JdbcTemplate jdbcTemplate;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ObjectMapper objectMapper;

    public DishIndexConsumer(JdbcTemplate jdbcTemplate,
                             ElasticsearchOperations elasticsearchOperations,
                             ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.elasticsearchOperations = elasticsearchOperations;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = SystemCode.DISH_INDEX_QUEUE)
    public void onMessage(String message) {
        DishIndexMessage msg;
        try {
            msg = objectMapper.readValue(message, DishIndexMessage.class);
        } catch (Exception e) {
            log.error("索引变更消息解析失败：{}", message, e);
            return;
        }
        if (msg == null || msg.getId() == null) {
            log.warn("索引变更消息为空，忽略");
            return;
        }
        if (DishIndexMessage.ACTION_DELETE.equals(msg.getAction())) {
            elasticsearchOperations.delete(msg.getId(), DishEs.class);
            log.info("ES 索引删除成功, id={}", msg.getId());
            return;
        }
        DishEs doc = loadDoc(msg.getType(), msg.getId());
        if (doc == null) {
            log.warn("ES 索引同步忽略：库中不存在或已删除, type={}, id={}", msg.getType(), msg.getId());
            return;
        }
        elasticsearchOperations.save(doc);
        log.info("ES 索引写入成功, id={}", doc.getId());
    }

    private DishEs loadDoc(Integer type, String id) {
        int docType = (type != null && type == SystemCode.DISH_TYPE_SETMEAL)
                ? SystemCode.DISH_TYPE_SETMEAL : SystemCode.DISH_TYPE_MORMAL;
        String sql = SystemCode.DISH_TYPE_SETMEAL == docType
                ? "SELECT setmeal_id,setmeal_name,code,image,price,shop_id,store_id,last_update_time FROM t_setmeal WHERE setmeal_id=? AND is_deleted=0"
                : "SELECT dish_id,dish_name,code,image,price,shop_id,store_id,last_update_time FROM t_dish WHERE dish_id=? AND is_deleted=0";
        try {
            return jdbcTemplate.queryForObject(sql, new DishEsRowMapper(docType), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
