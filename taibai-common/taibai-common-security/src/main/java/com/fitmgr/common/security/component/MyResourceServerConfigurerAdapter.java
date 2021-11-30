
package com.fitmgr.common.security.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.web.client.RestTemplate;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Fitmgr
 * @date 2018/6/22
 *       <p>
 *       1. 支持remoteTokenServices 负载均衡 2. 支持 获取用户全部信息
 */
@Slf4j
public class MyResourceServerConfigurerAdapter extends
        org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter {
    @Autowired
    protected ResourceAuthExceptionEntryPoint resourceAuthExceptionEntryPoint;
    @Autowired
    protected RemoteTokenServices remoteTokenServices;
    @Autowired
    private PermitAllUrlProperties permitAllUrlProperties;
    @Autowired
    private RestTemplate lbRestTemplate;

    /**
     * 默认的配置，对外暴露
     *
     * @param httpSecurity
     */
    @Override
    @SneakyThrows
    public void configure(HttpSecurity httpSecurity) {
        // 允许使用iframe 嵌套，避免swagger-ui 不被加载的问题
        httpSecurity.headers().frameOptions().disable();
        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry = httpSecurity
                .authorizeRequests();
        permitAllUrlProperties.getIgnoreUrls().forEach(url -> registry.antMatchers(url).permitAll());
        registry.anyRequest().authenticated().and().csrf().disable();
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        DefaultAccessTokenConverter accessTokenConverter = new DefaultAccessTokenConverter();
        org.springframework.security.oauth2.provider.token.UserAuthenticationConverter userTokenConverter = new FitmgrUserAuthenticationConverter();
        accessTokenConverter.setUserTokenConverter(userTokenConverter);

        remoteTokenServices.setRestTemplate(lbRestTemplate);
        remoteTokenServices.setAccessTokenConverter(accessTokenConverter);
        resources.authenticationEntryPoint(resourceAuthExceptionEntryPoint).tokenServices(remoteTokenServices);
    }
}
