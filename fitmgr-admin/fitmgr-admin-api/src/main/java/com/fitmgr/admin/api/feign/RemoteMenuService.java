package com.fitmgr.admin.api.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.fitmgr.admin.api.config.AdminFeignConfig;
import com.fitmgr.admin.api.entity.Menu;
import com.fitmgr.common.core.constant.ServiceNameConstants;
import com.fitmgr.common.core.util.R;

/**
 * @Classname RemoteAuthService
 * @Description 权限feign接口
 * @Date 2019/11/19 15:18
 * @Created by DZL
 */
@FeignClient(contextId = "remoteMenuService", value = ServiceNameConstants.UMPS_SERVICE, configuration = AdminFeignConfig.class)
public interface RemoteMenuService {
    /**
     * getListByUrl
     * 
     * @param url url
     * @return R
     */
    @GetMapping("/menu/template/url")
    R<Menu> getListByUrl(@RequestParam(name = "url") String url);

    /**
     * getMenuByTemplateId
     * 
     * @param templateId templateId
     * @return R
     */
    @GetMapping("/menu/{templateId}}")
    R<Menu> getMenuByTemplateId(@PathVariable(name = "templateId") Integer templateId);
}