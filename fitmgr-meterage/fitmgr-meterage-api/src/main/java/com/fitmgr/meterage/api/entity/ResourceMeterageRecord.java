package com.fitmgr.meterage.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 * 计量记录表
 *
 * @author dzl
 * @since 2020-02-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ResourceMeterageRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "meterage_id", type = IdType.AUTO)
    private Integer meterageId;


    /**
     * 计量开始时间
     */
    private LocalDateTime startTime;

    /**
     * 计量结束时间
     */
    private LocalDateTime endTime;

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
