package com.taibai.common.core.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 折扣应用范围
 *
 * @author Taibai
 * @date 2020/10/21 16:35
 */
@Getter
@AllArgsConstructor
public enum DiscountRangeEnum {
    /**
     * 折扣应用范围
     */
    SYSTEM_RANGE(1, "系统范围"), 
    TENANT_RANGE(2, "VDC范围"), 
    PROJECT_RANGE(3, "PROJECT范围");

    private Integer status;
    private String msg;
}
