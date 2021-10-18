package com.fitmgr.meterage.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @ClassName DiscountStatusEnum
 * @Description 折扣状态字典
 * @Author BDWang
 * @Date 2021/6/4 15:04
 **/
@AllArgsConstructor
@Getter
public enum DiscountStatusEnum {

    ON(0, "启用"),
    OFF(1, "禁用"),
    ;

    private Integer key;
    private String value;

    public static String getValue(Integer key) {
        for (DiscountStatusEnum value : DiscountStatusEnum.values()) {
            if (value.getKey().equals(key)) {
                return value.getValue();
            }
        }
        return null;
    }
}
