package com.fitmgr.meterage.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @ClassName DiscountTypeEnum
 * @Description 折扣类型字典转化
 * @Author BDWang
 * @Date 2021/6/4 14:55
 **/
@Getter
@AllArgsConstructor
public enum DiscountTypeEnum {

    SYS(0, "系统"),
    MANUAL(1, "自定义"),
    ;

    private Integer key;
    private String value;

    public static String getValue(Integer key) {
        for (DiscountTypeEnum value : DiscountTypeEnum.values()) {
            if (value.getKey().equals(key)) {
                return value.getValue();
            }
        }
        return null;
    }
}
