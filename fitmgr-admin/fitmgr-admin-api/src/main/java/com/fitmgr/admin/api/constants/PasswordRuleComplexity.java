package com.fitmgr.admin.api.constants;

import lombok.Getter;

@Getter
public enum PasswordRuleComplexity {

    /**
     * 密码规则
     */
    SIMPLE("1", "简单", "且包含大写英文字符、小写英文字符、数字、特殊字符:~!@#$%^&*()_中至少1种"),
    STANDARD("2", "标准", "且包含大写英文字符、小写英文字符、数字、特殊字符:~!@#$%^&*()_中至少2种组合"),
    HIGH("3", "高", "且包含大写英文字符、小写英文字符、数字、特殊字符:~!@#$%^&*()_中至少3种组合"),
    STRONG("4", "强", "且包含大写英文字符、小写英文字符、数字、特殊字符:~!@#$%^&*()_中至少4种组合");

    PasswordRuleComplexity(String code, String desc, String msg) {
        this.code = code;
        this.desc = desc;
        this.msg = msg;
    }

    String code;

    String desc;

    String msg;

    public static String getValue(String code) {
        for (PasswordRuleComplexity ele : values()) {
            if (ele.getCode().equals(code))
                return ele.getDesc();
        }
        return null;
    }

    public static String getMsg(String code) {
        for (PasswordRuleComplexity ele : values()) {
            if (ele.getCode().equals(code))
                return ele.getMsg();
        }
        return null;
    }
}
