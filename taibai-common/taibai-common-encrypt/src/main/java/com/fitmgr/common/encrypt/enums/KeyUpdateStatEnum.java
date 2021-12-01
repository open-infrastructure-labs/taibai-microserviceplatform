package com.taibai.common.encrypt.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 
 * 未更新、更新中、更新失败、更新成功
 */

@Getter
@AllArgsConstructor
public enum KeyUpdateStatEnum {
    /**
     * 秘钥更新状态
     */
    REGISTERED("Registered"), 
    PRE_UPDATE("Pre_Update"), 
    UPDATING("Updating"), 
    UPDATE_FAIL("Update_Fail"),
    UPDATE_SUC("Update_Suc"), 
    UNKNOWN("Unknown");

    private String key;
}
