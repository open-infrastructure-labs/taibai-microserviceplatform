
package com.taibai.common.log.util;

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.taibai.common.core.constant.CommonConstants;
import com.taibai.log.api.entity.OperateLog;

import cn.hutool.core.util.URLUtil;
import cn.hutool.extra.servlet.ServletUtil;
import lombok.experimental.UtilityClass;

/**
 * 系统日志工具类
 *
 * @author Taibai
 */
@UtilityClass
public class SysLogUtils {
    public OperateLog getSysLog() {
        HttpServletRequest request = ((ServletRequestAttributes) Objects
                .requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        OperateLog operateLog = new OperateLog();
        operateLog.setCreateBy(Objects.requireNonNull(getUsername()));
        operateLog.setType(CommonConstants.STATUS_NORMAL);
        operateLog.setRemoteAddr(ServletUtil.getClientIP(request));
        operateLog.setRequestUri(URLUtil.getPath(request.getRequestURI()));
        operateLog.setMethod(request.getMethod());
        operateLog.setUserAgent(request.getHeader("user-agent"));
        return operateLog;
    }

    /**
     * 获取客户端
     *
     * @return clientId
     */
    private String getClientId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2Authentication) {
            OAuth2Authentication auth2Authentication = (OAuth2Authentication) authentication;
            return auth2Authentication.getOAuth2Request().getClientId();
        }
        return null;
    }

    /**
     * 获取用户名称
     *
     * @return username
     */
    private String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        return authentication.getName();
    }

}
