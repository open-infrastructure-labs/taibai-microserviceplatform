package com.taibai.admin.api.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 分域配置开关表
 * </p>
 *
 * @author Taibai
 * @since 2019-11-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SubDomainConfigSwitch implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "id", name = "id", required = false)
    private Integer id;

    /**
     * 网络池类型
     */
    @ApiModelProperty(value = "网络池类型", name = "networkPoolType", required = true)
    private String networkPoolType;

    /**
     * 开1、关0
     */
    @ApiModelProperty(value = "开关", name = "configSwitch", required = false)
    private String configSwitch;
}
