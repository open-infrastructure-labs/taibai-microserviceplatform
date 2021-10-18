package com.fitmgr.meterage.api.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fitmgr.meterage.api.entity.ResourceMeterageRecord;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Classname ResourceMeterageRecordVO
 * @Description 计量记录 VO
 * @Date 2019/11/14 16:54
 * @Created by DZL
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ResourceMeterageRecordVO implements Serializable {

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
     * 计量开始时间
     */
    private String startTime;

    /**
     * 计量结束时间
     */
    private String endTime;

    /**
     * 租户名称
     */
    @ApiModelProperty(value = "租户名称", name = "tenantName", required = false)
    private String tenantName;


    /**
     * project名称
     */
    @ApiModelProperty(value = "project名称", name = "projectName", required = false)
    private String projectName;


    /**
     * 用户名称
     */
    @ApiModelProperty(value = "用户名称", name = "userName", required = false)
    private String userName;

    /**
     * project集合id
     */
    private List<Integer> projects;

    /**
     * 租户集合
     */
    private List<Integer> tenantIds;

    /**
     * id
     */
    private Integer meterageId;

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
