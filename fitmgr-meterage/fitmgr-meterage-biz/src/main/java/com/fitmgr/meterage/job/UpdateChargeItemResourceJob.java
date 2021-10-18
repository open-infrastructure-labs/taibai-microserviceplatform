package com.fitmgr.meterage.job;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitmgr.common.core.constant.enums.DeleteFlagStatusEnum;
import com.fitmgr.common.core.constant.enums.EnableStatusEnum;
import com.fitmgr.common.core.util.SpringContextHolder;
import com.fitmgr.job.api.core.biz.model.ReturnT;
import com.fitmgr.job.api.entity.Task;
import com.fitmgr.job.api.excutor.XxlBaseTaskExec;
import com.fitmgr.meterage.api.dto.ChargeItemDTO;
import com.fitmgr.meterage.api.entity.ChargeItem;
import com.fitmgr.meterage.api.entity.ResourceChargeRecord;
import com.fitmgr.meterage.constant.ChargeConstant;
import com.fitmgr.meterage.mapper.ChargeItemMapper;
import com.fitmgr.meterage.mapper.ResourceChargeRecordMapper;
import com.fitmgr.meterage.service.IResourceChargeRecordService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangxiaokang
 * @date 2020/12/3 18:17
 */
@Slf4j
@Component
@Scope("prototype")
@AllArgsConstructor
public class UpdateChargeItemResourceJob extends XxlBaseTaskExec {
    @Override
    public ReturnT<String> taskCallback(Task task) throws Exception {
        String metadata = task.getMetadata();
        if (StringUtils.isBlank(metadata)) {
            log.info("========= metadata is null! ========");
            return new ReturnT<>(0, null);
        }
        JSONObject jsonObject = JSONObject.parseObject(metadata);
        String chargeId = jsonObject.getString(ChargeConstant.CHARGE_ID);
        String resourceData = jsonObject.getString(ChargeConstant.RESOURCE_CHARGE_DATA);
        if (StringUtils.isBlank(chargeId) || StringUtils.isBlank(resourceData)) {
            log.info("========== chargeId is null or resourceData is null , chargeId is {},resourceData is {}========", chargeId, resourceData);
            return new ReturnT<>(0, null);
        }
        ResourceChargeRecord resourceChargeRecord = JSONObject.parseObject(resourceData, ResourceChargeRecord.class);
        if (null == resourceChargeRecord) {
            log.info(" resourceChargeRecord is null !");
            return new ReturnT<>(0, null);
        }
        log.info(" resourceChargeRecord = {}", JSONObject.toJSONString(resourceChargeRecord));
        ChargeItemMapper chargeItemMapper = SpringContextHolder.getBean(ChargeItemMapper.class);
        LambdaQueryWrapper<ChargeItem> itemLambdaQueryWrapper = Wrappers.<ChargeItem>lambdaQuery()
                .eq(ChargeItem::getUuid, chargeId)
                .eq(ChargeItem::getChargeStatus, EnableStatusEnum.ENABLE.getStatus())
                .eq(ChargeItem::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus());
        ChargeItem chargeItem = chargeItemMapper.selectOne(itemLambdaQueryWrapper);
        if (null == chargeItem) {
            log.info("chargeItem is null !");
            return new ReturnT<>(0, null);
        }

        // 查询是否已经有该资源启用信息，如果有则不能重复入库
        LambdaQueryWrapper<ResourceChargeRecord> queryWrapperDbResourceRecord = Wrappers.<ResourceChargeRecord>lambdaQuery()
                .eq(ResourceChargeRecord::getCmpInstanceName, resourceChargeRecord.getCmpInstanceName())
                .eq(ResourceChargeRecord::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus())
                .eq(ResourceChargeRecord::getEnableFlag, EnableStatusEnum.ENABLE.getStatus())
                .eq(ResourceChargeRecord::getResourceOffFlag, 0)
                .isNull(ResourceChargeRecord::getFinishUseTime);
        ResourceChargeRecordMapper resourceChargeRecordMapper = SpringContextHolder.getBean(ResourceChargeRecordMapper.class);
        ResourceChargeRecord dbChargeRecord = resourceChargeRecordMapper.selectOne(queryWrapperDbResourceRecord);
        if (dbChargeRecord != null) {
            log.info("========== 计费记录中已经存在该资源未结算的计费记录，不能重复入库==========，资源信息：{}", JSONObject.toJSONString(dbChargeRecord));
            return new ReturnT<>(0, null);
        }

        List<ResourceChargeRecord> insertResourceChargeRecordList = new ArrayList<>();
        insertResourceChargeRecordList.add(resourceChargeRecord);

        // 新增资源记录，重新换成新的计费项
        ChargeItemDTO chargeItemDTO = new ChargeItemDTO();
        BeanUtils.copyProperties(chargeItem, chargeItemDTO);
        IResourceChargeRecordService chargeBillDetailService = SpringContextHolder.getBean(IResourceChargeRecordService.class);
        chargeItemDTO.setRemark(ChargeConstant.UPDATE_CHARGE_ITEM_CHANGE);
        chargeBillDetailService.insertChargeBill(insertResourceChargeRecordList, chargeItemDTO);
        return new ReturnT<>(0, null);
    }

    @Override
    public void taskRollback(Task task, Exception error) {

    }
}
