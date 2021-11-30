
package com.fitmgr.common.security.exception;

import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fitmgr.common.security.component.Auth2ExceptionSerializer;

import lombok.Getter;

/**
 * @author Fitmgr
 * @date 2018/7/8 自定义OAuth2Exception
 */
@JsonSerialize(using = Auth2ExceptionSerializer.class)
public class Auth2Exception extends OAuth2Exception {
    @Getter
    private String errorCode;

    public Auth2Exception(String msg) {
        super(msg);
    }

    public Auth2Exception(String msg, String errorCode) {
        super(msg);
        this.errorCode = errorCode;
    }
}
