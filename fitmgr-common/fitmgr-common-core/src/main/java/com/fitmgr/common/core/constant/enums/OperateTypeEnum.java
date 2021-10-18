package com.fitmgr.common.core.constant.enums;

import lombok.Getter;

/**
 * @author Fitmgr
 * @date 2020/7/9 19:38
 */
@Getter
public enum OperateTypeEnum {
    /**
     * 操作类型
     */
    PLATFORM("平台类", 1), 
    TENANT("租户类", 2), 
    PROJECT("Project类", 3), 
    OTHER("其它类", 4);

    OperateTypeEnum(String name, Integer code) {
        this.name = name;
        this.code = code;
    }

    String name;

    Integer code;
}
