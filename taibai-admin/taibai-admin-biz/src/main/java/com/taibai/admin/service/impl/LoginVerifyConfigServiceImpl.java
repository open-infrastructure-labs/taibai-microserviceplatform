package com.taibai.admin.service.impl;

import com.taibai.admin.api.entity.LoginVerifyConfig;
import com.taibai.admin.mapper.LoginVerifyConfigMapper;
import com.taibai.admin.service.LoginVerifyConfigService;
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
