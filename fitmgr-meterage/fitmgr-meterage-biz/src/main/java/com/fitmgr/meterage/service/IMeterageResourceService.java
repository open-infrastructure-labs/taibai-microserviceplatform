package com.fitmgr.meterage.service;


import com.fitmgr.resource.api.dto.ResourceOperateDTO;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author jy
 * @version 1.0
 * @date 2021/1/12 10:25
 */
public interface IMeterageResourceService {

    /**
     * 编排执行完成后处理计量
     *
     * @param componentCode
     * @param map
     * @param operateCode
     */
    void handleMeterageAfterTerraform(String componentCode, Map<String, Object> map, String operateCode, LocalDateTime endTime);

    /**
     * 纳管执行完成后处理计量
     *
     * @param componentCode
     * @param map
     * @param operateCode
     */
    void  handleMeterageAfterTerraformForNanotube(String componentCode,Map<String,Object> map, String operateCode);

    /**
     * 调用模板后处理计量
     */
    String  handleMeterageAfterXml(ResourceOperateDTO resourceOperateDTO);

}
