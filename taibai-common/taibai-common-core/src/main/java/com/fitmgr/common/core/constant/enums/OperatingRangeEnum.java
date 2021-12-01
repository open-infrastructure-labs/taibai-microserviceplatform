package com.taibai.common.core.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OperatingRangeEnum {
    /**
     * 操作范围
     */
    ALL("0", "全局"), 
    TENANT("1", "VDC"), 
    PROJECT("2", "项目"), 
    SELF("3", "自己");

    private String code;

    private String desc;

    public static final String ALL_CODE = "0";

    public static final String TENANT_CODE = "1";

    public static final String PROJECT_CODE = "2";

    public static final String SELF_CODE = "3";
}
