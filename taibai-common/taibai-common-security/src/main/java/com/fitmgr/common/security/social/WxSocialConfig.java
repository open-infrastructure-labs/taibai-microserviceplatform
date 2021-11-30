
package com.fitmgr.common.security.social;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * @author Fitmgr
 * @date 2018/8/16 微信登录配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "social.wx")
public class WxSocialConfig {
    private String appid;
    private String secret;
}
