
package com.fitmgr.common.security.exception;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fitmgr.common.security.component.Auth2ExceptionSerializer;

/**
 * @author Fitmgr
 * @date 2018/7/8
 */
@JsonSerialize(using = Auth2ExceptionSerializer.class)
public class InvalidException extends Auth2Exception {

    public InvalidException(String msg, Throwable t) {
        super(msg);
    }

    @Override
    public String getOAuth2ErrorCode() {
        return "invalid_exception";
    }

    @Override
    public int getHttpErrorCode() {
        return 426;
    }

}
