
package com.fitmgr.common.security.component;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.fitmgr.common.core.constant.SecurityConstants;
import com.fitmgr.common.security.annotation.Inner;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Fitmgr
 * @date 2018/11/26
 *       <p>
 *       服务间接口不鉴权处理逻辑
 */
@Slf4j
@Aspect
@Component
@AllArgsConstructor
public class SecurityInnerAspect {
    private final HttpServletRequest request;

    @SneakyThrows
    @Around("@annotation(inner)")
    public Object around(ProceedingJoinPoint point, Inner inner) {
        String header = request.getHeader(SecurityConstants.FROM);
        if (inner.value() && !StrUtil.equals(SecurityConstants.FROM_IN, header)) {
            log.warn("访问接口 {} 没有权限", point.getSignature().getName());
            throw new AccessDeniedException("Access is denied");
        }
        return point.proceed();
    }

}
