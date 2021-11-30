package com.fitmgr.common.key.update.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fitmgr.common.encrypt.config.ServiceAppNameCfg;

@Configuration
public class RabbitConfig {

    private static final String RABBIT_MQ_KMS_UPDATE = "fitmgr_kms_msg";
    public static final String RABBIT_MQ_KMS_UPDATE_EXCHANGE = "fitmgr_mq_kms_update_exchange";

    @Autowired
    private ServiceAppNameCfg serviceAppNameCfg;

    @Bean
    public Queue queue() {
        return new Queue(getQueueName());
    }

    @Bean
    FanoutExchange exchange() {
        return new FanoutExchange(RABBIT_MQ_KMS_UPDATE_EXCHANGE);
    }

    /**
     * 4.队列与交换机绑定队列， 队列最后实现动态绑定
     * 
     * @param queue    queue
     * @param exchange exchange
     * @return Binding
     */
    @Bean
    Binding bindingExchangeSms(Queue queue, FanoutExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange);
    }

    public String getQueueName() {
        return RABBIT_MQ_KMS_UPDATE + serviceAppNameCfg.getApplicationName();
    }

}