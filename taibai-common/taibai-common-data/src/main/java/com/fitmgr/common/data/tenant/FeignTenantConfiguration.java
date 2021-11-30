
package com.taibai.common.data.tenant;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.RequestInterceptor;

/**
 * @author Taibai
 * @date 2018/9/14 feign 租户信息拦截
 */
@Configuration
public class FeignTenantConfiguration {
    @Bean
    public RequestInterceptor pigxFeignTenantInterceptor() {
        return new FeignTenantInterceptor();
    }
}
