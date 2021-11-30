
package com.fitmgr.common.security.social;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * @author Fitmgr
 * @date 2018/8/16 qq登录配置信息
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "social.qq")
public class QqSocialConfig {
    private String appid;
    private String secret;
}
