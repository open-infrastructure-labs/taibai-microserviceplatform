
package com.fitmgr.admin.handler;

import com.fitmgr.admin.api.dto.UserInfo;

/**
 * @author Fitmgr
 * @date 2018/11/18
 *       <p>
 *       登录处理器
 */
public interface LoginHandler {

    /***
     * 数据合法性校验
     * 
     * @param loginStr 通过用户传入获取唯一标识
     * @return Boolean
     */
    Boolean check(String loginStr);

    /**
     * 通过用户传入获取唯一标识
     *
     * @param loginStr
     * @return String
     */
    String identify(String loginStr);

    /**
     * 通过openId 获取用户信息
     *
     * @param identify
     * @return UserInfo
     */
    UserInfo info(String identify);

    /**
     * 处理方法
     *
     * @param loginStr 登录参数
     * @return UserInfo
     */
    UserInfo handle(String loginStr);
}
