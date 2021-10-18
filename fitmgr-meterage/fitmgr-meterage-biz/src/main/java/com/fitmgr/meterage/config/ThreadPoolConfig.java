package com.fitmgr.meterage.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置类
 *
 * @author zhangxiaokang
 * @date 2020/10/29 18:08
 */
@Slf4j
@Configuration
public class ThreadPoolConfig {

    private int threadPoolCorePoolSize = 100;

    private int threadPoolMaxPoolSize = 200;

    private int threadPoolQueueCapacity = 500;

    private int threadPoolKeepAliveSeconds = 300;

    @Primary
    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        /** 核心线程数，默认为1 **/
        threadPoolTaskExecutor.setCorePoolSize(threadPoolCorePoolSize);
        /** 最大线程数，默认为Integer.MAX_VALUE **/
        threadPoolTaskExecutor.setMaxPoolSize(threadPoolMaxPoolSize);
        /** 队列最大长度，一般需要设置值: 大于等于notifyScheduledMainExecutor.maxNum；默认为Integer.MAX_VALUE **/
        threadPoolTaskExecutor.setQueueCapacity(threadPoolQueueCapacity);
        /** 线程池维护线程所允许的空闲时间，默认为60s **/
        threadPoolTaskExecutor.setKeepAliveSeconds(threadPoolKeepAliveSeconds);
        /**
         * 线程池对拒绝任务（无线程可用）的处理策略，目前只支持AbortPolicy、CallerRunsPolicy；默认为后者
         *
         * AbortPolicy:直接抛出java.util.concurrent.RejectedExecutionException异常
         * CallerRunsPolicy:主线程直接执行该任务，执行完之后尝试添加下一个任务到线程池中，可以有效降低向线程池内添加任务的速度
         * DiscardOldestPolicy:抛弃旧的任务、暂不支持；会导致被丢弃的任务无法再次被执行
         * DiscardPolicy:抛弃当前任务、暂不支持；会导致被丢弃的任务无法再次被执行
         */
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return threadPoolTaskExecutor;
    }
}
