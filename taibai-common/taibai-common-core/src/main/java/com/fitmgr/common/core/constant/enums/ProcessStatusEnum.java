
package com.taibai.common.core.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Taibai
 * @date 2018/9/30 流程状态
 */
@Getter
@AllArgsConstructor
public enum ProcessStatusEnum {
    /**
     * 图片资源
     */
    ACTIVE("active", "图片资源"),

    /**
     * xml资源
     */
    SUSPEND("suspend", "xml资源");

    /**
     * 类型
     */
    private final String status;
    /**
     * 描述
     */
    private final String description;
}
