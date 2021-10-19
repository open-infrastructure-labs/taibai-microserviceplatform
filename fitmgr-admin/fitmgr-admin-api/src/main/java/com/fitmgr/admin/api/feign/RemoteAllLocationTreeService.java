package com.fitmgr.admin.api.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;

import com.fitmgr.admin.api.config.AdminFeignConfig;
import com.fitmgr.common.core.constant.ServiceNameConstants;
import com.fitmgr.common.core.util.R;

/**
 * 创建人 dzl 创建时间 2020/2/29 描述
 **/

@FeignClient(contextId = "remoteAllLocationTreeService", value = ServiceNameConstants.UMPS_SERVICE, configuration = AdminFeignConfig.class)
public interface RemoteAllLocationTreeService {

    /**
     * 更新位置树信息
     *
     * @return
     */
    @PutMapping("/allLocationTree")
    public R updateAllLocationTree();
}
