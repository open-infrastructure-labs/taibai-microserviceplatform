package com.fitmgr.meterage.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 计量项表
 * </p>
 *
 * @author dzl
 * @since 2020-05-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class MeterageItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 云平台 阿里云 腾讯云  烽火云...
     */
    @ApiModelProperty(value = "云平台 阿里云 腾讯云  烽火云...", name = "cloudPlatform")
    private String cloudPlatform;

    /**
     * 组件识别码
     */
    @ApiModelProperty(value = "组件识别码", name = "componentCode")
    private String componentCode;

    /**
     * 计量项名称
     */
    @ApiModelProperty(value = "计量项名称", name = "meterageName")
    private String meterageName;

    /**
     * 计量单位 G-容量  个-数量
     */
    @ApiModelProperty(value = "计量单位 G-容量  个-数量", name = "meterageUnit")
    private String meterageUnit;

    /**
     * 计量项项类型 1-类型/规格/参数 2-类型/参数 3-参数
     */
    @ApiModelProperty(value = "计量项项类型 1-类型/规格/参数 2-类型/参数 3-参数", name = "meterageItemType")
    private String meterageItemType;

    /**
     * 状态（0 启用 1 禁用）
     */
    @ApiModelProperty(value = "状态（0 启用 1 禁用）", name = "status")
    private String status;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间", name = "createTime")
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @ApiModelProperty(value = "修改时间", name = "updateTime")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除(0=正常,1=删除)
     */
    @TableLogic
    @ApiModelProperty(value = "逻辑删除(0=正常,1=删除)", name = "delFlag")
    private String delFlag;


}
