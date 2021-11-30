
package com.taibai.admin.api.feign;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taibai.admin.api.config.AdminFeignConfig;
import com.taibai.admin.api.dto.TokenDTO;
import com.taibai.common.core.constant.SecurityConstants;
import com.taibai.common.core.constant.ServiceNameConstants;
import com.taibai.common.core.util.R;

/**
 * @author Taibai
 * @date 2018/9/4
 */
@FeignClient(contextId = "remoteTokenService", value = ServiceNameConstants.AUTH_SERVICE, configuration = AdminFeignConfig.class)
public interface RemoteTokenService {
    /**
     * 分页查询token 信息
     *
     * @param from   内部调用标志
     * @param params 分页参数
     * @param from   内部调用标志
     * @return page
     */
    @PostMapping("/auth/token/page")
    R<Page> getTokenPage(@RequestBody Map<String, Object> params, @RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 修改token信息
     * 
     * @param tokenDTO tokenDTO
     * @param from     from
     * @return R
     */
    @PostMapping("/auth/token/update-token")
    R updateRdisToken(@RequestBody TokenDTO tokenDTO, @RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 删除token
     *
     * @param from  内部调用标志
     * @param token token
     * @param from  内部调用标志
     * @return R<Boolean>
     */
    @DeleteMapping("/auth/token/{token}")
    R<Boolean> removeTokenById(@PathVariable("token") String token, @RequestHeader(SecurityConstants.FROM) String from);

    /**
     * kickout
     * 
     * @param tokenDTO tokenDTO
     * @return R<Boolean>
     */
    @PostMapping("/auth/token/kickout")
    R<Boolean> kickout(@RequestBody TokenDTO tokenDTO);

}
