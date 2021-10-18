package com.fitmgr.common.core.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 启用状态
 *
 * @author Fitmgr
 * @date 2020/10/21 16:21
 */
@Getter
@AllArgsConstructor
public enum EnableStatusEnum {
    /**
     * 启用状态
     */
    ENABLE(0, "启用"), 
    DISABLE(1, "禁用");

    private Integer status;
    private String msg;

    public static String getMsg(Integer status) {
        for (EnableStatusEnum value : EnableStatusEnum.values()) {
            if (value.getStatus().equals(status)) {
                return value.getMsg();
            }
        }
        return null;
    }
}
