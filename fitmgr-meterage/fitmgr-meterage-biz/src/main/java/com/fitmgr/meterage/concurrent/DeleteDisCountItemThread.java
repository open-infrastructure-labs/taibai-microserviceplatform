package com.fitmgr.meterage.concurrent;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import com.fitmgr.common.core.constant.enums.EnableStatusEnum;
import com.fitmgr.common.core.util.SpringContextHolder;
import com.fitmgr.job.api.entity.Task;
import com.fitmgr.job.api.entity.XxlJobInfo;
import com.fitmgr.job.api.enums.ExecutorRouteStrategyEnum;
import com.fitmgr.job.api.enums.TaskExecTypeEnum;
import com.fitmgr.job.api.enums.TaskTypeEnum;
import com.fitmgr.job.api.sdk.FhJobApiController;
import com.fitmgr.meterage.api.dto.ChargeItemDTO;
import com.fitmgr.meterage.api.entity.ChargeItem;
import com.fitmgr.meterage.api.entity.DiscountItem;
import com.fitmgr.meterage.api.entity.ResourceChargeRecord;
import com.fitmgr.meterage.api.vo.ChargeItemVO;
import com.fitmgr.meterage.constant.ChargeConstant;
import com.fitmgr.meterage.service.IResourceChargeRecordService;
import com.fitmgr.meterage.utils.DateConvertCronUtil;
import com.fitmgr.meterage.utils.DateTimeConvertUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * @author jy
 * @version 1.0
 * @date 2021/5/13 9:48
 */
public class DeleteDisCountItemThread implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(DisableChargeItemThread.class);

    private List<ResourceChargeRecord> resourceChargeRecordList;

    private ChargeItem insertChargeItem;

    private List<DiscountItem> disCountItemList;

    public DeleteDisCountItemThread(List<DiscountItem> disCountItemList, ChargeItem insertChargeItem, List<ResourceChargeRecord> resourceChargeRecordList) {
        this.insertChargeItem = insertChargeItem;
        this.disCountItemList = disCountItemList;
        this.resourceChargeRecordList = resourceChargeRecordList;
    }

    @Override
    public void run() {
        IResourceChargeRecordService chargeBillDetailService = SpringContextHolder.getBean(IResourceChargeRecordService.class);
        resourceChargeRecordList.forEach(resourceChargeRecord -> resourceChargeRecord.setEnableFlag(EnableStatusEnum.DISABLE.getStatus()));

        String remark = ChargeConstant.DELETE_DISCOUNT_ITEM_CHANGE;
        chargeBillDetailService.counterCharge(resourceChargeRecordList,remark);

        //修改当前数据的折扣信息
        for (ResourceChargeRecord resourceChargeRecord:resourceChargeRecordList) {
            DiscountItem discountItem = getDisCount(resourceChargeRecord);
            resourceChargeRecord.setDiscountId(discountItem.getUuid());
            resourceChargeRecord.setDiscount(discountItem.getCurrentDiscount());
        }
        logger.info("resourceChargeRecordList is", resourceChargeRecordList);
        //创建定时任务执行启用操作，实现数据的重新计费
        ResourceChargeRecord maxDisableResourceChargeRecord = resourceChargeRecordList.stream().max(Comparator.comparingInt(ResourceChargeRecord::getId)).get();
        LocalDateTime beginUseTime = maxDisableResourceChargeRecord.getBeginUseTime();
        LocalDateTime nowDateTime = LocalDateTime.now();
        Long serviceTime = chargeBillDetailService.getTime(insertChargeItem, beginUseTime, nowDateTime);
        ChargeItemDTO chargeItemDTO = new ChargeItemDTO();
        BeanUtil.copyProperties(insertChargeItem, chargeItemDTO);
        // 开始时间与禁用时间不满一个计费周期
        if (serviceTime < 1) {
            //立即设置定时任务，设置定时任务的时间
            LocalDateTime taskDateTime = DateTimeConvertUtil.calculateDateTime(beginUseTime, 1L, insertChargeItem.getChargeFlavorTime());
            this.setSecudleTask(taskDateTime, resourceChargeRecordList, chargeItemDTO);
        }else {
            for (ResourceChargeRecord resourceChargeRecord:resourceChargeRecordList) {
                resourceChargeRecord.setBeginUseTime(LocalDateTime.now());
                resourceChargeRecord.setBillCycleTime(LocalDateTime.now());
            }
            chargeBillDetailService.insertChargeBill(resourceChargeRecordList, chargeItemDTO);
        }
    }

    private DiscountItem getDisCount(ResourceChargeRecord resourceChargeRecord){
        DiscountItem priorityDiscount = new DiscountItem();
        for (DiscountItem discountItemVO : disCountItemList) {
            if (null == discountItemVO.getProjectId()) {
                continue;
            }
            priorityDiscount = discountItemVO;
        }
        // 没有查询到Project折扣，匹配租户折扣
        for (DiscountItem discountItemVO : disCountItemList) {
            // 系统级别租户
            if (discountItemVO.getTenantId().equals(-1)) {
                continue;
            }
            if (discountItemVO.getTenantId().equals(resourceChargeRecord.getTenantId())) {
                // tenant级别折扣
                priorityDiscount = discountItemVO;
            }
        }
        // 没有租户及Project级别租户，只有系统级别租户，直接用系统级别租户
        for (DiscountItem discountItemVO : disCountItemList) {
            // 系统级别租户
            if (discountItemVO.getTenantId().equals(-1)) {
                priorityDiscount = discountItemVO;
            }
        }

        return priorityDiscount;
    }

    private void setSecudleTask(LocalDateTime taskDateTime, List<ResourceChargeRecord> resourceChargeRecords, ChargeItemDTO chargeItemDTO) {
        logger.info("======添加定时任务，计费待执行数据为：{}======", resourceChargeRecords);
        logger.info("======添加定时任务，计费继续执行时间为：{}======", taskDateTime);
        logger.info("======添加定时任务，计费折扣数据为：{}======", disCountItemList);
        Task task = new Task();
        XxlJobInfo jobInfo = new XxlJobInfo();
        jobInfo.setExecutorHandler("defaultBeanHandler");
        jobInfo.setAuthor("admin_enable");
        jobInfo.setJobDesc("计费计划执行时间：" + taskDateTime);
        // 设置任务的触发为轮询策略
        jobInfo.setExecutorRouteStrategy(ExecutorRouteStrategyEnum.ROUND.getCode());
        task.setJobInfo(jobInfo);
        task.setUuid(UUID.randomUUID().toString());
        task.setName("计费折扣变更任务");
        task.setTaskExecType(TaskExecTypeEnum.SINGLE.getCode());
        task.setTaskPeriod("{\"corn\":\"" + DateConvertCronUtil.getCron(taskDateTime) + "\"}");
        task.setCallback("com.fitmgr.meterage.job.DeleteDisCountItemJob");
        task.setTaskType(TaskTypeEnum.CALCULATE_CHARGE.getCode());
        task.setSubTaskType(ChargeConstant.CHARGE);
        JSONObject jsonObject = new JSONObject();
        // 计费记录资源
        jsonObject.put(ChargeConstant.RESOURCE_CHARGE_DATA, JSONObject.toJSONString(resourceChargeRecords));
        // 计费记录资源
        jsonObject.put("chargeItemDTO", JSONObject.toJSONString(chargeItemDTO));
        task.setMetadata(jsonObject.toJSONString());
        boolean resultFlag = FhJobApiController.create(task);
        logger.info("========添加定时任务执行结果：{}========", resultFlag);
    }
}
