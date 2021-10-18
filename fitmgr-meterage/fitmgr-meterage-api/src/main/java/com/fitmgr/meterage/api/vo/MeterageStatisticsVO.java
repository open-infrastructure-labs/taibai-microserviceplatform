package com.fitmgr.meterage.api.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class MeterageStatisticsVO implements Serializable {

    @ApiModelProperty(value = "租户id", name = "tenantId")
    private Integer tenantId;

    @ApiModelProperty(value = "projectId", name = "projectId")
    private Integer projectId;

    @ApiModelProperty(value = "用户id", name = "userId")
    private Integer userId;

    @ApiModelProperty(value = "租户名称", name = "tenantName")
    private String tenantName;

    @ApiModelProperty(value = "project名称", name = "projectName")
    private String projectName;

    @ApiModelProperty(value = "用户名称", name = "userName")
    private String userName;

    @ApiModelProperty(value = "组件code", name = "componentCode")
    private String componentCode;

    /**
     * 计量统计值
     */
    @ApiModelProperty(value = "计量统计值", name = "statisticsValue")
    private double statisticsValue;
}
