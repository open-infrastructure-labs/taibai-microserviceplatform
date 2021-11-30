package com.taibai.admin.api.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.taibai.admin.api.config.AdminFeignConfig;
import com.taibai.admin.api.entity.SysFile;
import com.taibai.common.core.constant.ServiceNameConstants;
import com.taibai.common.core.util.R;

/**
 * 文件.
 *
 * @date: 2020-09-03
 * @version: 1.0
 * @author Taibai
 */
@FeignClient(contextId = "sysFileService", value = ServiceNameConstants.UMPS_SERVICE, configuration = AdminFeignConfig.class)
public interface RemoteSysFileService {
    /**
     * 批量删除文件
     * 
     * @param fileList
     * @return
     */
    @DeleteMapping("/sys-file/batch-deletion")
    R batchDeletion(@RequestBody List<SysFile> fileList);
}
