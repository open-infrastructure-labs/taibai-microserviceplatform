package com.fitmgr.common.encrypt.enums;

import lombok.Getter;
@Getter
public enum SecretKeyType {
    /**
     * 密钥类型
     */
    MAIN_SECRET("1", "主密钥"),
    DATA_SECRET("2", "数据密钥"),
    ROOT_SECRET("0", "根秘钥");

    SecretKeyType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    String code;

    String desc;
}
