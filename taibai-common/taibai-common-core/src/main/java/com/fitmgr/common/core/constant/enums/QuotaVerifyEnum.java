package com.taibai.common.core.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum QuotaVerifyEnum {
    /**
     * 配额验证
     */
    QUOTA_VERIFY_SUCCESS(0, "通过"), 
    QUOTA_VERIFY_FAIL(1, "不通过");

    private int status;
    private String description;
}
