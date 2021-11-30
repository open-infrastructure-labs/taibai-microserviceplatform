package com.taibai.admin.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.extern.slf4j.Slf4j;

/**
 * 异步配置.
 *
 * @date: 2020-07-21
 * @version: 1.0
 * @author Taibai
 */
@Slf4j
@Configuration
public class ThreadConfig {

    @Bean
    public Executor asyncExecutor() {
        log.info("-----------------------Executor---------start");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-service-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        log.info("-----------------------Executor----initialize-----end");
        return executor;
    }
}
