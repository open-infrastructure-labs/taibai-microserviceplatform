package com.fitmgr.meterage.concurrent;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitmgr.common.core.constant.SecurityConstants;
import com.fitmgr.common.core.constant.enums.DeleteFlagStatusEnum;
import com.fitmgr.common.core.constant.enums.EnableStatusEnum;
import com.fitmgr.common.core.util.SpringContextHolder;
import com.fitmgr.job.api.entity.Task;
import com.fitmgr.job.api.entity.TaskJobInfo;
import com.fitmgr.job.api.entity.XxlJobInfo;
import com.fitmgr.job.api.enums.ExecutorRouteStrategyEnum;
import com.fitmgr.job.api.enums.TaskExecTypeEnum;
import com.fitmgr.job.api.enums.TaskTypeEnum;
import com.fitmgr.job.api.feign.XxlTaskService;
import com.fitmgr.job.api.sdk.FhJobApiController;
import com.fitmgr.meterage.api.entity.ChargeItem;
import com.fitmgr.meterage.api.entity.ResourceChargeRecord;
import com.fitmgr.meterage.constant.ChargeConstant;
import com.fitmgr.meterage.mapper.ChargeItemMapper;
import com.fitmgr.meterage.service.IResourceChargeRecordService;
import com.fitmgr.meterage.utils.DateConvertCronUtil;
import com.fitmgr.meterage.utils.DateTimeConvertUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 月度账单统计业务处理
 *
 * @author zhangxiaokang
 * @date 2020/10/30 10:27
 */
public class MonthTotalChargeBillThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(MonthTotalChargeBillThread.class);

    private List<ResourceChargeRecord> resourceChargeRecords;

    public MonthTotalChargeBillThread(List<ResourceChargeRecord> resourceChargeRecords) {
        this.resourceChargeRecords = resourceChargeRecords;
    }

    @Override
    public void run() {
        IResourceChargeRecordService chargeBillDetailService = SpringContextHolder.getBean(IResourceChargeRecordService.class);
        XxlTaskService xxlTaskService = SpringContextHolder.getBean(XxlTaskService.class);

        // 更新计费记录，当前计费记录费用截至
        String remark = ChargeConstant.MONTH_CHARGE_SUM.replace(ChargeConstant.MONTH, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM")));
        chargeBillDetailService.counterCharge(resourceChargeRecords, remark);

        // 新增计费记录的时候，需要查询更新之后的记录是否满一个计费周期，如果不满足一个计费周期则添加到定时任务当中，满足一个计费周期才新增
        List<String> chargeIdList = new ArrayList<>(resourceChargeRecords.stream().map(resourceChargeRecord -> resourceChargeRecord.getChargeId()).collect(Collectors.toSet()));
        LambdaQueryWrapper<ChargeItem> lambdaQueryWrapper = Wrappers.<ChargeItem>lambdaQuery()
                .in(ChargeItem::getUuid, chargeIdList)
                .eq(ChargeItem::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus())
                .eq(ChargeItem::getChargeStatus, EnableStatusEnum.ENABLE.getStatus());
        ChargeItemMapper itemMapper = SpringContextHolder.getBean(ChargeItemMapper.class);
        List<ChargeItem> chargeItems = itemMapper.selectList(lambdaQueryWrapper);
        if (CollectionUtils.isEmpty(chargeItems)) {
            logger.info("======== 计费项列表为空！ ==========");
            return;
        }
        Map<String, ChargeItem> chargeItemMap = chargeItems.stream().collect(Collectors.toMap(ChargeItem::getUuid, (p) -> p));
        List<ResourceChargeRecord> insertRecourdList = new ArrayList<>();
        for (ResourceChargeRecord resourceChargeRecord : resourceChargeRecords) {
            // 获取该资源对应的计费项目是否还存在，未删除并且是启用状态的
            ChargeItem chargeItem = chargeItemMap.get(resourceChargeRecord.getChargeId());
            if (null == chargeItem) {
                logger.info("chargeItem is null");
                continue;
            }
            Long serviceTime = chargeBillDetailService.getTime(chargeItem, resourceChargeRecord.getBeginUseTime(), LocalDateTime.now());
            if (serviceTime < 1) {
                TaskJobInfo taskJobInfo = new TaskJobInfo();
                taskJobInfo.setJobDesc(ChargeConstant.MONTH_TOTAL + resourceChargeRecord.getCmpInstanceName());
                taskJobInfo.setName(ChargeConstant.MONTH_TOTAL + resourceChargeRecord.getCmpInstanceName());
                List<TaskJobInfo> taskJobInfos = FhJobApiController.queryList(taskJobInfo);
                if (!CollectionUtils.isEmpty(taskJobInfos)) {
                    // 查询定时任务列表
                    Integer resultCount = 0;
                    for (TaskJobInfo jobInfo : taskJobInfos) {
                        // 执行状态 0-就绪 1-执行中 2-执行完成 3-执行失败
                        if (jobInfo.getExecStatus().equals(0) || jobInfo.getExecStatus().equals(1)) {
                            resultCount++; // 定时任务还未执行或者正在执行中
                        } else {
                            xxlTaskService.delete(jobInfo.getUuid(), SecurityConstants.FROM_IN);
                        }
                    }
                    if (!resultCount.equals(0)) {
                        logger.info("=============该资源已存在未执行完成的定时任务，不再创建新的定时任务！=============");
                        continue;
                    }
                }

                // 对该资源创建定时任务
                LocalDateTime taskDateTime = DateTimeConvertUtil.calculateDateTime(resourceChargeRecord.getBeginUseTime(), 1L, chargeItem.getChargeFlavorTime());
                if (taskDateTime.isBefore(LocalDateTime.now())) {
                    logger.error("计划执行时间早于当前时间，创建定时任务错误！计划执行时间：{}，当前时间：{}", taskDateTime, LocalDateTime.now());
                    return;
                }
                logger.info("======添加定时任务，计费项定时执行时间为：{}======", taskDateTime);
                Task task = new Task();
                XxlJobInfo jobInfo = new XxlJobInfo();
                jobInfo.setExecutorHandler("defaultBeanHandler");
                jobInfo.setAuthor("admin_enable");
                jobInfo.setJobDesc("月度费用统计资源定时新增时间为：" + taskDateTime);
                // 设置任务的触发为轮询策略
                jobInfo.setExecutorRouteStrategy(ExecutorRouteStrategyEnum.ROUND.getCode());
                task.setJobInfo(jobInfo);
                task.setUuid(UUID.randomUUID().toString());
                task.setName(ChargeConstant.MONTH_TOTAL + resourceChargeRecord.getCmpInstanceName());
                task.setTaskExecType(TaskExecTypeEnum.SINGLE.getCode());
                task.setTaskPeriod("{\"corn\":\"" + DateConvertCronUtil.getCron(taskDateTime) + "\"}");
                task.setCallback("com.fitmgr.meterage.job.EnableChargeItemJob");
                task.setTaskType(TaskTypeEnum.CALCULATE_CHARGE.getCode());
                task.setSubTaskType(ChargeConstant.CHARGE);
                // 设置计费项参数uuid
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(ChargeConstant.CHARGE_ID, chargeItem.getUuid());
                // 计费记录资源UUID
                jsonObject.put(ChargeConstant.RESOURCE_CHARGE_DATA, JSONObject.toJSONString(resourceChargeRecord));
                task.setMetadata(jsonObject.toJSONString());
                FhJobApiController.create(task);
                continue;
            }
            insertRecourdList.add(resourceChargeRecord);
        }
        if (CollectionUtils.isEmpty(insertRecourdList)) {
            logger.info("============= insertRecourdList is null ==========");
            return;
        }
        // 新增计费记录，对上面更新过的计费记录重新新增一条计费记录
        chargeBillDetailService.insertChargeBill(resourceChargeRecords, null);
    }
}
