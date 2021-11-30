
package com.fitmgr.common.security.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.fitmgr.admin.api.vo.AuthVO;
import com.fitmgr.common.security.exception.InvalidException;

/**
 * @author Fitmgr
 * @date 2018/8/15
 */
public interface FitmgrUserDetailsService extends UserDetailsService {

    /**
     * 第三方鉴权
     * 
     * @param username username
     * @param password password
     * @return boolean
     * @throws InvalidException
     */
    boolean auth(String username, String password) throws InvalidException;

    /**
     * 根据社交登录code 登录
     *
     * @param code TYPE@CODE
     * @return UserDetails
     * @throws UsernameNotFoundException
     */
    UserDetails loadUserBySocial(String code) throws UsernameNotFoundException;

    /**
     * 当前用户通过功能code获取权限
     *
     * @param code 功能的唯一編碼
     * @return
     */
    AuthVO getUserByAuth(String code);

}
