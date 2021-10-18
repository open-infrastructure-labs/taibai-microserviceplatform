package com.fitmgr.meterage.mq;

import com.fitmgr.meterage.constant.MeterageConst;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName RabbitMqConfig
 * @Description rabbitmq 配置类
 * @Author BDWang
 * @Date 2021/8/12 9:20
 **/
@Configuration
public class RabbitMqConfig {


    @Bean
    public Queue queueHa() {
        //队列持久
        return new Queue(MeterageConst.METE_HA_QUEUE_NAME, true);
    }

    @Bean
    public FanoutExchange defaultExchange() {
        return new FanoutExchange(MeterageConst.METE_HA_EX_NAME, true, false);
    }

    @Bean
    public Binding binding() {
        return BindingBuilder.bind(queueHa()).to(defaultExchange());
    }
}
