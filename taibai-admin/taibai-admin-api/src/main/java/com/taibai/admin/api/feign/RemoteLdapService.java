package com.taibai.admin.api.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.taibai.admin.api.config.AdminFeignConfig;
import com.taibai.admin.api.entity.LdapConfig;
import com.taibai.admin.api.entity.LdapUser;
import com.taibai.common.core.constant.SecurityConstants;
import com.taibai.common.core.constant.ServiceNameConstants;
import com.taibai.common.core.util.R;

/**
 * 
 * Ldap服务
 *
 * @author Taibai
 * @date: 2021年8月9日 上午9:58:02
 */
@FeignClient(contextId = "remoteLdapService", value = ServiceNameConstants.UMPS_SERVICE, configuration = AdminFeignConfig.class)
public interface RemoteLdapService {

    /**
     * 
     * 根据用户名查询用户是否存在
     *
     * @param username
     * @param from
     * @return
     */
    @GetMapping(value = { "/ldap/getByUsername" })
    R<LdapUser> getByUsername(@RequestParam(value = "username") String username,
            @RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 
     * ldap鉴权
     *
     * @param username
     * @param password
     * @param from
     * @return
     */
    @PostMapping(value = { "/ldap/auth" })
    R auth(@RequestParam(value = "username") String username, @RequestParam(value = "password") String password,
            @RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 
     * ldap鉴权
     *
     * @param username
     * @param password
     * @param from
     * @return
     */
    @GetMapping(value = { "/ldap/getConfigInfo" })
    R<LdapConfig> getConfig(@RequestHeader(SecurityConstants.FROM) String from);

}
