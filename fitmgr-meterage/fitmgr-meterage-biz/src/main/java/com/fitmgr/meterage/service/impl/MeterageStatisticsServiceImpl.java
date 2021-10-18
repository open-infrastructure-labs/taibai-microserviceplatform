package com.fitmgr.meterage.service.impl;

import cn.hutool.core.util.PageUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitmgr.admin.api.feign.RemoteAuthService;
import com.fitmgr.admin.api.feign.RemoteTenantService;
import com.fitmgr.common.core.constant.CommonConstants;
import com.fitmgr.common.core.constant.enums.BusinessEnum;
import com.fitmgr.common.core.constant.enums.ResponseCodeEnum;
import com.fitmgr.common.core.exception.BusinessException;
import com.fitmgr.common.core.util.R;
import com.fitmgr.common.security.service.FitmgrUser;
import com.fitmgr.common.security.util.SecurityUtils;
import com.fitmgr.meterage.api.dto.StatisticsDTO;
import com.fitmgr.meterage.api.entity.MeterageProject;
import com.fitmgr.meterage.api.entity.MeterageProjectProperty;
import com.fitmgr.meterage.api.entity.ResourceMeterageRecord;
import com.fitmgr.meterage.api.vo.ResourceMeterageRecordVO;
import com.fitmgr.meterage.service.IMeterageProjectPropertyService;
import com.fitmgr.meterage.service.IMeterageProjectService;
import com.fitmgr.meterage.service.IMeterageRecordService;
import com.fitmgr.meterage.service.IMeterageStatisticsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@AllArgsConstructor
public class MeterageStatisticsServiceImpl implements IMeterageStatisticsService {

    private RemoteTenantService remoteTenantService;

    private IMeterageProjectService meterageProjectService;

    private IMeterageProjectPropertyService meterageProjectPropertyService;

    private IMeterageRecordService meterageRecordService;

    private static final Pattern PATTERN = Pattern.compile("[0-9]*");

    @Override
    public IPage<Map<String, String>> getList(StatisticsDTO statisticsDTO) {
        Page<Map<String, String>> page = statisticsDTO.getPage();
        List<Integer> tableHeads = statisticsDTO.getTableHeadList();
        List<Map<String, String>> tableHeadList = new ArrayList<>();
        Map<String, String> tableHeadMap = new HashMap<>();
        for (Integer headId:tableHeads) {
            MeterageProjectProperty meterageProjectProperty = meterageProjectPropertyService.getById(headId);
            if(null != meterageProjectProperty.getForeignKey()){
                tableHeadMap.put("prop",meterageProjectProperty.getForeignKey());
                tableHeadMap.put("label",meterageProjectProperty.getKeyName());
            }else {
                tableHeadMap.put("prop",meterageProjectProperty.getSourceKey());
                tableHeadMap.put("label",meterageProjectProperty.getKeyName());
            }
        }
        tableHeadList.add(tableHeadMap);
        log.info("tableHeadList is jy" +tableHeadList);
        ResourceMeterageRecordVO resourceMeterageRecordVO = new ResourceMeterageRecordVO();


        //==========开发阶段测试时暂时去掉 当前登录用户部分 ======================================================================
        //获取当前登录用户
        FitmgrUser user = SecurityUtils.getUser();
        if(null == user){
            throw new BusinessException("用户未登录");
        }

        //=====================================================================================================================*/

        List<ResourceMeterageRecord> meterageRecordList;
        List<Map<String, String>> mapList = new ArrayList<>();
        Map<Integer, Map<String, String>> indexMap = new HashMap<>();


                meterageRecordList = getResourceMeterageRecordS(resourceMeterageRecordVO);

                if (meterageRecordList.size() < 1) {
                    page.setRecords(null);
                    return page;
                }

                //遍历计量记录，将属于同一个租户的用户的 计量值累加

                for (ResourceMeterageRecord meterageRecord : meterageRecordList) {
                    //获取该条计量记录的租户id
                    String statisticsFlag = statisticsDTO.getStatisticsFlag();
                    Integer tenantId = meterageRecord.getTenantId();
                    Integer projectId = meterageRecord.getProjectId();
                    Integer userId = meterageRecord.getUserId();
                    if("project".equals(statisticsFlag)){
                        Map<String, String> map = indexMap.get(projectId);
                            //没有则新建一个map
                            if (null == map) {
                                map = new HashMap<>();
                                //给各个表头 初始化 值
                                putTableValue(tableHeadList, tenantId, projectId, userId, map);
                                //将该map存入索引map
                                indexMap.put(projectId, map);
                                //该map加入mapList中
                                mapList.add(map);
                            }
                            calculate(meterageRecord, map, tableHeadList);
                    } else if("tenant".equals(statisticsFlag)){
                        Map<String, String> map = indexMap.get(tenantId);
                            //没有则新建一个map
                            if (null == map) {
                                map = new HashMap<>();
                                //给各个表头 初始化 值
                                putTableValue(tableHeadList, tenantId, projectId, userId, map);
                                //将该map存入索引map
                                indexMap.put(tenantId, map);
                                //该map加入mapList中
                                mapList.add(map);
                            }
                            calculate(meterageRecord, map, tableHeadList);
                    }else if("system".equals(statisticsFlag)){
                      //TODO:待开发，暂不支持
                    }
                }

//                break;
//// ===================开发测试阶段暂时去掉其他权限级别=========================================================================
//            case "1"://租户级别
//                resourceMeterageRecordVO.setTenantId(tenantId);
//
//                meterageRecordList = getResourceMeterageRecordS(resourceMeterageRecordVO);
//
//                if (meterageRecordList.size() < 1) {
//                    page.setRecords(null);
//                    return page;
//                }
//
//                //遍历计量记录，将属于同一个project的用户的 计量值累加
//
//                for (ResourceMeterageRecord meterageRecord : meterageRecordList) {
//                    //获取该条计量记录的租户id
//                    Integer tenant_id = meterageRecord.getTenantId();
//                    Integer project_id = meterageRecord.getProjectId();
//                    Integer user_id = meterageRecord.getUserId();
//
//                    //从索引map中获取该租户的 统计值
//                    Map<String, String> map = indexMap.get(project_id);
//                    if (null == map) {//没有则新建一个map
//                        map = new HashMap<>();
//
//                        //给各个表头 初始化 值
//                        putTableValue(tableHeadList, tenant_id, project_id, user_id, map);
//
//                        //将该map存入索引map
//                        indexMap.put(project_id, map);
//                        //该map加入mapList中
//                        mapList.add(map);
//                    }
//
//                    calculate(meterageRecord, map, tableHeadList);
//
//                }
//
//                break;
//
//            case "2"://project级别
//
//                resourceMeterageRecordVO.setProjectId(defaultProject);
//
//                meterageRecordList = getResourceMeterageRecordS(resourceMeterageRecordVO);
//
//                if (meterageRecordList.size() < 1) {
//                    page.setRecords(null);
//                    return page;
//                }
//
//                //遍历计量记录，将属于同一个用户的 计量值累加
//
//                for (ResourceMeterageRecord meterageRecord : meterageRecordList) {
//                    //获取该条计量记录的租户id
//                    Integer tenant_id = meterageRecord.getTenantId();
//                    Integer project_id = meterageRecord.getProjectId();
//                    Integer user_id = meterageRecord.getUserId();
//
//                    //从索引map中获取该租户的 统计值
//                    Map<String, String> map = indexMap.get(user_id);
//                    if (null == map) {//没有则新建一个map
//                        map = new HashMap<>();
//
//                        //给各个表头 初始化 值
//                        putTableValue(tableHeadList, tenant_id, project_id, user_id, map);
//
//                        //将该map存入索引map
//                        indexMap.put(user_id, map);
//                        //该map加入mapList中
//                        mapList.add(map);
//                    }
//
//                    calculate(meterageRecord, map, tableHeadList);
//
//                }
//
//                break;
//
//            case "3"://普通用户级别
//
//                resourceMeterageRecordVO.setUserId(userId);
//
//                meterageRecordList = getResourceMeterageRecordS(resourceMeterageRecordVO);
//
//                if (meterageRecordList.size() < 1) {
//                    page.setRecords(null);
//                    return page;
//                }
//
//                //遍历计量记录，将属于同一个租户的用户的 计量值累加
//
//                for (ResourceMeterageRecord meterageRecord : meterageRecordList) {
//                    //获取该条计量记录的租户id
//                    Integer tenant_id = meterageRecord.getTenantId();
//                    Integer project_id = meterageRecord.getProjectId();
//                    Integer user_id = meterageRecord.getUserId();
//
//                    //从索引map中获取该租户的 统计值
//                    Map<String, String> map = indexMap.get(project_id);
//                    if (null == map) {//没有则新建一个map
//                        map = new HashMap<>();
//
//                        //给各个表头 初始化 值
//                        putTableValue(tableHeadList, tenant_id, project_id, user_id, map);
//
//                        //将该map存入索引map
//                        indexMap.put(project_id, map);
//                        //该map加入mapList中
//                        mapList.add(map);
//                    }
//
//                    calculate(meterageRecord, map, tableHeadList);
//
//                }
//
//                break;
//
//            default:
//                return null;

//// //========================================================================================================================*/
//        }

        page.setTotal(mapList.size());
        page.setPages(PageUtil.totalPage(mapList.size(), (int) page.getSize()));
        int[] startEnd = PageUtil.transToStartEnd((int) page.getCurrent(), (int) page.getSize());

        List<Map<String, String>> statisticsMaps = mapList.subList(startEnd[0], Math.min(startEnd[1], mapList.size()));

        translateIdToName(statisticsMaps);

        page.setRecords(statisticsMaps);
        return page;
    }

    private void translateIdToName(List<Map<String, String>> dataMapList) {
        for (Map<String, String> dataMap : dataMapList) {
            Set<Map.Entry<String, String>> entries = dataMap.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                String prop = entry.getKey();
                String s = dataMap.get(prop);

                if ("tenantName".equals(prop)) {
                    Integer tenantId = StringUtils.isNotBlank(s) ? Integer.parseInt(s) : -1;
                    R<String> translation = remoteTenantService.translateIdToName(CommonConstants.TENANT_PREFIX, tenantId);
                    dataMap.put(prop, translation.getData());
                } else if ("projectName".equals(prop)) {
                    Integer projectId = StringUtils.isNotBlank(s) ? Integer.parseInt(s) : -1;
                    R<String> translation = remoteTenantService.translateIdToName(CommonConstants.PROJECT_PREFIX, projectId);
                    dataMap.put(prop, translation.getData());
                } else if ("userName".equals(prop)) {
                    Integer userId = StringUtils.isNotBlank(s) ? Integer.parseInt(s) : -1;
                    R<String> translation = remoteTenantService.translateIdToName(CommonConstants.USER_PREFIX, userId);
                    dataMap.put(prop, translation.getData());
                }
            }

        }
    }

    private void putTableValue(List<Map<String, String>> tableHeadList, Integer tenantId, Integer projectId, Integer userId, Map<String, String> map) {
        for (Map<String, String> tableHead : tableHeadList) {
            String prop = tableHead.get("prop");
            if ("tenantName".equals(prop)) {
                map.put(prop, tenantId == null ? "" : tenantId + "");
            } else if ("projectName".equals(prop)) {
                map.put(prop, projectId == null ? "" : projectId + "");
            } else if ("userName".equals(prop)) {
                map.put(prop, userId == null ? "" : userId + "");
            } else {
                map.put(prop, "0.00");
            }
        }
    }

    private void calculate(ResourceMeterageRecord meterageRecord, Map<String, String> map, List<Map<String, String>> tableHeadList) {
        //开始时间秒数
        long statisticStartTime = meterageRecord.getStartTime().toEpochSecond(ZoneOffset.of("+8"));
        //结束时间
        LocalDateTime endTime = meterageRecord.getEndTime() == null ? LocalDateTime.now() : meterageRecord.getEndTime();
        //结束时间秒数
        long statisticEndTime = endTime.toEpochSecond(ZoneOffset.of("+8"));
        //计量时长 （小时）
        double statisticTime = (statisticEndTime - statisticStartTime) / 3600.0;

        //获取该条计量记录的组件标识
        String data = meterageRecord.getData();
        JSONObject dataJson = JSON.parseObject(data);
        Map<String,Object> flavorMap = dataJson.getInnerMap();
        DecimalFormat df = new DecimalFormat("0.00");
        for (Map<String, String> header :tableHeadList) {
            //获取之前累加起来的统计值
            double statisticCal = Double.parseDouble(map.get(header.get("prop")));
            //加上该条记录的 规格*时长
            String prop = null;
            if(null != flavorMap.get(header.get("prop"))){
                prop = flavorMap.get(header.get("prop")).toString();
            }
            //此代码块是将数据中的string过滤出来，其余的做计算
            Integer cal = 1;
            if(null != prop){
                Matcher isNum = PATTERN.matcher(prop);
                if(isNum.matches()){
                    cal = Integer.parseInt(prop);
                }else {
                    cal = 1;
                }
            }
            statisticCal += cal* statisticTime;
            map.put(map.get(header.get("prop")), df.format(statisticCal));
        }

    }

    private List<ResourceMeterageRecord> getResourceMeterageRecordS(ResourceMeterageRecordVO resourceMeterageRecordVO) {

        R<List<ResourceMeterageRecord>> listR = meterageRecordService.getList(resourceMeterageRecordVO);
        if (ResponseCodeEnum.SUCCESS.getCode() != listR.getCode()) {
            throw new RuntimeException(BusinessEnum.FEIGN_FAULT.getDescription());
        }
        return listR.getData();
    }

    private boolean isVolumeComponent(String componentCode, List<Map<String, String>> tableHeadList) {
        for (Map<String, String> tableHeadMap : tableHeadList) {
            Set<Map.Entry<String, String>> entries = tableHeadMap.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                if (entry.getValue().equals(componentCode)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public R<List<Map<String, String>>> getTableHead(Integer meterageItem) {
        List<Map<String, String>> tableHeadList = new ArrayList<>();
        List<MeterageProjectProperty> meterageProjectPropertys = meterageProjectPropertyService.list();
        MeterageProject meterageProject = meterageProjectService.getById(meterageItem);
        for (MeterageProjectProperty meterageProjectProperty : meterageProjectPropertys) {
            Map<String, String> tableHead = new HashMap<>();
                if (meterageProject.getId().equals(meterageProjectProperty.getMeterageProjectId()) && null != meterageProjectProperty.getForeignKey()) {
                    tableHead.put("prop", meterageProjectProperty.getForeignKey());
                    tableHead.put("label", meterageProjectProperty.getKeyName() + "(" + meterageProjectProperty.getMeterageUnit() + "小时)");
                    tableHeadList.add(tableHead);
                } else if(meterageProject.getId().equals(meterageProjectProperty.getMeterageProjectId())){
                    tableHead.put("prop", meterageProjectProperty.getSourceKey());
                    tableHead.put("label", meterageProjectProperty.getKeyName() + "(" + meterageProjectProperty.getMeterageUnit() + "小时)");
                    tableHeadList.add(tableHead);
                }

        }
        return R.ok(tableHeadList);
    }


}
