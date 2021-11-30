package com.taibai.admin.api.constants;

import lombok.Getter;

@Getter
public enum NetworkPoolEnum {
    /**
     * 网络池分域
     */
    ACTION_CONFIG("config", "查看分域配置"), 
    CONFIG_SWITCH_ON("1", "打开"), 
    CONFIG_SWITCH_OFF("0", "关闭"), 
    CHOICE_ON("1", "已选"),
    CHOICE_OFF("0", "未选");

    String code;

    String desc;

    NetworkPoolEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
