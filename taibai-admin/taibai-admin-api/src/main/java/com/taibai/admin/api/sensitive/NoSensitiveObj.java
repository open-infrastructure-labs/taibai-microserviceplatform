package com.taibai.admin.api.sensitive;

/**
 * @Author:menghuan
 * @Date:2020/5/19 9:35
 * @description 重要数据信息脱敏
 */
public interface NoSensitiveObj<T> {
    /**
     * noSensitiveObj
     * 
     * @return T
     */
    default T noSensitiveObj() {
        return (T) this;
    }

}
