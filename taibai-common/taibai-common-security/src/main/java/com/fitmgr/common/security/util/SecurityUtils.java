
package com.taibai.common.security.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.taibai.common.core.config.InternalAdminConfig;
import com.taibai.common.core.constant.SecurityConstants;
import com.taibai.common.core.util.SpringContextHolder;
import com.taibai.common.security.service.FitmgrUser;

import cn.hutool.core.util.StrUtil;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * 安全工具类
 *
 * @author Taibai
 */
@Slf4j
@UtilityClass
public class SecurityUtils {

    private RestTemplate restTemplate;

    private static final String AUTH_SERVICE = "taibai-auth";

    public static final String INTERNAL_ADMIN = "internal_admin";

    private static InternalAdminConfig internalAdminConfig;

    /**
     * 获取Authentication
     */
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 获取用户
     *
     * @param authentication
     * @return TaibaiUser
     *         <p>
     *         获取当前用户的全部信息 EnableFitmgrResourceServer true 获取当前用户的用户名
     *         EnableFitmgrResourceServer false
     */
    public FitmgrUser getUser(Authentication authentication) {
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof FitmgrUser) {
                return (FitmgrUser) principal;
            }
        }
        return null;
    }

    /**
     * 获取用户
     */
    public FitmgrUser getUser() {
        Authentication authentication = getAuthentication();
        return getUser(authentication);
    }

    /**
     * 获取用户角色信息
     *
     * @return 角色集合
     */
    public List<Integer> getRoles() {
        Authentication authentication = getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        List<Integer> roleIds = new ArrayList<>();
        authorities.stream().filter(granted -> StrUtil.startWith(granted.getAuthority(), SecurityConstants.ROLE))
                .forEach(granted -> {
                    String id = StrUtil.removePrefix(granted.getAuthority(), SecurityConstants.ROLE);
                    roleIds.add(Integer.parseInt(id));
                });
        return roleIds;
    }

    public String createInternalAdminToken() {
        if (restTemplate == null) {
            restTemplate = SpringContextHolder.getBean("SecurityInternalRestTemplate");
        }
        if (internalAdminConfig == null) {
            internalAdminConfig = SpringContextHolder.getBean(InternalAdminConfig.class);
        }
        String url = "http://taibai-auth/oauth/token" + "?scope=server&grant_type=password&username="
                + internalAdminConfig.getInternalUserName() + "&password=" + internalAdminConfig.getInternalUserPass();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic dGVzdDp0ZXN0");
        headers.add(SecurityConstants.HEADER_CALL_MODE, SecurityConstants.INNER_CALL);
        HttpEntity<Map> entity = new HttpEntity<>(new HashMap<>(), headers);
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity(url, entity, Map.class);
        if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
            log.error("create internal admin token fail ", JSON.toJSONString(responseEntity));
            return null;
        }

        return (String) responseEntity.getBody().get("access_token");
    }

}
