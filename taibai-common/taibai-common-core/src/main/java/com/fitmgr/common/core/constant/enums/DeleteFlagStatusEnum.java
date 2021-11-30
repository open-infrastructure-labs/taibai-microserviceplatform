package com.taibai.common.core.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Taibai
 * @date 2020/10/21 14:01
 */
@Getter
@AllArgsConstructor
public enum DeleteFlagStatusEnum {
    /**
     * 删除状态
     */
    VIEW(0, "展示"), 
    DELETE(1, "删除");

    private Integer status;
    private String msg;
}
