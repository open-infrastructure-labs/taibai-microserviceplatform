package com.fitmgr.meterage.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitmgr.common.core.constant.SecurityConstants;
import com.fitmgr.common.core.constant.enums.*;
import com.fitmgr.common.core.exception.BusinessException;
import com.fitmgr.common.core.util.R;
import com.fitmgr.common.security.util.SecurityUtils;
import com.fitmgr.job.api.entity.Task;
import com.fitmgr.job.api.entity.TaskJobInfo;
import com.fitmgr.job.api.entity.XxlJobInfo;
import com.fitmgr.job.api.enums.ExecutorRouteStrategyEnum;
import com.fitmgr.job.api.enums.TaskExecTypeEnum;
import com.fitmgr.job.api.enums.TaskTypeEnum;
import com.fitmgr.job.api.feign.XxlTaskService;
import com.fitmgr.job.api.sdk.FhJobApiController;
import com.fitmgr.meterage.api.dto.ChargeItemDTO;
import com.fitmgr.meterage.api.dto.ChargeItemPropertyDTO;
import com.fitmgr.meterage.api.dto.DiscountItemDTO;
import com.fitmgr.meterage.api.dto.MeterageProjectPropertyDTO;
import com.fitmgr.meterage.api.entity.*;
import com.fitmgr.meterage.api.vo.ChargeItemPropertyVO;
import com.fitmgr.meterage.api.vo.ChargeItemVO;
import com.fitmgr.meterage.api.vo.DiscountItemVO;
import com.fitmgr.meterage.concurrent.CreateChargeItemThread;
import com.fitmgr.meterage.concurrent.DisableChargeItemThread;
import com.fitmgr.meterage.concurrent.EnableChargeItemThread;
import com.fitmgr.meterage.constant.*;
import com.fitmgr.meterage.mapper.*;
import com.fitmgr.meterage.service.*;
import com.fitmgr.meterage.utils.*;
import com.fitmgr.resource.api.dto.ComponentDTO;
import com.fitmgr.resource.api.feign.RemoteComponentService;
import com.fitmgr.resource.api.feign.RemoteResourceTypeService;
import com.fitmgr.resource.api.vo.ComponentVO;
import com.fitmgr.resource.api.vo.ResourceTypeVO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * <p>
 * 计费项-服务实现类
 * </p>
 *
 * @author zhangxiaokang
 * @since 2020-10-21
 */
@Slf4j
@Service
@AllArgsConstructor
public class MeterageChargeItemServiceImpl extends ServiceImpl<ChargeItemMapper, ChargeItem> implements IMeterageChargeItemService {

    private final ChargeItemMapper chargeItemMapper;

    private final XxlTaskService xxlTaskService;

    private final RemoteComponentService componentService;

    private final IDiscountItemService discountItemService;

    private final MeterageProjectMapper meterageProjectMapper;

    private final RemoteResourceTypeService resourceTypeService;

    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;

    private final IMeterageProjectService meterageProjectService;

    private final ChargeItemPropertyMapper chargeItemPropertyMapper;

    private final ResourceChargeRecordMapper resourceChargeRecordMapper;

    private final IResourceChargeRecordService resourceChargeRecordService;

    private final MeterageProjectPropertyMapper meterageProjectPropertyMapper;

    private final IMeterageProjectPropertyService meterageProjectPropertyService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createChargeItem(ChargeItemDTO chargeItemDTO) {
        log.info("新增计费项入参：chargeItemDTO = {}", JSONObject.toJSONString(chargeItemDTO));
        boolean repNameResult = this.validateChargeName(chargeItemDTO);
        if (repNameResult) {
            throw new BusinessException(BusinessEnum.CHARGE_ITEM_NAME_REPETITION);
        }

        // 校验同一计量项计费项属性/属性值是否重复
        List<ChargeItemPropertyDTO> insertChargeItemPropertyDTOS = chargeItemDTO.getChargeItemPropertyDTOS();
        Set<String> keySet = new HashSet<>();
        insertChargeItemPropertyDTOS.stream().forEach(chargeItemPropertyDTO -> keySet.add(chargeItemPropertyDTO.getChargePropertyKey()));
        if (keySet.size() < insertChargeItemPropertyDTOS.size()) {
            throw new BusinessException(BusinessEnum.CHARGE_ITEM_PROPERTY_ERROR);
        }

        //校验是否已经创建了无条件限制的计费项
        LambdaQueryWrapper<ChargeItem> queryMeterageWrapper = Wrappers.<ChargeItem>lambdaQuery()
                .eq(ChargeItem::getMeterageItemId, chargeItemDTO.getMeterageItemId())
                .eq(ChargeItem::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus());
        List<ChargeItem> chargeItems = chargeItemMapper.selectList(queryMeterageWrapper);
        if (chargeItems.size() > 0) {
            String chargeUuid = chargeItems.get(0).getUuid();
            LambdaQueryWrapper<ChargeItemProperty> queryChargeItemPropertiesWrapper = Wrappers.<ChargeItemProperty>lambdaQuery()
                    .eq(ChargeItemProperty::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus())
                    .eq(ChargeItemProperty::getChargeUuid, chargeUuid);
            // 该计量项下所有计费项所对应的所有计费属性
            List<ChargeItemProperty> chargeItemProperties = chargeItemPropertyMapper.selectList(queryChargeItemPropertiesWrapper);
            if (chargeItemProperties.size() == 0) {
                throw new BusinessException(BusinessEnum.CHARGE_ITEM_NOT_ALLOW_CREATE);
            } else if (chargeItemDTO.getChargeItemPropertyDTOS().size() == 0) {
                throw new BusinessException(BusinessEnum.CHARGE_ITEM_NOT_ALLOW_CREATE);
            }
        }
        // 校验计费项属性/属性值是在库中已存在重复
        final boolean chargePropertiesResult = this.validateChargeProperties(chargeItemDTO);
        if (chargePropertiesResult) {
            throw new BusinessException(BusinessEnum.CHARGE_ITEM_PROPERTY_REPETITION);
        }

        // 创建计费项
        ChargeItem insertChargeItem = new ChargeItem();
        BeanUtils.copyProperties(chargeItemDTO, insertChargeItem);
        String chargeUuidStr = UUID.randomUUID().toString();
        // 解析计费单位
        Integer chargeFlavorUnit = chargeItemDTO.getChargeFlavorUnit();
        Integer chargeFlavorTime = chargeItemDTO.getChargeFlavorTime();
        String unitName = ChargeFlavorUnitEnum.getUnitName(chargeFlavorUnit);
        String timeName = ChargeFlavorTimeEnum.getTimeName(chargeFlavorTime);
        // 设置备注
        List<String> remarkStr = new ArrayList<>();
        // 初始化时间
        String timeStr = DateTimeConvertUtil.getTimeStr(LocalDateTime.now());
        String remark = timeStr + " " + ChargeConstant.CREATE_CHANGE_ITEM.replace(ChargeConstant.CONCURRENT_PRICE, chargeItemDTO.getPrice().toString()) + ChargeConstant.UNITARY + "/" + unitName + "/" + timeName;
        remarkStr.add(remark);
        insertChargeItem.setRemark(JSONObject.toJSONString(remarkStr));
        insertChargeItem.setUuid(chargeUuidStr);
        insertChargeItem.setDelFlag(DeleteFlagStatusEnum.VIEW.getStatus());
        insertChargeItem.setCreateTime(LocalDateTime.now());

        // 入库计费表
        log.info("insertChargeItem = {}", JSONObject.toJSONString(insertChargeItem));
        final int insertCount = chargeItemMapper.insert(insertChargeItem);
        if (insertCount != 1) {
            throw new BusinessException(BusinessEnum.CREATE_CHARGE_ITEM_ERROR);
        }

        // 填充计费项属性
        List<ChargeItemPropertyDTO> chargeItemPropertyDTOS = chargeItemDTO.getChargeItemPropertyDTOS();
        List<ChargeItemProperty> chargeItemPropertyList = new ArrayList<>();
        for (ChargeItemPropertyDTO chargeItemPropertyDTO : chargeItemPropertyDTOS) {
            ChargeItemProperty chargeItemProperty = new ChargeItemProperty();
            BeanUtils.copyProperties(chargeItemPropertyDTO, chargeItemProperty);
            String chargePropertyUuid = UUID.randomUUID().toString();
            chargeItemProperty.setUuid(chargePropertyUuid);
            chargeItemProperty.setChargeUuid(chargeUuidStr);
            chargeItemProperty.setDelFlag(DeleteFlagStatusEnum.VIEW.getStatus());
            chargeItemProperty.setCreateTime(LocalDateTime.now());
            chargeItemPropertyList.add(chargeItemProperty);
        }

        // 入库计费属性表
        log.info("chargeItemPropertyList = {}", JSONObject.toJSONString(chargeItemPropertyList));
        int chargeItemPropertiesCount = 0;
        if (chargeItemPropertyList.size() > 0) {
            chargeItemPropertiesCount = chargeItemPropertyMapper.saveChargeItemProperties(chargeItemPropertyList);
            log.info("chargeItemPropertiesCount count {}", chargeItemPropertiesCount);
            if (chargeItemPropertiesCount == 0) {
                throw new BusinessException(BusinessEnum.CREATE_CHARGE_ITEM_PROPERTY_ERROR);
            }
        }
        // 新增系统折扣项
        List<DiscountItemVO> disCountItemList = discountItemService.getDisCountItemList(insertChargeItem.getUuid());
        if (CollectionUtils.isEmpty(disCountItemList)) {
            log.info("该计量项目下无折扣项，需要创建一个该计费项的系统折扣项！");
            // 该计费项下面无任何折扣项，添加系统折扣
            DiscountItemDTO discountItemDTO = new DiscountItemDTO();
            discountItemDTO.setUuid(UUID.randomUUID().toString());
            MeterageProject meterageProject = meterageProjectMapper.selectById(chargeItemDTO.getMeterageItemId());
            discountItemDTO.setDiscountName(ChargeConstant.DISCOUNT_NAME + meterageProject.getName());
            discountItemDTO.setMeterageId(chargeItemDTO.getMeterageItemId());
            discountItemDTO.setChargeId(chargeUuidStr);
            discountItemDTO.setCurrentDiscount(ChargeConstant.SYSTEM_DISCOUNT);
            discountItemDTO.setCurrentDiscountEffectTime(LocalDateTime.now());
            discountItemDTO.setDiscountType(ChargeConstant.DISCOUNT_TYPE);
            discountItemDTO.setEffectRange(ChargeConstant.EFFECT_RANGE);
            discountItemDTO.setTenantId(ChargeConstant.TENANT_ID);
            discountItemDTO.setDiscountStatus(ChargeConstant.DISCOUNT_STATUS);
            log.info("discountItemDTO = {}", JSONObject.toJSONString(discountItemDTO));
            // 创建默认系统折扣，入库计费属性表
            discountItemService.createDiscountItem(discountItemDTO);
            log.info("创建系统折扣成功！");
        }

        // 初次创建计费项如果为禁用状态(1)，则不匹配资源
        if (chargeItemDTO.getChargeStatus().equals(EnableStatusEnum.DISABLE.getStatus())) {
            return true;
        }

        // 异步，匹配计费记录已经创建的资源需要匹配计费记录，并开始计费
        CreateChargeItemThread chargeItemThread = new CreateChargeItemThread(insertChargeItem, chargeItemPropertyList);
        threadPoolTaskExecutor.execute(chargeItemThread);
        return true;
    }

    @Override
    public ChargeItemVO findChargeItemVOByUuid(String chargeId) {
        LambdaQueryWrapper<ChargeItem> queryChargeItmWrapper = Wrappers.<ChargeItem>lambdaQuery()
                .eq(ChargeItem::getUuid, chargeId)
                .eq(ChargeItem::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus());
        ChargeItem chargeItem = chargeItemMapper.selectOne(queryChargeItmWrapper);
        ChargeItemVO chargeItemVO = new ChargeItemVO();
        if (null == chargeItem) {
            return chargeItemVO;
        }
        BeanUtils.copyProperties(chargeItem, chargeItemVO);
        LambdaQueryWrapper<ChargeItemProperty> chargeItemPropertyLambdaQueryWrapper = Wrappers.<ChargeItemProperty>lambdaQuery()
                .eq(ChargeItemProperty::getChargeUuid, chargeId)
                .eq(ChargeItemProperty::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus());
        List<ChargeItemProperty> chargeItemPropertyList = chargeItemPropertyMapper.selectList(chargeItemPropertyLambdaQueryWrapper);
        if (CollectionUtils.isEmpty(chargeItemPropertyList)) {
            return chargeItemVO;
        }
        // 填充云平台名称、计量项名称、计费单位名称等信息
        R<ResourceTypeVO> resourceTypeVOR = resourceTypeService.get(chargeItemVO.getCloudPlatformId());
        chargeItemVO.setCloudPlatformName(resourceTypeVOR.getData().getResourceTypeName());
        LambdaQueryWrapper<MeterageProject> projectLambdaQueryWrapper = Wrappers.<MeterageProject>lambdaQuery()
                .eq(MeterageProject::getId, chargeItemVO.getMeterageItemId())
                .eq(MeterageProject::getDelFlag, String.valueOf(DeleteFlagStatusEnum.VIEW.getStatus()));
        MeterageProject meterageProject = meterageProjectMapper.selectOne(projectLambdaQueryWrapper);
        chargeItemVO.setMeterageItemName(meterageProject.getName());
        List<ChargeItemPropertyVO> chargeItemPropertyVOS = new ArrayList<>();

        // 计量项属性中文填充
        Integer meterageItemId = chargeItem.getMeterageItemId();
        MeterageProjectPropertyDTO meterageProjectPropertyDTO = new MeterageProjectPropertyDTO();
        meterageProjectPropertyDTO.setMeterageProjectId(meterageItemId);
        List<MeterageProjectProperty> meterageProjectProperties = meterageProjectPropertyService.selectFilterForChargeItem(meterageProjectPropertyDTO);
        if (CollectionUtils.isEmpty(meterageProjectProperties)) {
            return chargeItemVO;
        }
        Map<String, String> keyToNameMap = new ConcurrentHashMap<>();
        meterageProjectProperties.forEach(meterageProjectProperty -> {
            String key = StringUtils.isNotBlank(meterageProjectProperty.getForeignKey()) ? meterageProjectProperty.getForeignKey() : meterageProjectProperty.getSourceKey();
            keyToNameMap.put(key, meterageProjectProperty.getKeyName());
        });

        // 根据计量项id查询计量项属性,暂不处理
        List<ChargeItemProperty> computerProperty = new ArrayList<>();
        Iterator<ChargeItemProperty> iterator = chargeItemPropertyList.iterator();
        while (iterator.hasNext()) {
            ChargeItemProperty next = iterator.next();
            if (chargeItem.getComponentCode().equals(ResourceViewNameConvertEnum.COMPUTER_INSTANCE.getComponentCode()) &&
                    ("disk".equals(next.getChargePropertyKey()) ||
                            "cpu".equals(next.getChargePropertyKey()) ||
                            "memory".equals(next.getChargePropertyKey()))) {
                computerProperty.add(next);
                iterator.remove();
                continue;
            }
            ChargeItemPropertyVO chargeItemPropertyVO = new ChargeItemPropertyVO();
            BeanUtils.copyProperties(next, chargeItemPropertyVO);
            // 计量项/计费项名称
            if (StringUtils.isNotBlank(keyToNameMap.get(chargeItemPropertyVO.getChargePropertyKey()))) {
                chargeItemPropertyVO.setChargePropertyKeyName(keyToNameMap.get(chargeItemPropertyVO.getChargePropertyKey()));
            }
            chargeItemPropertyVOS.add(chargeItemPropertyVO);
        }
        // 处理虚拟机规格
        if (!CollectionUtils.isEmpty(computerProperty)) {
            Map<String, ChargeItemProperty> collect = computerProperty.stream().collect(Collectors.toMap(ChargeItemProperty::getChargePropertyKey, (p) -> p));
            String cpu = collect.get("cpu").getChargePropertyValue();
            String memory = collect.get("memory").getChargePropertyValue();
            String disk = collect.get("disk").getChargePropertyValue();
            StringBuffer sbf = new StringBuffer();
            sbf.append("CPU:").append(cpu).append("核").append("内存:").append(memory).append("G").append("磁盘:").append(disk).append("G");
            log.info("chargeItemPropertyVOS valie is {}", JSONObject.toJSONString(chargeItemPropertyVOS));
            Integer max = 0;
            if (!CollectionUtils.isEmpty(chargeItemPropertyVOS)) {
                max = Collections.max(chargeItemPropertyVOS.stream().map(chargeItemPropertyVO -> chargeItemPropertyVO.getId()).collect(Collectors.toList()));
            } else {
                max = 1;
            }
            ChargeItemPropertyVO chargeItemPropertyVO = new ChargeItemPropertyVO();
            chargeItemPropertyVO.setId(max + 10);
            chargeItemPropertyVO.setChargePropertyKeyName("云主机规格");
            chargeItemPropertyVO.setChargePropertyKey("flavor_id");
            chargeItemPropertyVO.setChargePropertyValue(sbf.toString());
            chargeItemPropertyVOS.add(chargeItemPropertyVO);
        }
        chargeItemVO.setChargeItemPropertyVOS(chargeItemPropertyVOS);
        return chargeItemVO;
    }

    @Override
    public List<MeterageProject> getComponentListByCondition(Integer resourceId) {
        log.info("resourceId = " + resourceId);
        if (null == resourceId) {
            return new ArrayList<>();
        }
        ComponentDTO componentDTO = new ComponentDTO();
        componentDTO.setResourceId(resourceId);
        R<List<ComponentVO>> componentResult = componentService.list(componentDTO);
        if (null == componentResult) {
            log.info("调用资源管理查询组件集合失败！");
            return new ArrayList<>();
        }
        List<ComponentVO> componentVOS = componentResult.getData();
        if (CollectionUtils.isEmpty(componentVOS)) {
            return new ArrayList<>();
        }

        // 转换资源管理查询出来的资源组件集合
        Map<String, ComponentVO> componentVOMap = componentVOS.stream().collect(Collectors.toMap(ComponentVO::getComponentCode, (p) -> p));
        if (CollectionUtils.isEmpty(componentVOMap)) {
            return new ArrayList<>();
        }
        // 查询所有计量项
        List<MeterageProject> meterageProjects = meterageProjectService.list();
        if (CollectionUtils.isEmpty(meterageProjects)) {
            return new ArrayList<>();
        }
        Iterator<MeterageProject> iterator = meterageProjects.iterator();
        while (iterator.hasNext()) {
            MeterageProject meterageProject = iterator.next();
            if (componentVOMap.get(meterageProject.getComponentCode()) != null) {
                continue;
            }
            iterator.remove();
        }
        log.info("======= meterageProjects list is： {} ======", CollectionUtils.isEmpty(meterageProjects) ? null : JSONObject.toJSONString(meterageProjects));
        return meterageProjects;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateChargeItem(ChargeItemDTO chargeItemDTO) {
        if (StringUtils.isBlank(chargeItemDTO.getUuid())) {
            throw new BusinessException(BusinessEnum.CHARGE_ITEM_UUID_NULL);
        }
        // 计费项的修改只能修改名称/计费单位/折前单价/生效时间，不能修改计费属性
        LambdaQueryWrapper<ChargeItem> queryWrapper = Wrappers.<ChargeItem>lambdaQuery()
                .eq(ChargeItem::getUuid, chargeItemDTO.getUuid())
                .eq(ChargeItem::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus());
        ChargeItem chargeItem = chargeItemMapper.selectOne(queryWrapper);
        if (null == chargeItem) {
            throw new BusinessException(BusinessEnum.CHARGE_ITEM_IS_NULL);
        }
        // 重名校验
        LambdaQueryWrapper<ChargeItem> wrapper = Wrappers.<ChargeItem>lambdaQuery()
                .eq(ChargeItem::getChargeName, chargeItemDTO.getChargeName())
                .eq(ChargeItem::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus())
                .ne(ChargeItem::getUuid, chargeItemDTO.getUuid());
        List<ChargeItem> chargeItems = chargeItemMapper.selectList(wrapper);
        if (!CollectionUtils.isEmpty(chargeItems)) {
            throw new BusinessException(BusinessEnum.UPDATE_CHARGE_ITEM_NAME_REPT);
        }
        // 获取界面传递计费项名称
        String updateChargeName = chargeItemDTO.getChargeName();
        BigDecimal updatePrice = chargeItemDTO.getPrice();
        LocalDateTime updatePlanExecuteTime = chargeItemDTO.getPlanExecuteTime();
        // 获取数据库计费项名称、价格、期望执行时间
        String dbChargeName = chargeItem.getChargeName();
        BigDecimal dbPrice = chargeItem.getPrice();
        if (updatePrice.compareTo(dbPrice) == 0) {
            if (updateChargeName.equals(dbChargeName)) {
                log.info("计费项单价和名称未做任何修改，不做任何操作!");
            } else {
                // 只修改计费项名称
                String chargeItemRemark = chargeItem.getRemark();
                List<String> remarkStr = null;
                if (StringUtils.isBlank(chargeItemRemark)) {
                    remarkStr = new ArrayList<>();
                } else {
                    remarkStr = JSONObject.parseObject(chargeItemRemark, List.class);
                }
                StringBuilder stringBuilder = new StringBuilder();
                StringBuilder sb = new StringBuilder();
                stringBuilder.append(DateTimeConvertUtil.getTimeStr(LocalDateTime.now())).append(" ");
                if (!chargeItemDTO.getChargeName().equals(chargeItem.getChargeName())) {
                    sb.append(ChargeConstant.METERAGE_ITEM_NAME).append(chargeItemDTO.getChargeName()).append("\r\n");
                }
                String remartStr = sb.toString();
                if (StringUtils.isNotBlank(remartStr)) {
                    String remStr = stringBuilder.append(remartStr).toString();
                    remarkStr.add(remStr);
                }
                ChargeItem updateChargeItem = new ChargeItem();
                updateChargeItem.setId(chargeItem.getId());
                updateChargeItem.setChargeName(updateChargeName);
                updateChargeItem.setUpdateTime(LocalDateTime.now());
                updateChargeItem.setRemark(JSONObject.toJSONString(remarkStr));
                chargeItemMapper.updateById(updateChargeItem);
            }
            return true;
        } else {
            // 价格发生变更，期望执行时间不能为空！
            if (null == updatePlanExecuteTime) {
                throw new BusinessException(BusinessEnum.UPDATE_CHAEGE_ITEM_EXE_TIME_NULL);
            }
        }

        // 计划更新计费项
        if (chargeItemDTO.getPlanExecuteTime().isBefore(LocalDateTime.now())) {
            log.error("计划执行时间早于当前时间，创建定时任务错误！计划执行时间：{}，当前时间：{}", chargeItemDTO.getPlanExecuteTime(), LocalDateTime.now());
            throw new BusinessException(BusinessEnum.UPDATE_CHARGE_ITEM_DATE_ERROR);
        }
        // 定时计划执行--》刪除原有定时任务，新增新的定时任务
        TaskJobInfo taskJobInfo = new TaskJobInfo();
        taskJobInfo.setUuid(chargeItemDTO.getUuid());
        List<TaskJobInfo> taskJobInfos = FhJobApiController.queryList(taskJobInfo);
        if (!CollectionUtils.isEmpty(taskJobInfos)) {
            // 执行状态 0-就绪 1-执行中 2-执行完成 3-执行失败
            for (TaskJobInfo jobInfo : taskJobInfos) {
                log.info("========== 删除已存在的更新计划执行计费项任务 {}==========", ChargeConstant.UPDATE_CHARGE + chargeItemDTO.getChargeName());
                xxlTaskService.delete(jobInfo.getUuid(), SecurityConstants.FROM_IN);
            }
        }
        log.info("======添加定时任务，计费项定时执行时间为：{}======", chargeItemDTO.getPlanExecuteTime());
        Task task = new Task();
        XxlJobInfo jobInfo = new XxlJobInfo();
        jobInfo.setExecutorHandler("defaultBeanHandler");
        jobInfo.setAuthor("admin_update");
        jobInfo.setAuthor(SecurityUtils.getUser().getUsername());
        jobInfo.setJobDesc("更新计费项，计费项计划执行时间：" + chargeItemDTO.getPlanExecuteTime());
        // 设置任务的触发为轮询策略
        jobInfo.setExecutorRouteStrategy(ExecutorRouteStrategyEnum.ROUND.getCode());
        task.setJobInfo(jobInfo);
        task.setUuid(chargeItemDTO.getUuid());
        task.setName(ChargeConstant.UPDATE_CHARGE + chargeItemDTO.getChargeName());
        task.setTaskExecType(TaskExecTypeEnum.SINGLE.getCode());
        task.setTaskPeriod("{\"corn\":\"" + DateConvertCronUtil.getCron(chargeItemDTO.getPlanExecuteTime()) + "\"}");
        task.setCallback("com.fitmgr.meterage.job.UpdateChargeItemJob");
        task.setTaskType(TaskTypeEnum.CALCULATE_CHARGE.getCode());
        task.setSubTaskType(ChargeConstant.CHARGE);
        // 设置计费项uuid
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(ChargeConstant.CHARGE_ID, chargeItemDTO.getUuid());
        task.setMetadata(jsonObject.toJSONString());
        boolean resultFlag = FhJobApiController.create(task);
        log.info("====== 定时任务执行结果：{}==========", resultFlag);

        // 计划生效，更新计费项
        ChargeItem updateChargeItem = new ChargeItem();
        updateChargeItem.setId(chargeItem.getId());
        LocalDateTime planExecuteTime = chargeItemDTO.getPlanExecuteTime();
        updateChargeItem.setPlanExecuteTime(planExecuteTime);
        updateChargeItem.setUpdateTime(LocalDateTime.now());
        updateChargeItem.setPlanExecuteData(JSONObject.toJSONString(chargeItemDTO));

        // 设置备注
        String chargeItemRemark = chargeItem.getRemark();
        List<String> remarkStr = null;
        if (StringUtils.isBlank(chargeItemRemark)) {
            remarkStr = new ArrayList<>();
        } else {
            remarkStr = JSONObject.parseObject(chargeItemRemark, List.class);
        }
        Integer chargeFlavorUnit = chargeItemDTO.getChargeFlavorUnit();
        Integer chargeFlavorTime = chargeItemDTO.getChargeFlavorTime();
        String unitName = ChargeFlavorUnitEnum.getUnitName(chargeFlavorUnit);
        String timeName = ChargeFlavorTimeEnum.getTimeName(chargeFlavorTime);
        String timeStr = DateTimeConvertUtil.getTimeStr(LocalDateTime.now());
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        stringBuilder.append(timeStr).append(" ");
        if (!chargeItemDTO.getChargeName().equals(chargeItem.getChargeName())) {
            // 计费项名称发生变化
            sb.append(ChargeConstant.METERAGE_ITEM_NAME).append(chargeItemDTO.getChargeName()).append("\r\n");
        }
        if (!chargeItemDTO.getPrice().equals(chargeItem.getPrice())) {
            // 折前单价发生变化
            sb.append(ChargeConstant.METERAGE_ITEM_CHANGE.replace(ChargeConstant.CONCURRENT_PRICE, chargeItemDTO.getPrice().toString())).append(ChargeConstant.UNITARY).append("/").append(unitName).append("/").append(timeName).append("\r\n");
        }
        if (planExecuteTime != null) {
            // 计费项计划时间发生改变
            sb.append(ChargeConstant.METERAGE_ITEM_EXPLANT_TIME).append(DateTimeConvertUtil.getTimeStr(chargeItemDTO.getPlanExecuteTime())).append("\r\n");
        }
        String remartStr = sb.toString();
        if (StringUtils.isNotBlank(remartStr)) {
            String remStr = stringBuilder.append(remartStr).toString();
            remarkStr.add(remStr);
        }
        updateChargeItem.setChargeName(chargeItemDTO.getChargeName());
        updateChargeItem.setPrice(chargeItemDTO.getPrice());
        updateChargeItem.setRemark(JSONObject.toJSONString(remarkStr));
        updateChargeItem.setExecuteFlag(DeleteFlagStatusEnum.VIEW.getStatus());
        chargeItemMapper.updateById(updateChargeItem);
        return true;
    }

    /**
     * 立即更新计费项
     *
     * @param chargeItemDTO
     * @param chargeItem
     * @return
     */
    private boolean immediateUpdateChargeItem(ChargeItemDTO chargeItemDTO, ChargeItem chargeItem) {
        // 判断任务中心是否有待执行的期望执行更新的计费项，如果有则需要删除
        this.deleteTaskJob(chargeItemDTO);
        // 禁用状态更新计费项不需要结算资源及新增记录
        if (chargeItem.getChargeStatus().equals(EnableStatusEnum.DISABLE.getStatus())) {
            log.info("========= 当前计费项为禁用状态，只需要更新计费项！ ========");
            // 立即生效更新计费项
            this.updateChargeItem(chargeItemDTO, chargeItem);
            return true;
        }
        // 查询有没有适配了该计费项的资源，还未进行结算的
        LambdaQueryWrapper<ResourceChargeRecord> lambdaQueryWrapper = Wrappers.<ResourceChargeRecord>lambdaQuery()
                .eq(ResourceChargeRecord::getChargeId, chargeItem.getUuid())
                .eq(ResourceChargeRecord::getEnableFlag, EnableStatusEnum.ENABLE.getStatus())
                .eq(ResourceChargeRecord::getResourceOffFlag, 0)
                .eq(ResourceChargeRecord::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus())
                .isNull(ResourceChargeRecord::getFinishUseTime);
        List<ResourceChargeRecord> dbResourceChargeRecordList = resourceChargeRecordMapper.selectList(lambdaQueryWrapper);
        if (CollectionUtils.isEmpty(dbResourceChargeRecordList)) {
            // 不存在资源计费记录的更新及新增
            log.info("========= 无需要更新的资源计费记录！ ========");
            // 立即生效更新计费项
            this.updateChargeItem(chargeItemDTO, chargeItem);
            return true;
        }
        // 删除已存在的定时任务
        LambdaQueryWrapper<ResourceChargeRecord> dbLambdaQueryWrapper = Wrappers.<ResourceChargeRecord>lambdaQuery()
                .eq(ResourceChargeRecord::getChargeId, chargeItem.getUuid());
        List<ResourceChargeRecord> chargeRecordList = resourceChargeRecordMapper.selectList(dbLambdaQueryWrapper);
        if (!CollectionUtils.isEmpty(chargeRecordList)) {
            Set<String> collect = chargeRecordList.stream().map(resourceChargeRecord -> resourceChargeRecord.getCmpInstanceName()).collect(Collectors.toSet());
            for (String cmpInstance : collect) {
                TaskJobInfo taskJobInfo = new TaskJobInfo();
                taskJobInfo.setJobDesc(ChargeConstant.UPDATE_CHARGE + cmpInstance);
                taskJobInfo.setName(ChargeConstant.UPDATE_CHARGE + cmpInstance);
                List<TaskJobInfo> taskJobInfos = FhJobApiController.queryList(taskJobInfo);
                if (CollectionUtils.isEmpty(taskJobInfos)) {
                    continue;
                }
                for (TaskJobInfo jobInfo : taskJobInfos) {
                    log.info("======删除未执行的需要更新的计费项资源！======");
                    xxlTaskService.delete(jobInfo.getUuid(), SecurityConstants.FROM_IN);
                }
            }
        }

        // 结算现有资源记录费用
        String remark = ChargeConstant.CHARGE_ITEM_CHANGE.replace(ChargeConstant.CONCURRENT_PRICE, chargeItemDTO.getPrice().toString());
        resourceChargeRecordService.counterCharge(dbResourceChargeRecordList, remark);
        // 更新计费项 立即生效更新计费项
        this.updateChargeItem(chargeItemDTO, chargeItem);
        // 将所有需要更新的资源都添加到定时任务当中
        for (ResourceChargeRecord chargeRecord : dbResourceChargeRecordList) {
            LocalDateTime taskDateTime = chargeRecord.getFinishUseTime();
            if (taskDateTime.isBefore(LocalDateTime.now())) {
                log.error("========== 定时执行时间早于当前时间，创建定时任务报错==========");
                throw new BusinessException(BusinessEnum.UPDATE_CHARGE_ITEM_DATE_ERROR);
            }
            // 新增定时任务，将资源添加到定时任务当中
            Task task = new Task();
            XxlJobInfo jobInfo = new XxlJobInfo();
            jobInfo.setExecutorHandler("defaultBeanHandler");
            jobInfo.setAuthor("admin_update");
            jobInfo.setJobDesc("更新计费项，资源计划执行时间：" + taskDateTime);
            // 设置任务的触发为轮询策略
            jobInfo.setExecutorRouteStrategy(ExecutorRouteStrategyEnum.ROUND.getCode());
            task.setJobInfo(jobInfo);
            task.setUuid(UUID.randomUUID().toString());
            task.setName(ChargeConstant.UPDATE_CHARGE + chargeRecord.getCmpInstanceName());
            task.setTaskExecType(TaskExecTypeEnum.SINGLE.getCode());
            task.setTaskPeriod("{\"corn\":\"" + DateConvertCronUtil.getCron(taskDateTime) + "\"}");
            task.setCallback("com.fitmgr.meterage.job.UpdateChargeItemResourceJob");
            task.setTaskType(TaskTypeEnum.CALCULATE_CHARGE.getCode());
            task.setSubTaskType(ChargeConstant.CHARGE);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(ChargeConstant.CHARGE_ID, chargeItemDTO.getUuid());
            jsonObject.put(ChargeConstant.RESOURCE_CHARGE_DATA, JSONObject.toJSONString(chargeRecord));
            task.setMetadata(jsonObject.toJSONString());
            FhJobApiController.create(task);
        }
        return true;
    }

    private void updateChargeItem(ChargeItemDTO chargeItemDTO, ChargeItem chargeItem) {
        ChargeItem updateChargeItem = new ChargeItem();
        BeanUtils.copyProperties(chargeItemDTO, updateChargeItem);
        updateChargeItem.setId(chargeItem.getId());
        updateChargeItem.setUpdateTime(LocalDateTime.now());

        // 设置备注
        String chargeItemRemark = chargeItem.getRemark();
        List<String> remarkStr;
        if (StringUtils.isBlank(chargeItemRemark)) {
            remarkStr = new ArrayList<>();
        } else {
            remarkStr = JSONObject.parseObject(chargeItemRemark, List.class);
        }
        Integer chargeFlavorUnit = chargeItemDTO.getChargeFlavorUnit();
        Integer chargeFlavorTime = chargeItemDTO.getChargeFlavorTime();
        String unitName = ChargeFlavorUnitEnum.getUnitName(chargeFlavorUnit);
        String timeName = ChargeFlavorTimeEnum.getTimeName(chargeFlavorTime);
        String timeStr = DateTimeConvertUtil.getTimeStr(LocalDateTime.now());
        String remark = "";
        if (!chargeItemDTO.getChargeName().equals(chargeItem.getChargeName())) {
            // 计费项名称发生变化
            remark = timeStr + " " + ChargeConstant.METERAGE_ITEM_NAME + chargeItemDTO.getChargeName();
            remarkStr.add(remark);
        }
        if (!chargeItemDTO.getPrice().equals(chargeItem.getPrice())) {
            // 折前单价发生变化
            remark = timeStr + " " + ChargeConstant.METERAGE_ITEM_CHANGE.replace(ChargeConstant.CONCURRENT_PRICE, chargeItemDTO.getPrice().toString()) + ChargeConstant.UNITARY + "/" + unitName + "/" + timeName;
            remarkStr.add(remark);
        }
        LocalDateTime planExecuteTime = chargeItemDTO.getPlanExecuteTime();
        LocalDateTime dbPlanExecuteTime = chargeItem.getPlanExecuteTime();
        if ((null == dbPlanExecuteTime && planExecuteTime != null) ||
                (dbPlanExecuteTime != null && planExecuteTime != null && !dbPlanExecuteTime.equals(planExecuteTime))) {
            remark = timeStr + " " + ChargeConstant.METERAGE_ITEM_EXPLANT_TIME + chargeItemDTO.getPlanExecuteTime();
            remarkStr.add(remark);
        }

        updateChargeItem.setRemark(JSONObject.toJSONString(remarkStr));
        updateChargeItem.setExecuteFlag(DeleteFlagStatusEnum.DELETE.getStatus());
        chargeItemMapper.updateById(updateChargeItem);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enableChargeItem(String chargeItemId) {

        // 查询计费项信息
        LambdaQueryWrapper<ChargeItem> queryWrapper = Wrappers.<ChargeItem>lambdaQuery()
                .eq(ChargeItem::getUuid, chargeItemId)
                .eq(ChargeItem::getChargeStatus, EnableStatusEnum.DISABLE.getStatus())
                .eq(ChargeItem::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus());
        ChargeItem chargeItem = chargeItemMapper.selectOne(queryWrapper);
        if (null == chargeItem) {
            throw new BusinessException(BusinessEnum.CHARGE_ITEM_PROPERTY_ENABLE);
        }
        // 更新计费项状态-->启用
        chargeItem.setChargeStatus(EnableStatusEnum.ENABLE.getStatus());
        chargeItemMapper.update(chargeItem, queryWrapper);

        // 启用计费项，重新查询计量资源记录，资源重新匹配新的计费项目
        log.info("========== 启用计费项，重新查询计量资源记录，适配计费项目，入库计费记录表！==========");
        LambdaQueryWrapper<ChargeItemProperty> lambdaQueryWrapper = Wrappers.<ChargeItemProperty>lambdaQuery().eq(ChargeItemProperty::getChargeUuid, chargeItem.getUuid());
        List<ChargeItemProperty> chargeItemPropertyList = chargeItemPropertyMapper.selectList(lambdaQueryWrapper);

        // 判断是否有资源已经存在于定时任务当中，如果有资源在定时任务当中，则不需要重新新增资源，等待定时任务新增资源即可
        EnableChargeItemThread chargeItemThread = new EnableChargeItemThread(chargeItem, chargeItemPropertyList);
        threadPoolTaskExecutor.execute(chargeItemThread);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disableChargeItem(String chargeItemId) {

        // 查询计费项数据
        LambdaQueryWrapper<ChargeItem> queryWrapper = Wrappers.<ChargeItem>lambdaQuery()
                .eq(ChargeItem::getUuid, chargeItemId)
                .eq(ChargeItem::getChargeStatus, EnableStatusEnum.ENABLE.getStatus())
                .eq(ChargeItem::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus());
        ChargeItem chargeItem = chargeItemMapper.selectOne(queryWrapper);
        if (null == chargeItem) {
            throw new BusinessException(BusinessEnum.CHARGE_ITEM_PROPERTY_DISABLE);
        }

        // 更新计费项状态-->禁用
        chargeItem.setChargeStatus(EnableStatusEnum.DISABLE.getStatus());
        chargeItemMapper.update(chargeItem, queryWrapper);

        // 禁用计费项的时候需要删除任务中，计划执行的该计费项的定时任务--存在编辑之后计划执行的计费项的定时任务
        TaskJobInfo chargeTaskJobInfo = new TaskJobInfo();
        chargeTaskJobInfo.setUuid(chargeItem.getUuid());
        List<TaskJobInfo> chargeTaskJobInfos = FhJobApiController.queryList(chargeTaskJobInfo);
        if (!CollectionUtils.isEmpty(chargeTaskJobInfos)) {
            for (TaskJobInfo jobInfo : chargeTaskJobInfos) {
                log.info("======禁用计费项，删除未执行的需要更新的计费项！======");
                xxlTaskService.delete(jobInfo.getUuid(), SecurityConstants.FROM_IN);
            }
        }

        // 查询定时任务中是否有启用的还未执行的资源任务，如果有则删除该定时任务
        List<ResourceChargeRecord> deleteDbResourceChargeRecords = resourceChargeRecordMapper.selectList(Wrappers.lambdaQuery());
        Set<String> dbCmpInstanceNameSet = new HashSet<>();
        for (ResourceChargeRecord deleteDbResourceChargeRecord : deleteDbResourceChargeRecords) {
            dbCmpInstanceNameSet.add(deleteDbResourceChargeRecord.getCmpInstanceName());
        }
        log.info("dbCmpInstanceNameSet={}", dbCmpInstanceNameSet);
        if (!CollectionUtils.isEmpty(dbCmpInstanceNameSet)) {
            for (String cmpInstanceName : dbCmpInstanceNameSet) {
                TaskJobInfo taskJobInfo = new TaskJobInfo();
                taskJobInfo.setJobDesc(ChargeConstant.ENABLE_CHARGE + cmpInstanceName);
                taskJobInfo.setName(ChargeConstant.ENABLE_CHARGE + cmpInstanceName);
                List<TaskJobInfo> enableTaskJobInfos = FhJobApiController.queryList(taskJobInfo);
                if (!CollectionUtils.isEmpty(enableTaskJobInfos)) {
                    for (TaskJobInfo jobInfo : enableTaskJobInfos) {
                        log.info("=====刪除该资源启用项的定时任务，任务名称为：{}", jobInfo.getJobDesc());
                        xxlTaskService.delete(jobInfo.getUuid(), SecurityConstants.FROM_IN);
                    }
                }

                taskJobInfo.setJobDesc(ChargeConstant.INSERT_CHARGE + cmpInstanceName);
                taskJobInfo.setName(ChargeConstant.INSERT_CHARGE + cmpInstanceName);
                List<TaskJobInfo> insertTaskJobInfos = FhJobApiController.queryList(taskJobInfo);
                if (!CollectionUtils.isEmpty(insertTaskJobInfos)) {
                    for (TaskJobInfo jobInfo : insertTaskJobInfos) {
                        log.info("=====刪除该资源启用项的定时任务，任务名称为：{}", jobInfo.getJobDesc());
                        xxlTaskService.delete(jobInfo.getUuid(), SecurityConstants.FROM_IN);
                    }
                }

                taskJobInfo.setJobDesc(ChargeConstant.UPDATE_CHARGE + cmpInstanceName);
                taskJobInfo.setName(ChargeConstant.UPDATE_CHARGE + cmpInstanceName);
                List<TaskJobInfo> updateTaskJobInfos = FhJobApiController.queryList(taskJobInfo);
                if (!CollectionUtils.isEmpty(updateTaskJobInfos)) {
                    for (TaskJobInfo jobInfo : updateTaskJobInfos) {
                        log.info("=====刪除该资源启用项的定时任务，任务名称为：{}", jobInfo.getJobDesc());
                        xxlTaskService.delete(jobInfo.getUuid(), SecurityConstants.FROM_IN);
                    }
                }

                taskJobInfo.setJobDesc(ChargeConstant.MONTH_TOTAL + cmpInstanceName);
                taskJobInfo.setName(ChargeConstant.MONTH_TOTAL + cmpInstanceName);
                List<TaskJobInfo> monthTaskJobInfos = FhJobApiController.queryList(taskJobInfo);
                if (!CollectionUtils.isEmpty(monthTaskJobInfos)) {
                    for (TaskJobInfo jobInfo : monthTaskJobInfos) {
                        log.info("=====刪除该资源启用项的定时任务，任务名称为：{}", jobInfo.getJobDesc());
                        xxlTaskService.delete(jobInfo.getUuid(), SecurityConstants.FROM_IN);
                    }
                }
            }
        }
        // 查询资源计费记录中适配了该计费项的所有资源，并对其进行费用结算并停止计费
        LambdaQueryWrapper<ResourceChargeRecord> lambdaQueryWrapper = Wrappers.<ResourceChargeRecord>lambdaQuery()
                .eq(ResourceChargeRecord::getChargeId, chargeItemId)
                .eq(ResourceChargeRecord::getResourceOffFlag, 0)
                .isNull(ResourceChargeRecord::getFinishUseTime)
                .eq(ResourceChargeRecord::getEnableFlag, EnableStatusEnum.ENABLE.getStatus())
                .eq(ResourceChargeRecord::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus());
        List<ResourceChargeRecord> resourceChargeRecords = resourceChargeRecordMapper.selectList(lambdaQueryWrapper);
        if (CollectionUtils.isEmpty(resourceChargeRecords)) {
            log.info("=======计费项禁用，无该计费项对应的资源账单记录需要更新结算！！！========");
            return true;
        }

        // 异步任务处理资源数据
        DisableChargeItemThread disableChargeItemThread = new DisableChargeItemThread(chargeItemId, resourceChargeRecords);
        threadPoolTaskExecutor.submit(disableChargeItemThread);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteChargeItem(String chargeItemId) {
        // 删除计费项
        LambdaQueryWrapper<ChargeItem> queryWrapper = Wrappers.<ChargeItem>lambdaQuery()
                .eq(ChargeItem::getUuid, chargeItemId)
                .eq(ChargeItem::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus());
        ChargeItem chargeItem = chargeItemMapper.selectOne(queryWrapper);
        if (null == chargeItem) {
            throw new BusinessException(BusinessEnum.CHARGE_ITEM_ENABLE_OR_DELETE);
        }
        chargeItem.setDelFlag(DeleteFlagStatusEnum.DELETE.getStatus());
        List<DiscountItem> discountItems = discountItemService.list(Wrappers.<DiscountItem>lambdaQuery().eq(DiscountItem::getChargeId, chargeItemId));
        for (DiscountItem discountItem : discountItems) {
            discountItem.setDelFlag(DeleteFlagStatusEnum.DELETE.getStatus());
        }
        // 删除计费项，如果该计费项有存在的定时任务，则一并删除定时任务
        TaskJobInfo taskJobInfo = new TaskJobInfo();
        taskJobInfo.setUuid(chargeItem.getUuid());
        List<TaskJobInfo> taskJobInfos = FhJobApiController.queryList(taskJobInfo);
        if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(taskJobInfos)) {
            for (TaskJobInfo jobInfo : taskJobInfos) {
                log.info("======删除计费项，删除未执行的需要更新的计费项！======");
                xxlTaskService.delete(jobInfo.getUuid(), SecurityConstants.FROM_IN);
            }
        }
        // 查询定时任务中是否有适配了该计费项匹配的资源需要定时执行的，如果有需要删除
        List<ResourceChargeRecord> taskResourceChargeRecordList = resourceChargeRecordService.list(Wrappers.<ResourceChargeRecord>lambdaQuery()
                .eq(ResourceChargeRecord::getComponentCode, chargeItem.getComponentCode())
                .eq(ResourceChargeRecord::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus()));
        log.info("taskResourceChargeRecordList size is {}", taskResourceChargeRecordList.size());

        if (!CollectionUtils.isEmpty(taskResourceChargeRecordList)) {
            log.info("asynchronized delete resource job !");
            threadPoolTaskExecutor.execute(
                    () -> {
                        try {
                            for (ResourceChargeRecord resourceChargeRecord : taskResourceChargeRecordList) {
                                String cmpInstanceName = resourceChargeRecord.getCmpInstanceName();
                                TaskJobInfo newTaskJobInfo = new TaskJobInfo();
                                newTaskJobInfo.setJobDesc(ChargeConstant.INSERT_CHARGE + cmpInstanceName);
                                newTaskJobInfo.setName(ChargeConstant.INSERT_CHARGE + cmpInstanceName);
                                List<TaskJobInfo> enableTaskJobInfos = FhJobApiController.queryList(newTaskJobInfo);
                                if (!CollectionUtils.isEmpty(enableTaskJobInfos)) {
                                    for (TaskJobInfo jobInfo : enableTaskJobInfos) {
                                        log.info("=====刪除该资源启用项的定时任务，任务名称为======：{}", jobInfo.getJobDesc());
                                        xxlTaskService.delete(jobInfo.getUuid(), SecurityConstants.FROM_IN);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.error("删除计费项任务执行失败：{}", e.getMessage(), e);
                        }
                    }
            );
        }

        // 查询所有关联该计费项的费用数据，结算费用
        List<ResourceChargeRecord> dbResourceChargeRecordList = resourceChargeRecordService.list(Wrappers.<ResourceChargeRecord>lambdaQuery()
                .eq(ResourceChargeRecord::getChargeId, chargeItemId)
                .eq(ResourceChargeRecord::getResourceOffFlag, 0)
                .isNull(ResourceChargeRecord::getFinishUseTime)
                .eq(ResourceChargeRecord::getEnableFlag, EnableStatusEnum.ENABLE.getStatus())
                .eq(ResourceChargeRecord::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus()
                ));
        //没有数据则不作任何操作
        if (dbResourceChargeRecordList.size() > 0) {
            // 结算旧费用记录
            String remark = ChargeConstant.DELETE_CHARGE_ITEM_CHANGE;
            resourceChargeRecordService.counterCharge(dbResourceChargeRecordList, remark);
        }
        for (DiscountItem discountItem : discountItems) {
            discountItemService.deleteDiscountItem(discountItem.getUuid());
        }
        chargeItemMapper.update(chargeItem, queryWrapper);
        return true;
    }

    @Override
    public IPage<ChargeItemVO> selectPage(Page page, ChargeItemDTO chargeItemDTO) {
        log.info("查询条件为：chargeItemDTO = {}", JSONObject.toJSONString(chargeItemDTO));

        // 根据计量项目名称模糊查询计量项列表
        List<Integer> meterageItemIds = null;
        if (StringUtils.isNotBlank(chargeItemDTO.getMeterageItemName())) {
            LambdaQueryWrapper<MeterageProject> queryWrapper = Wrappers.<MeterageProject>lambdaQuery()
                    .like(MeterageProject::getName, chargeItemDTO.getMeterageItemName())
                    .eq(MeterageProject::getDelFlag, String.valueOf(DeleteFlagStatusEnum.VIEW.getStatus()));
            List<MeterageProject> meterageProjects = meterageProjectMapper.selectList(queryWrapper);
            if (CollectionUtils.isEmpty(meterageProjects)) {
                return new Page<ChargeItemVO>();
            }
            meterageItemIds = meterageProjects.stream().map(meterageItem -> meterageItem.getId()).collect(Collectors.toList());
        }

        // 根据计费项属性KEY/VALUE条件查询计费项列表
        Set<String> chargeItemIdSet = null;
        if (StringUtils.isNotBlank(chargeItemDTO.getChargePropertyKey()) ||
                StringUtils.isNotBlank(chargeItemDTO.getChargePropertyValue())) {
            // chargePropertyKey转换keyName字段进行搜索
            LambdaQueryWrapper<MeterageProjectProperty> propertyLambdaQueryWrapper = Wrappers.<MeterageProjectProperty>lambdaQuery()
                    .like(StringUtils.isNotBlank(chargeItemDTO.getChargePropertyKey()), MeterageProjectProperty::getKeyName, chargeItemDTO.getChargePropertyKey())
                    .eq(MeterageProjectProperty::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus());
            List<MeterageProjectProperty> meterageProjectProperties = meterageProjectPropertyMapper.selectList(propertyLambdaQueryWrapper);
            List<String> keys = new ArrayList<>();
            if (!CollectionUtils.isEmpty(meterageProjectProperties)) {
                for (MeterageProjectProperty meterageProjectProperty : meterageProjectProperties) {
                    String key = StringUtils.isNotBlank(meterageProjectProperty.getForeignKey()) ? meterageProjectProperty.getForeignKey() : meterageProjectProperty.getSourceKey();
                    keys.add(key);
                }
            }
            LambdaQueryWrapper<ChargeItemProperty> wrapper = Wrappers.<ChargeItemProperty>lambdaQuery()
                    .in(!CollectionUtils.isEmpty(keys), ChargeItemProperty::getChargePropertyKey, keys)
                    .like(StringUtils.isNotBlank(chargeItemDTO.getChargePropertyValue()), ChargeItemProperty::getChargePropertyValue, chargeItemDTO.getChargePropertyValue())
                    .eq(ChargeItemProperty::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus());
            List<ChargeItemProperty> chargeItemPropertyVOList = chargeItemPropertyMapper.selectList(wrapper);
            log.info("计费项属性查询结果：chargeItemPropertyVOList = {}", CollectionUtils.isEmpty(chargeItemPropertyVOList) ? null : JSONObject.toJSONString(chargeItemPropertyVOList));
            if (CollectionUtils.isEmpty(chargeItemPropertyVOList)) {
                return new Page<ChargeItemVO>();
            }
            chargeItemIdSet = chargeItemPropertyVOList.stream().map(chargeItemPropertyVO -> chargeItemPropertyVO.getChargeUuid()).collect(Collectors.toSet());
        }

        // 查询计费项列表
        IPage<ChargeItemVO> chargeItemVOIPage = new Page<>();
        LambdaQueryWrapper<ChargeItem> chargeItemLambdaQueryWrapper = Wrappers.<ChargeItem>lambdaQuery()
                .like(StringUtils.isNotBlank(chargeItemDTO.getChargeName()), ChargeItem::getChargeName, chargeItemDTO.getChargeName())
                .in(!CollectionUtils.isEmpty(meterageItemIds), ChargeItem::getMeterageItemId, meterageItemIds)
                .like(StringUtils.isNotBlank(chargeItemDTO.getComponentCode()), ChargeItem::getComponentCode, chargeItemDTO.getComponentCode())
                .in(!CollectionUtils.isEmpty(chargeItemIdSet), ChargeItem::getUuid, chargeItemIdSet)
                .gt(chargeItemDTO.getBeginPrice() != null, ChargeItem::getPrice, chargeItemDTO.getBeginPrice())
                .lt(chargeItemDTO.getEndPrice() != null, ChargeItem::getPrice, chargeItemDTO.getEndPrice())
                .eq(chargeItemDTO.getChargeStatus() != null, ChargeItem::getChargeStatus, chargeItemDTO.getChargeStatus())
                .gt(chargeItemDTO.getBeginTime() != null, ChargeItem::getCreateTime, chargeItemDTO.getBeginTime())
                .lt(chargeItemDTO.getEndTime() != null, ChargeItem::getCreateTime, chargeItemDTO.getEndTime())
                .eq(ChargeItem::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus())
                .orderByDesc(ChargeItem::getCreateTime);

        // 根据条件查询计费项
        page.setSize(chargeItemDTO.getSize());
        page.setCurrent(chargeItemDTO.getCurrent());
        IPage<ChargeItem> chargeItemPage = chargeItemMapper.selectPage(page, chargeItemLambdaQueryWrapper);
        log.info("计费项分页列表：chargeItemPage = {}", JSONObject.toJSONString(chargeItemPage));
        if (null == chargeItemPage || CollectionUtils.isEmpty(chargeItemPage.getRecords())) {
            return chargeItemVOIPage;
        }

        // chargeItemPage转换为chargeItemVOIPage
        this.convertChargeItemVO(chargeItemVOIPage, chargeItemPage);
        log.info("计费项分页列表：chargeItemVOIPage ={}", JSONObject.toJSONString(chargeItemVOIPage));

        // 查询计量项对应的所有计费项属性
        Set<String> chargeItemUUIDList = chargeItemPage.getRecords().stream().map(chargeItem -> chargeItem.getUuid()).collect(Collectors.toSet());
        LambdaQueryWrapper<ChargeItemProperty> chargeItemPropertyLambdaQueryWrapper = Wrappers.<ChargeItemProperty>lambdaQuery()
                .in(!CollectionUtils.isEmpty(chargeItemUUIDList), ChargeItemProperty::getChargeUuid, chargeItemUUIDList);
        List<ChargeItemProperty> chargeItemPropertyList = chargeItemPropertyMapper.selectList(chargeItemPropertyLambdaQueryWrapper);
        log.info("计费项属性列表：chargeItemPropertyList = {}", JSONObject.toJSONString(chargeItemPropertyList));
        if (CollectionUtils.isEmpty(chargeItemPropertyList)) {
            return chargeItemVOIPage;
        }
        // 转换计费项属性，KEY为计费项的UUID，VALUE为计费项对应的计费属性的List集合
        Map<String, List<ChargeItemPropertyVO>> chargeItemPropertyMap = new ConcurrentHashMap<>();
        ChargeItemPropertyVO chargeItemPropertyVO = null;
        List<ChargeItemPropertyVO> chargeItemPropertyVOList = null;
        for (ChargeItemProperty chargeItemProperty : chargeItemPropertyList) {
            if (null == chargeItemPropertyMap.get(chargeItemProperty.getChargeUuid())) {
                chargeItemPropertyVOList = new ArrayList<>();
                chargeItemPropertyVO = new ChargeItemPropertyVO();
                BeanUtils.copyProperties(chargeItemProperty, chargeItemPropertyVO);
                chargeItemPropertyVOList.add(chargeItemPropertyVO);
                chargeItemPropertyMap.put(chargeItemProperty.getChargeUuid(), chargeItemPropertyVOList);
                continue;
            }
            chargeItemPropertyVOList = chargeItemPropertyMap.get(chargeItemProperty.getChargeUuid());
            chargeItemPropertyVO = new ChargeItemPropertyVO();
            BeanUtils.copyProperties(chargeItemProperty, chargeItemPropertyVO);
            chargeItemPropertyVOList.add(chargeItemPropertyVO);
            chargeItemPropertyMap.put(chargeItemProperty.getChargeUuid(), chargeItemPropertyVOList);
        }

        // 设置计费属性中文名称，即设置keyName
        if (!CollectionUtils.isEmpty(chargeItemPropertyMap)) {
            Set<Map.Entry<String, List<ChargeItemPropertyVO>>> entries = chargeItemPropertyMap.entrySet();
            for (Map.Entry<String, List<ChargeItemPropertyVO>> entry : entries) {
                String chargeUuid = entry.getKey();
                LambdaQueryWrapper<ChargeItem> queryWrapper = Wrappers.<ChargeItem>lambdaQuery()
                        .eq(ChargeItem::getUuid, chargeUuid)
                        .eq(ChargeItem::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus());
                // 获取计费项
                ChargeItem chargeItem = chargeItemMapper.selectOne(queryWrapper);
                if (null == chargeItem) {
                    continue;
                }
                // 根据计量项id查询计量项属性
                LambdaQueryWrapper<MeterageProjectProperty> propertyLambdaQueryWrapper = Wrappers.<MeterageProjectProperty>lambdaQuery()
                        .eq(MeterageProjectProperty::getMeterageProjectId, chargeItem.getMeterageItemId())
                        .eq(MeterageProjectProperty::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus());
                List<MeterageProjectProperty> meterageProjectProperties = meterageProjectPropertyMapper.selectList(propertyLambdaQueryWrapper);
                // 将所有字段都转换到foreignKey这个字段上面
                for (MeterageProjectProperty meterageProjectProperty : meterageProjectProperties) {
                    String foreignKey = StringUtils.isNotBlank(meterageProjectProperty.getForeignKey()) ? meterageProjectProperty.getForeignKey() : meterageProjectProperty.getSourceKey();
                    meterageProjectProperty.setForeignKey(foreignKey);
                }
                // 转换为Map集合，Key为foreignKey,Value为MeterageProjectProperty
                Map<String, MeterageProjectProperty> collect = meterageProjectProperties.stream().collect(Collectors.toMap(MeterageProjectProperty::getForeignKey, (p) -> p));
                // 特殊处理虚拟机集合
                List<ChargeItemPropertyVO> computerProperty = new ArrayList<>();
                List<ChargeItemPropertyVO> value = entry.getValue();
                Iterator<ChargeItemPropertyVO> iterator = value.iterator();
                while (iterator.hasNext()) {
                    ChargeItemPropertyVO next = iterator.next();
                    if (chargeItem.getComponentCode().equals(ResourceViewNameConvertEnum.COMPUTER_INSTANCE.getComponentCode()) &&
                            ("disk".equals(next.getChargePropertyKey()) ||
                                    "cpu".equals(next.getChargePropertyKey()) ||
                                    "memory".equals(next.getChargePropertyKey()))) {
                        computerProperty.add(next);
                        iterator.remove();
                        continue;
                    }
                    if (collect.get(next.getChargePropertyKey()) != null) {
                        MeterageProjectProperty meterageProjectProperty = collect.get(next.getChargePropertyKey());
                        next.setChargePropertyKeyName(meterageProjectProperty.getKeyName());
                    }
                }

                // 处理虚拟机规格
                if (!CollectionUtils.isEmpty(computerProperty)) {
                    Map<String, ChargeItemPropertyVO> itemPropertyVOMap = computerProperty.stream().collect(Collectors.toMap(ChargeItemPropertyVO::getChargePropertyKey, (p) -> p));
                    String cpu = itemPropertyVOMap.get("cpu").getChargePropertyValue();
                    String memory = itemPropertyVOMap.get("memory").getChargePropertyValue();
                    String disk = itemPropertyVOMap.get("disk").getChargePropertyValue();
                    StringBuffer sbf = new StringBuffer();
                    sbf.append("CPU:").append(cpu).append("核").append("内存:").append(memory).append("G").append("磁盘:").append(disk).append("G");
                    Integer max = 0;
                    if (!CollectionUtils.isEmpty(value)) {
                        max = Collections.max(value.stream().map(chargeItemPropertyVO1 -> chargeItemPropertyVO1.getId()).collect(Collectors.toList()));
                    } else {
                        max = 1;
                    }
                    ChargeItemPropertyVO chargeItemPropertyVO2 = new ChargeItemPropertyVO();
                    chargeItemPropertyVO2.setId(max + 10);
                    chargeItemPropertyVO2.setChargePropertyKeyName("云主机规格");
                    chargeItemPropertyVO2.setChargePropertyKey("flavor_id");
                    chargeItemPropertyVO2.setChargePropertyValue(sbf.toString());
                    value.add(chargeItemPropertyVO2);
                }
            }
        }

        // 填充计费属性到计费项目的展示层VO
        for (ChargeItemVO chargeItemVO : chargeItemVOIPage.getRecords()) {
            List<ChargeItemPropertyVO> chargeItemPropertyVOS = chargeItemPropertyMap.get(chargeItemVO.getUuid());
            if (CollectionUtils.isEmpty(chargeItemPropertyVOS)) {
                continue;
            }
            chargeItemVO.setChargeItemPropertyVOS(chargeItemPropertyVOS);
        }
        return chargeItemVOIPage;
    }

    private void convertChargeItemVO(IPage<ChargeItemVO> chargeItemVOIPage, IPage<ChargeItem> chargeItemPage) {
        chargeItemVOIPage.setPages(chargeItemPage.getPages());
        chargeItemVOIPage.setCurrent(chargeItemPage.getCurrent());
        chargeItemVOIPage.setSize(chargeItemPage.getSize());
        chargeItemVOIPage.setTotal(chargeItemPage.getTotal());
        List<ChargeItemVO> chargeItemVOS = new ArrayList<>();
        // 获取计量项列表
        LambdaQueryWrapper<MeterageProject> queryWrapperMeterageItem = Wrappers.<MeterageProject>lambdaQuery()
                .eq(MeterageProject::getDelFlag, String.valueOf(DeleteFlagStatusEnum.VIEW.getStatus()));
        List<MeterageProject> meterageProjects = meterageProjectMapper.selectList(queryWrapperMeterageItem);
        Map<Integer, MeterageProject> meterageItemIdToMap = meterageProjects.stream().collect(Collectors.toMap(MeterageProject::getId, (p) -> p));
        // 获取云平台列表
        List<ResourceTypeVO> resourceTypeVOS = resourceTypeService.list().getData();
        Map<Integer, ResourceTypeVO> resourceTypeIdToMap = resourceTypeVOS.stream().collect(Collectors.toMap(ResourceTypeVO::getId, (p) -> p));
        for (ChargeItem record : chargeItemPage.getRecords()) {
            ChargeItemVO chargeItemVO = new ChargeItemVO();
            BeanUtils.copyProperties(record, chargeItemVO);
            // 获取云平台名称
            Integer cloudPlatformId = chargeItemVO.getCloudPlatformId();
            if (resourceTypeIdToMap.get(cloudPlatformId) != null) {
                chargeItemVO.setCloudPlatformName(resourceTypeIdToMap.get(cloudPlatformId).getResourceTypeName());
            }
            // 获取计量项名称
            Integer meterageItemId = chargeItemVO.getMeterageItemId();
            if (meterageItemIdToMap.get(meterageItemId) != null) {
                chargeItemVO.setMeterageItemName(meterageItemIdToMap.get(meterageItemId).getName());
            }
            // 启用/禁用转换
            String msg = EnableStatusEnum.getMsg(chargeItemVO.getChargeStatus());
            if (StringUtils.isNotBlank(msg)) {
                chargeItemVO.setChargeStatusName(msg);
            }
            chargeItemVOS.add(chargeItemVO);
        }
        chargeItemVOIPage.setRecords(chargeItemVOS);
    }

    @Override
    public void exportExcel(HttpServletResponse response, Page page, ChargeItemDTO chargeItemDTO) {
        log.info("========== Charge item export ==========");
        // 获取需要导出的数据
        IPage<ChargeItemVO> chargeItemVOIPage = this.selectPage(page, chargeItemDTO);
        List<ChargeItemVO> chargeItemVOS = chargeItemVOIPage.getRecords();
        if (CollectionUtils.isEmpty(chargeItemVOS)) {
            return;
        }
        for (ChargeItemVO chargeItemVO : chargeItemVOS) {
            // 格式化时间
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime planExecuteTime = chargeItemVO.getPlanExecuteTime();
            if (null == planExecuteTime) {
                chargeItemVO.setCreateTimeStr(dateTimeFormatter.format(chargeItemVO.getCreateTime()));
            } else {
                chargeItemVO.setCreateTimeStr(dateTimeFormatter.format(chargeItemVO.getPlanExecuteTime()));
            }
            // 格式化计费属性
            if (!CollectionUtils.isEmpty(chargeItemVO.getChargeItemPropertyVOS())) {
                StringBuilder sb = new StringBuilder();
                for (ChargeItemPropertyVO chargeItemPropertyVO : chargeItemVO.getChargeItemPropertyVOS()) {
                    sb.append(chargeItemPropertyVO.getChargePropertyKeyName()).append("=").append(chargeItemPropertyVO.getChargePropertyValue()).append("\n");
                }
                chargeItemVO.setChargeItemPropertyStr(sb.toString());
            } else {
                chargeItemVO.setChargeItemPropertyStr("");
            }
            // 设置多云管/组件Code值
            StringBuilder compSbr = new StringBuilder();
            compSbr.append(chargeItemVO.getCloudPlatformName()).append("\n").append(chargeItemVO.getComponentCode());
            chargeItemVO.setCloudComponentCode(compSbr.toString());
        }
        log.info("excelChargeItemVOS = {}", JSONObject.toJSONString(chargeItemVOS));
        // 获取导出模板
        String xmlTemplateName = chargeItemDTO.getXmlTemplateName();
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
                ExcelUtil<ChargeItemVO> ex = new ExcelUtil<>();
                String templateName = ExportTypeEnum.getTemplateName(xmlTemplateName);
                ex.exportExcel(templateName, headers, fields, chargeItemVOS, response.getOutputStream(), ChargeConstant.DATE_TYPE);
            }
        } catch (Exception e) {
            log.info("模板不存在或导出报错！！！");
            e.printStackTrace();
        }
        log.info("========== Charge item end ==========");
    }

    @Override
    public List<ChargeItemVO> selectChargeItemListByChargeItemIds(List<String> chargeItemIds) {
        final LambdaQueryWrapper<ChargeItem> queryWrapper = Wrappers.<ChargeItem>lambdaQuery()
                .in(!CollectionUtils.isEmpty(chargeItemIds), ChargeItem::getUuid, chargeItemIds);
        List<ChargeItem> chargeItems = chargeItemMapper.selectList(queryWrapper);
        List<ChargeItemVO> chargeItemVOS = new ArrayList<>();
        if (CollectionUtils.isEmpty(chargeItems)) {
            return chargeItemVOS;
        }
        for (ChargeItem chargeItem : chargeItems) {
            ChargeItemVO chargeItemVO = new ChargeItemVO();
            BeanUtils.copyProperties(chargeItem, chargeItemVO);
            chargeItemVOS.add(chargeItemVO);
        }
        return chargeItemVOS;
    }

    /**
     * 校验计费项属性是否完全重复
     * @param chargeItemDTO
     * @return
     */
    private boolean validateChargeProperties(ChargeItemDTO chargeItemDTO) {
        LambdaQueryWrapper<ChargeItem> queryMeterageWrapper = Wrappers.<ChargeItem>lambdaQuery()
                .eq(ChargeItem::getMeterageItemId, chargeItemDTO.getMeterageItemId())
                .eq(ChargeItem::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus());
        List<ChargeItem> chargeItems = chargeItemMapper.selectList(queryMeterageWrapper);
        log.info("chargeItems={}", JSONObject.toJSONString(chargeItems));
        if (CollectionUtils.isEmpty(chargeItems)) {
            log.info("该计量项未创建任何计费项，允许初次创建计费项！");
            return false;
        }
        // 非初次创建计费项，属性进行校验，校验规则：KEY完全相等，VALUE完全相等
        List<String> chargeUuidList = chargeItems.stream().map(chargeItem -> chargeItem.getUuid()).collect(Collectors.toList());
        LambdaQueryWrapper<ChargeItemProperty> queryChargeItemPropertiesWrapper = Wrappers.<ChargeItemProperty>lambdaQuery()
                .eq(ChargeItemProperty::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus())
                .in(ChargeItemProperty::getChargeUuid, chargeUuidList);
        // 该计量项下所有计费项所对应的所有计费属性
        List<ChargeItemProperty> chargeItemProperties = chargeItemPropertyMapper.selectList(queryChargeItemPropertiesWrapper);
        log.info("chargeItemProperties={}", JSONObject.toJSONString(chargeItemProperties));
        if (CollectionUtils.isEmpty(chargeItemProperties)) {
            throw new BusinessException(BusinessEnum.CHARGE_ITEM_PROPERTY_EMPTY);
        }
        // 将数据库查询出来的属性转换为Map的形式，KEY为计费项的UUID，VALUE为该计费项对象的属性，存放在List集合当中
        Map<String, List<ChargeItemProperty>> chargeItemPropertyMap = new ConcurrentHashMap<>();
        for (ChargeItemProperty chargeItemProperty : chargeItemProperties) {
            if (CollectionUtils.isEmpty(chargeItemPropertyMap.get(chargeItemProperty.getChargeUuid()))) {
                List<ChargeItemProperty> chargeItemPropertyList = new ArrayList<>();
                chargeItemPropertyList.add(chargeItemProperty);
                chargeItemPropertyMap.put(chargeItemProperty.getChargeUuid(), chargeItemPropertyList);
                continue;
            }
            List<ChargeItemProperty> chargeItemPropertyList1 = chargeItemPropertyMap.get(chargeItemProperty.getChargeUuid());
            chargeItemPropertyList1.add(chargeItemProperty);
            chargeItemPropertyMap.put(chargeItemProperty.getChargeUuid(), chargeItemPropertyList1);
        }
        log.info("chargeItemPropertyMap = {}", JSONObject.toJSONString(chargeItemPropertyMap));
        // 将新增的计费项的计费属性转换为Map集合，Key为计费属性的名称，VALUE为计费属性的值
        Map<String, String> insertPropertiesMap = new ConcurrentHashMap<>();
        chargeItemDTO.getChargeItemPropertyDTOS().forEach(chargeItemProperty -> {
            insertPropertiesMap.put(chargeItemProperty.getChargePropertyKey(), chargeItemProperty.getChargePropertyValue());
        });
        // 校验已存在的计费项的计费属性是否完全和需要新增的计费项的计费属性相等，KEY和VALUE都完全相等
        boolean validateFlag = false;
        for (Map.Entry<String, List<ChargeItemProperty>> stringListEntry : chargeItemPropertyMap.entrySet()) {
            List<ChargeItemProperty> properties = stringListEntry.getValue();
            Map<String, String> dbPropertiesMap = new ConcurrentHashMap<>();
            properties.forEach(chargeItemProperty -> {
                // 数据库查询出来的已存在的计费项属性集合
                dbPropertiesMap.put(chargeItemProperty.getChargePropertyKey(), chargeItemProperty.getChargePropertyValue());
            });
            if (dbPropertiesMap.equals(insertPropertiesMap)) {
                // 只要有一组属性KEY和VALUE值完全相等，则判定该计费项已经存在
                validateFlag = true;
            }
        }
        return validateFlag ? true : false;
    }

    /**
     * 校验计费名称是否重复
     * @param chargeItemDTO
     * @return
     */
    private boolean validateChargeName(ChargeItemDTO chargeItemDTO) {
        LambdaQueryWrapper<ChargeItem> queryChargeItemDTOWrapper = Wrappers.<ChargeItem>lambdaQuery()
                .eq(ChargeItem::getChargeName, chargeItemDTO.getChargeName())
                .eq(ChargeItem::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus());
        ChargeItem chargeItem = chargeItemMapper.selectOne(queryChargeItemDTOWrapper);
        log.info("chargeItem = {}", JSONObject.toJSONString(chargeItem));
        return chargeItem != null ? true : false;
    }

    /**
     * 判断任务中心是否有待执行的期望执行更新的计费项，如果有则需要删除
     *
     * @param chargeItemDTO
     */
    private void deleteTaskJob(ChargeItemDTO chargeItemDTO) {
        TaskJobInfo taskJobInfo = new TaskJobInfo();

        taskJobInfo.setUuid(chargeItemDTO.getUuid());
        List<TaskJobInfo> taskJobInfos = FhJobApiController.queryList(taskJobInfo);
        if (!CollectionUtils.isEmpty(taskJobInfos)) {
            // 执行状态 0-就绪 1-执行中 2-执行完成 3-执行失败
            for (TaskJobInfo jobInfo : taskJobInfos) {
                log.info("========== 删除已存在的更新计划执行计费项任务 {}==========", ChargeConstant.UPDATE_CHARGE + chargeItemDTO.getChargeName());
                xxlTaskService.delete(jobInfo.getUuid(), SecurityConstants.FROM_IN);
            }
        }
    }
}
