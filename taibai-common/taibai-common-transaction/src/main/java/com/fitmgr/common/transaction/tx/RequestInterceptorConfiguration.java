
package com.fitmgr.common.transaction.tx;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fitmgr.common.transaction.tx.springcloud.feign.TransactionRestTemplateInterceptor;

import feign.RequestInterceptor;

/**
 * @author Fitmgr
 * @date 2018/1/18
 * @since 4.1.0
 */
@Configuration
public class RequestInterceptorConfiguration {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new TransactionRestTemplateInterceptor();
    }
}
