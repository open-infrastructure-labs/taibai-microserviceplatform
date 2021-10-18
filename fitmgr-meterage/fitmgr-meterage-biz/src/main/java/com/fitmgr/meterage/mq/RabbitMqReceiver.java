package com.fitmgr.meterage.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitmgr.meterage.api.dto.ResourceBpOperateDTO;
import com.fitmgr.meterage.constant.MeterageConst;
import com.fitmgr.meterage.controller.MeterageResourceController;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @ClassName RabbitMqReceiver
 * @Description 计量服务高可用，当计量服务不可用时，用作防止消息丢失的手段
 * @Description 目前针对的只是从回收站删除的资源
 * @Author BDWang
 * @Date 2021/8/10 15:47
 **/
@Component
/**
 * exchange自动删除的条件，有队列或者交换器绑定了本交换器，然后所有队列或交换器都与本交换器解除绑定，autoDelete=true时，此交换器就会被自动删除
 * 队列自动删除的条件，有消息者订阅本队列，然后所有消费者都解除订阅此队列，autoDelete=true时，此队列会自动删除，即使此队列中还有消息。
 **/
@RabbitListener(queues = MeterageConst.METE_HA_QUEUE_NAME)
@AllArgsConstructor
@Slf4j
public class RabbitMqReceiver {

    private MeterageResourceController meterageResourceController;

    private static ObjectMapper objectMapper;

    @RabbitHandler
    public void process(String msg) {
        log.info("mq........receiver: " + msg);
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        ResourceBpOperateDTO resourceBpOperateDTO;
        try {
            resourceBpOperateDTO = objectMapper.readValue(msg, ResourceBpOperateDTO.class);
            meterageResourceController.handleMeterageAfterTerraform(resourceBpOperateDTO);
            log.info("异常消息队列消息处理完毕！");
        }catch (Exception e){
            log.error("mq handler go wrong : {}",e.getMessage(),e);
            // 调用消息通知
        }
    }
}
