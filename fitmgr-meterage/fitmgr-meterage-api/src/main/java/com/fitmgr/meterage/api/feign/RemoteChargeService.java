package com.fitmgr.meterage.api.feign;

import com.fitmgr.common.core.constant.SecurityConstants;
import com.fitmgr.common.core.constant.ServiceNameConstants;
import com.fitmgr.common.core.util.R;
import com.fitmgr.meterage.api.entity.ResourceMeterageRecord;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;


/**
 * 创建人   mhp
 * 创建时间 2019/11/29
 * 描述
 **/

@FeignClient(contextId = "remoteChargeService", value = ServiceNameConstants.METERAGE_SERVICE)
public interface RemoteChargeService {

    /**
     * 资源下线，资源计费
     *
     * @param resourceMeterageRecord
     * @return
     */
    @PostMapping("/charge/record/resource/delete")
    R deleteResourceBillDetail(@RequestBody ResourceMeterageRecord resourceMeterageRecord, @RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 新增资源计费记录
     *
     * @param resourceMeterageRecord
     * @return
     */
    @PostMapping("/charge/record/resource/add")
    R saveResourceBillDetail(@RequestBody ResourceMeterageRecord resourceMeterageRecord, @RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 新增资源计费记录
     *
     * @param resourceMeterageRecord
     * @return
     */
    @PostMapping("/charge/record/resource/update")
    R updateResourceBillDetail(@RequestBody ResourceMeterageRecord resourceMeterageRecord, @RequestHeader(SecurityConstants.FROM) String from);
}
