
package com.taibai.common.log.event;

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;

import com.taibai.common.core.constant.SecurityConstants;
import com.taibai.log.api.entity.OperateLog;
import com.taibai.log.api.feign.RemoteOperateLogService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 异步监听日志事件
 * 
 * @author Taibai
 */
@Slf4j
@AllArgsConstructor
public class SysLogListener {
    private final RemoteOperateLogService remoteOperateLogService;

    @Async
    @Order
    @EventListener(SysLogEvent.class)
    public void saveSysLog(SysLogEvent event) {
        OperateLog operateLog = event.getOperateLog();
        remoteOperateLogService.saveLog(operateLog, SecurityConstants.FROM_IN);
    }
}
