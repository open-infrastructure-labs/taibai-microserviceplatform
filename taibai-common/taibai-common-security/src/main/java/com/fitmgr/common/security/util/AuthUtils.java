
package com.fitmgr.common.security.util;

import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fitmgr.common.core.constant.SecurityConstants;
import com.fitmgr.common.core.util.SpringContextHolder;
import com.fitmgr.common.security.service.FitmgrUser;

import cn.hutool.core.codec.Base64;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Fitmgr
 * @date 2018/5/13 认证授权相关工具类
 */
@Slf4j
@UtilityClass
public class AuthUtils {
    public static final String BASIC_ = "Basic ";
    public static final String BEARER = "Bearer ";
    public static final String UNKNOWN = "unknown";

    private RedisTemplate linkRedisTemplate;

    /**
     * 从header 请求中的clientId/clientsecect
     *
     * @param header header中的参数
     * @throws RuntimeException if the Basic header is not present or is not valid
     *                          Base64
     */
    @SneakyThrows
    public String[] extractAndDecodeHeader(String header) {

        byte[] base64Token = header.substring(6).getBytes("UTF-8");
        byte[] decoded;
        try {
            decoded = Base64.decode(base64Token);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Failed to decode basic authentication token");
        }

        String token = new String(decoded, StandardCharsets.UTF_8);

        int delim = token.indexOf(":");

        if (delim == -1) {
            throw new RuntimeException("Invalid basic authentication token");
        }
        return new String[] { token.substring(0, delim), token.substring(delim + 1) };
    }

    /**
     * *从header 请求中的clientId/clientsecect
     *
     * @param request
     * @return
     */
    @SneakyThrows
    public String[] extractAndDecodeHeader(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header == null || !header.startsWith(BASIC_)) {
            throw new RuntimeException("请求头中client信息为空");
        }

        return extractAndDecodeHeader(header);
    }

    public String getClientIpAddress() {
        return getClientIp(getHttpServletRequest());
    }

    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    public HttpServletRequest getHttpServletRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        return ((ServletRequestAttributes) requestAttributes).getRequest();
    }

    public HttpSession getSession() {
        return getHttpServletRequest().getSession(false);
    }

    public String getSessonId() {
        return getSession().getId();
    }

    public String getToken() {
        try {
            String authorization = getHttpServletRequest().getHeader("Authorization");
            if (StringUtils.isNotEmpty(authorization)) {
                return authorization.substring(BEARER.length());
            }
        } catch (Throwable th) {
            log.error("getToken fail", th);
        }
        return null;
    }

    public OAuth2Authentication getOauth2AuthenticationFromRedis() {
        try {
            String token = getToken();
            if (StringUtils.isNotEmpty(token)) {
                if (linkRedisTemplate == null) {
                    linkRedisTemplate = SpringContextHolder.getBean("linkRedisTemplate");
                }
                ValueOperations<String, Object> valueOperations = linkRedisTemplate.opsForValue();
                return (OAuth2Authentication) valueOperations
                        .get(SecurityConstants.FITMGR_OAUTH_PREFIX + ":auth:" + token);
            }
        } catch (Throwable th) {
            log.error("getOAuth2AuthenticationFromRedis fail", th);
        }
        return null;
    }

    public OAuth2Authentication getOauth2AuthenticationFromRedis(String token) {
        try {
            if (StringUtils.isNotEmpty(token)) {
                if (linkRedisTemplate == null) {
                    linkRedisTemplate = SpringContextHolder.getBean("linkRedisTemplate");
                }
                ValueOperations<String, Object> valueOperations = linkRedisTemplate.opsForValue();
                return (OAuth2Authentication) valueOperations
                        .get(SecurityConstants.FITMGR_OAUTH_PREFIX + ":auth:" + token);
            }
        } catch (Throwable th) {
            log.error("getOAuth2AuthenticationFromRedis fail", th);
        }
        return null;
    }

    public FitmgrUser getFitmgrUserFromReidsAuthentication() {
        OAuth2Authentication oAuth2Authentication = getOauth2AuthenticationFromRedis();
        if (oAuth2Authentication != null) {
            return (FitmgrUser) oAuth2Authentication.getPrincipal();
        }
        return null;
    }

    public FitmgrUser getFitmgrUserFromReidsAuthentication(String token) {
        OAuth2Authentication oAuth2Authentication = getOauth2AuthenticationFromRedis(token);
        if (oAuth2Authentication != null) {
            return (FitmgrUser) oAuth2Authentication.getPrincipal();
        }
        return null;
    }

}
