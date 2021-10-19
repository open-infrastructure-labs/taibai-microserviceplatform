package com.fitmgr.admin.service;

import com.fitmgr.admin.api.entity.LoginVerifyConfig;

public interface LoginVerifyConfigService {
    /**
     * queryConfig
     * 
     * @return LoginVerifyConfig
     */
    LoginVerifyConfig queryConfig();

    /**
     * updateConfig
     * 
     * @param loginVerifyConfig loginVerifyConfig
     */
    void updateConfig(LoginVerifyConfig loginVerifyConfig);
}
