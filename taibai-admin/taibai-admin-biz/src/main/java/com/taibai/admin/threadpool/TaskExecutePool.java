package com.fitmgr.admin.threadpool;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class TaskExecutePool {

    @Bean(name = "threadPoola")
    public ThreadPoolTaskExecutor myTaskAsyncPool() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);

        executor.setMaxPoolSize(8);

        executor.setQueueCapacity(100);

        executor.setKeepAliveSeconds(60);

        executor.setThreadNamePrefix("Pool-A");

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();

        return executor;

    }

}
