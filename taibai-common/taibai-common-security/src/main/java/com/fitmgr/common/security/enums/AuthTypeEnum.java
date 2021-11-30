package com.fitmgr.common.security.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * 登录类型枚举
 *
 * @author Fitmgr
 * @date: 2021年3月8日 下午5:01:29
 */
public enum AuthTypeEnum {
    /**
     * 登录类型
     */
    LOCAL_USER(0, "fitmgrUserDetailsServiceImpl", "本地用户"), LDAP_USER(1, "ldapUserDetailsServiceImpl", "LDAP用户");

    private int code;

    private String service;

    private String desc;

    private AuthTypeEnum(int code, String service, String desc) {
        this.code = code;
        this.service = service;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String getService() {
        return service;
    }

    /**
     * 
     * 是否本地用户
     *
     * @param type
     * @return
     */
    public static boolean isLocalUser(String type) {
        return String.valueOf(LOCAL_USER.getCode()).equals(type);
    }

    /**
     * 
     * 是否LDAP用户
     *
     * @param type
     * @return
     */
    public static boolean isLdapUser(String type) {
        return String.valueOf(LDAP_USER.getCode()).equals(type);
    }

    /**
     * 
     * 根据类型返回实现类
     *
     * @param type
     * @return
     */
    public static String getServiceByCode(String type) {
        if (StringUtils.isBlank(type)) {
            return null;
        }
        for (AuthTypeEnum t : AuthTypeEnum.values()) {
            if (String.valueOf(t.getCode()).equals(type)) {
                return t.getService();
            }
        }
        return null;
    }

}
