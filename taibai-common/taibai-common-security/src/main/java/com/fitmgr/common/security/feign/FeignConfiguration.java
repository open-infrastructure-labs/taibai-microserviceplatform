
package com.taibai.common.security.feign;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.security.oauth2.client.AccessTokenContextRelay;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;

import feign.Feign;
import feign.RequestInterceptor;

/**
 * fegin 配置增强
 *
 * @author Taibai
 */
@Configuration
@ConditionalOnClass(Feign.class)
public class FeignConfiguration {

    @Bean
    @ConditionalOnProperty("security.oauth2.client.client-id")
    public RequestInterceptor oauth2FeignRequestInterceptor(OAuth2ClientContext oAuth2ClientContext,
            OAuth2ProtectedResourceDetails resource, AccessTokenContextRelay accessTokenContextRelay) {
        return new FeignClientInterceptor(oAuth2ClientContext, resource, accessTokenContextRelay);
    }

}
