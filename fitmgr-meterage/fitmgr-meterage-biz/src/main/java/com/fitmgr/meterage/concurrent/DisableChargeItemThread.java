package com.fitmgr.meterage.concurrent;

import com.fitmgr.common.core.constant.enums.EnableStatusEnum;
import com.fitmgr.common.core.util.SpringContextHolder;
import com.fitmgr.meterage.api.entity.ResourceChargeRecord;
import com.fitmgr.meterage.constant.ChargeConstant;
import com.fitmgr.meterage.service.IResourceChargeRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 禁用计费项，异步更新账单记录
 *
 * @author zhangxiaokang
 * @date 2020/10/29 10:56
 */
public class DisableChargeItemThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(DisableChargeItemThread.class);

    private String chargeItemId;

    private List<ResourceChargeRecord> resourceChargeRecordList;

    public DisableChargeItemThread(String chargeItemId, List<ResourceChargeRecord> resourceChargeRecordList) {
        this.chargeItemId = chargeItemId;
        this.resourceChargeRecordList = resourceChargeRecordList;
    }

    @Override
    public void run() {
        IResourceChargeRecordService chargeBillDetailService = SpringContextHolder.getBean(IResourceChargeRecordService.class);
        resourceChargeRecordList.forEach(resourceChargeRecord -> resourceChargeRecord.setEnableFlag(EnableStatusEnum.DISABLE.getStatus()));

        // 对上面截至的计费记录，重新新增一条计费记录
        String remark = ChargeConstant.DISABLE_CHARGE_ITEM_CHANGE;
        chargeBillDetailService.counterCharge(resourceChargeRecordList,remark);
    }
}
