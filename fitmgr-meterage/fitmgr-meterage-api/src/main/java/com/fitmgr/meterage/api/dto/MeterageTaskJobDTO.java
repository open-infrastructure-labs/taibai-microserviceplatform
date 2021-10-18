package com.fitmgr.meterage.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 定时任务入参DTO
 *
 * @author zhangxiaokang
 * @date 2020/11/12 10:40
 */
@Data
public class MeterageTaskJobDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务名称（必须）
     */
    @ApiModelProperty(value = "任务名称", name = "name")
    private String name;

    /**
     * 任务周期（corn表达式或interval）（周期任务必须）
     *
     * 格式：{"inteval":"0000-00-00 00:00:10"} 或 {"corn":"0/10 * * * * ?"}
     *
     */
    @ApiModelProperty(value = "任务周期", name = "taskPeriod")
    private String taskPeriod;

    /**
     * 执行类型 0-周期 1-单次（默认1）
     */
    @ApiModelProperty(value = "执行类型", name = "taskExecType")
    private Integer taskExecType;

    /**
     * 任务回调函数（必须）
     */
    @ApiModelProperty(value = "任务回调函数", name = "callback")
    private String callback;

    /**
     * 任务描述
     */
    @ApiModelProperty(value = "任务描述", name = "jobDesc")
    private String jobDesc;

}
