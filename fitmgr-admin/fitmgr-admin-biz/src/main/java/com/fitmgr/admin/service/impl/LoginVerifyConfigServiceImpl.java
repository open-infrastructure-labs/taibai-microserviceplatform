package com.fitmgr.admin.service.impl;

import com.fitmgr.admin.api.entity.LoginVerifyConfig;
import com.fitmgr.admin.mapper.LoginVerifyConfigMapper;
import com.fitmgr.admin.service.LoginVerifyConfigService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class LoginVerifyConfigServiceImpl implements LoginVerifyConfigService {

    private final LoginVerifyConfigMapper loginVerifyConfigMapper;

    @Override
    public LoginVerifyConfig queryConfig() {
        return loginVerifyConfigMapper.selectById(1);
    }

    @Override
    public void updateConfig(LoginVerifyConfig loginVerifyConfig) {
        loginVerifyConfig.setId(1);
        loginVerifyConfig.setUpdateTime(LocalDateTime.now());
        loginVerifyConfigMapper.updateById(loginVerifyConfig);
    }
}
