package com.fitmgr.meterage.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @ClassName EffectRangeEnum
 * @Description 生效范围字典
 * @Author BDWang
 * @Date 2021/6/4 15:02
 **/
@AllArgsConstructor
@Getter
public enum EffectRangeEnum {

    ALL(1, "全部范围"),
    VDC(2, "VDC范围"),
    PROJECT(3, "项目范围"),
    ;

    private Integer key;
    private String value;

    public static String getValue(Integer key) {
        for (EffectRangeEnum value : EffectRangeEnum.values()) {
            if (value.getKey().equals(key)) {
                return value.getValue();
            }
        }
        return null;
    }

}
