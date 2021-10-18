package com.fitmgr.meterage.job;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitmgr.job.api.core.biz.model.ReturnT;
import com.fitmgr.job.api.entity.Task;
import com.fitmgr.job.api.excutor.XxlBaseTaskExec;
import com.fitmgr.job.api.sdk.FhJobApiController;
import com.fitmgr.meterage.api.entity.DiscountItem;
import com.fitmgr.meterage.api.entity.ResourceChargeRecord;
import com.fitmgr.meterage.constant.ChargeConstant;
import com.fitmgr.meterage.service.IDiscountItemService;
import com.fitmgr.meterage.service.IResourceChargeRecordService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author jy
 * @version 1.0
 * @date 2021/4/12 9:10
 * 计划生效的折扣任务，该任务主要执行当前计费项的旧折扣结算和新折扣的新计费
 */
@Slf4j
@Component
@Scope("prototype")
@AllArgsConstructor
public class CreateStartDisCountTaskJob extends XxlBaseTaskExec {

    private final IDiscountItemService discountItemService;

    @Override
    public ReturnT<String> taskCallback(Task task) throws Exception {
        JSONObject jsonObject = JSONObject.parseObject(task.getMetadata());
        String disCountItemUuid = jsonObject.get("disCountItemUuid").toString();
        String start = null;
        String end = null;
        if(null != jsonObject.get("planTime")){
            start = jsonObject.get("planTime").toString();
        }
        if(null != jsonObject.get("endTime")){
            end = jsonObject.get("endTime").toString();
        }
        //修改生效时间
        DiscountItem discountItem = discountItemService.getOne(Wrappers.<DiscountItem>lambdaQuery().eq(DiscountItem::getUuid, disCountItemUuid));
        discountItem.setCurrentDiscountEffectTime(LocalDateTime.now());
        if(start != null){
            discountItem.setDiscountStatus(0);
            FhJobApiController.delete(disCountItemUuid + "start");
        }else if(end != null){
            discountItem.setDiscountStatus(1);
            FhJobApiController.delete(disCountItemUuid + "end");
        }
        discountItemService.updateById(discountItem);

        return ReturnT.SUCCESS;
    }

    @Override
    public void taskRollback(Task task, Exception error) {

    }
}
