package com.taibai.admin.syncproject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableScheduling
public class ProjectSyncScheduleTask implements SchedulingConfigurer {

    private final String T = "true";

    @Value("${projectsync.schedule.cron}")
    private String cronConfig;

    @Value("${projectsync.isSync}")
    private String isSync;

    @Autowired
    private ProjectSyncTask projectSyncTask;

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        if (!T.equals(isSync)) {
            log.info("isSync={}", isSync);
            return;
        }
        scheduledTaskRegistrar.addTriggerTask(() -> process(), triggerContext -> {
            return new CronTrigger("0 0 0/1 * * ?").nextExecutionTime(triggerContext);
        });
    }

    private void process() {
        projectSyncTask.sync();
    }
}
