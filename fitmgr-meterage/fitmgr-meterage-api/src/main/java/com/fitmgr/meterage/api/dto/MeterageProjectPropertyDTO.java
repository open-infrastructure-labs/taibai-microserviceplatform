package com.fitmgr.meterage.api.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 * @author zhaock
 * @since 2020-08-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class MeterageProjectPropertyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "资源id", name = "id")
    private Integer id;
    /**
     * 计量项ID
     */
    @NotNull(message = "计量项ID不能为空！")
    @ApiModelProperty(value = "计量项ID", name = "meterageProjectId")
    private Integer meterageProjectId;

    /**
     * 自定义属性名称
     */
    @NotBlank(message = "自定义属性名称不能为空！")
    @ApiModelProperty(value = "自定义属性名称", name = "keyName")
    private String keyName;

    /**
     * 源组件key
     */
    @ApiModelProperty(value = "源组件key", name = "sourceKey")
    private String sourceKey;

    /**
     * 计量单位：G容量、个数量
     */
    @ApiModelProperty(value = "计量单位：G容量、个数量", name = "meterageUnit")
    private String meterageUnit;

    /**
     * 关联组件
     */
    @ApiModelProperty(value = "关联组件", name = "foreignComponentId")
    private String foreignComponentId;

    /**
     * 关联属性
     */
    @ApiModelProperty(value = "关联属性", name = "foreignKey")
    private String foreignKey;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间", name = "createTime")
    private LocalDateTime createTime;

    /**
     * 删除标记0正常1删除
     */
    @ApiModelProperty(value = "删除标记0正常1删除", name = "delFlag")
    private Integer delFlag;

    /**
     * 是否参与计算0、不参与，1、参与（默认不参与）
     */
    @ApiModelProperty(value = "是否参与计算0、不参与，1、参与（默认不参与）", name = "calFlag")
    private Integer calFlag;


}
