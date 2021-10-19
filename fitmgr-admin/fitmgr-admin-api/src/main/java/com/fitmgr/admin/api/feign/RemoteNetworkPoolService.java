package com.fitmgr.admin.api.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;

import com.fitmgr.admin.api.config.AdminFeignConfig;
import com.fitmgr.common.core.constant.ServiceNameConstants;
import com.fitmgr.common.core.util.R;

/**
 * 创建人 dzl 创建时间 2020/2/29 描述
 **/

@FeignClient(contextId = "RemoteNetworkPoolService", value = ServiceNameConstants.UMPS_SERVICE, configuration = AdminFeignConfig.class)
public interface RemoteNetworkPoolService {

    /**
     * 更新全量网络池信息
     *
     * @param tenantDTO 租户信息
     * @return R
     */
    @PutMapping("/networkPool")
    public R updateNetworkPool();
}
