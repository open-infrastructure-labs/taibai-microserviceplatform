package com.taibai.admin.api.constants;

import lombok.Getter;

@Getter
public enum UserLockStatus {
    /**
     * 用户锁定
     */
    LOCK("0", "锁定"), 
    UN_LOCK("1", "解锁"), 
    SUPPORT_LOCK("0", "支持锁定"), 
    UN_SUPPORT_LOCK("1", "不支持锁定"), 
    AUTO_UNLOCK("0", "自动解锁"),
    HAND_UNLOCK("1", "手动解锁");

    String code;

    String desc;

    UserLockStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
