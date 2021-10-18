package com.fitmgr.common.security.feign;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName FeignLogConfiguration
 * @Description feign 错误捕获
 * @Author BDWang
 * @Date 2021/6/10 10:01
 **/
@Slf4j
@Configuration
public class FeignLogConfiguration {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new UserErrorDecoder();
    }

    /**
     * 自定义错误
     */
    public class UserErrorDecoder extends ErrorDecoder.Default {

        @Override
        public Exception decode(String s, Response response) {
            Exception exception = null;
            try {
                // response 不会为空
                String url = response.request().url();
                // exception 不会为空 返回的是FeignException的子类
                exception = super.decode(s, response);
                // 此错误处理不会捕获超时这种异常，如果下面这行代码没有打印，则有超时的可能
                log.error(String.format("error class : %s , url : %s , error msg : %s", s, url, exception.getMessage()), exception);
            } catch (Exception ex) {
                log.error(String.format("decode method error : %s", ex.getMessage()), ex);
            }
            return exception;
        }
    }
}
