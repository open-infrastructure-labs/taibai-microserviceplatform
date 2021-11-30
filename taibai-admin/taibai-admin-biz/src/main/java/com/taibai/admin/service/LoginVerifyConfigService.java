package com.taibai.admin.service;

import com.taibai.admin.api.entity.LoginVerifyConfig;

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
