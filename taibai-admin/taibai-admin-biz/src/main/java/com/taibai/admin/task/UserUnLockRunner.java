package com.taibai.admin.task;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.taibai.job.api.entity.Task;
import com.taibai.job.api.entity.XxlJobInfo;
import com.taibai.job.api.enums.TaskExecTypeEnum;
import com.taibai.job.api.sdk.FhJobApiController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UserUnLockRunner implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        startCheckSessionTimeout();
    }

    private void startCheckSessionTimeout() {
        log.info("startUserUnLock");
        Task task = new Task();

        //任务名称，描述这个任务具体的作用
        task.setName("自动解锁用户");
        //任务是周期任务还是单次任务，0为周期，1为单次
        task.setTaskExecType(TaskExecTypeEnum.PERIOD.getCode());
        task.setTaskPeriod("{\"corn\":\"0/30 * * * * ?\"}");
        task.setUuid("UserUnLockTask");
        //设置任务类型，对应任务中心二级菜单
        task.setTaskType(8);
        //设置任务子类型,方便对任务进行过滤查询
        task.setSubTaskType("自动解锁用户");
        //任务的Metadata，可以用来传递任务的上下文
        Map<String, Object> meta = new HashMap<>();
        task.setMetadata(JSON.toJSONString(meta));
        //注册任务回调的类
        task.setCallback(UserUnLockTask.class.toString().split("class ")[1]);

        XxlJobInfo jobInfo = new XxlJobInfo();
        //任务的执行者
        jobInfo.setAuthor("CMP");
        //任务的执行器
        jobInfo.setExecutorHandler("defaultBeanHandler");
        task.setJobInfo(jobInfo);

        //创建任务
        try {
            boolean r = FhJobApiController.create(task);
        } catch (Exception e) {
        }
    }
}
