package com.fitmgr.admin.service;

import com.fitmgr.admin.api.entity.SessionConfig;

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
