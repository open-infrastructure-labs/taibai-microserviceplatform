package com.fitmgr.meterage.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 查询费用类型
 *
 * @author zhangxiaokang
 * @date 2020/11/5 11:22
 */
@Getter
@AllArgsConstructor
public enum SearchChargeTypeEnum {

    TENANT(1, "VDC"),
    PROJECT(2, "PROJECT"),
    ;
    private Integer code;
    private String typeName;
}
