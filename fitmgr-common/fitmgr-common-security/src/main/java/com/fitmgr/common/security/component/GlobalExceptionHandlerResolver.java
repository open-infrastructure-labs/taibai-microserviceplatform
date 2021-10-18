
package com.fitmgr.common.security.component;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fitmgr.common.core.constant.CommonConstants;
import com.fitmgr.common.core.exception.BusinessException;
import com.fitmgr.common.core.util.R;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Fitmgr
 * @date 2018/8/30 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandlerResolver {

    /**
     * 全局异常.
     *
     * @param e the e
     * @return R
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R handleGlobalException(Exception e) {
        log.error("全局异常信息 ex=", e);
        return R.builder().msg(e.getLocalizedMessage()).code(CommonConstants.FAIL).build();
    }

    /**
     * AccessDeniedException
     *
     * @param e the e
     * @return R
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public R handleAccessDeniedException(AccessDeniedException e) {
        String msg = SpringSecurityMessageSource.getAccessor().getMessage("AbstractAccessDecisionManager.accessDenied",
                e.getMessage());
        log.error("拒绝授权异常信息 ex=", e);
        return R.builder().msg(msg).code(CommonConstants.FAIL).build();
    }

    /**
     * validation Exception
     *
     * @param exception
     * @return R
     */
    @ExceptionHandler({ MethodArgumentNotValidException.class, BindException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R handleBodyValidException(MethodArgumentNotValidException exception) {
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
        log.error("参数绑定异常,ex = ", exception);
        return R.builder().msg(fieldErrors.get(0).getDefaultMessage()).code(CommonConstants.FAIL).build();
    }

    /**
     * 业务异常.
     *
     * @param e the e
     *
     * @return the wrapper
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public R businessException(BusinessException e) {
        log.error("业务异常=", e);
        return R.failed(e.getMessage());
    }
}
