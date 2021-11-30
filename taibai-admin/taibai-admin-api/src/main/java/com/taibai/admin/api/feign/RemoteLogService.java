
package com.taibai.admin.api.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.taibai.admin.api.config.AdminFeignConfig;
import com.taibai.admin.api.entity.SysLog;
import com.taibai.common.core.constant.SecurityConstants;
import com.taibai.common.core.constant.ServiceNameConstants;
import com.taibai.common.core.util.R;

/**
 * @author Taibai
 * @date 2018/6/28
 */
@FeignClient(contextId = "remoteLogService", value = ServiceNameConstants.UMPS_SERVICE, configuration = AdminFeignConfig.class)
public interface RemoteLogService {
    /**
     * 保存日志
     *
     * @param sysLog 日志实体
     * @param from   是否内部调用
     * @return succes、false
     */
    @PostMapping("/log/save")
    R<Boolean> saveLog(@RequestBody SysLog sysLog, @RequestHeader(SecurityConstants.FROM) String from);
}
