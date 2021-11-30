package com.taibai.common.core.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Taibai
 * @date 2018/9/30 云平台枚举
 */
@Getter
@AllArgsConstructor
public enum CloudEnum {

    /**
     * 腾讯云
     */
    TENCENT_CLOUD("tencentcloud"),

    /**
     * 阿里云
     */
    ALI_CLOUD("alicloud"),

    /**
     * 烽火云
     */
    RESOUR_CECENTER("resourcecenter");

    /**
     * 唯一编码
     */
    private String code;
}
