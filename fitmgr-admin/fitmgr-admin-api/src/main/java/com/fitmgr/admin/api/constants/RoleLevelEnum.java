package com.fitmgr.admin.api.constants;

import lombok.Getter;

@Getter
public enum RoleLevelEnum {
    /**
     * 角色级别
     */
    SYSTEM(1, "系统级别"), 
    TENANT(2, "租户级别"), 
    PROJECT(3, "project级别");

    int code;

    String desc;

    RoleLevelEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getNameByCode(int code) {
        for (RoleLevelEnum r : RoleLevelEnum.values()) {
            if (code == r.getCode()) {
                return r.toString();
            }
        }
        return null;
    }
}
