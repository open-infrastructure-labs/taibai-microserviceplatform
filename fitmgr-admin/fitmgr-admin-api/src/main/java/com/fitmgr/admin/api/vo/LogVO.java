
package com.fitmgr.admin.api.vo;

import java.io.Serializable;

import com.fitmgr.admin.api.entity.SysLog;

import lombok.Data;

/**
 * @author Fitmgr
 * @date 2017/11/20
 */
@Data
public class LogVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private SysLog sysLog;
    private String username;
}
