package com.fitmgr.meterage.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author jy
 * @version 1.0
 * @date 2021/4/9 11:20
 */
@Data
public class MeterageViewDataDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 资源名称
     */
    private String resourceName;

    /**
     * 使用时长
     */
    private String usageTime;

    /**
     * 使用时长
     */
    private Long time;
}
