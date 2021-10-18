package com.fitmgr.meterage.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fitmgr.common.core.util.R;
import com.fitmgr.meterage.api.dto.ResourceBpOperateDTO;
import com.fitmgr.meterage.service.IMeterageResourceService;
import com.fitmgr.resource.api.dto.ResourceOperateDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jy
 * @version 1.0
 * @date 2021/1/12 10:30
 */
@RestController
@AllArgsConstructor
@RequestMapping("/meterageResource")
@Api(value = "meterageResource", tags = "计量数据操作模块")
@Slf4j
public class MeterageResourceController {

    private final IMeterageResourceService meterageResourceService;

    @ApiOperation(value = "编排计量数据处理")
    @PostMapping("/resourceOperate")
    @ApiImplicitParams(@ApiImplicitParam(name = "resourceBpOperateDTO", value = "计量数据对象", dataType = "ResourceBpOperateDTO", paramType = "body", required = true))
    public R handleMeterageAfterTerraform(@RequestBody ResourceBpOperateDTO resourceBpOperateDTO) {
        String componentCode = resourceBpOperateDTO.getComponentCode();
        Map<String,Object> map = resourceBpOperateDTO.getMap();
        String operateCode = resourceBpOperateDTO.getOperateCode();
        LocalDateTime endTime = resourceBpOperateDTO.getEndTime();
        log.info("endTime is :{}",endTime);
        if(map == null){
            return R.failed("实例数据为空！");
        }
        meterageResourceService.handleMeterageAfterTerraform(componentCode, map, operateCode, endTime);
        // 对创建云主机时同时创建的云硬盘做特殊处理
        ArrayList volumesList = (ArrayList) map.get("volumes");
        JSONArray volumes= JSONArray.parseArray(JSONObject.toJSONString(volumesList));
        if (volumes != null && volumes.size() > 0) {
            log.warn("该云主机创建时同时创建了数据盘，数据盘处理开始！");
            for (int i = 0; i < volumes.size(); i++) {
                //等于0是数据盘
                if ((Integer) volumes.getJSONObject(i).get("usage_type") == 0){
                    Map<String, Object> copyMap = new HashMap<>(map);
                    Map<String,Object> innerMap = volumes.getJSONObject(i).getInnerMap();
                    copyMap.putAll(innerMap);
                    String volumesCmpName = (String) volumes.getJSONObject(i).get("volume_id");
                    copyMap.put("cmp_instance_name",volumesCmpName);
                    log.info("disk cmp_instance_name is {}", copyMap.get("cmp_instance_name"));
                    meterageResourceService.handleMeterageAfterTerraform("resourcecenter_blockstorage_volume_v1", copyMap, "create", endTime);
                }
            }
        }
        return R.ok();
    }

    @ApiOperation(value = "纳管计量数据处理")
    @PostMapping("/nanotubeOperate")
    @ApiImplicitParams(@ApiImplicitParam(name = "resourceBpOperateDTO", value = "计量数据对象", dataType = "ResourceBpOperateDTO", paramType = "body", required = true))
    public R handleMeterageAfterTerraformForNanotube(@RequestBody ResourceBpOperateDTO resourceBpOperateDTO) {
        String componentCode = resourceBpOperateDTO.getComponentCode();
        Map<String,Object> map = resourceBpOperateDTO.getMap();
        String operateCode = resourceBpOperateDTO.getOperateCode();
        meterageResourceService.handleMeterageAfterTerraformForNanotube(componentCode, map, operateCode);
        return R.ok();
    }

    /**
     * 资源更新的计量数据处理接口(模板使用)
     */
    @PutMapping("/updateMeterage")
    @ApiOperation(value = "资源更新的计量数据处理接口", notes = "资源更新的计量数据处理接口")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "resourceOperateDTO", dataType = "ResourceOperateDTO",
                    required = true, value = "修改的实例")})
    public R updateMeterage(@RequestBody ResourceOperateDTO resourceOperateDTO) {
        log.info("开始处理计费");
        String result = meterageResourceService.handleMeterageAfterXml(resourceOperateDTO);
        return R.ok(null,result);
    }

}
