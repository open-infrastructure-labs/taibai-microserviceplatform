package com.taibai.admin.cache;

import com.netflix.discovery.converters.Auto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

@Slf4j
@Configuration
@EnableScheduling
public class UpdateFunctionCache implements SchedulingConfigurer {

    @Autowired
    private FunctionCache functionCache;

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.addTriggerTask(() -> process(),
                triggerContext -> {
                    return new CronTrigger("0/5 * * * * ?").nextExecutionTime(triggerContext);
                }
        );
    }

    private void process(){
        functionCache.updateCache();
    }
}
