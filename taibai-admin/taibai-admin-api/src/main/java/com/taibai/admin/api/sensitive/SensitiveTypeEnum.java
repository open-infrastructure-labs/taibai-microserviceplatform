package com.taibai.admin.api.sensitive;

/**
 * @Author:menghuan
 * @Date:2020/5/19 18:01
 * @Description 数据脱敏枚举
 */
public enum SensitiveTypeEnum {
    /**
     * 自定义 根据属性的指定哪几位打码
     */
    CUSTOMER,
    /**
     * 用户名, 刘*华, 徐*
     */
    CHINESE_NAME,
    /**
     * 身份证号, 110110********1234
     */
    ID_CARD,
    /**
     * 座机号, ****1234
     */
    FIXED_PHONE,
    /**
     * 手机号, 176****1234
     */
    MOBILE_PHONE,
    /**
     * 地址, 北京********
     */
    ADDRESS,
    /**
     * 电子邮件, s*****o@xx.com
     */
    EMAIL,
    /**
     * 银行卡, 622202************1234
     */
    BANK_CARD,
    /**
     * 密码, 永远是 ******, 与长度无关
     */
    PASSWORD,
    /**
     * 密钥, 永远是 ******, 与长度无关
     */
    KEY
}
