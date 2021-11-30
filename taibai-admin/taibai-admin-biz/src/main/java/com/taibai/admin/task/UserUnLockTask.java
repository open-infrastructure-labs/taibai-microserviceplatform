package com.taibai.admin.task;

import java.time.LocalDateTime;
import java.util.List;

import com.taibai.admin.api.constants.UserLockStatus;
import com.taibai.admin.api.entity.LockedAccountRecord;
import com.taibai.admin.api.feign.RemoteAccountLockStrategyService;
import com.taibai.admin.api.feign.RemoteUserService;
import com.taibai.common.core.util.SpringContextHolder;
import com.taibai.job.api.core.biz.model.ReturnT;
import com.taibai.job.api.entity.Task;
import com.taibai.job.api.excutor.XxlBaseTaskExec;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserUnLockTask extends XxlBaseTaskExec {

    private RemoteAccountLockStrategyService remoteAccountLockStrategyService = SpringContextHolder
            .getBean(RemoteAccountLockStrategyService.class);
    private RemoteUserService remoteUserService = SpringContextHolder.getBean(RemoteUserService.class);

    @Override
    public ReturnT<String> taskCallback(Task task) throws Exception {
        List<LockedAccountRecord> lockedAccountRecords = remoteAccountLockStrategyService.getLockedAccountRecord()
                .getData();
        if (lockedAccountRecords != null && lockedAccountRecords.size() > 0) {
            for (LockedAccountRecord lockedAccountRecord : lockedAccountRecords) {
                LocalDateTime planUnlockTime = lockedAccountRecord.getPlanUnlockTime();
                if (planUnlockTime != null && LocalDateTime.now().isAfter(planUnlockTime)) {
                    // 解锁用户
                    remoteUserService.lockById(lockedAccountRecord.getUserId(), UserLockStatus.UN_LOCK.getCode());
                }
            }
        }
        ReturnT returnt = new ReturnT();
        returnt.setCode(0);
        return returnt;
    }

    @Override
    public void taskRollback(Task task, Exception e) {

    }
}
