package com.fitmgr.meterage.concurrent;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitmgr.common.core.constant.SecurityConstants;
import com.fitmgr.common.core.constant.enums.BusinessEnum;
import com.fitmgr.common.core.exception.BusinessException;
import com.fitmgr.common.core.util.SpringContextHolder;
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
import com.fitmgr.meterage.api.entity.ChargeItem;
import com.fitmgr.meterage.api.entity.ChargeItemProperty;
import com.fitmgr.meterage.api.entity.ResourceChargeRecord;
import com.fitmgr.meterage.api.entity.ResourceMeterageRecord;
import com.fitmgr.meterage.api.vo.DiscountItemVO;
import com.fitmgr.meterage.constant.ChargeConstant;
import com.fitmgr.meterage.constant.ResourceViewNameConvertEnum;
import com.fitmgr.meterage.mapper.MeterageRecordMapper;
import com.fitmgr.meterage.mapper.ResourceChargeRecordMapper;
import com.fitmgr.meterage.service.IDiscountItemService;
import com.fitmgr.meterage.service.IMatchingMeterageRecordService;
import com.fitmgr.meterage.service.IMeterageRecordService;
import com.fitmgr.meterage.service.IResourceChargeRecordService;
import com.fitmgr.meterage.utils.DateConvertCronUtil;
import com.fitmgr.meterage.utils.DateTimeConvertUtil;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 启用计费项，异步更新账单记录
 *
 * @author zhangxiaokang
 * @date 2020/10/29 10:56
 */
public class EnableChargeItemThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(EnableChargeItemThread.class);

    /**
     * 每批次处理的数据量
     */
    private static final Integer SIZE = 1000;

    private ChargeItem insertChargeItem;

    private List<ChargeItemProperty> chargeItemPropertyList;

    public EnableChargeItemThread(ChargeItem insertChargeItem, List<ChargeItemProperty> chargeItemPropertyList) {
        this.insertChargeItem = insertChargeItem;
        this.chargeItemPropertyList = chargeItemPropertyList;
    }

    @Override
    public void run() {
        ChargeItemDTO chargeItemDTO = new ChargeItemDTO();
        BeanUtils.copyProperties(insertChargeItem, chargeItemDTO);
        // 填充计费属性
        List<ChargeItemPropertyDTO> chargeItemPropertyDTOS = new ArrayList<>();
        for (ChargeItemProperty chargeItemProperty : chargeItemPropertyList) {
            ChargeItemPropertyDTO chargeItemPropertyDTO = new ChargeItemPropertyDTO();
            BeanUtils.copyProperties(chargeItemProperty, chargeItemPropertyDTO);
            chargeItemPropertyDTOS.add(chargeItemPropertyDTO);
        }
        chargeItemDTO.setChargeItemPropertyDTOS(chargeItemPropertyDTOS);
        // 启用计费项时，适配计量记录，并添加到计费记录表当中
        logger.info("计费项DTO为：chargeItemDTO = {}", JSONObject.toJSONString(chargeItemDTO));
        // 查询计量记录总数，当数量>1000时，分批次查询处理，并将查询出来的结果进行处理，入库到计费表当中
        IMeterageRecordService meterageRecordService = SpringContextHolder.getBean(IMeterageRecordService.class);
        int totalCount = meterageRecordService.count(Wrappers.query());
        logger.info("资源总数量：totalCount = {}", totalCount);
        if (totalCount == 0) {
            return;
        }
        int times = (int) Math.ceil((double) totalCount / SIZE);
        // 注入Bean
        IMatchingMeterageRecordService matchingMeterageRecordService = SpringContextHolder.getBean(IMatchingMeterageRecordService.class);
        IResourceChargeRecordService chargeBillDetailService = SpringContextHolder.getBean(IResourceChargeRecordService.class);
        ResourceChargeRecordMapper resourceChargeRecordMapper = SpringContextHolder.getBean(ResourceChargeRecordMapper.class);
        IDiscountItemService discountItemService = SpringContextHolder.getBean(IDiscountItemService.class);
        XxlTaskService xxlTaskService = SpringContextHolder.getBean(XxlTaskService.class);

        // 分批适配计量记录到计费记录
        for (int i = 1; i <= times; i++) {
            // 查询计量记录
            List<ResourceMeterageRecord> resourceMeterageRecords = matchingMeterageRecordService.selectMeterageRecords(chargeItemDTO.getMeterageItemId(), i, SIZE);
            if (CollectionUtils.isEmpty(resourceMeterageRecords)) {
                logger.info("计量记录为空！");
                continue;
            }
            // 根据计费项的计费属性，匹配计量记录中符合条件的记录，将新增计费属性转换为Map的形式，key为属性名，value为属性值
            List<ChargeItemPropertyDTO> chargeItemProperties = chargeItemDTO.getChargeItemPropertyDTOS();
            Map<String, String> insertMapParam = getInsertChargePropertiesMap(chargeItemProperties);

            // 循环获取、校验计量记录里面的资源，并进行匹配计费项属性，将符合条件的计量记录过滤出来
            List<ResourceMeterageRecord> insertResourceMeterageRecords = this.validateMeterageItemAndChargeItemAttributes(resourceMeterageRecords, insertMapParam);
            logger.info("新增计费项，匹配到需要添加计费记录的资源记录集合为：{}", JSONObject.toJSONString(insertResourceMeterageRecords));
            if (CollectionUtils.isEmpty(insertResourceMeterageRecords)) {
                logger.info("该计费项未匹配到合适的计量记录！");
                continue;
            }
            List<String> meterageCmpInstanceNameList = insertResourceMeterageRecords.stream().map(resourceMeterageRecord -> resourceMeterageRecord.getCmpInstanceName()).collect(Collectors.toList());
            // 根据名称查询计费记录表数据，是否该计量记录已经存在有其他计费项目开始计费了！
            logger.info("资源名称CmpInstanceName集合为：{}", meterageCmpInstanceNameList);
            // 查询计费记录中被禁用的资源
            List<ResourceChargeRecord> resourceChargeRecords = chargeBillDetailService.selectChargeDisableBilllList(meterageCmpInstanceNameList);
            logger.info("根据资源名称CmpInstanceName查询被禁用计费记录集合为：{}", JSONObject.toJSONString(resourceChargeRecords));

            // 查询计费记录中未被禁用的资源
            List<ResourceChargeRecord> dbResourceChargeRecordList = chargeBillDetailService.selectChargeBillDetailListByCmpInstanceNameList(meterageCmpInstanceNameList);
            Set<String> dbCmpInstanceNameSet = dbResourceChargeRecordList.stream().map(ResourceChargeRecord::getCmpInstanceName).collect(Collectors.toSet());
            if (dbResourceChargeRecordList.size() != dbCmpInstanceNameSet.size()) {
                throw new RuntimeException("存在重复计费的资源记录");
            }
            List<String> dbCmpInstanceNameList = new ArrayList<>(dbCmpInstanceNameSet);

            // 根据计量项查询折扣数据
            List<DiscountItemVO> disCountItemList = this.getDiscountItemList(chargeItemDTO, discountItemService);
            // 账单记录该计量记录全部为空，直接新增
            List<ResourceChargeRecord> insertResourceChargeRecordList = new ArrayList<>();
            if (CollectionUtils.isEmpty(resourceChargeRecords)) {
                // 计量记录对应的账单记录不存在，全部新增账单记录
                for (ResourceMeterageRecord resourceMeterageRecord : insertResourceMeterageRecords) {
                    if (dbCmpInstanceNameList.contains(resourceMeterageRecord.getCmpInstanceName())) {
                        // 计费记录表中已经存在启用的计费记录，无需再次启用入库
                        logger.info("计费记录表中已经存在启用的计费记录，无需再次启用入库，资源名称为：{}", resourceMeterageRecord.getCmpInstanceName());
                        continue;
                    }
                    insertResourceChargeRecordList.add(chargeBillDetailService.getChargeBillDetail(resourceMeterageRecord, disCountItemList, chargeItemDTO));
                }
                if (CollectionUtils.isEmpty(insertResourceChargeRecordList)) {
                    continue;
                }
                // 设置资源名称
                for (ResourceChargeRecord resourceChargeRecord : insertResourceChargeRecordList) {
                    String nameKey = ResourceViewNameConvertEnum.getNameKey(resourceChargeRecord.getComponentCode());
                    if (StringUtils.isBlank(nameKey)) {
                        nameKey = "name";
                    }
                    String resourceData = resourceChargeRecord.getResourceData();
                    if (StringUtils.isEmpty(resourceData)) {
                        continue;
                    }
                    JSONObject jsonObject = JSONObject.parseObject(resourceData);
                    String appName = jsonObject.getString(nameKey);
                    resourceChargeRecord.setAppName(appName);
                }
                // 批量新增计费记录入库
                resourceChargeRecordMapper.saveChargeBillDetailList(insertResourceChargeRecordList);
                logger.info("insertResourceChargeRecordList success ！");
                continue;
            }
            // 查询禁用的资源是否和重新启用的资源相同，并且计费时长不满一个计费周期，如果>1个计费周期，则正常添加，否则<1个计费周的，需要将其添加到定时任务，由定时任务去触发执行
            Map<String, List<ResourceChargeRecord>> mapToResourceChargeDate = new ConcurrentHashMap<>();
            for (ResourceChargeRecord resourceChargeRecord : resourceChargeRecords) {
                if (mapToResourceChargeDate.get(resourceChargeRecord.getCmpInstanceName()) != null) {
                    List<ResourceChargeRecord> resourceChargeRecordList = mapToResourceChargeDate.get(resourceChargeRecord.getCmpInstanceName());
                    resourceChargeRecordList.add(resourceChargeRecord);
                    mapToResourceChargeDate.put(resourceChargeRecord.getCmpInstanceName(), resourceChargeRecordList);
                } else {
                    List<ResourceChargeRecord> resourceChargeRecordList = new ArrayList<>();
                    resourceChargeRecordList.add(resourceChargeRecord);
                    mapToResourceChargeDate.put(resourceChargeRecord.getCmpInstanceName(), resourceChargeRecordList);
                }
            }
            Map<String, ResourceMeterageRecord> collect = insertResourceMeterageRecords.stream().collect(Collectors.toMap(ResourceMeterageRecord::getCmpInstanceName, (p) -> p));
            List<ResourceMeterageRecord> addResourceList = new ArrayList<>();
            for (Map.Entry<String, ResourceMeterageRecord> stringResourceMeterageRecordEntry : collect.entrySet()) {
                if (dbCmpInstanceNameList.contains(stringResourceMeterageRecordEntry.getKey())) {
                    // 计费记录表中已经存在启用的计费记录，无需再次启用入库
                    logger.info("计费记录表中已经存在启用的计费记录，无需再次启用入库，资源名称为：{}", stringResourceMeterageRecordEntry.getKey());
                    continue;
                }
                if (CollectionUtils.isEmpty(mapToResourceChargeDate.get(stringResourceMeterageRecordEntry.getKey()))) {
                    // 计费记录中不存在该资源，直接新增
                    addResourceList.add(stringResourceMeterageRecordEntry.getValue());
                    continue;
                }
                // 获取最近一次禁用的计费记录
                List<ResourceChargeRecord> resourceChargeRecordList = mapToResourceChargeDate.get(stringResourceMeterageRecordEntry.getKey());
                ResourceChargeRecord maxDisableResourceChargeRecord = resourceChargeRecordList.stream().max(Comparator.comparingInt(ResourceChargeRecord::getId)).get();
                LocalDateTime beginUseTime = maxDisableResourceChargeRecord.getBeginUseTime();
                LocalDateTime nowDateTime = LocalDateTime.now();
                Long serviceTime = chargeBillDetailService.getTime(insertChargeItem, beginUseTime, nowDateTime);
                // 开始时间与禁用时间不满一个计费周期
                if (serviceTime < 1) {
                    // 判断该资源之前是否存在过定时任务，如果存在则跳过，否则需要设置新的定时任务
                    if (this.findExistsTaskList(stringResourceMeterageRecordEntry, xxlTaskService)) {
                        continue;
                    }
                    //  立即设置定时任务，设置定时任务的时间
                    LocalDateTime taskDateTime = DateTimeConvertUtil.calculateDateTime(beginUseTime, 1L, insertChargeItem.getChargeFlavorTime());
                    this.setSecudleTask(chargeItemDTO, chargeBillDetailService, disCountItemList, stringResourceMeterageRecordEntry, taskDateTime);
                    continue;
                }
                // 开始时间与计费时间 > 一个计费周期，获取禁用的资源计费开始时间与计费禁用时间的时间差
                LocalDateTime updateTime = maxDisableResourceChargeRecord.getUpdateTime();
                Long updateCount = chargeBillDetailService.getTime(insertChargeItem, beginUseTime, updateTime);
                // 获取当前资源的截至计费时间
                LocalDateTime taskDateTime = DateTimeConvertUtil.calculateDateTime(beginUseTime, updateCount + 1, insertChargeItem.getChargeFlavorTime());
                LocalDateTime concurrentTime = LocalDateTime.now();
                if (concurrentTime.isBefore(taskDateTime)) {
                    // 当前资源启用计费时间比资源费用早，取定时任务，时间为任务结束时间再次启动时间：taskDateTime
                    if (this.findExistsTaskList(stringResourceMeterageRecordEntry, xxlTaskService)) {
                        continue;
                    }
                    //  立即设置定时任务，设置定时任务的时间
                    this.setSecudleTask(chargeItemDTO, chargeBillDetailService, disCountItemList, stringResourceMeterageRecordEntry, taskDateTime);
                    continue;
                }
                addResourceList.add(stringResourceMeterageRecordEntry.getValue());
            }
            if (CollectionUtils.isEmpty(addResourceList)) {
                continue;
            }
            for (ResourceMeterageRecord resourceMeterageRecord : addResourceList) {
                insertResourceChargeRecordList.add(chargeBillDetailService.getChargeBillDetail(resourceMeterageRecord, disCountItemList, chargeItemDTO));
            }
            // 设置资源名称
            for (ResourceChargeRecord resourceChargeRecord : insertResourceChargeRecordList) {
                String nameKey = ResourceViewNameConvertEnum.getNameKey(resourceChargeRecord.getComponentCode());
                if (StringUtils.isBlank(nameKey)) {
                    nameKey = "name";
                }
                String resourceData = resourceChargeRecord.getResourceData();
                if (StringUtils.isEmpty(resourceData)) {
                    continue;
                }
                JSONObject jsonObject = JSONObject.parseObject(resourceData);
                String appName = jsonObject.getString(nameKey);
                resourceChargeRecord.setAppName(appName);
            }
            // 新增记录入库账单表
            logger.info("insertResourceChargeRecordList = {}", CollectionUtils.isEmpty(insertResourceChargeRecordList) ? null : JSONObject.toJSONString(insertResourceChargeRecordList));
            resourceChargeRecordMapper.saveChargeBillDetailList(insertResourceChargeRecordList);
        }
    }

    /**
     * 判断定时任务是否已经存在
     * @param stringResourceMeterageRecordEntry
     * @param xxlTaskService
     * @return
     */
    private boolean findExistsTaskList(Map.Entry<String, ResourceMeterageRecord> stringResourceMeterageRecordEntry, XxlTaskService xxlTaskService) {
        TaskJobInfo taskJobInfo = new TaskJobInfo();
        taskJobInfo.setJobDesc(ChargeConstant.ENABLE_CHARGE + stringResourceMeterageRecordEntry.getKey());
        taskJobInfo.setName(ChargeConstant.ENABLE_CHARGE + stringResourceMeterageRecordEntry.getKey());
        List<TaskJobInfo> taskJobInfos = FhJobApiController.queryList(taskJobInfo);
        if (!CollectionUtils.isEmpty(taskJobInfos)) {
            // 查询定时任务列表
            Integer resultCount = 0;
            // 执行状态 0-就绪 1-执行中 2-执行完成 3-执行失败
            for (TaskJobInfo jobInfo : taskJobInfos) {
                // 就绪/执行中不做处理
                if (jobInfo.getExecStatus().equals(0) || jobInfo.getExecStatus().equals(1)) {
                    // 说明定时任务已经执行完成
                    resultCount++;
                }
                // 执行完成的删除
                if (jobInfo.getExecStatus().equals(2)) {
                    xxlTaskService.delete(jobInfo.getUuid(), SecurityConstants.FROM_IN);
                }
            }
            if (!resultCount.equals(0)) {
                logger.info("=============该资源已存在未执行完成的定时任务，不再创建新的定时任务！=============");
                return true;
            }
        }
        return false;
    }

    private void setSecudleTask(ChargeItemDTO chargeItemDTO, IResourceChargeRecordService chargeBillDetailService, List<DiscountItemVO> disCountItemList, Map.Entry<String, ResourceMeterageRecord> stringResourceMeterageRecordEntry, LocalDateTime taskDateTime) {
        // 需要添加到定时任务，定时启用
        ResourceChargeRecord chargeBillDetail = chargeBillDetailService.getChargeBillDetail(stringResourceMeterageRecordEntry.getValue(), disCountItemList, chargeItemDTO);
        if (taskDateTime.isBefore(LocalDateTime.now())) {
            logger.error("计划执行时间早于当前时间，创建定时任务错误！计划执行时间：{}，当前时间：{}", taskDateTime, LocalDateTime.now());
            return;
        }
        logger.info("======添加定时任务，计费项定时执行时间为：{}======", taskDateTime);
        Task task = new Task();
        XxlJobInfo jobInfo = new XxlJobInfo();
        jobInfo.setExecutorHandler("defaultBeanHandler");
        jobInfo.setAuthor("admin_enable");
        jobInfo.setJobDesc("启用计费项，计划执行时间：" + taskDateTime);
        // 设置任务的触发为轮询策略
        jobInfo.setExecutorRouteStrategy(ExecutorRouteStrategyEnum.ROUND.getCode());
        task.setJobInfo(jobInfo);
        task.setUuid(UUID.randomUUID().toString());
        task.setName(ChargeConstant.ENABLE_CHARGE + stringResourceMeterageRecordEntry.getKey());
        task.setTaskExecType(TaskExecTypeEnum.SINGLE.getCode());
        task.setTaskPeriod("{\"corn\":\"" + DateConvertCronUtil.getCron(taskDateTime) + "\"}");
        task.setCallback("com.fitmgr.meterage.job.EnableChargeItemJob");
        task.setTaskType(TaskTypeEnum.CALCULATE_CHARGE.getCode());
        task.setSubTaskType(ChargeConstant.CHARGE);
        // 设置计费项参数uuid
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(ChargeConstant.CHARGE_ID, insertChargeItem.getUuid());
        // 计费记录资源UUID
        jsonObject.put(ChargeConstant.RESOURCE_CHARGE_DATA, JSONObject.toJSONString(chargeBillDetail));
        task.setMetadata(jsonObject.toJSONString());
        boolean resultFlag = FhJobApiController.create(task);
        logger.info("========添加定时任务执行结果：{}========", resultFlag);
    }

    private Map<String, String> getInsertChargePropertiesMap(List<ChargeItemPropertyDTO> chargeItemPropertyDTOS) {
        Map<String, String> insertMapParam = new ConcurrentHashMap<>();
        for (ChargeItemPropertyDTO chargeItemPropertyDTO : chargeItemPropertyDTOS) {
            String chargePropertyKey = chargeItemPropertyDTO.getChargePropertyKey();
            String chargePropertyValue = chargeItemPropertyDTO.getChargePropertyValue();
            insertMapParam.put(chargePropertyKey, chargePropertyValue);
        }
        return insertMapParam;
    }

    /**
     * 校验计量记录中，计量项属性和计费项目属性，并过滤计量记录
     * @param resourceMeterageRecords
     * @param insertMapParam
     * @return
     */
    private List<ResourceMeterageRecord> validateMeterageItemAndChargeItemAttributes(List<ResourceMeterageRecord> resourceMeterageRecords, Map<String, String> insertMapParam) {
        List<ResourceMeterageRecord> fiterResourceMeterageRecords = new ArrayList<>();
        for (ResourceMeterageRecord resourceMeterageRecord : resourceMeterageRecords) {
            if (insertMapParam != null) {
                // 获取属性数据
                JSONObject jsonObject = JSONObject.parseObject(resourceMeterageRecord.getData());
                // 获取某个计量项下面的的每一个计量属性及属性值，并解析为map形式
                Map<String, String> dbMapParam = new ConcurrentHashMap<>();
                for (Map.Entry<String, Object> stringObjectEntry : jsonObject.entrySet()) {
                    if (null == stringObjectEntry.getValue()) {
                        continue;
                    }
                    dbMapParam.put(stringObjectEntry.getKey(), stringObjectEntry.getValue().toString());
                }
                // 资源的计量项属性KEY是否完全包含计费项属性KEY
                if (!dbMapParam.keySet().containsAll(insertMapParam.keySet())) {
                    continue;
                }
                // 资源的计量项属性KEY及VALUE是否完全与计费项属性相等，也就是KEY相同，但是KEY对应的VALUE不相等
                boolean flag = false;
                for (Map.Entry<String, String> insertEntry : insertMapParam.entrySet()) {
                    if (!dbMapParam.get(insertEntry.getKey()).equals(insertEntry.getValue())) {
                        // 计费项与计量项属性KEY相同，但是VALUE值不同
                        flag = true;
                        break;
                    }
                }
                if (flag) {
                    continue;
                }
            }
            fiterResourceMeterageRecords.add(resourceMeterageRecord);
        }
        return fiterResourceMeterageRecords;
    }

    /**
     * 根据计量项（已匹配了计费项）查询折扣项信息
     * @param chargeItemDTO
     * @param discountItemService
     * @return
     */
    private List<DiscountItemVO> getDiscountItemList(ChargeItemDTO chargeItemDTO, IDiscountItemService discountItemService) {
        List<DiscountItemVO> disCountItemList = discountItemService.getDisCountItemList(chargeItemDTO.getUuid());
        logger.info("disCountItemList={}", JSONObject.toJSONString(disCountItemList));
        if (CollectionUtils.isEmpty(disCountItemList)) {
            throw new BusinessException(BusinessEnum.DISCOUNT_ITEM_IS_NULL);
        }
        return disCountItemList;
    }
}
