package com.taibai.common.log.event;

import com.taibai.log.api.entity.OperateLog;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 系统日志事件
 * 
 * @author Taibai
 */
@Getter
@AllArgsConstructor
public class SysLogEvent {
    private final OperateLog operateLog;
}
