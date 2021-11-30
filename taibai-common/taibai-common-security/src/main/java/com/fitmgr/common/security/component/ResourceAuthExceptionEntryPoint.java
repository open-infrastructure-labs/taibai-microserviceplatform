
package com.fitmgr.common.security.component;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitmgr.common.core.constant.CommonConstants;
import com.fitmgr.common.core.util.R;

import cn.hutool.http.HttpStatus;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Fitmgr
 * @date 2018/7/8 客户端异常处理 1. 可以根据 AuthenticationException 不同细化异常处理
 */
@Slf4j
@Component
@AllArgsConstructor
public class ResourceAuthExceptionEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) {
        response.setCharacterEncoding(CommonConstants.UTF8);
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        R<String> result = new R<>();
        result.setCode(CommonConstants.FAIL);
        if (authException != null) {
            log.error(String.format("Authentication produce an error : %s , url is : %s", authException.getMessage(),
                    request.getRequestURL()), authException);
            result.setMsg(authException.getMessage());
            result.setData(authException.getMessage());
        }
        response.setStatus(HttpStatus.HTTP_UNAUTHORIZED);
        PrintWriter printWriter = response.getWriter();
        printWriter.append(objectMapper.writeValueAsString(result));
    }
}
