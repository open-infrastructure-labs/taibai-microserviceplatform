
package com.fitmgr.common.log;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import com.fitmgr.common.log.aspect.SysLogAspect;
import com.fitmgr.common.log.event.SysLogListener;
import com.fitmgr.log.api.feign.RemoteOperateLogService;

import lombok.AllArgsConstructor;

/**
 * @author Fitmgr
 * @date 2019/11/27
 *       <p>
 *       日志自动配置
 */
@EnableAsync
@Configuration
@AllArgsConstructor
@ConditionalOnWebApplication
public class LogAutoConfiguration {
    private final RemoteOperateLogService remoteOperateLogService;

    @Bean
    public SysLogListener sysLogListener() {
        return new SysLogListener(remoteOperateLogService);
    }

    @Bean
    public SysLogAspect sysLogAspect(ApplicationEventPublisher publisher) {
        return new SysLogAspect(publisher);
    }
}
