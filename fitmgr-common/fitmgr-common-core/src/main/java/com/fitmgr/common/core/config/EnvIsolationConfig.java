package com.fitmgr.common.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvIsolationConfig {

    @Value("${env-isolation.switch}")
    private Boolean confEnvIsolationSwitch;

    @Value("${env-isolation.log-switch}")
    private Boolean logSwitch;

    public Boolean getConfEnvIsolationSwitch() {
        return confEnvIsolationSwitch;
    }

    public Boolean getLogSwitch() {
        return logSwitch;
    }
}
