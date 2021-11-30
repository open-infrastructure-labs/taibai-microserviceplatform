package com.fitmgr.common.key.update.register;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fitmgr.common.encrypt.config.ServiceAppNameCfg;
import com.fitmgr.common.encrypt.constant.UrlConstant;
import com.fitmgr.common.encrypt.util.TokenUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RegisterToKms implements ApplicationRunner {

    /**
     * 程序启动后,等待相应服务启动后,去给kms服务注册
     */
    private static final int WAIT_TIME_TO_KMS = 20 * 60 * 1000;

    private static final int RETRY_CNT = 3;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ServiceAppNameCfg serviceAppNameCfg;

    @Override
    public void run(ApplicationArguments args) {
        int retryCount = 0;
        while (true) {
            if (work()) {
                log.info("The service has successfully registered {} times for kms。", retryCount);
                break;
            } else {
                retryCount++;
                if (retryCount >= RETRY_CNT) {
                    log.error("End register! The service has failed registered {} times for kms。", RETRY_CNT);
                    break;
                }
            }
        }
    }

    private boolean work() {
        try {
            String appName = serviceAppNameCfg.getApplicationName();
            log.info("waiting {}ms latter, {} will register msg to KMS", WAIT_TIME_TO_KMS, appName);
            Thread.sleep(WAIT_TIME_TO_KMS);
            String url = UrlConstant.URL_SERVICE_REGISTER_TO_KMS + appName;
            HttpEntity<Map> entity = TokenUtil.buildToken();
            restTemplate.postForEntity(url, entity, Map.class);
        } catch (Exception ex) {
            log.error("The registration service request failed after the App started! {}", ex.getMessage());
            return false;
        }
        return true;
    }
}
