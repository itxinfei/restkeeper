package com.restkeeper.store.config;

import com.restkeeper.constants.SystemCode;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * rabbitMQ配置类：声明菜品/套餐索引变更队列（K16）。
 * <p>账号下发队列 account_queue 由 operator/shop provider 声明（全局共享，同名队列只需一处声明）。
 */
@Configuration
public class RabbitMQConfig {

    @Bean(SystemCode.DISH_INDEX_QUEUE)
    public Queue dishIndexQueue() {
        return new Queue(SystemCode.DISH_INDEX_QUEUE);
    }
}
