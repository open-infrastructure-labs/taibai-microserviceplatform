package com.fitmgr.admin.api.constants;

import lombok.Getter;

@Getter
public enum ProjectDefaultEnum {

    /**
     * 默认创建项目
     */
    YES("0", "是"), 
    NO("1", "不是");

    ProjectDefaultEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    String code;

    String desc;
}
