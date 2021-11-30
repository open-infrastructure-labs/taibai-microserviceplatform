package com.fitmgr.admin.exceptions;

import com.fitmgr.common.core.constant.enums.BusinessEnum;
import com.fitmgr.common.core.exception.BusinessException;

/**
 * @Auther: DZL
 * @Date: 2019/11/30
 * @Description: 用户中心业务异常
 */
public class UserCenterException extends BusinessException {

    public UserCenterException() {
    }

    public UserCenterException(String message) {
        super(message);
    }

    public UserCenterException(BusinessEnum codeEnum, Object... args) {
        super(codeEnum, args);
    }
}
