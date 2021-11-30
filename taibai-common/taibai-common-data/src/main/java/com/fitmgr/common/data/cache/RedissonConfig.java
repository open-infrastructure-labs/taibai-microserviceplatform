package com.taibai.common.data.cache;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.config.Config;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName RedissonConfig
 * @Description redis starter
 * @Author BDWang
 * @Date 2021/6/9 16:09
 **/
@ConditionalOnClass({Redisson.class})
@Configuration
@EnableConfigurationProperties({RedisProperties.class})
@AllArgsConstructor
@Slf4j
public class RedissonConfig implements RedissonAutoConfigurationCustomizer {

    private static final String REDIS_PROTOCOL_PREFIX = "redis://";

    private RedisProperties redisProperties;


    @Override
    public void customize(Config configuration) {
        log.info("RedissonConfig customize host :" + REDIS_PROTOCOL_PREFIX + redisProperties.getHost() + ":" + redisProperties.getPort());
        configuration.useSingleServer()
                .setAddress(REDIS_PROTOCOL_PREFIX + redisProperties.getHost() + ":" + redisProperties.getPort()).setPassword(redisProperties.getPassword());
    }

}
