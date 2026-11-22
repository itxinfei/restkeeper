package com.restkeeper.operator.config;

import com.restkeeper.constants.SystemCode;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * rabbitMQ配置类：仅声明账号下发队列。
 * <p>发送方（operator/shop/store）经默认交换机直投 account_queue，监听方 @RabbitListener 同名队列消费。
 * 原 sms_exchange / account_queue_key 绑定未被任何发送方使用（死配置），K18 已删除。
 */
@Component
public class RabbitMQConfig {

    //帐号下发队列
    public static final String ACCOUNT_QUEUE = SystemCode.SMS_ACCOUNT_QUEUE;

    //声明队列
    @Bean(ACCOUNT_QUEUE)
    public Queue accountQueue() {
        return new Queue(ACCOUNT_QUEUE);
    }
}
