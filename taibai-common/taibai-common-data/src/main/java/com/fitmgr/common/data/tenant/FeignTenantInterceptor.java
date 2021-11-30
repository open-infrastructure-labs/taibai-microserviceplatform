
package com.taibai.common.data.tenant;

import com.taibai.common.core.constant.CommonConstants;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Fitmgr
 * @date 2018/9/14
 */
@Slf4j
public class FeignTenantInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        if (TenantContextHolder.getTenantId() == null) {
            return;
        }
        requestTemplate.header(CommonConstants.TENANT_ID, TenantContextHolder.getTenantId().toString());
    }
}
