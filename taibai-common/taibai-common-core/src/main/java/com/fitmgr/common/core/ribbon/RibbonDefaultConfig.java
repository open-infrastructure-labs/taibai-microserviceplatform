package com.fitmgr.common.core.ribbon;

import com.fitmgr.common.core.config.EnvIsolationConfig;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RoundRobinRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RibbonDefaultConfig {

    @Autowired
    private EnvIsolationConfig envIsolationConfig;

    @Bean
    public IRule ribbonRule() {
        System.out.println("envIsolationConfig.getConfEnvIsolationSwitch()=" + envIsolationConfig.getConfEnvIsolationSwitch() + " envIsolationConfig.getLogSwitch()=" + envIsolationConfig.getLogSwitch());
        if(envIsolationConfig.getConfEnvIsolationSwitch()) {
            return new FhRibbonRule();
        } else {
            return new RoundRobinRule();
        }
    }
}
