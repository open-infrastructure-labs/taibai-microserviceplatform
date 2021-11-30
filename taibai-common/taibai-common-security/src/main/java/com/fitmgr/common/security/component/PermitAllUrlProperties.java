
package com.fitmgr.common.security.component;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * @author Fitmgr
 * @date 2018/11/10
 *       <p>
 *       资源服务器对外直接暴露URL
 */
@Data
@Configuration
@RefreshScope
@ConditionalOnExpression("!'${security.oauth2.client.ignore-urls}'.isEmpty()")
@ConfigurationProperties(prefix = "security.oauth2.client")
public class PermitAllUrlProperties {
    private List<String> ignoreUrls = new ArrayList<>();
}
