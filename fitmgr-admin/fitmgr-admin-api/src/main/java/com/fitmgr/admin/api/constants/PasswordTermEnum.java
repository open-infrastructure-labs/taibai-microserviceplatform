package com.fitmgr.admin.api.constants;

import lombok.Getter;

@Getter
public enum PasswordTermEnum {

    /**
     * 密码有效期
     */
    ALWAYS("0", "一直有效"), 
    AWEEK("1", "1周"), 
    ONE_MONTH("2", "1个月"), 
    THREE_MONTHS("3", "3个月"), 
    HALF_YEAR("4", "半年"),
    AYEAR("5", "1年");

    PasswordTermEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    String code;

    String desc;

    public static String getValue(String code) {
        for (PasswordTermEnum ele : values()) {
            if (ele.getCode().equals(code)) {
                return ele.getDesc();
            }
        }
        return null;
    }
}
