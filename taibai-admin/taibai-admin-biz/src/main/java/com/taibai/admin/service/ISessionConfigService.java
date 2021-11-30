package com.taibai.admin.service;

import com.taibai.admin.api.entity.SessionConfig;

public interface ISessionConfigService {
    /**
     * querySessionConfig
     * 
     * @return SessionConfig
     */
    SessionConfig querySessionConfig();

    /**
     * modifySessionConfig
     * 
     * @param sessionConfig sessionConfig
     */
    void modifySessionConfig(SessionConfig sessionConfig);
}
