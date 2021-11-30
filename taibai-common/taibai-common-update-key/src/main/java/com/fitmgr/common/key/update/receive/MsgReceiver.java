package com.fitmgr.common.key.update.receive;

import com.fitmgr.common.encrypt.config.ServiceAppNameCfg;
import com.fitmgr.common.encrypt.constant.UrlConstant;
import com.fitmgr.common.encrypt.enums.KeyUpdateStatEnum;
import com.fitmgr.common.encrypt.kms.MqKmsMessage;
import com.fitmgr.common.encrypt.util.TokenUtil;
import com.fitmgr.common.key.update.config.RabbitConfig;
import com.fitmgr.common.encrypt.enums.MqMsgTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
public class MsgReceiver {

    @Autowired
    private ServiceWorker4Kms serviceWorker4Kms;

    @Autowired
    private RabbitConfig rabbitConfig;

    @Autowired
    protected ServiceAppNameCfg serviceAppNameCfg;

    @Autowired
    protected RestTemplate restTemplate;

    @Bean
    public String[] queueNames(){
        return new String[]{rabbitConfig.getQueueName()};
    }

    @RabbitHandler
    @RabbitListener(queues = {"#{queueNames}"})
    public void process(MqKmsMessage msg) {
        if (serviceWorker4Kms == null) {
            log.warn("No instance to receive KMS MQ message was found");
            return;
        }

        if (!isCurrentService(msg)) {
            return;
        }

        if (!serviceWorker4Kms.isLegalStatus(msg.getMsgType())) {
            return;
        }

        if (MqMsgTypeEnum.UPDATE_MASTER_KEY.getKey().equals(msg.getMsgType())) {
            updateMasterKey(msg);
        } else if(MqMsgTypeEnum.UPDATE_DATA_KEY.getKey().equals(msg.getMsgType())) {
            updateDataKey(msg);
        }
    }

    private void updateMasterKey(MqKmsMessage msg) {
      try {
          // 通知kms,正在更新
          notifyKmsMasterKey(KeyUpdateStatEnum.UPDATING);
          boolean isOk = serviceWorker4Kms.updateMasterKey(msg);
          KeyUpdateStatEnum status = isOk ? KeyUpdateStatEnum.UPDATE_SUC : KeyUpdateStatEnum.UPDATE_FAIL;

          // 更新通知结果
          notifyKmsMasterKey(status);
      } catch (Exception ex) {
          log.error("Failed to update master key, {}", ex.getMessage());
          notifyKmsMasterKey(KeyUpdateStatEnum.UPDATE_FAIL);
      }
    }

    private void updateDataKey(MqKmsMessage msg) {
        try {
            //通知KMS 说自己正在更新秘钥
            notifyKmsService(KeyUpdateStatEnum.UPDATING, msg.getBatchNum());

            serviceWorker4Kms.necessaryWork4DataKey();
            boolean isOk = serviceWorker4Kms.updateDataKey(msg);

            //调用接口通知KMS更新结果
            KeyUpdateStatEnum status = isOk ? KeyUpdateStatEnum.UPDATE_SUC : KeyUpdateStatEnum.UPDATE_FAIL;

            notifyKmsService(status, msg.getBatchNum());
        } catch (Exception ex) {
            log.error("Failed to update data key, {}", ex.getMessage());
            notifyKmsService(KeyUpdateStatEnum.UPDATE_FAIL, msg.getBatchNum());
        }
    }

    private void notifyKmsMasterKey(KeyUpdateStatEnum status) {
        HttpEntity<Map> entity = TokenUtil.buildToken();
        String url = UrlConstant.URL_SERVICE_NOTIFY_TO_KMS_MASTERKEYUPDATESTATUS + status.getKey();
        restTemplate.postForEntity(url, entity, Map.class);
    }

    private void notifyKmsService(KeyUpdateStatEnum status, int batchNum) {
        String appName = serviceAppNameCfg.getApplicationName();
        HttpEntity<Map> entity = TokenUtil.buildToken();
        String url = UrlConstant.URL_SERVICE_NOTIFY_TO_KMS_KEYUPDATESTATUS
                + appName
                + "/" + status.getKey()
                + "/" + batchNum ;
        restTemplate.postForEntity(url, entity, Map.class);
    }


    public boolean isCurrentService(MqKmsMessage msg) {
        log.info("receive msg sender = [{}], receiver = [{}], batch = [{}]", msg.getSendName(), msg.getReceiveName(), msg.getBatchNum());

        // 判断当前广播的消息接收者是否包含自己
        if (!msg.getReceiveName().contains(serviceAppNameCfg.getApplicationName())) {
            return false;
        }

        return true;
    }


}