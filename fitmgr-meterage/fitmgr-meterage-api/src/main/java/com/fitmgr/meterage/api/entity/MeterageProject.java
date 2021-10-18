package com.fitmgr.meterage.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 
 * </p>
 *
 * @author zhaock
 * @since 2020-08-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class MeterageProject implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.INPUT)
    @ApiModelProperty(value = "id", name = "id")
    private Integer id;
    /**
     * 计量项名称
     */
    @ApiModelProperty(value = "计量项名称", name = "name")
    private String name;

    /**
     * 计量组件
     */
    @ApiModelProperty(value = "计量组件", name = "componentCode")
    private String componentCode;

    /**
     * 状态0启用，1禁用
     */
    @ApiModelProperty(value = "状态0启用，1禁用", name = "state")
    private Integer state;

    /**
     * 计量单位：G容量、个数量
     */
    @ApiModelProperty(value = "计量单位：G容量、个数量", name = "meterageUnit")
    private String meterageUnit;

    /**
     * 删除0正常1删除
     */
    @ApiModelProperty(value = "删除0正常1删除", name = "delFlag")
    private String delFlag;

    @ApiModelProperty(value = "创建时间", name = "createTime")
    private LocalDateTime createTime;

}
