package com.fitmgr.meterage.service.impl;

import cn.hutool.core.date.BetweenFormater;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitmgr.admin.api.dto.ProjectDTO;
import com.fitmgr.admin.api.dto.TenantDTO;
import com.fitmgr.admin.api.dto.UserDTO;
import com.fitmgr.admin.api.entity.*;
import com.fitmgr.admin.api.feign.RemoteAuthService;
import com.fitmgr.admin.api.feign.RemoteProjectService;
import com.fitmgr.admin.api.feign.RemoteTenantService;
import com.fitmgr.admin.api.feign.RemoteUserService;
import com.fitmgr.admin.api.vo.TenantProjectUserVO;
import com.fitmgr.common.core.constant.enums.ApiEnum;
import com.fitmgr.common.core.constant.enums.OperatingRangeEnum;
import com.fitmgr.common.core.util.ExcelData;
import com.fitmgr.common.core.util.ExcelUtil;
import com.fitmgr.common.core.util.R;
import com.fitmgr.meterage.api.dto.MeterageViewDataDTO;
import com.fitmgr.meterage.api.entity.MeterageProject;
import com.fitmgr.meterage.api.entity.MeterageProjectProperty;
import com.fitmgr.meterage.api.entity.ResourceMeterageRecord;
import com.fitmgr.meterage.api.vo.MeterageViewDataVO;
import com.fitmgr.meterage.api.vo.ResourceMeterageRecordListVo;
import com.fitmgr.meterage.api.vo.ResourceMeterageRecordMapVO;
import com.fitmgr.meterage.api.vo.ResourceMeterageRecordVO;
import com.fitmgr.meterage.mapper.MeterageRecordMapper;
import com.fitmgr.meterage.service.IMeterageItemHeaderService;
import com.fitmgr.meterage.service.IMeterageProjectService;
import com.fitmgr.meterage.service.IMeterageRecordService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author dzl
 * @since 2020-02-14
 */
@Slf4j
@Service
@AllArgsConstructor
public class MeterageRecordServiceImpl extends ServiceImpl<MeterageRecordMapper, ResourceMeterageRecord> implements IMeterageRecordService {

    private final RemoteAuthService remoteAuthService;

    private final IMeterageItemHeaderService meterageItemHeaderService;

    private final MeterageRecordMapper meterageRecordMapper;

    private final RemoteTenantService remoteTenantService;

    private final RemoteProjectService remoteProjectService;

    private final RemoteUserService remoteUserService;

    private final IMeterageProjectService meterageProjectService;

    @Override
    public R<Object> getMeterageRecordList(ResourceMeterageRecordVO resourceMeterageRecordVO) {

        // 获取数据权限
        R<AuthCheck> r = remoteAuthService.newAuthCheck(ApiEnum.SELECT_METERAGE_RECORD_PAGE.getCode(), null, null, null);
        AuthCheck authCheck = r.getData();
        if(authCheck.isStatus()){
            String operateCode = authCheck.getOperatingRange();
            log.info("authCheck is {}", authCheck);
            if (OperatingRangeEnum.ALL_CODE.equals(operateCode)) {
                return getMeterageRecordPage(resourceMeterageRecordVO);
            } else if (OperatingRangeEnum.TENANT_CODE.equals(operateCode)) {
                resourceMeterageRecordVO.setTenantIds(authCheck.getTenantIds());
                return getMeterageRecordPage(resourceMeterageRecordVO);
            } else if (OperatingRangeEnum.PROJECT_CODE.equals(operateCode)) {
                if (resourceMeterageRecordVO.getProjectId() == null || resourceMeterageRecordVO.getProjectId() == 0) {
                    List<Integer> projectIds = authCheck.getProjectIds();
                    List<Integer> tenantIds = authCheck.getTenantIds();
                    if(tenantIds != null && projectIds.size() > 0){
                        resourceMeterageRecordVO.setProjects(projectIds);
                    }
                    if(!CollectionUtils.isEmpty(tenantIds)){
                        resourceMeterageRecordVO.setTenantIds(tenantIds);
                    }
                    return getMeterageRecordPage(resourceMeterageRecordVO);
                }else {
                    Integer projectId = resourceMeterageRecordVO.getProjectId();
                    List<Integer> ids = new ArrayList<>();
                    ids.add(projectId);
                    resourceMeterageRecordVO.setProjects(ids);
                }
                return getMeterageRecordPage(resourceMeterageRecordVO);
            }else {
                resourceMeterageRecordVO.setUserId(authCheck.getUserId());
                return getMeterageRecordPage(resourceMeterageRecordVO);
            }
        }
        return R.failed("权限不足");
    }

    @Override
    public void exportExcel(HttpServletResponse response, ResourceMeterageRecordVO resourceMeterageRecordVO) {
        String fileName = "计量数据记录表";
        List<String> list = new ArrayList<>();
        log.info("resourceMeterageRecordVO.getComponentCode() is " + resourceMeterageRecordVO.getComponentCode());
        List<MeterageProjectProperty> title = meterageItemHeaderService.getMeterageItemHeaderList(resourceMeterageRecordVO.getComponentCode());
        log.info("title is jy" + title);
        for (MeterageProjectProperty meterageProjectProperty : title) {
            list.add(meterageProjectProperty.getKeyName());
        }
        list.add("VDC");
        list.add("项目");
        list.add("用户");
        list.add("计量开始时间");
        list.add("计量结束时间");
        list.add("使用时长");

        log.info("list title is jy" + list);
        List<List<Object>> rows = getRows(resourceMeterageRecordVO, title);
        ExcelData data = new ExcelData();
        data.setName(fileName);
        data.setRows(rows);
        data.setTitles(list);
        try {
            ExcelUtil.exportExcel(response, fileName, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public R<Object> getMeterageRecordPage(ResourceMeterageRecordVO resourceMeterageRecordVO) {
        List<Map<String, String>> maps = new ArrayList<>();
        Page<Object> page = new Page<>();
        page.setCurrent(resourceMeterageRecordVO.getCurrent());
        page.setSize(resourceMeterageRecordVO.getSize());
        Page<ResourceMeterageRecordListVo> meterageRecordListPage = new Page<>();
        R<AuthCheck> r = remoteAuthService.newAuthCheck(ApiEnum.SELECT_METERAGE_RECORD_PAGE.getCode(), null, null, null);
        AuthCheck authCheck = r.getData();
        if(authCheck.isStatus()) {
            String operateCode = authCheck.getOperatingRange();
            log.info("authCheck is {}", authCheck);
            if (OperatingRangeEnum.ALL_CODE.equals(operateCode)) {
                meterageRecordListPage = meterageRecordMapper.getMeterageList(page, resourceMeterageRecordVO);
            } else if (OperatingRangeEnum.TENANT_CODE.equals(operateCode)) {
                resourceMeterageRecordVO.setTenantIds(authCheck.getTenantIds());
                meterageRecordListPage = meterageRecordMapper.getMeterageList(page, resourceMeterageRecordVO);
            } else if (OperatingRangeEnum.PROJECT_CODE.equals(operateCode)) {
                if (resourceMeterageRecordVO.getProjectId() == null || resourceMeterageRecordVO.getProjectId() == 0) {
                    List<Integer> projectIds = authCheck.getProjectIds();
                    List<Integer> tenantIds = authCheck.getTenantIds();
                    if(tenantIds != null && projectIds.size() > 0){
                        resourceMeterageRecordVO.setProjects(projectIds);
                    }
                    if(!CollectionUtils.isEmpty(tenantIds)){
                        resourceMeterageRecordVO.setTenantIds(tenantIds);
                    }
                }else {
                    Integer projectId = resourceMeterageRecordVO.getProjectId();
                    List<Integer> ids = new ArrayList<>();
                    ids.add(projectId);
                    resourceMeterageRecordVO.setProjects(ids);
                }
                meterageRecordListPage = meterageRecordMapper.getMeterageList(page, resourceMeterageRecordVO);
            }else {
                resourceMeterageRecordVO.setUserId(authCheck.getUserId());
                meterageRecordListPage = meterageRecordMapper.getMeterageList(page, resourceMeterageRecordVO);
            }
        }
        List<ResourceMeterageRecordListVo> meterageRecordList = meterageRecordListPage.getRecords();
        // 查询租户集合
        Set<Integer> collectTenant = meterageRecordList.stream().map(meterageRecord -> meterageRecord.getTenantId()).collect(Collectors.toSet());
        List<Integer> tenantIdList = new ArrayList<>(collectTenant);
        TenantDTO tenantDTO = new TenantDTO();
        R<List<Tenant>> listTenant = new R<>();
        if(tenantIdList.size() > 0){
            tenantDTO.setTenantIds(tenantIdList);
            listTenant = remoteTenantService.tenantLists(tenantDTO);
        }
        List<Tenant> tenantList = listTenant.getData();
        Map<Integer, Tenant> tenantMap = new HashMap<>();
        if(null != tenantList && tenantList.size() > 0){
            tenantMap = tenantList.stream().collect(Collectors.toMap(Tenant::getId, (p) -> p));
        }
        //查询project集合
        Set<Integer> collectProject = meterageRecordList.stream().map(meterageRecord -> meterageRecord.getProjectId()).collect(Collectors.toSet());
        List<Integer> projectIdList = new ArrayList<>(collectProject);
        ProjectDTO projectDTO = new ProjectDTO();
        R<List<Project>> listProject = new R<>();
        if(projectIdList.size() > 0){
            projectDTO.setProjectIds(projectIdList);
            listProject = remoteProjectService.projectLists(projectDTO);
        }

        List<Project> projectList = listProject.getData();
        Map<Integer, Project> projectMap = new HashMap<>();
        if(null != projectList && projectList.size() > 0){
            projectMap = projectList.stream().collect(Collectors.toMap(Project::getId, (p) -> p));
        }
        //查询用户集合
        Set<Integer> collectUser = meterageRecordList.stream().map(meterageRecord -> meterageRecord.getUserId()).collect(Collectors.toSet());
        List<Integer> userIdList = new ArrayList<>(collectUser);
        UserDTO userDTO = new UserDTO();
        R<List<User>> listUser = new R<>();
        if(userIdList.size() > 0){
            userDTO.setUserIds(userIdList);
            listUser = remoteUserService.userLists(userDTO);
        }

        List<User> userList = listUser.getData();
        Map<Integer, User> userMap = new HashMap<>();
        if(null != userList && userList.size() > 0){
            userMap = userList.stream().collect(Collectors.toMap(User::getId, (p) -> p));
        }
        for (ResourceMeterageRecordListVo resourceMeterageRecord : meterageRecordList) {
            if (tenantMap.get(resourceMeterageRecord.getTenantId())!=null && !"".equals(tenantMap.get(resourceMeterageRecord.getTenantId()))){
                resourceMeterageRecord.setTenantName(tenantMap.get(resourceMeterageRecord.getTenantId()).getName());
            }
            if (projectMap.get(resourceMeterageRecord.getProjectId())!=null && !"".equals(projectMap.get(resourceMeterageRecord.getProjectId()))){
                resourceMeterageRecord.setProjectName(projectMap.get(resourceMeterageRecord.getProjectId()).getName());
            }
            if (userMap.get(resourceMeterageRecord.getUserId())!=null && !"".equals(userMap.get(resourceMeterageRecord.getUserId()))){
                resourceMeterageRecord.setUserName(userMap.get(resourceMeterageRecord.getUserId()).getName());
            }

        }
        log.info("meterageRecordList before jy is {}", meterageRecordList);
        for (ResourceMeterageRecordListVo resourceMeterageRecord : meterageRecordList) {
            TenantProjectUserVO tenantProjectUserVO = new TenantProjectUserVO();
            tenantProjectUserVO.setTenantId(resourceMeterageRecord.getTenantId());
            tenantProjectUserVO.setProjectId(resourceMeterageRecord.getProjectId());
            tenantProjectUserVO.setUserId(resourceMeterageRecord.getUserId());
            TenantProjectUserVO data = remoteTenantService.translation(tenantProjectUserVO).getData();
            JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(resourceMeterageRecord));
            Map<String, String> map = JSONObject.toJavaObject(jsonObject, Map.class);
            //计算使用时长
            String startTime = resourceMeterageRecord.getStartTime();
            String endTime;
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if (resourceMeterageRecord.getEndTime() == null) {
                LocalDateTime endTimeS = LocalDateTime.now();
                endTime = endTimeS.format(dateTimeFormatter);
            } else {
                endTime = resourceMeterageRecord.getEndTime();
            }

            LocalDateTime stareTime1 = LocalDateTime.parse(startTime,dateTimeFormatter);
            LocalDateTime endTime1 = LocalDateTime.parse(endTime,dateTimeFormatter);

            String usageTime = DateUtil.formatBetween(ChronoUnit.MILLIS.between(stareTime1, endTime1), BetweenFormater.Level.SECOND);
            map.put("usageTime", usageTime);
            map.remove("data");
            Map<String, String> map2;
            map2 = JSON.parseObject(resourceMeterageRecord.getData(), HashMap.class);
            map2.put("tenantName", resourceMeterageRecord.getTenantName());
            map2.put("projectName",resourceMeterageRecord.getProjectName());
            map2.put("userName", resourceMeterageRecord.getUserName());
            map.putAll(map2);
            maps.add(map);
        }
        ResourceMeterageRecordMapVO resourceMeterageRecordMapVO = new ResourceMeterageRecordMapVO();
        resourceMeterageRecordMapVO.setMaps(maps);
        resourceMeterageRecordMapVO.setTotal(meterageRecordListPage.getTotal());
        resourceMeterageRecordMapVO.setSize(meterageRecordListPage.getSize());
        resourceMeterageRecordMapVO.setCurrent(meterageRecordListPage.getCurrent());
        log.info("meterageRecordList after jy is {}", maps);
        return R.ok(resourceMeterageRecordMapVO);

    }

    @Override
    public R<List<ResourceMeterageRecord>> getListPage(ResourceMeterageRecordVO resourceMeterageRecordVO) {
        Page<Object> page = new Page<>();
        page.setCurrent(resourceMeterageRecordVO.getCurrent());
        page.setSize(resourceMeterageRecordVO.getSize());
        Page<ResourceMeterageRecord> meterageRecordList = meterageRecordMapper.getListPage(page, resourceMeterageRecordVO);
        List<ResourceMeterageRecord> list= meterageRecordList.getRecords();
        return R.ok(list);
    }

    /**
     * 条件查询计量记录列表
     *
     * @param resourceMeterageRecordVO 筛选条件对象
     * @return
     */
    @Override
    public R<List<ResourceMeterageRecord>> getList(ResourceMeterageRecordVO resourceMeterageRecordVO) {
        return R.ok(meterageRecordMapper.getList(resourceMeterageRecordVO));

    }

    @Override
    public R<List<Map<String, Object>>> getMeterageRecordExport(ResourceMeterageRecordVO resourceMeterageRecordVO) {
        List<Map<String, Object>> maps = new ArrayList<>();
        List<ResourceMeterageRecordListVo> meterageRecordList = meterageRecordMapper.getListVo(resourceMeterageRecordVO);
        // 查询租户集合
        Set<Integer> collectTenant = meterageRecordList.stream().map(meterageRecord -> meterageRecord.getTenantId()).collect(Collectors.toSet());
        List<Integer> tenantIdList = new ArrayList<>(collectTenant);
        TenantDTO tenantDTO = new TenantDTO();
        R<List<Tenant>> listTenant = new R<>();
        if(tenantIdList.size() > 0){
            tenantDTO.setTenantIds(tenantIdList);
            listTenant = remoteTenantService.tenantLists(tenantDTO);
        }
        List<Tenant> tenantList = listTenant.getData();
        Map<Integer, Tenant> tenantMap = new HashMap<>();
        if(null != tenantList && tenantList.size() > 0){
            tenantMap = tenantList.stream().collect(Collectors.toMap(Tenant::getId, (p) -> p));
        }
        //查询project集合
        Set<Integer> collectProject = meterageRecordList.stream().map(meterageRecord -> meterageRecord.getProjectId()).collect(Collectors.toSet());
        List<Integer> projectIdList = new ArrayList<>(collectProject);
        ProjectDTO projectDTO = new ProjectDTO();
        R<List<Project>> listProject = new R<>();
        if(projectIdList.size() > 0){
            projectDTO.setProjectIds(projectIdList);
            listProject = remoteProjectService.projectLists(projectDTO);
        }

        List<Project> projectList = listProject.getData();
        Map<Integer, Project> projectMap = new HashMap<>();
        if(null != projectList && projectList.size() > 0){
            projectMap = projectList.stream().collect(Collectors.toMap(Project::getId, (p) -> p));
        }
        //查询用户集合
        Set<Integer> collectUser = meterageRecordList.stream().map(meterageRecord -> meterageRecord.getUserId()).collect(Collectors.toSet());
        List<Integer> userIdList = new ArrayList<>(collectUser);
        UserDTO userDTO = new UserDTO();
        R<List<User>> listUser = new R<>();
        if(userIdList.size() > 0){
            userDTO.setUserIds(userIdList);
            listUser = remoteUserService.userLists(userDTO);
        }

        List<User> userList = listUser.getData();
        Map<Integer, User> userMap = new HashMap<>();
        if(null != userList && userList.size() > 0){
            userMap = userList.stream().collect(Collectors.toMap(User::getId, (p) -> p));
        }
        for (ResourceMeterageRecordListVo resourceMeterageRecord : meterageRecordList) {
            if (tenantMap.get(resourceMeterageRecord.getTenantId())!=null && !"".equals(tenantMap.get(resourceMeterageRecord.getTenantId()))){
                resourceMeterageRecord.setTenantName(tenantMap.get(resourceMeterageRecord.getTenantId()).getName());
            }
            if (projectMap.get(resourceMeterageRecord.getProjectId())!=null && !"".equals(projectMap.get(resourceMeterageRecord.getProjectId()))){
                resourceMeterageRecord.setProjectName(projectMap.get(resourceMeterageRecord.getProjectId()).getName());
            }
            if (userMap.get(resourceMeterageRecord.getUserId())!=null && !"".equals(userMap.get(resourceMeterageRecord.getUserId()))){
                resourceMeterageRecord.setUserName(userMap.get(resourceMeterageRecord.getUserId()).getName());
            }

        }
        log.info("meterageRecordList before jy is {}", meterageRecordList);
        for (ResourceMeterageRecordListVo resourceMeterageRecord : meterageRecordList) {
            TenantProjectUserVO tenantProjectUserVO = new TenantProjectUserVO();
            tenantProjectUserVO.setTenantId(resourceMeterageRecord.getTenantId());
            tenantProjectUserVO.setProjectId(resourceMeterageRecord.getProjectId());
            tenantProjectUserVO.setUserId(resourceMeterageRecord.getUserId());
            JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(resourceMeterageRecord));
            Map<String, Object> map = JSONObject.toJavaObject(jsonObject, Map.class);
            //计算使用时长
            String startTime = resourceMeterageRecord.getStartTime();
            String endTime;
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if (resourceMeterageRecord.getEndTime() == null) {
                LocalDateTime endTimeS = LocalDateTime.now();
                endTime = endTimeS.format(dateTimeFormatter);
            } else {
                endTime = resourceMeterageRecord.getEndTime();
            }

            LocalDateTime stareTime1 = LocalDateTime.parse(startTime,dateTimeFormatter);
            LocalDateTime endTime1 = LocalDateTime.parse(endTime,dateTimeFormatter);

            String usageTime = DateUtil.formatBetween(ChronoUnit.MILLIS.between(stareTime1, endTime1), BetweenFormater.Level.SECOND);
            map.put("usageTime", usageTime);
            map.remove("data");
            Map<String, String> map2;
            map2 = JSON.parseObject(resourceMeterageRecord.getData(), HashMap.class);
            map2.put("tenantName", resourceMeterageRecord.getTenantName());
            map2.put("projectName",resourceMeterageRecord.getProjectName());
            map2.put("userName", resourceMeterageRecord.getUserName());
            map.putAll(map2);
            maps.add(map);
        }
        log.info("meterageRecordList after jy is {}", maps);
        return R.ok(maps);
    }

    @Override
    public R<Map<Integer, Map<String, List<MeterageViewDataDTO>>>> getViewDate(MeterageViewDataVO meterageViewDataVO) {
        List<Integer> vdcIdList = meterageViewDataVO.getVdcIds();
        if(vdcIdList.size() == 1 && vdcIdList.get(0) == -1){
            vdcIdList = null;
        }
        //默认
        Map<String, String> propertyMap = meterageViewDataVO.getProperty();
        Map<Integer, Map<String, List<MeterageViewDataDTO>>> map = new HashMap<>();
        List<MeterageProject> meterageProjectList = meterageProjectService.list();
        Map<String, MeterageProject> meterageProjectMap = new HashMap<>();
        if(null != meterageProjectList  && meterageProjectList.size() > 0 ){
            meterageProjectMap = meterageProjectList.stream().collect(Collectors.toMap(MeterageProject::getComponentCode, (p) -> p));
        }
        List<ResourceMeterageRecord> resourceMeterageRecords = meterageRecordMapper.getViewList(vdcIdList);
        //通过vdcId查询数据列表(按照创建时间排序)，无权限。

        List<Integer> vdcIds = resourceMeterageRecords.stream().map(ResourceMeterageRecord::getTenantId).collect(Collectors.toList());
        for (Integer vdcId:vdcIds) {
            Map<String, List<MeterageViewDataDTO>> resourceMap = new HashMap<>();
            for (ResourceMeterageRecord resourceMeterageRecord:resourceMeterageRecords) {
                JSONObject data = JSONObject.parseObject(resourceMeterageRecord.getData());
                // 配合概览页的改造，之前是传name现在改为传code（getComponentCode）
                String sourceName = meterageProjectMap.get(resourceMeterageRecord.getComponentCode()).getComponentCode();
                LocalDateTime startTime = resourceMeterageRecord.getStartTime();
                LocalDateTime endTime = LocalDateTime.now();
                String usageTime = DateUtil.formatBetween(ChronoUnit.MILLIS.between(startTime, endTime), BetweenFormater.Level.SECOND);
                Long time = ChronoUnit.MILLIS.between(startTime, endTime);
                if(vdcId.toString().equals(resourceMeterageRecord.getTenantId().toString())){
                    if(resourceMap.containsKey(sourceName)){
                        List<MeterageViewDataDTO> meterageViewDataDTOList = resourceMap.get(sourceName);
                        MeterageViewDataDTO meterageViewDataDTO = new MeterageViewDataDTO();
                        for (Map.Entry<String,String> entry :propertyMap.entrySet()) {
                            if (data.get(entry.getValue()) != null) {
                                meterageViewDataDTO.setResourceName(data.get(entry.getValue()).toString());
                            }
                        }
                        meterageViewDataDTO.setUsageTime(usageTime);
                        meterageViewDataDTO.setTime(time);
                        meterageViewDataDTOList.add(meterageViewDataDTO);
                    }else {
                        List<MeterageViewDataDTO> meterageViewDataDTOList = new ArrayList<>();
                        MeterageViewDataDTO meterageViewDataDTO = new MeterageViewDataDTO();
                        for (Map.Entry<String, String> entry : propertyMap.entrySet()) {
                            if (data.get(entry.getValue()) != null) {
                                meterageViewDataDTO.setResourceName(data.get(entry.getValue()).toString());
                            }
                        }
                        meterageViewDataDTO.setUsageTime(usageTime);
                        meterageViewDataDTO.setTime(time);
                        meterageViewDataDTOList.add(meterageViewDataDTO);
                        resourceMap.put(sourceName, meterageViewDataDTOList);
                    }
                }
            }
            map.put(vdcId, resourceMap);
        }
        //封装返回数据。
        return R.ok(map);
    }

    List<List<Object>> getRows(ResourceMeterageRecordVO resourceMeterageRecordVO, List<MeterageProjectProperty> title) {
        List<List<Object>> rows = new ArrayList<>();
        R<List<Map<String, Object>>> r = getMeterageRecordExport(resourceMeterageRecordVO);
        log.info("r is jy" + r);
        List<Map<String, Object>> listRecord = r.getData();
        if (listRecord.size() > 0) {
            for (int i = 0; i < listRecord.size(); i++) {
                List<Object> tem = new ArrayList<>();
                for (MeterageProjectProperty meterageProjectProperty : title) {
                    if (null != meterageProjectProperty.getForeignKey() && !"".equals(meterageProjectProperty.getForeignKey())) {
                        if (ObjectUtils.isNotEmpty(listRecord.get(i).get(meterageProjectProperty.getForeignKey())) && listRecord.get(i).get(meterageProjectProperty.getForeignKey()).toString().trim().equals("openstack")){
                            tem.add("OpenStack");
                        }else if(ObjectUtils.isNotEmpty(listRecord.get(i).get(meterageProjectProperty.getForeignKey())) && listRecord.get(i).get(meterageProjectProperty.getForeignKey()).toString().trim().equals("vmware")){
                            tem.add("Vmware");
                        }else {
                            tem.add(listRecord.get(i).get(meterageProjectProperty.getForeignKey()));
                        }
                    } else {
                        if (ObjectUtils.isNotEmpty(listRecord.get(i).get(meterageProjectProperty.getSourceKey())) && listRecord.get(i).get(meterageProjectProperty.getSourceKey()).toString().trim().equals("openstack")){
                            tem.add("OpenStack");
                        }else if(ObjectUtils.isNotEmpty(listRecord.get(i).get(meterageProjectProperty.getSourceKey())) && listRecord.get(i).get(meterageProjectProperty.getSourceKey()).toString().trim().equals("vmware")){
                            tem.add("Vmware");
                        }else {
                            tem.add(listRecord.get(i).get(meterageProjectProperty.getSourceKey()));
                        }
                    }
                }
                tem.add(listRecord.get(i).get("tenantName"));
                tem.add(listRecord.get(i).get("projectName"));
                tem.add(listRecord.get(i).get("userName"));
                if(ObjectUtils.isNotEmpty(listRecord.get(i).get("startTime")) && listRecord.get(i).get("startTime").toString().contains("T")){
                    tem.add(listRecord.get(i).get("startTime").toString().replace("T"," "));
                }else {
                    tem.add(listRecord.get(i).get("startTime"));
                }
                if (ObjectUtils.isNotEmpty(listRecord.get(i).get("endTime")) && listRecord.get(i).get("endTime").toString().contains("T")){
                    tem.add(listRecord.get(i).get("endTime").toString().replace("T"," "));
                }else {
                    tem.add(listRecord.get(i).get("endTime"));
                }
                tem.add(listRecord.get(i).get("usageTime"));
                rows.add(tem);
            }
        }
        return rows;
    }
}
