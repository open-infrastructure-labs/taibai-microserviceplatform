package com.fitmgr.meterage.api.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author jy
 * @version 1.0
 * @date 2020/11/6 14:53
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ResourceMeterageRecordListVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页
     */
    @ApiModelProperty(value = "当前页", name = "current", required = false)
    private long current=1L;

    /**
     * 每页条数
     */
    @ApiModelProperty(value = "每页条数", name = "size", required = false)
    private long size=10L;

    /**
     * id
     */
    private Integer meterageId;


    /**
     * 计量开始时间
     */
    private String startTime;

    /**
     * 计量结束时间
     */
    private String endTime;

    /**
     * 逻辑删：0-正常 1-删除
     */
    private String delFlag;

    /**
     * 租户
     */
    private Integer tenantId;


    /**
     * project
     */
    private Integer projectId;


    /**
     * 用户
     */
    private Integer userId;

    /**
     * 租户名称
     */
    private String tenantName;

    /**
     * project名称
     */
    private String projectName;

    /**
     * 用户名称
     */
    private String userName;


    /**
     * 自定义名
     */
    private String cmpInstanceName;

    /**
     * 组件code
     */
    private String componentCode;


    /**
     * 实际计量数据
     */
    private String data;

    /**
     * 订单id
     */
    private Integer orderId;

    /**
     * 纳管标识
     */
    private String remarkFlag;

}
