
package com.fitmgr.common.security.exception;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fitmgr.common.security.component.Auth2ExceptionSerializer;

/**
 * @author Fitmgr
 * @date 2018/7/8
 */
@JsonSerialize(using = Auth2ExceptionSerializer.class)
public class UnauthorizedException extends Auth2Exception {

    public UnauthorizedException(String msg, Throwable t) {
        super(msg);
    }

    @Override
    public String getOAuth2ErrorCode() {
        return "unauthorized";
    }

    @Override
    public int getHttpErrorCode() {
        return HttpStatus.UNAUTHORIZED.value();
    }

}
