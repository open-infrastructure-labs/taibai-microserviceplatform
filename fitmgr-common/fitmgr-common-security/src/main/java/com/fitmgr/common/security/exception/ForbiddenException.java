
package com.fitmgr.common.security.exception;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fitmgr.common.security.component.Auth2ExceptionSerializer;

/**
 * @author Fitmgr
 * @date 2018/7/8
 */
@JsonSerialize(using = Auth2ExceptionSerializer.class)
public class ForbiddenException extends Auth2Exception {

    public ForbiddenException(String msg, Throwable t) {
        super(msg);
    }

    @Override
    public String getOAuth2ErrorCode() {
        return "access_denied";
    }

    public ForbiddenException(String msg) {
        super(msg);
    }

    @Override
    public int getHttpErrorCode() {
        return HttpStatus.FORBIDDEN.value();
    }

}
