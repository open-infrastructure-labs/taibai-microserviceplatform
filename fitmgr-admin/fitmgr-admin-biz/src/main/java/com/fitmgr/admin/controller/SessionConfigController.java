package com.fitmgr.admin.controller;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.fitmgr.admin.api.entity.RestrictLloginTime;
import com.fitmgr.admin.api.entity.SessionConfig;
import com.fitmgr.admin.service.IRestrictLoginTimeService;
import com.fitmgr.admin.service.ISessionConfigService;
import com.fitmgr.admin.service.ISessionService;
import com.fitmgr.admin.task.RestrictLoginTimeTask;
import com.fitmgr.common.core.util.R;
import com.fitmgr.common.log.annotation.SysLog;
import com.fitmgr.job.api.entity.Task;
import com.fitmgr.job.api.entity.XxlJobInfo;
import com.fitmgr.job.api.enums.TaskExecTypeEnum;
import com.fitmgr.job.api.sdk.FhJobApiController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/session-config")
@Api(value = "session-config", tags = "会话配置")
public class SessionConfigController {

    private final String SWITCHES_ON = "0";

    private final ISessionConfigService sessionConfigService;
    private final IRestrictLoginTimeService restrictLoginTimeService;
    private final ISessionService sessionService;

    @ApiOperation(value = "查询会话配置")
    @GetMapping
    public R<SessionConfig> querySessionConfig() {
        return R.ok(sessionConfigService.querySessionConfig());
    }

    @SysLog(value = "修改会话配置", cloudResType = "会话")
    @ApiOperation(value = "修改会话配置")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "sessionConfig", dataType = "SessionConfig", required = true, value = "会话配置") })
    @PutMapping
    public R updateSessionConfig(@RequestBody SessionConfig sessionConfig) {
        sessionConfigService.modifySessionConfig(sessionConfig);
        return R.ok();
    }

    @ApiOperation(value = "查询不允许访问时间段配置")
    @GetMapping("/restrict-login-time")
    public R<RestrictLloginTime> queryRestrictLloginTime() {
        return R.ok(restrictLoginTimeService.list().get(0));
    }

    @SysLog(value = "修改不允许访问时间段配置", cloudResType = "会话")
    @ApiOperation(value = "修改不允许访问时间段配置")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "restrictLloginTime", dataType = "RestrictLloginTime", required = true, value = "不允许访问时间段配置") })
    @PutMapping("/restrict-login-time")
    public R<Boolean> updateRestrictLloginTime(@RequestBody RestrictLloginTime restrictLloginTime) {
        LocalDateTime now = LocalDateTime.now();
        if (SWITCHES_ON.equals(restrictLloginTime.getSwitches()) && now.isBefore(restrictLloginTime.getEndTime())) {
            SimpleDateFormat sdf = new SimpleDateFormat("ss mm HH dd MM ? yyyy");
            // LocalDateTime转Date
            LocalDateTime localDateTime = restrictLloginTime.getStartTime();
            ZoneId zone = ZoneId.systemDefault();
            Instant instant = localDateTime.atZone(zone).toInstant();
            Date date = Date.from(instant);

            Task task = new Task();

            // 任务名称，描述这个任务具体的作用
            task.setName("不允许访问时间段踢出所有用户");
            // 任务是周期任务还是单次任务，0为周期，1为单次
            task.setTaskExecType(TaskExecTypeEnum.SINGLE.getCode());
            if (now.isBefore(localDateTime)) {
                task.setTaskPeriod("{\"corn\":\"" + sdf.format(date) + "\"}");
            }
            // 设置任务类型，对应任务中心二级菜单
            task.setTaskType(8);
            // 设置任务子类型,方便对任务进行过滤查询
            task.setSubTaskType("不允许访问时间段踢出所有用户");
            // 任务的Metadata，可以用来传递任务的上下文
            Map<String, Object> meta = new HashMap<>();
            task.setMetadata(JSON.toJSONString(meta));
            // 注册任务回调的类
            task.setCallback(RestrictLoginTimeTask.class.toString().split("class ")[1]);

            XxlJobInfo jobInfo = new XxlJobInfo();
            // 任务的执行者
            jobInfo.setAuthor("CMP");
            // 任务的执行器
            jobInfo.setExecutorHandler("defaultBeanHandler");
            task.setJobInfo(jobInfo);

            // 创建任务
            try {
                boolean r = FhJobApiController.create(task);
            } catch (Exception e) {
            }
        }
        return R.ok(restrictLoginTimeService.updateById(restrictLloginTime));
    }
}
