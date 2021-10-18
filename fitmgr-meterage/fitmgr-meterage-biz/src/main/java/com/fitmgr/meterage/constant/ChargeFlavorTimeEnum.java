package com.fitmgr.meterage.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 时间单位
 * @author zhangxiaokang
 * @date 2020/10/27 15:23
 */
@Getter
@AllArgsConstructor
public enum ChargeFlavorTimeEnum {
    //时间单位：1-时，2-天，3-月，5-年
    HOUR(1,"时"),
    DAY(2,"天"),
    MONTH(3,"月"),
    QUARTER(4,"季"),
    YEAR(5,"年"),
    ;
    private Integer code;
    private String name;

    /**
     *  根据code获取name
     * @param code
     * @return
     */
    public static String getTimeName(Integer code) {
        for (ChargeFlavorTimeEnum value : ChargeFlavorTimeEnum.values()) {
            if (value.getCode().equals(code)) {
                return value.getName();
            }
        }
        return null;
    }
}
