package com.fitmgr.meterage.concurrent;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fitmgr.common.core.constant.SecurityConstants;
import com.fitmgr.common.core.util.SpringContextHolder;
import com.fitmgr.job.api.entity.TaskJobInfo;
import com.fitmgr.job.api.feign.XxlTaskService;
import com.fitmgr.job.api.sdk.FhJobApiController;
import com.fitmgr.meterage.constant.ChargeConstant;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 删除计量项，异步删除资源对用的定时任务
 *
 * @author zhangxiaokang
 * @date 2021/8/23 16:27
 */
@Slf4j
public class AsynDeleteResourceTaskThread implements Runnable {

    private String cmpInstanceName;

    public AsynDeleteResourceTaskThread(String cmpInstanceName) {
        this.cmpInstanceName = cmpInstanceName;
    }

    @Override
    public void run() {
        log.info("delete resource , asynchronize delete charge task start ......");
        XxlTaskService xxlTaskService = SpringContextHolder.getBean(XxlTaskService.class);
        // 需要下线的资源需要查询任务中心有没有待执行需要入库的资源，如果有则需要删除任务
        TaskJobInfo taskJobInfo = new TaskJobInfo();
        taskJobInfo.setJobDesc(ChargeConstant.INSERT_CHARGE + cmpInstanceName);
        taskJobInfo.setName(ChargeConstant.INSERT_CHARGE + cmpInstanceName);
        List<TaskJobInfo> insertTaskJobInfos = FhJobApiController.queryList(taskJobInfo);
        if (CollectionUtils.isNotEmpty(insertTaskJobInfos)) {
            for (TaskJobInfo jobInfo : insertTaskJobInfos) {
                log.info("delete insert task ......");
                xxlTaskService.delete(jobInfo.getUuid(), SecurityConstants.FROM_IN);
            }
        }
        taskJobInfo.setJobDesc(ChargeConstant.ENABLE_CHARGE + cmpInstanceName);
        taskJobInfo.setName(ChargeConstant.ENABLE_CHARGE + cmpInstanceName);
        List<TaskJobInfo> enableTaskJobInfos = FhJobApiController.queryList(taskJobInfo);
        if (CollectionUtils.isNotEmpty(enableTaskJobInfos)) {
            for (TaskJobInfo jobInfo : enableTaskJobInfos) {
                log.info("delete enable task ......");
                xxlTaskService.delete(jobInfo.getUuid(), SecurityConstants.FROM_IN);
            }
        }
        taskJobInfo.setJobDesc(ChargeConstant.UPDATE_CHARGE + cmpInstanceName);
        taskJobInfo.setName(ChargeConstant.UPDATE_CHARGE + cmpInstanceName);
        List<TaskJobInfo> updateTaskJobInfos = FhJobApiController.queryList(taskJobInfo);
        if (CollectionUtils.isNotEmpty(updateTaskJobInfos)) {
            for (TaskJobInfo jobInfo : updateTaskJobInfos) {
                log.info("delete update task ......");
                xxlTaskService.delete(jobInfo.getUuid(), SecurityConstants.FROM_IN);
            }
        }
        taskJobInfo.setJobDesc(ChargeConstant.MONTH_TOTAL + cmpInstanceName);
        taskJobInfo.setName(ChargeConstant.MONTH_TOTAL + cmpInstanceName);
        List<TaskJobInfo> mothTaskJobInfos = FhJobApiController.queryList(taskJobInfo);
        if (CollectionUtils.isNotEmpty(mothTaskJobInfos)) {
            for (TaskJobInfo jobInfo : mothTaskJobInfos) {
                log.info("delete month total task ......");
                xxlTaskService.delete(jobInfo.getUuid(), SecurityConstants.FROM_IN);
            }
        }
        log.info("delete resource , asynchronize delete charge task end ......");
    }
}
