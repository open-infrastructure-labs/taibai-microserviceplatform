package com.taibai.admin.api.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.taibai.admin.api.config.AdminFeignConfig;
import com.taibai.admin.api.vo.ProjectVO;
import com.taibai.common.core.constant.SecurityConstants;
import com.taibai.common.core.constant.ServiceNameConstants;
import com.taibai.common.core.util.R;

@FeignClient(contextId = "remoteProjectServiceInner", value = ServiceNameConstants.UMPS_SERVICE, configuration = AdminFeignConfig.class)
public interface RemoteProjectServiceInner {
    /**
     * selectProject
     * 
     * @param id   id
     * @param from from
     * @return R
     */
    @GetMapping("/project/detail/{id}")
    R<ProjectVO> selectProject(@PathVariable("id") Integer id, @RequestHeader(SecurityConstants.FROM) String from);
}
