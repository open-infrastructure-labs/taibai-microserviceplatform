package com.fitmgr.admin.api.constants;

import lombok.Getter;

@Getter
public enum ProjectStatusEnum {
    /**
     * 项目状态
     */
    ENABLE("0", "启用"), 
    DISABLE("1", "禁用");

    ProjectStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    String code;

    String desc;
}
