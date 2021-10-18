package com.fitmgr.meterage.concurrent;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 创建计费项，异步同步账单明细数据异步类
 *
 * @author zhangxiaokang
 * @date 2020/10/29 10:01
 */
public class CreateChargeItemThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(CreateChargeItemThread.class);

    /**
     * 每批次处理的数据量
     */
    private static final Integer SIZE = 1000;

    private ChargeItem insertChargeItem;

    private List<ChargeItemProperty> chargeItemPropertyList;

    public CreateChargeItemThread(ChargeItem insertChargeItem, List<ChargeItemProperty> chargeItemPropertyList) {
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
        // 新增计费项时，适配计量记录，并添加到详情记录表当中
        logger.info("计费项DTO为：chargeItemDTO = {}", JSONObject.toJSONString(chargeItemDTO));
        // 查询计量记录总数，当数量>1000时，分批次查询处理，并将查询出来的结果进行处理，入库到计费表当中
        IMeterageRecordService meterageRecordService = SpringContextHolder.getBean(IMeterageRecordService.class);
        int totalCount = meterageRecordService.count();
        logger.info("资源总数量：totalCount = {}", totalCount);
        if (totalCount == 0) {
            return;
        }
        int times = (int) Math.ceil((double) totalCount / SIZE);
        // 分批适配计量记录到计费记录
        for (int i = 1; i <= times; i++) {
            // 查询计量记录
            IMatchingMeterageRecordService matchingMeterageRecordService = SpringContextHolder.getBean(IMatchingMeterageRecordService.class);
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
                logger.info("该计费项目，未匹配到合适的计量记录！");
                continue;
            }
            List<String> meterageCmpInstanceNameList = insertResourceMeterageRecords.stream().map(resourceMeterageRecord -> resourceMeterageRecord.getCmpInstanceName()).collect(Collectors.toList());
            // 根据名称查询计费记录表数据，是否该计量记录已经存在有其他计费项目开始计费了！
            logger.info("资源名称CmpInstanceName集合为：{}", meterageCmpInstanceNameList);
            IResourceChargeRecordService chargeBillDetailService = SpringContextHolder.getBean(IResourceChargeRecordService.class);
            List<ResourceChargeRecord> resourceChargeRecords = chargeBillDetailService.selectChargeBillDetailListByCmpInstanceNameList(meterageCmpInstanceNameList);
            logger.info("根据资源名称CmpInstanceName查询计费记录集合为：{}", JSONObject.toJSONString(resourceChargeRecords));
            ResourceChargeRecordMapper resourceChargeRecordMapper = SpringContextHolder.getBean(ResourceChargeRecordMapper.class);
            XxlTaskService xxlTaskService = SpringContextHolder.getBean(XxlTaskService.class);
            // 根据计量项查询折扣数据
            List<DiscountItemVO> disCountItemList = this.getDiscountItemList(chargeItemDTO);
            // 账单记录该计量记录全部为空，直接新增
            List<ResourceChargeRecord> insertResourceChargeRecordList = new ArrayList<>();
            if (CollectionUtils.isEmpty(resourceChargeRecords)) {
                // 计量记录对应的账单记录不存在，全部新增账单记录
                for (ResourceMeterageRecord resourceMeterageRecord : insertResourceMeterageRecords) {
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
                /**
                 * 从计量记录里面查询的资源数据，需要判断该资源是否在账单记录里存在，并且已经结束计费，并且结束计费时间是否晚于当前系统时间，如果有这样的数据，则需要给这个资源加上
                 * 任务，只有当上个资源的账单记录到期，本次的资源的计费记录才会随定时任务入库
                 */
                Iterator<ResourceChargeRecord> iterator = insertResourceChargeRecordList.iterator();
                while (iterator.hasNext()) {
                    ResourceChargeRecord dbChargeRecord = iterator.next();
                    LambdaQueryWrapper<ResourceChargeRecord> dbRecordLambdaQueryWrapper = Wrappers.<ResourceChargeRecord>lambdaQuery().eq(ResourceChargeRecord::getCmpInstanceName, dbChargeRecord.getCmpInstanceName());
                    List<ResourceChargeRecord> resourceChargeRecordList = resourceChargeRecordMapper.selectList(dbRecordLambdaQueryWrapper);
                    if (CollectionUtils.isEmpty(resourceChargeRecordList)) {
                        continue;
                    }
                    List<ResourceChargeRecord> updateResourceChargeRecord = new ArrayList<>();
                    for (ResourceChargeRecord resourceChargeRecord : resourceChargeRecordList) {
                        LocalDateTime finishUseTime = resourceChargeRecord.getFinishUseTime();
                        if (null == finishUseTime) {
                            continue;
                        }
                        LocalDateTime nowDateTime = LocalDateTime.now();
                        // 有符合上述要求的数据
                        if (nowDateTime.isBefore(finishUseTime)) {
                            updateResourceChargeRecord.add(resourceChargeRecord);
                        }
                    }
                    if (CollectionUtils.isEmpty(updateResourceChargeRecord)) {
                        continue;
                    }
                    if (updateResourceChargeRecord.size() > 1) {
                        logger.error("账单记录存在相同资源重复计费情况，该资源不予以再次入库，逻辑上不应该有这样的数据！");
                        iterator.remove();
                        continue;
                    }
                    ResourceChargeRecord resourceChargeRecord = updateResourceChargeRecord.get(0);
                    TaskJobInfo taskJobInfo = new TaskJobInfo();
                    taskJobInfo.setJobDesc(ChargeConstant.INSERT_CHARGE + resourceChargeRecord.getCmpInstanceName());
                    taskJobInfo.setName(ChargeConstant.INSERT_CHARGE + resourceChargeRecord.getCmpInstanceName());
                    List<TaskJobInfo> taskJobInfos = FhJobApiController.queryList(taskJobInfo);
                    if (CollectionUtils.isNotEmpty(taskJobInfos)) {
                        for (TaskJobInfo jobInfo : taskJobInfos) {
                            logger.info("======新增计费项，删除未执行的需要更新的计费项资源！======");
                            xxlTaskService.delete(jobInfo.getUuid(), SecurityConstants.FROM_IN);
                        }
                    }
                    // 新增定时任务，将资源添加到定时任务当中
                    LocalDateTime taskDateTime = resourceChargeRecord.getFinishUseTime();
                    Task task = new Task();
                    XxlJobInfo jobInfo = new XxlJobInfo();
                    jobInfo.setExecutorHandler("defaultBeanHandler");
                    jobInfo.setAuthor("admin_update");
                    jobInfo.setJobDesc("新增计费项，资源计划执行时间：" + taskDateTime);
                    // 设置任务的触发为轮询策略
                    jobInfo.setExecutorRouteStrategy(ExecutorRouteStrategyEnum.ROUND.getCode());
                    task.setJobInfo(jobInfo);
                    task.setUuid(UUID.randomUUID().toString());
                    task.setName(ChargeConstant.INSERT_CHARGE + resourceChargeRecord.getCmpInstanceName());
                    task.setTaskExecType(TaskExecTypeEnum.SINGLE.getCode());
                    task.setTaskPeriod("{\"corn\":\"" + DateConvertCronUtil.getCron(taskDateTime) + "\"}");
                    task.setCallback("com.fitmgr.meterage.job.CreateChargeItemJob");
                    task.setTaskType(TaskTypeEnum.CALCULATE_CHARGE.getCode());
                    task.setSubTaskType(ChargeConstant.CHARGE);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(ChargeConstant.CHARGE_ID, chargeItemDTO.getUuid());
                    jsonObject.put(ChargeConstant.RESOURCE_CHARGE_DATA, JSONObject.toJSONString(resourceChargeRecord));
                    task.setMetadata(jsonObject.toJSONString());
                    FhJobApiController.create(task);
                    iterator.remove();
                }
                if (CollectionUtils.isEmpty(insertResourceChargeRecordList)) {
                    continue;
                }
                // 批量新增计费记录入库
                resourceChargeRecordMapper.saveChargeBillDetailList(insertResourceChargeRecordList);
                logger.info("insertResourceChargeRecordList success ！");
                continue;
            }
            // 区分出新增和更新的计量记录集合
            List<String> cmpInstanceList = new ArrayList<>(resourceChargeRecords.stream().map(chargeBillDetail -> chargeBillDetail.getCmpInstanceName()).collect(Collectors.toSet()));
            meterageCmpInstanceNameList.removeAll(cmpInstanceList);
            if (CollectionUtils.isEmpty(meterageCmpInstanceNameList)) {
                continue;
            }
            // 新增计费记录数据集合
            for (ResourceMeterageRecord resourceMeterageRecord : insertResourceMeterageRecords) {
                if (!meterageCmpInstanceNameList.contains(resourceMeterageRecord.getCmpInstanceName())) {
                    continue;
                }
                insertResourceChargeRecordList.add(chargeBillDetailService.getChargeBillDetail(resourceMeterageRecord, disCountItemList, chargeItemDTO));
            }
            // 新增记录入库账单表
            logger.info("insertResourceChargeRecordList = {}", JSONObject.toJSONString(insertResourceChargeRecordList));
            if (CollectionUtils.isEmpty(insertResourceChargeRecordList)) {
                continue;
            }
            resourceChargeRecordMapper.saveChargeBillDetailList(insertResourceChargeRecordList);
        }
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
                    logger.info("资源属性与计费项属性KEY不完全相等！资源属性为：{}，计费属性为：{}", JSONObject.toJSONString(dbMapParam), JSONObject.toJSONString(insertMapParam));
                    continue;
                }
                // 资源的计量项属性KEY及VALUE是否完全与计费项属性相等，也就是KEY相同，但是KEY对应的VALUE不相等
                boolean flag = false;
                for (Map.Entry<String, String> insertEntry : insertMapParam.entrySet()) {
                    if (!dbMapParam.get(insertEntry.getKey()).equals(insertEntry.getValue())) {
                        // 计费项与计量项属性KEY相同，但是VALUE值不同
                        logger.info("资源属性与计费属性KEY相同，但是VALUE值不同，资源VALUE：{}，计费VALUE：{}", dbMapParam.get(insertEntry.getKey()), insertEntry.getValue());
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
     * @return
     */
    private List<DiscountItemVO> getDiscountItemList(ChargeItemDTO chargeItemDTO) {
        IDiscountItemService discountItemService = SpringContextHolder.getBean(IDiscountItemService.class);
        List<DiscountItemVO> disCountItemList = discountItemService.getDisCountItemList(chargeItemDTO.getUuid());
        logger.info("disCountItemList={}", JSONObject.toJSONString(disCountItemList));
        if (CollectionUtils.isEmpty(disCountItemList)) {
            throw new BusinessException(BusinessEnum.DISCOUNT_ITEM_IS_NULL);
        }
        return disCountItemList;
    }
}
