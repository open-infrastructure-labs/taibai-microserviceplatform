package com.fitmgr.common.log.event;

import com.fitmgr.log.api.entity.OperateLog;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 系统日志事件
 * 
 * @author Fitmgr
 */
@Getter
@AllArgsConstructor
public class SysLogEvent {
    private final OperateLog operateLog;
}
