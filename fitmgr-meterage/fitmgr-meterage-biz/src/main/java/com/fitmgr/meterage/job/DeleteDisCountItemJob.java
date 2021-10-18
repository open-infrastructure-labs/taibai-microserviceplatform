package com.fitmgr.meterage.job;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitmgr.common.core.util.SpringContextHolder;
import com.fitmgr.job.api.core.biz.model.ReturnT;
import com.fitmgr.job.api.entity.Task;
import com.fitmgr.job.api.excutor.XxlBaseTaskExec;
import com.fitmgr.meterage.api.dto.ChargeItemDTO;
import com.fitmgr.meterage.api.entity.ResourceChargeRecord;
import com.fitmgr.meterage.constant.ChargeConstant;
import com.fitmgr.meterage.service.IResourceChargeRecordService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jy
 * @version 1.0
 * @date 2021/5/13 13:59
 */
@Slf4j
@Component
@Scope("prototype")
@AllArgsConstructor
public class DeleteDisCountItemJob extends XxlBaseTaskExec {
    @Override
    public ReturnT<String> taskCallback(Task task) throws Exception {
        IResourceChargeRecordService chargeBillDetailService = SpringContextHolder.getBean(IResourceChargeRecordService.class);
        List<ResourceChargeRecord> enableResourceChargeRecordList = new ArrayList<>();
        List<ResourceChargeRecord> ResourceChargeRecordList = new ArrayList<>();
        JSONObject jsonObject = JSONObject.parseObject(task.getMetadata());
        String resourceData = jsonObject.get(ChargeConstant.RESOURCE_CHARGE_DATA).toString();
        JSONArray jsonArray = JSONObject.parseArray(resourceData);
        //若当前资源已经在执行此任务时下线，则跳过当前资源计费
        List<Integer> recordIds = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            ResourceChargeRecord resourceChargeRecord = JSONObject.parseObject(jsonArray.get(i).toString(), ResourceChargeRecord.class);
            enableResourceChargeRecordList.add(resourceChargeRecord);
            recordIds.add(resourceChargeRecord.getId());
        }
        Map<Integer,Boolean> notCalMap = new HashMap<>();
        List<ResourceChargeRecord> resourceChargeRecords = chargeBillDetailService.list(Wrappers.<ResourceChargeRecord>lambdaQuery().in(recordIds.size() > 0, ResourceChargeRecord::getId, recordIds));
        for (ResourceChargeRecord resourceChargeRecord:resourceChargeRecords) {
            if(resourceChargeRecord.getResourceOffFlag() == 1){
                notCalMap.put(resourceChargeRecord.getId(), false);
            }
        }
        for (ResourceChargeRecord resourceChargeRecord:enableResourceChargeRecordList) {
            if(!notCalMap.containsKey(resourceChargeRecord.getId())){
                ResourceChargeRecordList.add(resourceChargeRecord);
            }
        }
        ChargeItemDTO chargeItemDTO = JSONObject.parseObject(jsonObject.get("chargeItemDTO").toString(), ChargeItemDTO.class);
        chargeBillDetailService.insertChargeBill(ResourceChargeRecordList, chargeItemDTO);
        return ReturnT.SUCCESS;
    }

    @Override
    public void taskRollback(Task task, Exception error) {

    }
}
