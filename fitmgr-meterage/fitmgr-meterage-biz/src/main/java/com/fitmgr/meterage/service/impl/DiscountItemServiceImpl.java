package com.fitmgr.meterage.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitmgr.admin.api.dto.ProjectDTO;
import com.fitmgr.admin.api.dto.TenantDTO;
import com.fitmgr.admin.api.entity.Project;
import com.fitmgr.admin.api.entity.Tenant;
import com.fitmgr.admin.api.feign.RemoteProjectService;
import com.fitmgr.admin.api.feign.RemoteTenantService;
import com.fitmgr.common.core.constant.enums.BusinessEnum;
import com.fitmgr.common.core.constant.enums.DeleteFlagStatusEnum;
import com.fitmgr.common.core.constant.enums.EnableStatusEnum;
import com.fitmgr.common.core.exception.BusinessException;
import com.fitmgr.common.core.util.R;
import com.fitmgr.job.api.entity.Task;
import com.fitmgr.job.api.entity.XxlJobInfo;
import com.fitmgr.job.api.enums.ExecutorRouteStrategyEnum;
import com.fitmgr.job.api.enums.TaskExecTypeEnum;
import com.fitmgr.job.api.enums.TaskTypeEnum;
import com.fitmgr.job.api.sdk.FhJobApiController;
import com.fitmgr.meterage.api.dto.DiscountItemDTO;
import com.fitmgr.meterage.api.entity.ChargeItem;
import com.fitmgr.meterage.api.entity.DiscountItem;
import com.fitmgr.meterage.api.entity.ResourceChargeRecord;
import com.fitmgr.meterage.api.vo.DiscountItemVO;
import com.fitmgr.meterage.concurrent.DeleteDisCountItemThread;
import com.fitmgr.meterage.constant.*;
import com.fitmgr.meterage.mapper.ChargeItemMapper;
import com.fitmgr.meterage.mapper.DiscountItemMapper;
import com.fitmgr.meterage.mapper.ResourceChargeRecordMapper;
import com.fitmgr.meterage.service.IDiscountItemService;
import com.fitmgr.meterage.utils.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author zhangxiaokang
 * @since 2020-10-21
 */
@Slf4j
@Service
@AllArgsConstructor
public class DiscountItemServiceImpl extends ServiceImpl<DiscountItemMapper, DiscountItem> implements IDiscountItemService {

    private static final Integer DEFAULT_TENANT = -1;

    private final DiscountItemMapper discountItemMapper;

    private final ChargeItemMapper chargeItemMapper;

    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;

    private final ResourceChargeRecordMapper resourceChargeRecordMapper;

    private final RemoteTenantService remoteTenantService;

    private final RemoteProjectService remoteProjectService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createDiscountItem(DiscountItemDTO discountItemDTO) {
        log.info("discountItemDTO={}", JSONObject.toJSONString(discountItemDTO));
        if (null == discountItemDTO) {
            return false;
        }
        if (discountItemDTO.getEffectRange() != 1) {
            if (!(discountItemDTO.getPlanTime().isBefore(discountItemDTO.getEndTime()))) {
                throw new BusinessException("计划时间异常，初始和结束时间不允许是同一时间！");
            }
        }
        String disCountUuidStr = UUID.randomUUID().toString();
        discountItemDTO.setUuid(disCountUuidStr);
        //默认手动
        if (null == discountItemDTO.getDiscountType()) {
            discountItemDTO.setDiscountType(1);
            discountItemDTO.setDiscountStatus(1);
        }
        //折扣唯一性校验
        int disCountItem = 0;
        String disCountItemId = discountItemDTO.getChargeId();
        if (null != discountItemDTO.getTenantId()) {
            if (discountItemDTO.getTenantId().equals(DEFAULT_TENANT)) {
                discountItemDTO.setEffectRange(1);
            } else {
                discountItemDTO.setEffectRange(2);
            }
            disCountItem = this.count(Wrappers.<DiscountItem>lambdaQuery().eq(DiscountItem::getChargeId, disCountItemId).eq(DiscountItem::getTenantId, discountItemDTO.getTenantId()));
        } else if (null != discountItemDTO.getProjectId()) {
            discountItemDTO.setEffectRange(3);
            disCountItem = this.count(Wrappers.<DiscountItem>lambdaQuery().eq(DiscountItem::getChargeId, disCountItemId).eq(DiscountItem::getProjectId, discountItemDTO.getProjectId()));
        }
        if (disCountItem > 0) {
            throw new BusinessException(BusinessEnum.CREATE_DISCOUNT_ITEM_DISABLE);
        }
        DiscountItem discountItem = new DiscountItem();
        BeanUtils.copyProperties(discountItemDTO, discountItem);
        if (discountItemDTO.getPlanTime() != null) {
            createStartDisCountTask(discountItemDTO.getPlanTime(), discountItemDTO.getEndTime(), disCountUuidStr);
        }
        final int insertCount;
        try {
            discountItem.setDelFlag(DeleteFlagStatusEnum.VIEW.getStatus());
            discountItem.setCreateTime(LocalDateTime.now());
            if (discountItem.getProjectId() != null) {
                discountItem.setTenantId(DEFAULT_TENANT);
            }
            insertCount = discountItemMapper.insert(discountItem);
            log.info("insertCount = {}", insertCount);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(BusinessEnum.CREATE_DISCOUNT_ITEM_ERROR);
        }
        return true;
    }

    /**
     * 生效任务
     * @param planTime
     * @param endTime
     * @param disCountItemUuid
     */
    void createStartDisCountTask(LocalDateTime planTime, LocalDateTime endTime, String disCountItemUuid) {
        Task startTask = new Task();
        Task endTask = new Task();
        XxlJobInfo jobInfo = new XxlJobInfo();
        jobInfo.setExecutorHandler("defaultBeanHandler");
        jobInfo.setAuthor("discount_create");
        jobInfo.setJobDesc("创建折扣项，计划执行时间：" + planTime);
        // 设置任务的触发为轮询策略
        jobInfo.setExecutorRouteStrategy(ExecutorRouteStrategyEnum.ROUND.getCode());
        startTask.setJobInfo(jobInfo);
        startTask.setUuid(disCountItemUuid + "start");
        startTask.setName(disCountItemUuid + ", 折扣倒计时启用任务");
        startTask.setTaskExecType(TaskExecTypeEnum.SINGLE.getCode());
        startTask.setTaskPeriod("{\"corn\":\"" + DateConvertCronUtil.getCron(planTime) + "\"}");
        startTask.setCallback("com.fitmgr.meterage.job.CreateStartDisCountTaskJob");
        startTask.setTaskType(TaskTypeEnum.CALCULATE_CHARGE.getCode());
        startTask.setSubTaskType(ChargeConstant.CHARGE);
        // 设置计费项uuid
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("disCountItemUuid", disCountItemUuid);
        jsonObject.put("planTime", planTime);
        startTask.setMetadata(jsonObject.toJSONString());

        BeanUtil.copyProperties(startTask, endTask);
        endTask.getJobInfo().setJobDesc("创建折扣项，计划执行时间：" + endTime);
        endTask.setTaskPeriod("{\"corn\":\"" + DateConvertCronUtil.getCron(endTime) + "\"}");
        endTask.setUuid(disCountItemUuid + "end");
        endTask.setName(disCountItemUuid + ", 折扣倒计时禁用任务");
        JSONObject json = new JSONObject();
        json.put("disCountItemUuid", disCountItemUuid);
        json.put("endTime", endTime);
        endTask.setMetadata(json.toJSONString());
        FhJobApiController.create(endTask);
        FhJobApiController.create(startTask);
    }

    @Override
    public boolean updateDiscountItem(DiscountItemDTO discountItemDTO) {
        int discountItemCount = 0;
        if (discountItemDTO.getCurrentDiscount() != null) {
            discountItemCount = this.count(Wrappers.<DiscountItem>lambdaQuery().eq(DiscountItem::getCurrentDiscount, discountItemDTO.getCurrentDiscount()).eq(DiscountItem::getUuid, discountItemDTO.getUuid()));
        }
        if (discountItemCount == 0) {
            FhJobApiController.delete(discountItemDTO.getUuid());
            createStartDisCountTask(discountItemDTO.getPlanTime(), discountItemDTO.getEndTime(), discountItemDTO.getUuid());
        }
        DiscountItem discountItem = new DiscountItem();
        log.info("discountItemDTO is {}", discountItemDTO);
        BeanUtils.copyProperties(discountItemDTO, discountItem);
        log.info("discountItem is {}", discountItem);
        int b = discountItemMapper.updateById(discountItem);
        return b > 0;
    }

    @Override
    public IPage<DiscountItemVO> selectPage(Page page, DiscountItemDTO discountItemDTO) {
        LambdaQueryWrapper<DiscountItem> queryWrapper = Wrappers.<DiscountItem>lambdaQuery()
                .like(StringUtils.isNotBlank(discountItemDTO.getDiscountName()), DiscountItem::getDiscountName, discountItemDTO.getDiscountName()).eq(DiscountItem::getDelFlag, 0).ne(DiscountItem::getEffectRange, 1);
        IPage<DiscountItem> discountItemIPage = discountItemMapper.selectPage(page, queryWrapper);
        IPage<DiscountItemVO> discountItemVOIPage = new Page<>();
        discountItemVOIPage.setCurrent(discountItemIPage.getCurrent());
        discountItemVOIPage.setSize(discountItemIPage.getSize());
        discountItemVOIPage.setTotal(discountItemIPage.getTotal());
        discountItemVOIPage.setPages(discountItemIPage.getPages());
        if (CollectionUtils.isEmpty(discountItemIPage.getRecords())) {
            return discountItemVOIPage;
        }
        List<String> chargeItemUuids = new ArrayList<>(discountItemIPage.getRecords().stream().map(discountItem -> discountItem.getChargeId()).collect(Collectors.toSet()));
        log.info("chargeItemUuids is {}", chargeItemUuids);
        LambdaQueryWrapper<ChargeItem> lambdaQueryWrapper = Wrappers.<ChargeItem>lambdaQuery()
                .in(ChargeItem::getUuid, chargeItemUuids)
                .eq(ChargeItem::getDelFlag, String.valueOf(DeleteFlagStatusEnum.VIEW.getStatus()));
        List<ChargeItem> chargeItems = chargeItemMapper.selectList(lambdaQueryWrapper);
        Map<String, ChargeItem> stringChargeItemMap = chargeItems.stream().collect(Collectors.toMap(ChargeItem::getUuid, (p) -> p));
        List<DiscountItemVO> discountItemVOS = new ArrayList<>();
        // 查询租户集合
        Set<Integer> collectTenant = discountItemIPage.getRecords().stream().map(item -> item.getTenantId()).collect(Collectors.toSet());
        List<Integer> tenantIdList = new ArrayList<>(collectTenant);
        TenantDTO tenantDTO = new TenantDTO();
        R<List<Tenant>> listTenant = new R<>();
        if (tenantIdList.size() > 0) {
            tenantDTO.setTenantIds(tenantIdList);
            listTenant = remoteTenantService.tenantLists(tenantDTO);
        }
        List<Tenant> tenantList = listTenant.getData();
        Map<Integer, Tenant> tenantMap = new HashMap<>();
        if (null != tenantList && tenantList.size() > 0) {
            tenantMap = tenantList.stream().collect(Collectors.toMap(Tenant::getId, (p) -> p));
        }
        //查询project集合
        Set<Integer> collectProject = discountItemIPage.getRecords().stream().map(item -> item.getProjectId()).collect(Collectors.toSet());
        List<Integer> projectIdList = new ArrayList<>(collectProject);
        ProjectDTO projectDTO = new ProjectDTO();
        R<List<Project>> listProject = new R<>();
        if (projectIdList.size() > 0) {
            projectDTO.setProjectIds(projectIdList);
            listProject = remoteProjectService.projectLists(projectDTO);
        }
        List<Project> projectList = listProject.getData();
        Map<Integer, Project> projectMap = new HashMap<>();
        if (null != projectList && projectList.size() > 0) {
            projectMap = projectList.stream().collect(Collectors.toMap(Project::getId, (p) -> p));
        }
        Project project = null;
        for (DiscountItem record : discountItemIPage.getRecords()) {
            DiscountItemVO discountItemVO = new DiscountItemVO();
            BeanUtils.copyProperties(record, discountItemVO);
            if (stringChargeItemMap.get(record.getChargeId()) != null) {
                discountItemVO.setMeterageName(stringChargeItemMap.get(record.getChargeId()).getChargeName());
                if (!record.getTenantId().equals(DEFAULT_TENANT)) {
                    discountItemVO.setTenantName(tenantMap.get(record.getTenantId()).getName());
                }
                if (record.getProjectId() != null) {
                    project = projectMap.get(record.getProjectId());
                    if (null == project) {
                        continue;
                    }
                    discountItemVO.setProjectName(project.getName());
                }
            }
            discountItemVOS.add(discountItemVO);
        }
        discountItemVOIPage.setRecords(discountItemVOS);
        return discountItemVOIPage;
    }

    @Override
    public DiscountItemVO getDisCountDetail(String disCountId) {
        DiscountItemVO discountItemVO = new DiscountItemVO();
        DiscountItem discountItem = this.getOne(Wrappers.<DiscountItem>lambdaQuery().eq(DiscountItem::getUuid, disCountId));
        BeanUtil.copyProperties(discountItem, discountItemVO);
        ChargeItem chargeItem = chargeItemMapper.selectOne(Wrappers.<ChargeItem>lambdaQuery().eq(ChargeItem::getUuid, discountItem.getChargeId()));
        discountItemVO.setMeterageName(chargeItem.getChargeName());
        return discountItemVO;
    }

    @Override
    public boolean deleteDiscountItem(String discountItemId) {
        //删除所有在线任务
        FhJobApiController.delete(discountItemId + "start");
        FhJobApiController.delete(discountItemId + "end");
        DiscountItem discountItem = this.getOne(Wrappers.<DiscountItem>lambdaQuery().eq(DiscountItem::getUuid, discountItemId));
        //结算当前折扣费用，计算时间为一个周期的时间，更换为下级折扣费用，顺序为project->vdc->系统
        // 异步任务处理资源数据
        // 查询资源计费记录中适配了该计费项的所有资源，并对其进行费用结算并停止计费
        boolean b = this.remove(Wrappers.<DiscountItem>lambdaQuery().eq(DiscountItem::getUuid, discountItemId));
        LambdaQueryWrapper<ResourceChargeRecord> lambdaQueryWrapper = Wrappers.<ResourceChargeRecord>lambdaQuery()
                .eq(ResourceChargeRecord::getChargeId, discountItem.getChargeId())
                .eq(ResourceChargeRecord::getResourceOffFlag, 0)
                .eq(ResourceChargeRecord::getDiscountId, discountItemId)
                .isNull(ResourceChargeRecord::getFinishUseTime)
                .eq(ResourceChargeRecord::getEnableFlag, EnableStatusEnum.ENABLE.getStatus())
                .eq(ResourceChargeRecord::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus());
        List<ResourceChargeRecord> resourceChargeRecords = resourceChargeRecordMapper.selectList(lambdaQueryWrapper);
        if (discountItem.getDiscountStatus() == 1 || org.springframework.util.CollectionUtils.isEmpty(resourceChargeRecords)) {
            log.info("=======无该折扣项对应的资源账单记录需要更新结算！！！========");
            return true;
        }
        // 查询计费项信息
        LambdaQueryWrapper<ChargeItem> queryWrapper = Wrappers.<ChargeItem>lambdaQuery()
                .eq(ChargeItem::getUuid, discountItem.getChargeId())
                .eq(ChargeItem::getChargeStatus, EnableStatusEnum.ENABLE.getStatus())
                .eq(ChargeItem::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus());
        ChargeItem chargeItem = chargeItemMapper.selectOne(queryWrapper);

        List<DiscountItem> priorityDiscount = this.list();

        //异步执行停止当前折扣的计费任务
        DeleteDisCountItemThread deleteDisCountItemThread = new DeleteDisCountItemThread(priorityDiscount, chargeItem, resourceChargeRecords);
        threadPoolTaskExecutor.submit(deleteDisCountItemThread);
        //异步执行开始新折扣的计费任务
        return b;
    }

    @Override
    public List<DiscountItemVO> getDisCountItemList(String chargeItemId) {
        log.info("chargeItemId is {}", chargeItemId);
        LambdaQueryWrapper<DiscountItem> discountItemLambdaQueryWrapper = Wrappers.<DiscountItem>lambdaQuery()
                .eq(chargeItemId != null, DiscountItem::getChargeId, chargeItemId)
                .eq(DiscountItem::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus())
                .eq(DiscountItem::getDiscountStatus, EnableStatusEnum.ENABLE.getStatus());
        List<DiscountItem> discountItems = discountItemMapper.selectList(discountItemLambdaQueryWrapper);
        log.info("discountItems = {}", JSONObject.toJSONString(discountItems));
        if (CollectionUtils.isEmpty(discountItems)) {
            return new ArrayList<>();
        }
        List<DiscountItemVO> discountItemVOS = new ArrayList<>();
        discountItems.forEach(discountItem -> {
            DiscountItemVO discountItemVO = new DiscountItemVO();
            BeanUtils.copyProperties(discountItem, discountItemVO);
            discountItemVOS.add(discountItemVO);
        });
        return discountItemVOS;
    }

    @Override
    public void exportExcel(HttpServletResponse response, Page page, DiscountItemDTO discountItemDTO) {
        log.info("========== discount item export ==========");
        // 获取需要导出的数据
        IPage<DiscountItemVO> discountItemVOIPage = this.selectPage(page, discountItemDTO);
        List<DiscountItemVO> records = discountItemVOIPage.getRecords();
        // 数据处理 bdwang
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (DiscountItemVO d : records) {
            d.setCurrentDiscountStr(String.format("%.1f", d.getCurrentDiscount().multiply(new BigDecimal(10))) + "%");
            d.setDiscountTypeName(DiscountTypeEnum.getValue(d.getDiscountType()));
            d.setEffectRangeName(EffectRangeEnum.getValue(d.getEffectRange()));
            d.setDiscountStatusName(DiscountStatusEnum.getValue(d.getDiscountStatus()));
            d.setPlanTimeFormat(dateTimeFormatter.format(d.getPlanTime()));
            d.setEndTimeFormat(dateTimeFormatter.format(d.getEndTime()));
            LocalDateTime currentDiscountEffectTime = d.getCurrentDiscountEffectTime();
            if (currentDiscountEffectTime != null) {
                d.setCurrentDiscountEffectTimeStr(dateTimeFormatter.format(currentDiscountEffectTime));
            }
            d.setCreateTimeFormat(dateTimeFormatter.format(d.getCreateTime()));
        }
        // 获取导出模板
        String xmlTemplateName = discountItemDTO.getXmlTemplateName();
        String fileName = "classpath:export/" + xmlTemplateName + ".xml";
        // 调用导出
        try {
            ResourceLoader resourceLoader = new DefaultResourceLoader();
            InputStream inputStream = resourceLoader.getResource(fileName).getInputStream();
            FieldsSettings fieldsSettings = JaxbUtil.converyToJavaBean(inputStream, FieldsSettings.class);
            if (fieldsSettings != null) {
                List<FieldSetting> fieldSetting = fieldsSettings.getFieldSetting();
                String[] headers = new String[fieldsSettings.getFieldSetting().size()];
                List<FieldSetting> fields = new ArrayList<>(fieldsSettings.getFieldSetting().size());
                for (int i = 0; i < fieldSetting.size(); i++) {
                    headers[i] = fieldSetting.get(i).getFieldCnName();
                    fields.add(fieldSetting.get(i));
                }
                ExcelUtil<DiscountItemVO> ex = new ExcelUtil<>();
                String templateName = ExportTypeEnum.getTemplateName(xmlTemplateName);
                ex.exportExcel(templateName, headers, fields, records, response.getOutputStream(), ChargeConstant.DATE_TYPE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("========== discount item end ==========");
    }
}
