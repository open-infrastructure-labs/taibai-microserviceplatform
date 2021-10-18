package com.fitmgr.meterage.service.impl;
import com.alibaba.fastjson.JSONObject;
import com.fitmgr.common.core.constant.enums.BusinessEnum;
import com.fitmgr.common.core.exception.BusinessException;
import com.fitmgr.common.core.util.R;
import com.fitmgr.meterage.api.entity.MeterageProjectProperty;
import com.fitmgr.meterage.api.entity.ResourceMeterageRecord;
import com.fitmgr.meterage.mapper.MeterageRecordMapper;
import com.fitmgr.meterage.service.IMeterageItemHeaderService;
import com.fitmgr.meterage.service.IMeterageResourceService;
import com.fitmgr.meterage.service.IResourceChargeRecordService;
import com.fitmgr.order.api.feign.RemoteServiceOrderTaskService;
import com.fitmgr.resource.api.feign.RemoteCmdbService;
import com.fitmgr.resource.api.feign.RemoteCmdbServiceInner;
import com.fitmgr.resource.api.feign.RemoteComponentService;
import com.fitmgr.resource.api.vo.ComponentVO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fitmgr.resource.api.dto.ResourceOperateDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jy
 * @version 1.0
 * @date 2021/1/12 10:28
 */
@Service
@AllArgsConstructor
@Slf4j
public class MeterageResourceServiceImpl implements IMeterageResourceService {

    private final IMeterageItemHeaderService meterageItemHeaderService;

    private final MeterageRecordMapper meterageRecordMapper;

    private final RemoteServiceOrderTaskService remoteServiceOrderTaskService;

    private final RemoteCmdbServiceInner remoteCmdbServiceInner;

    private final IResourceChargeRecordService resourceChargeRecordService;

    private final RemoteCmdbService cmdbService;

    private String getMeterageData(String componentCode, Map<String, Object> map, List<MeterageProjectProperty> meterageItemHeaderList) {
        String dataStr = "";
        dataStr = getConventionalMeterData(map, meterageItemHeaderList);
        return dataStr;
    }

    /**
     * 计算资源具体的计量数据   通用
     *
     * @param instance               资源实例在cmdb中的记录
     * @param meterageItemHeaderList 计量表头集合
     * @return
     */
    private String getConventionalMeterData(Map<String, Object> instance, List<MeterageProjectProperty> meterageItemHeaderList) {
        JSONObject meterageJsonObject = new JSONObject();
        log.info("instance start is jy" + instance);
        for (MeterageProjectProperty header : meterageItemHeaderList) {
            //TODO:foreign_component_id 是否为null，null则正常，不为null，则取foreign_key，和组件code进行二次数据查询，按照web_key，取值封装到data中。
            if (null != header.getForeignComponentId() && !"".equals(header.getForeignComponentId())) {
                String key = header.getForeignKey();
                String key1 = header.getSourceKey();
                Object value = null;
                log.info("key1 is jy " + key1);
                String id = null;
                if(instance.get(key1) != null){
                    id = instance.get(key1).toString();
                    log.info("id is jy " + id);
                }
                // getForeignComponentId 实际上是 getForeignComponentCode
                Map<String, Object> resource = selectMeterageForeignComponentInfo(header.getForeignComponentId(), id);
                log.info("resource select is " + resource);
                if (resource != null) {
                    value = resource.get(key);
                }
                meterageJsonObject.put(key, value);
            } else {
                String key = header.getSourceKey();
                Object value = instance.get(key);
                meterageJsonObject.put(key, value);
            }
        }
        log.info("getConventionalMeterData res is {}",meterageJsonObject.toJSONString());
        return meterageJsonObject.toJSONString();
    }

    private Map<String, Object> selectMeterageForeignComponentInfo(String componentCode, String id) {
        Map<String, Object> resourceInfo = null;
        Map<String, Object> objectMap = new HashMap<>();
        List<Map<String, Object>> mapList = new ArrayList<>();
        Map<String, Object> stringObjectMap = new HashMap<>();
        objectMap.put("componentCode", componentCode);
        stringObjectMap.put("key", "uuid");
        stringObjectMap.put("connector", 0);
        stringObjectMap.put("value", id);
        mapList.add(stringObjectMap);
        objectMap.put("searchDTOList",mapList);
        log.info("查询resource服务参数数据, {}", objectMap);
        R r = remoteCmdbServiceInner.selectByCondition(objectMap,"Y");
        List<Map<String, Object>> list = (List<Map<String, Object>>) r.getData();
        if (list != null && list.size() > 0) {
            resourceInfo = list.get(0);
        }
        return resourceInfo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleMeterageAfterTerraform(String componentCode, Map<String, Object> map, String operateCode, LocalDateTime endTime) {
        //资源实例在cmdb中没有对应的数据 则不进行计量
        if (map == null || map.isEmpty()) {
            return;
        }
        if(endTime == null){
            endTime = LocalDateTime.now();
        }
        log.info("real end time is : {} " , endTime);
        Integer orderId = null;
        Integer taskId = (Integer) map.get("cmp_task_id");
        if (taskId != null) {
            R r = remoteServiceOrderTaskService.getOrder(taskId, "Y");
            Object object = r.getData();
            Map<String, Object> objectMap = (Map) object;
            if (null != objectMap) {
                orderId = (Integer) objectMap.get("id");
            }

        }
        List<MeterageProjectProperty> meterageItemHeaderList = meterageItemHeaderService.getMeterageItemHeaderListForCal(componentCode);
        log.info("调用查询计量项表头接口返回的结果；" + meterageItemHeaderList);
        //如果计量表头集合为空，则说明，该组件不参与计量
        if (meterageItemHeaderList == null || meterageItemHeaderList.size() == 0) {
            return;
        }

        String cmpInstanceName = map.get("cmpInstanceName") == null ? map.get("cmp_instance_name").toString() : map.get(
                "cmpInstanceName").toString();
        //查询会查询到级联下的云硬盘
        List<ResourceMeterageRecord> meterageRecords = meterageRecordMapper.getUnfinishedRecordsByCmpInstanceName(cmpInstanceName);

        String dataStr = "";
        if ("create".equalsIgnoreCase(operateCode)) {
            for (ResourceMeterageRecord record : meterageRecords) {
                record.setEndTime(endTime);
                meterageRecordMapper.updateById(record);
            }
            ResourceMeterageRecord record = new ResourceMeterageRecord();
            Integer tenantId = map.get("cmp_tenant_id") != null ? (Integer) map.get("cmp_tenant_id") : null;
            Integer projectId = map.get("cmp_project_id") != null ? (Integer) map.get("cmp_project_id") : null;
            Integer userId = map.get("cmp_user_id") != null ? (Integer) map.get("cmp_user_id") : null;
            dataStr = getMeterageData(componentCode, map, meterageItemHeaderList);
            if (StringUtils.isNotBlank(dataStr)) {
                record.setTenantId(tenantId);
                record.setProjectId(projectId);
                record.setUserId(userId);
                record.setCmpInstanceName(cmpInstanceName);
                record.setComponentCode(componentCode);
                record.setStartTime(LocalDateTime.now());
                record.setData(dataStr);
                record.setOrderId(orderId);
                log.info("start insert,dataStr is {}", dataStr);
                meterageRecordMapper.insert(record);
                // 新增计费记录
                boolean result = resourceChargeRecordService.saveResourceBillDetail(record);
                if (!result) {
                    throw new BusinessException(BusinessEnum.CHARGE_RECORDS_CALLBACK_ERROR);
                }
            }
        } else if ("update".equalsIgnoreCase(operateCode)) {
            for (ResourceMeterageRecord record : meterageRecords) {
                if(!record.getComponentCode().equals(componentCode)){
                    // 虚拟机更新的时候防止更新云硬盘
                    continue;
                }
                record.setEndTime(endTime);
                meterageRecordMapper.updateById(record);
                // 当前计费记录进行结算
                boolean result = resourceChargeRecordService.updateResourceBillDetail(record);
                if (!result) {
                    throw new BusinessException(BusinessEnum.CHARGE_RECORDS_CALLBACK_ERROR);
                }
            }
            ResourceMeterageRecord record = new ResourceMeterageRecord();
            Integer tenantId = map.get("cmp_tenant_id") != null ? (Integer) map.get("cmp_tenant_id") : null;
            Integer projectId = map.get("cmp_project_id") != null ? (Integer) map.get("cmp_project_id") : null;
            Integer userId = map.get("cmp_user_id") != null ? (Integer) map.get("cmp_user_id") : null;
            dataStr = getMeterageData(componentCode, map, meterageItemHeaderList);
            //新增一条
            record.setTenantId(tenantId);
            record.setProjectId(projectId);
            record.setUserId(userId);
            record.setComponentCode(componentCode);
            record.setCmpInstanceName(cmpInstanceName);
            record.setStartTime(LocalDateTime.now());
            record.setData(dataStr);
            record.setOrderId(orderId);
            meterageRecordMapper.insert(record);
            // 新增计费记录
            boolean result = resourceChargeRecordService.saveResourceBillDetail(record);
            if (!result) {
                throw new BusinessException(BusinessEnum.CHARGE_RECORDS_CALLBACK_ERROR);
            }
        } else if ("delete".equalsIgnoreCase(operateCode)) {
            for (ResourceMeterageRecord record : meterageRecords) {
                record.setEndTime(endTime);
                meterageRecordMapper.updateById(record);
                // 资源下线，计费记录结算
                boolean result = resourceChargeRecordService.deleteResourceBillDetail(record);
                if (!result) {
                    throw new BusinessException(BusinessEnum.CHARGE_RECORDS_CALLBACK_ERROR);
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleMeterageAfterTerraformForNanotube(String componentCode, Map<String, Object> map, String operateCode) {
        //资源实例在cmdb中没有对应的数据 则不进行计量
        if (map == null || map.isEmpty()) {
            return;
        }
        List<MeterageProjectProperty> meterageItemHeaderList = meterageItemHeaderService.getMeterageItemHeaderListForCal(componentCode);
        log.info("调用查询计量项表头接口返回的结果；" + meterageItemHeaderList);
        //如果计量表头集合为空，则说明，该组件不参与计量
        if (meterageItemHeaderList == null || meterageItemHeaderList.size() == 0) {
            return;
        }
        String cmpInstanceName = map.get("cmpInstanceName") == null ? map.get("cmp_instance_name").toString() : map.get(
                "cmpInstanceName").toString();
        String dataStr = "";
        if ("create".equalsIgnoreCase(operateCode)) {
            ResourceMeterageRecord record = new ResourceMeterageRecord();
            Integer tenantId = map.get("cmp_tenant_id") != null ? (Integer) map.get("cmp_tenant_id") : null;
            Integer projectId = map.get("cmp_project_id") != null ? (Integer) map.get("cmp_project_id") : null;
            Integer userId = map.get("cmp_user_id") != null ? (Integer) map.get("cmp_user_id") : null;
            dataStr = getMeterageData(componentCode, map, meterageItemHeaderList);
            if (StringUtils.isNotBlank(dataStr)) {
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                log.info("map is {}", map);
                if (null != map.get("cmp_create_time")) {
                    record.setStartTime(LocalDateTime.parse(map.get("cmp_create_time").toString(), df));
                }
                record.setTenantId(tenantId);
                record.setRemarkFlag("inclusion_order_data");
                record.setProjectId(projectId);
                record.setUserId(userId);
                record.setCmpInstanceName(cmpInstanceName);
                record.setComponentCode(componentCode);
                record.setData(dataStr);
                meterageRecordMapper.insert(record);
            }
        }
    }

    @Override
    public String handleMeterageAfterXml(ResourceOperateDTO resourceOperateDTO) {
        log.info("计量XML接口调用----------------------------------------------");
        log.info("模板计量数据开始----------");
        String componentCode = resourceOperateDTO.getComponentCode();

        log.info("resourceOperateDTO jy is {}", resourceOperateDTO);
        Map<String, Object> instanceMaps = cmdbService.getCmpInstanceByOperateParams(resourceOperateDTO).getData();
        log.info("instanceMaps 数据---------- {}", instanceMaps);
        if (instanceMaps == null) {
            log.warn("handleMeterageAfterApi:根据操作参数与组件code没有在cmdb中找到对应的资源实例，请检查");
            return "handleMeterageAfterApi:根据操作参数与组件code没有在cmdb中找到对应的资源实例，请检查";
        }
        handleMeterageAfterTerraform(componentCode,instanceMaps,"update",null);

        return "模板计量数据更新结束---------- ";
    }
}
