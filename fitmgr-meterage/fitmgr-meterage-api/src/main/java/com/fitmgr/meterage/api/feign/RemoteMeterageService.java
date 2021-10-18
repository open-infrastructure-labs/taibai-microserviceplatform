package com.fitmgr.meterage.api.feign;

import com.fitmgr.common.core.constant.ServiceNameConstants;
import com.fitmgr.common.core.util.R;
import com.fitmgr.meterage.api.dto.MeterageViewDataDTO;
import com.fitmgr.meterage.api.dto.ResourceBpOperateDTO;
import com.fitmgr.meterage.api.vo.MeterageViewDataVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;


/**
 * 创建人   mhp
 * 创建时间 2019/11/29
 * 描述
 **/

@FeignClient(contextId = "remoteMeterageService", value = ServiceNameConstants.METERAGE_SERVICE)
public interface RemoteMeterageService {

    /**
     * 通用计量数据处理
     *
     * @param resourceBpOperateDTO
     * @return
     */
    @PostMapping("/meterageResource/resourceOperate")
    R handleMeterage(@RequestBody ResourceBpOperateDTO resourceBpOperateDTO);

    /**
     * 纳管计量数据处理
     *
     * @param resourceBpOperateDTO
     * @return
     */
    @PostMapping("/meterageResource/nanotubeOperate")
    R handleMeterageAfterTerraformForNanotube(@RequestBody ResourceBpOperateDTO resourceBpOperateDTO);

    /**
     * 计量概览数据
     *
     * @param meterageViewDataVO
     * @return
     */
    @PostMapping("meterageRecord/view/List")
    R<Map<Integer, Map<String, List<MeterageViewDataDTO>>>> getViewDataList(@RequestBody MeterageViewDataVO meterageViewDataVO);

}
