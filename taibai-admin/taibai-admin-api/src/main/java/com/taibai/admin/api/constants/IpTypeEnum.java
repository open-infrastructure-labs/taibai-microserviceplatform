package com.taibai.admin.api.constants;

import lombok.Getter;

@Getter
public enum IpTypeEnum {
    IP(0, "IP"),
    RANGE(1, "范围"),
    CIDR(2, "cidr");

    int code;

    String desc;

    IpTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
