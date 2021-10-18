
package com.fitmgr.common.security.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.fitmgr.admin.api.dto.UserInfo;
import com.fitmgr.admin.api.entity.User;
import com.fitmgr.admin.api.feign.RemoteAuthService;
import com.fitmgr.admin.api.feign.RemoteTenantService;
import com.fitmgr.admin.api.feign.RemoteUserService;
import com.fitmgr.admin.api.vo.AuthVO;
import com.fitmgr.common.core.constant.CommonConstants;
import com.fitmgr.common.core.constant.SecurityConstants;
import com.fitmgr.common.core.util.R;
import com.fitmgr.common.security.exception.InvalidException;
import com.fitmgr.common.security.util.AuthUtils;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户详细信息
 *
 * @author Fitmgr
 */
@Slf4j
@Service("fitmgrUserDetailsServiceImpl")
@AllArgsConstructor
public class FitmgrUserDetailsServiceImpl implements FitmgrUserDetailsService {

    private final RemoteUserService remoteUserService;
    private final CacheManager cacheManager;
    private final RemoteAuthService remoteAuthService;
    private final RemoteTenantService remoteTenantService;

    /**
     * 用户密码登录
     *
     * @param username 用户名
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        FitmgrUser authfitmgrUser = AuthUtils.getFitmgrUserFromReidsAuthentication();
        Cache cache = cacheManager.getCache("user_details");
        if (cache != null && cache.get(username) != null) {
            try {
                FitmgrUser userDetail = (FitmgrUser) Objects.requireNonNull(cache.get(username)).get();
                if (userDetail != null) {
                    if (authfitmgrUser != null) {
                        userDetail.setDefaultTenantId(authfitmgrUser.getDefaultTenantId());
                    }
                    return userDetail;
                }
            } catch (Throwable ex) {
                log.error("get user fail", ex);
            }
        }

        R<UserInfo> result = remoteUserService.loginInfo(username, SecurityConstants.FROM_IN);
        if (result.getCode() != 0 || result.getData() == null) {
            log.error("user not found. username={} result={}", username, JSON.toJSONString(result));
        }
        UserDetails userDetails = getUserDetails(result);
        if (authfitmgrUser != null) {
            FitmgrUser userDetail = (FitmgrUser) userDetails;
            userDetail.setDefaultTenantId(authfitmgrUser.getDefaultTenantId());
        }
        if (cache != null) {
            cache.put(username, userDetails);
        }
        return userDetails;
    }

    /**
     * 根据社交登录code 登录
     *
     * @param inStr TYPE@CODE
     * @return UserDetails
     * @throws UsernameNotFoundException
     */
    @Override
    @SneakyThrows
    public UserDetails loadUserBySocial(String inStr) {
        return getUserDetails(remoteUserService.social(inStr, SecurityConstants.FROM_IN));
    }

    /**
     * 当前用户通过功能code获取权限
     *
     * @param code 功能的唯一編碼
     * @return
     */
    @Override
    public AuthVO getUserByAuth(String code) {
        return remoteAuthService.getUserAuth(code);
    }

    /**
     * 构建userdetails
     *
     * @param result 用户信息
     * @return
     */
    private UserDetails getUserDetails(R<UserInfo> result) {
        if (result == null || result.getData() == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        UserInfo info = result.getData();

        Set<String> dbAuthsSet = new HashSet<>();
        dbAuthsSet.add(SecurityConstants.ROLE + "1");
        Collection<? extends GrantedAuthority> authorities = AuthorityUtils
                .createAuthorityList(dbAuthsSet.toArray(new String[0]));
        User user = info.getUser();
        // 构造security用户

        return new FitmgrUser(user.getId(), user.getEmail(), user.getDefaultTenantId(), user.getUsername(),
                SecurityConstants.BCRYPT + user.getPassword(), true, true, true,
                !CommonConstants.STATUS_LOCK.equals(user.getStatus()), authorities);
    }

    @Override
    public boolean auth(String username, String password) throws InvalidException {
        return false;
    }
}
