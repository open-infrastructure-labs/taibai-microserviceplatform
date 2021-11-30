package com.taibai.admin.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.Logger;

/**
 * @author Taibai
 * @date 2020/5/26 17:34
 */
@Configuration
public class AdminFeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

}
