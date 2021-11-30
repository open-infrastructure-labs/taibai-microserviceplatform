package com.taibai.admin.api.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.taibai.admin.api.config.AdminFeignConfig;
import com.taibai.admin.api.vo.UserVO;
import com.taibai.common.core.constant.SecurityConstants;
import com.taibai.common.core.constant.ServiceNameConstants;
import com.taibai.common.core.util.R;

@FeignClient(contextId = "remoteUserServiceInner", value = ServiceNameConstants.UMPS_SERVICE, configuration = AdminFeignConfig.class)
public interface RemoteUserServiceInner {
    /**
     * getById
     * 
     * @param userId userId
     * @param from   from
     * @return R<UserVO>
     */
    @GetMapping("/user/user-info/{userId}")
    R<UserVO> getById(@PathVariable(name = "userId") Integer userId,
            @RequestHeader(SecurityConstants.FROM) String from);
}
