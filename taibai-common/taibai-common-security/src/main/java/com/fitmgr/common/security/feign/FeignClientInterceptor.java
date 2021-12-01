
package com.taibai.common.security.feign;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.security.oauth2.client.AccessTokenContextRelay;
import org.springframework.cloud.security.oauth2.client.feign.OAuth2FeignRequestInterceptor;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.taibai.common.core.config.EnvIsolationConfig;
import com.taibai.common.core.constant.CommonConstants;
import com.taibai.common.core.constant.SecurityConstants;
import com.taibai.common.core.util.EnvIsoUtil;
import com.taibai.common.core.util.SpringContextHolder;
import com.taibai.common.security.util.SecurityUtils;

import cn.hutool.core.collection.CollUtil;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Taibai
 * @date 2018/8/13 扩展OAuth2FeignRequestInterceptor
 */
@Slf4j
public class FeignClientInterceptor extends OAuth2FeignRequestInterceptor {
    private final OAuth2ClientContext oAuth2ClientContext;
    private final AccessTokenContextRelay accessTokenContextRelay;

    private static EnvIsolationConfig envIsolationConfig;

    /**
     * Default constructor which uses the provided OAuth2ClientContext and Bearer
     * tokens within Authorization header
     *
     * @param oAuth2ClientContext     provided context
     * @param resource                type of resource to be accessed
     * @param accessTokenContextRelay
     */
    public FeignClientInterceptor(OAuth2ClientContext oAuth2ClientContext, OAuth2ProtectedResourceDetails resource,
            AccessTokenContextRelay accessTokenContextRelay) {
        super(oAuth2ClientContext, resource);
        this.oAuth2ClientContext = oAuth2ClientContext;
        this.accessTokenContextRelay = accessTokenContextRelay;
    }

    /**
     * Create a template with the header of provided name and extracted extract 1.
     * 如果使用 非web 请求，header 区别 2. 根据authentication 还原请求token
     *
     * @param template
     */
    @Override
    public void apply(RequestTemplate template) {
        template.header(SecurityConstants.HEADER_CALL_MODE, SecurityConstants.INNER_CALL);
        Collection<String> fromHeader = template.headers().get(SecurityConstants.FROM);
        configEnvHeader(template);
        if (CollUtil.isNotEmpty(fromHeader) && fromHeader.contains(SecurityConstants.FROM_IN)) {
            template.header(SecurityConstants.FROM, SecurityConstants.FROM_IN);
            return;
        }

        try {
            accessTokenContextRelay.copyToken();
            if (oAuth2ClientContext != null && oAuth2ClientContext.getAccessToken() != null) {
                super.apply(template);
            }
        } catch (Throwable th) {
            try {
                RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
                if (requestAttributes != null) {
                    HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
                    String token = request.getHeader("Authorization");
                    if (StringUtils.isNotEmpty(token)) {
                        template.header("Authorization", token);
                    } else {
                        configHeaderToken(template);
                    }
                } else {
                    configHeaderToken(template);
                }
            } catch (Throwable th2) {
                configHeaderToken(template);
            }
        }
    }

    private void configHeaderToken(RequestTemplate template) {
        String token = SecurityUtils.createInternalAdminToken();
        if (StringUtils.isEmpty(token)) {
            log.error("internal admin token is empty");
            return;
        }
        template.header("Authorization", "Bearer " + token);
    }

    private void configEnvHeader(RequestTemplate template) {
        if (envIsolationConfig == null) {
            envIsolationConfig = SpringContextHolder.getBean(EnvIsolationConfig.class);
        }
        if (envIsolationConfig.getConfEnvIsolationSwitch()) {
            String headerEnv = EnvIsoUtil.getHeaderEnv();
            String sysEnv = EnvIsoUtil.getSysEnv();
            if (StringUtils.isNotEmpty(headerEnv)) {
                template.header(CommonConstants.ENV_HEADER_ENV_KEY, headerEnv);
            } else if (StringUtils.isNotEmpty(sysEnv)) {
                template.header(CommonConstants.ENV_HEADER_ENV_KEY, sysEnv);
            }
        }
    }
}
