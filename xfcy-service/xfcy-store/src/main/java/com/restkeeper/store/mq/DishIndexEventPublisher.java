package com.restkeeper.store.mq;

import com.alibaba.fastjson.JSON;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.mq.DishIndexMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 菜品/套餐索引变更事件发布（K16）。
 * <p>发送方经默认交换机直投 dish_index_queue，es provider 消费后双写 ES 索引。
 */
@Component
public class DishIndexEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public DishIndexEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishDish(String id, String action) {
        publish(SystemCode.DISH_TYPE_MORMAL, id, action);
    }

    public void publishSetMeal(String id, String action) {
        publish(SystemCode.DISH_TYPE_SETMEAL, id, action);
    }

    private void publish(int type, String id, String action) {
        DishIndexMessage message = new DishIndexMessage(type, id, action);
        rabbitTemplate.convertAndSend(SystemCode.DISH_INDEX_QUEUE, JSON.toJSONString(message));
    }
}
