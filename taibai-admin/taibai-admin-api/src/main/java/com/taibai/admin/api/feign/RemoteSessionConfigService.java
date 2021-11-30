package com.taibai.admin.api.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.taibai.admin.api.config.AdminFeignConfig;
import com.taibai.admin.api.entity.RestrictLloginTime;
import com.taibai.admin.api.entity.SessionConfig;
import com.taibai.common.core.constant.ServiceNameConstants;
import com.taibai.common.core.util.R;

@FeignClient(contextId = "remoteSessionConfigService", value = ServiceNameConstants.UMPS_SERVICE, configuration = AdminFeignConfig.class)
public interface RemoteSessionConfigService {
    /**
     * querySessionConfig
     * 
     * @return R<SessionConfig>
     */
    @GetMapping("/session-config")
    R<SessionConfig> querySessionConfig();

    /**
     * queryRestrictLloginTime
     * 
     * @return R<RestrictLloginTime>
     */
    @GetMapping("/session-config/restrict-login-time")
    R<RestrictLloginTime> queryRestrictLloginTime();
}
