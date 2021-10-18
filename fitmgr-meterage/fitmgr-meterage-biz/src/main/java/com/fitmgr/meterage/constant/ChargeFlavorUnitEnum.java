package com.fitmgr.meterage.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 计量单位枚举
 *
 * @author zhangxiaokang
 * @date 2020/10/27 15:10
 */
@Getter
@AllArgsConstructor
public enum ChargeFlavorUnitEnum {

    STAND(1, "台"),
    UNIT(2, "个"),
    GB(3, "G"),
    TB(4, "T"),
    CORE(5, "Core"),
    PARtItioN(6, "partition区"),
    ;
    private Integer code;
    private String name;

    /**
     * 根据code获取name
     * @param code
     * @return
     */
    public static String getUnitName(Integer code) {
        for (ChargeFlavorUnitEnum value : ChargeFlavorUnitEnum.values()) {
            if (value.getCode().equals(code)) {
                return value.getName();
            }
        }
        return null;
    }
}
