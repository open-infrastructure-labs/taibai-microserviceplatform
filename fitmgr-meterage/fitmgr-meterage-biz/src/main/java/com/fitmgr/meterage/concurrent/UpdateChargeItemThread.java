package com.fitmgr.meterage.concurrent;

import com.alibaba.fastjson.JSONObject;
import com.fitmgr.common.core.util.SpringContextHolder;
import com.fitmgr.job.api.entity.Task;
import com.fitmgr.job.api.entity.XxlJobInfo;
import com.fitmgr.job.api.enums.ExecutorRouteStrategyEnum;
import com.fitmgr.job.api.enums.TaskExecTypeEnum;
import com.fitmgr.job.api.enums.TaskTypeEnum;
import com.fitmgr.job.api.sdk.FhJobApiController;
import com.fitmgr.meterage.api.dto.ChargeItemDTO;
import com.fitmgr.meterage.api.entity.ChargeItem;
import com.fitmgr.meterage.api.entity.ResourceChargeRecord;
import com.fitmgr.meterage.constant.ChargeConstant;
import com.fitmgr.meterage.service.IResourceChargeRecordService;
import com.fitmgr.meterage.utils.DateConvertCronUtil;
import com.fitmgr.meterage.utils.DateTimeConvertUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 更新计费项，匹配账单记录
 *
 * @author zhangxiaokang
 * @date 2020/10/29 10:53
 */
public class UpdateChargeItemThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(UpdateChargeItemThread.class);

    private ChargeItemDTO chargeItemDTO;

    private List<ResourceChargeRecord> resourceChargeRecordList;

    public UpdateChargeItemThread(ChargeItemDTO chargeItemDTO, List<ResourceChargeRecord> resourceChargeRecordList) {
        this.chargeItemDTO = chargeItemDTO;
        this.resourceChargeRecordList = resourceChargeRecordList;
    }

    @Override
    public void run() {
        IResourceChargeRecordService chargeBillDetailService = SpringContextHolder.getBean(IResourceChargeRecordService.class);

        // 更新计费项目，先对原先的费用截至计算
        String remark = ChargeConstant.CHARGE_ITEM_CHANGE.replace(ChargeConstant.CONCURRENT_PRICE, chargeItemDTO.getPrice().toString());
        chargeBillDetailService.counterCharge(resourceChargeRecordList, remark);

        // 不满一个计费周期的结算，然后需要创建一个定时任务，在本次结算之后再将本资源入库
        List<ResourceChargeRecord> insertResourceChargeRecord = new ArrayList<>();
        for (ResourceChargeRecord chargeRecord : resourceChargeRecordList) {
            ChargeItem chargeItem = new ChargeItem();
            BeanUtils.copyProperties(chargeItemDTO,chargeItem);
            Long accountTime = chargeBillDetailService.accountTime(chargeItem, chargeRecord.getBeginUseTime(), LocalDateTime.now());
            if (accountTime.equals(1L)) {
                logger.info("========时长duration为1，表示开始计费时间和结束计费时间不满一个计费时长周期，不计入更新计费记录集合，将需要更新计费的资源添加到定时任务，待满一个计费周期之后，再更新资源计费记录======");
                // 获取下一个定时任务执行的时间
                LocalDateTime taskDateTime = DateTimeConvertUtil.calculateDateTime(chargeRecord.getBeginUseTime(), 1L, chargeItemDTO.getChargeFlavorTime());
                // 新增定时任务，将资源添加到定时任务当中
                Task task = new Task();
                XxlJobInfo jobInfo = new XxlJobInfo();
                jobInfo.setExecutorHandler("defaultBeanHandler");
                jobInfo.setAuthor("admin_update");
                jobInfo.setJobDesc("更新资源计费项，计划执行时间：" + taskDateTime);
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
                // 设置计费项uuid
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(ChargeConstant.CHARGE_ID, chargeItemDTO.getUuid());
                jsonObject.put(ChargeConstant.RESOURCE_CHARGE_DATA, JSONObject.toJSONString(chargeRecord));
                task.setMetadata(jsonObject.toJSONString());
                FhJobApiController.create(task);
            }else {
                insertResourceChargeRecord.add(chargeRecord);
            }
        }
        if (CollectionUtils.isEmpty(insertResourceChargeRecord)){
            return;
        }
        // 新增资源记录，重新换成新的计费项
        chargeItemDTO.setRemark(ChargeConstant.UPDATE_CHARGE_ITEM_CHANGE);
        chargeBillDetailService.insertChargeBill(insertResourceChargeRecord, chargeItemDTO);
    }
}
