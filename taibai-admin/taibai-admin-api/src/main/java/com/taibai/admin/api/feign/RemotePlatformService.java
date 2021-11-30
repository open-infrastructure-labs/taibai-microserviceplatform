package com.taibai.admin.api.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.taibai.admin.api.config.AdminFeignConfig;
import com.taibai.admin.api.entity.Platform;
import com.taibai.common.core.constant.SecurityConstants;
import com.taibai.common.core.constant.ServiceNameConstants;
import com.taibai.common.core.util.R;

/**
 * 
 * 第三方平台操作
 *
 * @author Taibai
 * @date: 2021年3月25日 上午10:03:42
 */
@FeignClient(contextId = "remotePlatformService", value = ServiceNameConstants.UMPS_SERVICE, configuration = AdminFeignConfig.class)
public interface RemotePlatformService {

    /**
     * 获取平台信息
     * 
     * @param platfomId platfomId
     * @param from      from
     * @return R
     */
    @GetMapping(value = { "/platform/info" })
    R<Platform> info(@RequestParam(name = "platfomId") String platfomId,
            @RequestHeader(SecurityConstants.FROM) String from);

}
