package com.fitmgr.meterage.job;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitmgr.common.core.constant.SecurityConstants;
import com.fitmgr.common.core.constant.enums.BusinessEnum;
import com.fitmgr.common.core.constant.enums.DeleteFlagStatusEnum;
import com.fitmgr.common.core.constant.enums.EnableStatusEnum;
import com.fitmgr.common.core.exception.BusinessException;
import com.fitmgr.common.core.util.SpringContextHolder;
import com.fitmgr.job.api.core.biz.model.ReturnT;
import com.fitmgr.job.api.entity.Task;
import com.fitmgr.job.api.entity.TaskJobInfo;
import com.fitmgr.job.api.entity.XxlJobInfo;
import com.fitmgr.job.api.enums.ExecutorRouteStrategyEnum;
import com.fitmgr.job.api.enums.TaskExecTypeEnum;
import com.fitmgr.job.api.enums.TaskTypeEnum;
import com.fitmgr.job.api.excutor.XxlBaseTaskExec;
import com.fitmgr.job.api.feign.XxlTaskService;
import com.fitmgr.job.api.sdk.FhJobApiController;
import com.fitmgr.meterage.api.dto.ChargeItemDTO;
import com.fitmgr.meterage.api.entity.ChargeItem;
import com.fitmgr.meterage.api.entity.ResourceChargeRecord;
import com.fitmgr.meterage.concurrent.UpdateChargeItemThread;
import com.fitmgr.meterage.constant.ChargeConstant;
import com.fitmgr.meterage.constant.ChargeFlavorTimeEnum;
import com.fitmgr.meterage.constant.ChargeFlavorUnitEnum;
import com.fitmgr.meterage.mapper.ChargeItemMapper;
import com.fitmgr.meterage.mapper.ResourceChargeRecordMapper;
import com.fitmgr.meterage.service.IMeterageChargeItemService;
import com.fitmgr.meterage.service.IResourceChargeRecordService;
import com.fitmgr.meterage.utils.DateConvertCronUtil;
import com.fitmgr.meterage.utils.DateTimeConvertUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 计划更新计费项
 *
 * @author zhangxiaokang
 * @date 2020/11/18 16:08
 */
@Slf4j
@Component
@Scope("prototype")
@AllArgsConstructor
public class UpdateChargeItemJob extends XxlBaseTaskExec {
    @Override
    public ReturnT<String> taskCallback(Task task) throws Exception {
        String metadata = task.getMetadata();
        JSONObject jsonObject = JSONObject.parseObject(metadata);
        String chargeId = jsonObject.getString(ChargeConstant.CHARGE_ID);
        if (StringUtils.isBlank(chargeId)) {
            log.info("========= metadata is null! ========");
            return new ReturnT<String>(0, null);
        }
        // Bean注入
        ChargeItemMapper chargeItemMapper = SpringContextHolder.getBean(ChargeItemMapper.class);
        ResourceChargeRecordMapper resourceChargeRecordMapper = SpringContextHolder.getBean(ResourceChargeRecordMapper.class);
        XxlTaskService xxlTaskService = SpringContextHolder.getBean(XxlTaskService.class);
        IResourceChargeRecordService resourceChargeRecordService = SpringContextHolder.getBean(IResourceChargeRecordService.class);

        LambdaQueryWrapper<ChargeItem> itemLambdaQueryWrapper = Wrappers.<ChargeItem>lambdaQuery()
                .eq(ChargeItem::getUuid, chargeId)
                .eq(ChargeItem::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus());
        ChargeItem chargeItem = chargeItemMapper.selectOne(itemLambdaQueryWrapper);
        if (null == chargeItem) {
            log.info("========== chargeItem is null or deleted ！！！！！！ ==========");
            return new ReturnT<String>(0, null);
        }
        String dbPlanExecuteData = chargeItem.getPlanExecuteData();
        if (StringUtils.isBlank(dbPlanExecuteData)) {
            log.info("========== dbPlanExecuteData is ！！！！！！ ==========");
            return new ReturnT<String>(0, null);
        }

        ChargeItemDTO chargeItemDTO = JSONObject.parseObject(dbPlanExecuteData, ChargeItemDTO.class);
        // 禁用状态更新计费项不需要结算资源及新增记录
        if (chargeItem.getChargeStatus().equals(EnableStatusEnum.DISABLE.getStatus())) {
            // 立即生效更新计费项
            this.updateChargeItem(chargeItemDTO, chargeItem, chargeItemMapper);
            log.info("========= 当前计费项为禁用状态，只需要更新计费项！ ========");
            return new ReturnT<String>(0, null);
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
            this.updateChargeItem(chargeItemDTO, chargeItem, chargeItemMapper);
            return new ReturnT<String>(0, null);
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
        this.updateChargeItem(chargeItemDTO, chargeItem, chargeItemMapper);
        // 将所有需要更新的资源都添加到定时任务当中
        for (ResourceChargeRecord chargeRecord : dbResourceChargeRecordList) {
            LocalDateTime taskDateTime = chargeRecord.getFinishUseTime();
            if (taskDateTime.isBefore(LocalDateTime.now())) {
                log.error("========== 定时执行时间早于当前时间，创建定时任务报错==========");
                throw new BusinessException(BusinessEnum.UPDATE_CHARGE_ITEM_DATE_ERROR);
            }
            // 新增定时任务，将资源添加到定时任务当中
            Task insertTask = new Task();
            XxlJobInfo jobInfo = new XxlJobInfo();
            jobInfo.setExecutorHandler("defaultBeanHandler");
            jobInfo.setAuthor("admin_update");
            jobInfo.setJobDesc("更新计费项，资源计划执行时间：" + taskDateTime);
            // 设置任务的触发为轮询策略
            jobInfo.setExecutorRouteStrategy(ExecutorRouteStrategyEnum.ROUND.getCode());
            insertTask.setJobInfo(jobInfo);
            insertTask.setUuid(UUID.randomUUID().toString());
            insertTask.setName(ChargeConstant.UPDATE_CHARGE + chargeRecord.getCmpInstanceName());
            insertTask.setTaskExecType(TaskExecTypeEnum.SINGLE.getCode());
            insertTask.setTaskPeriod("{\"corn\":\"" + DateConvertCronUtil.getCron(taskDateTime) + "\"}");
            insertTask.setCallback("com.fitmgr.meterage.job.UpdateChargeItemResourceJob");
            insertTask.setTaskType(TaskTypeEnum.CALCULATE_CHARGE.getCode());
            insertTask.setSubTaskType(ChargeConstant.CHARGE);
            JSONObject jsonObjectChargeData = new JSONObject();
            jsonObjectChargeData.put(ChargeConstant.CHARGE_ID, chargeItemDTO.getUuid());
            jsonObjectChargeData.put(ChargeConstant.RESOURCE_CHARGE_DATA, JSONObject.toJSONString(chargeRecord));
            insertTask.setMetadata(jsonObjectChargeData.toJSONString());
            FhJobApiController.create(insertTask);
        }
        return new ReturnT<String>(0, null);
    }

    private void updateChargeItem(ChargeItemDTO chargeItemDTO, ChargeItem chargeItem, ChargeItemMapper chargeItemMapper) {
        ChargeItem updateChargeItem = new ChargeItem();
        BeanUtils.copyProperties(chargeItemDTO, updateChargeItem);
        updateChargeItem.setId(chargeItem.getId());
        updateChargeItem.setUpdateTime(LocalDateTime.now());
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
        // 初始化时间
        String timeStr = DateTimeConvertUtil.getTimeStr(LocalDateTime.now());
        String remark = timeStr + " " + ChargeConstant.METERAGE_ITEM_CHANGE.replace(ChargeConstant.CONCURRENT_PRICE, chargeItemDTO.getPrice().toString()) + ChargeConstant.UNITARY + "/" + unitName + "/" + timeName;
        remarkStr.add(remark);
        updateChargeItem.setRemark(JSONObject.toJSONString(remarkStr));
        chargeItemMapper.updateById(updateChargeItem);
    }

    @Override
    public void taskRollback(Task task, Exception error) {

    }
}
