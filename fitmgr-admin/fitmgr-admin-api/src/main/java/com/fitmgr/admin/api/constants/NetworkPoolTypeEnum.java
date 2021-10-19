package com.fitmgr.admin.api.constants;

import lombok.Getter;

@Getter
public enum NetworkPoolTypeEnum {
    /**
     * 网络池分域资源类型
     */
    SP_IP("spip", "浮动IP"), 
    IP_SUBNET("ipsubnet", "云主机IP"), 
    VLAN("vlan", "VLAN");

    String code;

    String desc;

    NetworkPoolTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
