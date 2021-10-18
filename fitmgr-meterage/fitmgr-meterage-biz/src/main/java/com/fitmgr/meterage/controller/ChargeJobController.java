package com.fitmgr.meterage.controller;

import com.fitmgr.common.security.util.SecurityUtils;
import com.fitmgr.job.api.entity.Task;
import com.fitmgr.job.api.entity.XxlJobInfo;
import com.fitmgr.job.api.enums.TaskTypeEnum;
import com.fitmgr.job.api.sdk.FhJobApiController;
import com.fitmgr.meterage.api.dto.MeterageTaskJobDTO;
import com.fitmgr.meterage.constant.ChargeConstant;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fitmgr.common.core.util.R;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * @author zhangxiaokang
 * @date 2020/11/12 10:17
 */
@Slf4j
@RestController
@RequestMapping("/charge/job")
@AllArgsConstructor
public class ChargeJobController {

    @PostMapping
    public R getChargeJob(@RequestBody MeterageTaskJobDTO taskJobDTO){
        Task task = new Task();
        XxlJobInfo jobInfo = new XxlJobInfo();
        jobInfo.setExecutorHandler("defaultBeanHandler");
        jobInfo.setAuthor(SecurityUtils.getUser().getUsername());
        jobInfo.setJobDesc(taskJobDTO.getJobDesc());
        task.setJobInfo(jobInfo);

        String uuid = UUID.randomUUID().toString();
        task.setUuid(uuid);
        task.setName(taskJobDTO.getName());
        task.setTaskExecType(taskJobDTO.getTaskExecType());
        task.setTaskPeriod("{\"corn\":\"" + taskJobDTO.getTaskPeriod() + "\"}");
        task.setCallback(taskJobDTO.getCallback());
        task.setTaskType(TaskTypeEnum.CALCULATE_CHARGE.getCode());
        task.setSubTaskType(ChargeConstant.CHARGE);
        return R.ok(FhJobApiController.create(task));
    }
}
