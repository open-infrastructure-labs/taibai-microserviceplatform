package com.taibai.common.core.interceptor;

import com.taibai.common.core.config.EnvIsolationConfig;
import com.taibai.common.core.constant.CommonConstants;
import com.taibai.common.core.util.EnvIsoUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class EnvIsoRestTemplateInterceptor implements ClientHttpRequestInterceptor {

    @Autowired
    private EnvIsolationConfig envIsolationConfig;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if(envIsolationConfig.getConfEnvIsolationSwitch()) {
            HttpHeaders headers = request.getHeaders();
            if (headers.get(CommonConstants.ENV_HEADER_ENV_KEY) != null) {
                return execution.execute(request, body);
            }
            String headerEnv = EnvIsoUtil.getHeaderEnv();
            String sysEnv = EnvIsoUtil.getSysEnv();
            String producerEnv = EnvIsoUtil.getProducerEnv(request.getURI().getHost());
            if (StringUtils.isNotEmpty(producerEnv)) {
                headers.add(CommonConstants.ENV_HEADER_ENV_KEY, producerEnv);
            } else if (StringUtils.isNotEmpty(headerEnv)) {
                headers.add(CommonConstants.ENV_HEADER_ENV_KEY, headerEnv);
            } else if (StringUtils.isNotEmpty(sysEnv)) {
                headers.add(CommonConstants.ENV_HEADER_ENV_KEY, sysEnv);
            }
        }

        return execution.execute(request, body);
    }
}
