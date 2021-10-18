package com.fitmgr.meterage.job;

import com.fitmgr.job.api.core.biz.model.ReturnT;
import com.fitmgr.job.api.entity.Task;
import com.fitmgr.job.api.excutor.XxlBaseTaskExec;
import com.fitmgr.meterage.service.IResourceChargeRecordService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * XxlJob开发示例（Bean模式）
 *
 * 开发步骤： 1、在Spring Bean实例中，开发Job方法，方式格式要求为 "public ReturnT<String>
 * execute(String param)" 2、为Job方法添加注解 "@XxlJob(value="自定义jobhandler名称", init =
 * "JobHandler初始化方法", destroy =
 * "JobHandler销毁方法")"，注解value值对应的是调度中心新建任务的JobHandler属性的值。 3、执行日志：需要通过
 * "XxlJobLogger.log" 打印执行日志；
 *
 * @author xuxueli 2019-12-11 21:52:51
 */
@Slf4j
@Component
@Scope("prototype")
@AllArgsConstructor
public class CalculateChargeJob extends XxlBaseTaskExec {

    private final IResourceChargeRecordService resourceChargeRecordService;

    @Override
    public ReturnT<String> taskCallback(Task task) throws Exception {
        resourceChargeRecordService.totalChargeBillRecord();
        return new ReturnT<String>(0, null);
    }

    @Override
    public void taskRollback(Task task, Exception e) {
        log.error("月度费用结算定时执行报错！");
        e.printStackTrace();
    }
}
