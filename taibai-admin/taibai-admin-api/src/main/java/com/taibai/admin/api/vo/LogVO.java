
package com.taibai.admin.api.vo;

import java.io.Serializable;

import com.taibai.admin.api.entity.SysLog;

import lombok.Data;

/**
 * @author Taibai
 * @date 2017/11/20
 */
@Data
public class LogVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private SysLog sysLog;
    private String username;
}
