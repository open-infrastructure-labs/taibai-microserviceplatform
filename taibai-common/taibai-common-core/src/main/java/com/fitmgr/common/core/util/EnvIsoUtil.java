package com.taibai.common.core.util;

import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.taibai.common.core.constant.CommonConstants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnvIsoUtil {

    public static String getHeaderEnv() {
        try {
            RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
            if (requestAttributes != null) {
                HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
                return request.getHeader(CommonConstants.ENV_HEADER_ENV_KEY);
            }
        } catch (Throwable th) {
            log.error("getHeaderEnv fail", th);
        }
        return null;
    }

    public static String getProducerEnv(String serviceName) {
        Properties properties = System.getProperties();
        if (!properties.isEmpty()) {
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                String producerKey = entry.getKey().toString();
                if (producerKey.startsWith("Producer-")) {
                    if (producerKey.substring("Producer-".length()).equalsIgnoreCase(serviceName)) {
                        return entry.getValue().toString();
                    }
                }
            }
        }
        return null;
    }

    public static String getSysEnv() {
        return System.getenv(CommonConstants.ENV_SYS_ENV_KEY);
    }

    public static String getEnvRegisterIp() {
        return System.getenv(CommonConstants.ENV_REGISTER_IP_KEY);
    }

    public static String getEnv(String serviceName) {
        String env = null;
        if (StringUtils.isNotEmpty(serviceName)) {
            env = getProducerEnv(serviceName);
        }
        if (StringUtils.isEmpty(env)) {
            env = getHeaderEnv();
        }
        if (StringUtils.isEmpty(env)) {
            env = getSysEnv();
        }
        return env;
    }
}
