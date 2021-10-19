package com.fitmgr.admin.api.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.fitmgr.admin.api.config.AdminFeignConfig;
import com.fitmgr.admin.api.entity.Ip;
import com.fitmgr.common.core.constant.SecurityConstants;
import com.fitmgr.common.core.constant.ServiceNameConstants;
import com.fitmgr.common.core.util.R;

/**
 * 
 * Ip白名单
 *
 * @author Fitmgr
 * @date: 2021年4月16日 下午5:49:04
 */
@FeignClient(contextId = "remoteIpService", value = ServiceNameConstants.UMPS_SERVICE, configuration = AdminFeignConfig.class)
public interface RemoteIpService {

    /**
     * 获取Ip白名单
     * 
     * @param from from
     * @return R
     */
    @GetMapping(value = { "/ip/list" })
    R<List<Ip>> list(@RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 获取启用的IP白名单
     * 
     * @param from from
     * @return R
     */
    @GetMapping(value = { "/ip/useList" })
    R<List<Ip>> listInUse(@RequestHeader(SecurityConstants.FROM) String from);

}
