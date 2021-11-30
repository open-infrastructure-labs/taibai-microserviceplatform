
package com.fitmgr.common.security.service.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.fitmgr.admin.api.dto.UserInfo;
import com.fitmgr.admin.api.entity.LdapUser;
import com.fitmgr.admin.api.entity.User;
import com.fitmgr.admin.api.feign.RemoteLdapService;
import com.fitmgr.admin.api.feign.RemoteUserService;
import com.fitmgr.admin.api.vo.AuthVO;
import com.fitmgr.common.core.constant.CommonConstants;
import com.fitmgr.common.core.constant.SecurityConstants;
import com.fitmgr.common.core.util.R;
import com.fitmgr.common.security.exception.InvalidException;
import com.fitmgr.common.security.service.FitmgrUser;
import com.fitmgr.common.security.service.FitmgrUserDetailsService;
import com.fitmgr.common.security.util.AuthUtils;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ldap用户鉴权查询
 *
 * @author Fitmgr
 * @date: 2021年3月23日 上午10:15:34
 */
@Slf4j
@Service("ldapUserDetailsServiceImpl")
@AllArgsConstructor
public class LdapUserDetailsServiceImpl implements FitmgrUserDetailsService {

    private final RemoteUserService remoteUserService;

    private final RemoteLdapService remoteLdapService;

    private final CacheManager cacheManager;

    /**
     * 用户密码登录
     *
     * @param username 用户名
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    @SneakyThrows
    public UserDetails loadUserByUsername(String username) {
        FitmgrUser authfitmgrUser = AuthUtils.getFitmgrUserFromReidsAuthentication();
        Cache cache = cacheManager.getCache("user_details");
        if (cache != null && cache.get(username) != null) {
            FitmgrUser userDetail = (FitmgrUser) cache.get(username).get();
            if (authfitmgrUser != null) {
                userDetail.setDefaultTenantId(authfitmgrUser.getDefaultTenantId());
            }
            return userDetail;
        }

        // 查询ldap用户
        R<LdapUser> r = remoteLdapService.getByUsername(username, SecurityConstants.FROM_IN);
        if (r == null) {
            log.error("[LDAP]获取用户失败，username={}, r=null", username);
            throw new UsernameNotFoundException("用户查询失败");
        }

        if (r.getCode() != 0 || r.getData() == null) {
            log.error("[LDAP]登录用户不存在，username={}", username);
            throw new UsernameNotFoundException("用户不存在");
        }
        R<UserInfo> result = remoteUserService.loginInfo(username, SecurityConstants.FROM_IN);
        if (result == null || result.getData() == null) {
            log.error("[FITMGR]登录用户不存在，username={}", username);
            throw new UsernameNotFoundException("用户不存在");
        }

        // 同步用户（是否需要将LDAP中的用户同步到本地）
        // TODO

        UserDetails userDetails = getUserDetails(result.getData());

        if (authfitmgrUser != null) {
            FitmgrUser userDetail = (FitmgrUser) userDetails;
            userDetail.setDefaultTenantId(authfitmgrUser.getDefaultTenantId());
        }
        cache.put(username, userDetails);

        return userDetails;
    }

    private UserDetails getUserDetails(UserInfo info) {
        Set<String> dbAuthsSet = new HashSet<>();
        dbAuthsSet.add(SecurityConstants.ROLE + "1");
        Collection<? extends GrantedAuthority> authorities = AuthorityUtils
                .createAuthorityList(dbAuthsSet.toArray(new String[0]));
        User user = info.getUser();

        return new FitmgrUser(user.getId(), user.getEmail(), user.getDefaultTenantId(), user.getUsername(),
                SecurityConstants.BCRYPT + user.getPassword(), true, true, true,
                !CommonConstants.STATUS_LOCK.equals(user.getStatus()), authorities);
    }

    /**
     * ldap鉴权
     */
    @Override
    public boolean auth(String username, String password) throws InvalidException {
        try {
            R r = remoteLdapService.auth(username, password, SecurityConstants.FROM_IN);
            if (r == null || r.getCode() != 0) {
                log.error("LDAP 鉴权失败，用户名或密码错误, username={}, r={}", username, r);
                return false;
            }
            log.info("鉴权通过，username=" + username);
            return true;
        } catch (Exception e) {
            log.error("LDAP 鉴权异常，username={}", username, e);
            throw new InvalidException("用户名或密码错误", e);
        }
    }

    @Override
    public UserDetails loadUserBySocial(String code) throws UsernameNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AuthVO getUserByAuth(String code) {
        // TODO Auto-generated method stub
        return null;
    }

}
