package com.taibai.admin.service.impl;

import com.taibai.admin.api.entity.SessionConfig;
import com.taibai.admin.exceptions.UserCenterException;
import com.taibai.admin.mapper.SessionConfigMapper;
import com.taibai.admin.service.ISessionConfigService;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class SessionConfigServiceImpl implements ISessionConfigService {

    private final SessionConfigMapper sessionConfigMapper;

    private final RedisTemplate linkRedisTemplate;

    private static final String SESSION_CONFIG_CACHE_KEY = "session_config:id:1";

    private static final int MIN_SESSION_MINUTES = 1;

    private static final int MAX_SESSION_MINUTES = 10080;

    @Override
    public SessionConfig querySessionConfig() {
        ValueOperations<String,Object> operations = linkRedisTemplate.opsForValue();
        SessionConfig sessionConfig = (SessionConfig)operations.get(SESSION_CONFIG_CACHE_KEY);
        if(sessionConfig != null && sessionConfig.getCheckHeartbeat() != null) {
            return sessionConfig;
        }
        sessionConfig = sessionConfigMapper.selectById(1);
        linkRedisTemplate.expire(SESSION_CONFIG_CACHE_KEY, 1, TimeUnit.DAYS);
        operations.set(SESSION_CONFIG_CACHE_KEY, sessionConfig);
        return sessionConfig;
    }

    @Override
    public void modifySessionConfig(SessionConfig sessionConfig) {
        if(sessionConfig.getMultiClients() == null && sessionConfig.getSessionMaxValidMinutes() == null) {
            throw new UserCenterException("参数错误");
        }
        if(sessionConfig.getSessionMaxValidMinutes() != null) {
            if(sessionConfig.getSessionMaxValidMinutes() < MIN_SESSION_MINUTES
                    || sessionConfig.getSessionMaxValidMinutes() > MAX_SESSION_MINUTES) {
                throw new UserCenterException("会话时长参数错误");
            }
        }
        sessionConfig.setId(1);
        sessionConfig.setUpdateTime(LocalDateTime.now());
        sessionConfigMapper.updateById(sessionConfig);
        linkRedisTemplate.delete(SESSION_CONFIG_CACHE_KEY);
    }
}
