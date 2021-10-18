
package com.fitmgr.common.core.config;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.fitmgr.common.core.interceptor.EnvIsoRestTemplateInterceptor;

/**
 * @author Fitmgr
 * @date 2018/8/16 RestTemplate
 */
@Configuration
public class RestTemplateConfig {

    @Autowired
    EnvIsoRestTemplateInterceptor envIsoRestTemplateInterceptor;

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(envIsoRestTemplateInterceptor));
        return restTemplate;
    }
}
