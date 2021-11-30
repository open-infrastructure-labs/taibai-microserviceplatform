package com.taibai.common.core.exception;

import com.taibai.common.core.constant.enums.BusinessEnum;

import lombok.Getter;
import lombok.Setter;

/**
 * 业务异常
 *
 * @author Taibai
 * @date 2019-11-25
 */
public class BusinessException extends RuntimeException {

    /**
     * 异常码
     */
    @Getter
    @Setter
    protected Integer code;

    public BusinessException() {
    }

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessException(Throwable cause) {
        super(cause);
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(BusinessEnum codeEnum, Object... args) {
        super(String.format(codeEnum.getDescription(), args));
        this.code = codeEnum.getCode();
    }

}
