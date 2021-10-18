package com.fitmgr.common.encrypt.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * mq 消息类型
 */

@Getter
@AllArgsConstructor
public enum MqMsgTypeEnum {

    // 更新数据密钥
    UPDATE_DATA_KEY("update_data_key"),

    // 更新主密钥
    UPDATE_MASTER_KEY("update_master_key"),

    UNKNOWN("Unknown");

    private String key;
}
