package com.fitmgr.meterage.controller;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fitmgr.common.core.constant.SecurityConstants;
import com.fitmgr.common.core.util.SpringContextHolder;
import com.fitmgr.job.api.entity.Task;
import com.fitmgr.job.api.entity.TaskJobInfo;
import com.fitmgr.job.api.entity.XxlJobInfo;
import com.fitmgr.job.api.enums.ExecutorRouteStrategyEnum;
import com.fitmgr.job.api.enums.TaskExecTypeEnum;
import com.fitmgr.job.api.enums.TaskTypeEnum;
import com.fitmgr.job.api.feign.XxlTaskService;
import com.fitmgr.job.api.sdk.FhJobApiController;
import com.fitmgr.meterage.constant.ChargeConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author zhangxiaokang
 * @date 2021/5/25 10:41
 */
@Slf4j
@Component
@Order(value = 99999)
public class ChargeFeeTotalTask implements ApplicationRunner {

    private static final String CHARGE_MONTH_FEE_TOTAL_TASK_UUID = "CHARGE_MONTH_FEE_TOTAL_TASK_UUID";

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 查询任务中心，查看任务是否已创建
        Task dbTask = FhJobApiController.queryByTaskId(CHARGE_MONTH_FEE_TOTAL_TASK_UUID);
        if (dbTask != null) {
            log.info("task is already created. task={}", dbTask);
            // task存在而job被删除，则删除task信息，重新创建
            if (dbTask.getJobInfo() == null) {
                FhJobApiController.delete(CHARGE_MONTH_FEE_TOTAL_TASK_UUID);
            } else if (dbTask.getJobInfo().getTriggerStatus() == 0) {
                // 如果任务已停止，则重新启动
                FhJobApiController.start(dbTask.getJobId());
                return;
            }
        } else {
            // 创建任务 每月1日00:00:00执行
            String corn = "0 0 0 1 * ?";
            Task task = new Task();
            XxlJobInfo jobInfo = new XxlJobInfo();
            jobInfo.setExecutorHandler("defaultBeanHandler");
            jobInfo.setAuthor("admin_update");
            jobInfo.setJobDesc("月度费用统计，资源计划执行时间：每月1日00:00:00执行");
            // 设置任务的触发为轮询策略
            jobInfo.setExecutorRouteStrategy(ExecutorRouteStrategyEnum.ROUND.getCode());

            task.setJobInfo(jobInfo);
            task.setUuid(CHARGE_MONTH_FEE_TOTAL_TASK_UUID);
            task.setName("账单月度费用统计");
            task.setTaskExecType(TaskExecTypeEnum.PERIOD.getCode());
            task.setTaskPeriod("{\"corn\":\"" + corn + "\"}");
            task.setCallback("com.fitmgr.meterage.job.CalculateChargeJob");
            task.setTaskType(TaskTypeEnum.CALCULATE_CHARGE.getCode());
            task.setSubTaskType(ChargeConstant.CHARGE);
            FhJobApiController.create(task);
        }
    }
}
