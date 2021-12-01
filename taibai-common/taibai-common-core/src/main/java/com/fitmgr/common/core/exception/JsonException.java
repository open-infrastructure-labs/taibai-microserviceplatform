package com.taibai.common.core.exception;

import com.taibai.common.core.constant.enums.BusinessEnum;

/**
 * JSON合并异常
 * 
 * @author Taibai
 * @date 2019-12-31
 */
public class JsonException extends BusinessException {
    public JsonException() {
    }

    public JsonException(String message) {
        super(message);
    }

    public JsonException(BusinessEnum codeEnum, Object... args) {
        super(codeEnum, args);
    }
}
