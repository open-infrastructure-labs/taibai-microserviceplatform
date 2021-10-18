package com.fitmgr.common.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InternalAdminConfig {

    @Value("${internal-admin.account}")
    private String internalUserName;

    @Value("${internal-admin.password}")
    private String internalUserPass;

    public String getInternalUserName() {
        return internalUserName;
    }

    public String getInternalUserPass() {
        return internalUserPass;
    }
}
