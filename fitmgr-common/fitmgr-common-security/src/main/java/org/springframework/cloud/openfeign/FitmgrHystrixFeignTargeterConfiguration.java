
package org.springframework.cloud.openfeign;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import feign.hystrix.HystrixFeign;

/**
 * @author Fitmgr
 *         <p>
 *         HystrixFeignTargeter 配置
 */
@Configuration
@ConditionalOnClass(HystrixFeign.class)
@ConditionalOnProperty("feign.hystrix.enabled")
public class FitmgrHystrixFeignTargeterConfiguration {

    @Bean
    @Primary
    public Targeter linkFeignTargeter() {
        return new FitmgrHystrixTargeter();
    }
}
