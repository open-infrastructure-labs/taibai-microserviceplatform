package com.fitmgr.common.core.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 折扣类型：0-系统，1-手动
 *
 * @author Fitmgr
 * @date 2020/10/21 16:33
 */
@Getter
@AllArgsConstructor
public enum DiscountTypeEunm {
    /**
     * 折扣类型
     */
    SYSTEM(0, "系统"), 
    MANUAL(1, "手动");

    private Integer status;
    private String msg;

}
