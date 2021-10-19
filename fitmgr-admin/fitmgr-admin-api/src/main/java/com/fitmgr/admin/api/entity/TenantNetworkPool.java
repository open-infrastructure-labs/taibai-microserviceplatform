package com.fitmgr.admin.api.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * VDC网络池信息表
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TenantNetworkPool implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "id", name = "id", required = false)
    private Integer id;

    /**
     * VDC id
     */
    @ApiModelProperty(value = "VDC id", name = "vdcId", required = true)
    private Integer vdcId;

    /**
     * 网络池类型
     */
    @ApiModelProperty(value = "网络池类型", name = "networkPoolType", required = true)
    private String networkPoolType;

    /**
     * 网络池信息
     */
    @ApiModelProperty(value = "网络池信息", name = "networkPoolInfo", required = true)
    private String networkPoolInfo;

    /**
     * 版本号
     */
    @ApiModelProperty(value = "版本号", name = "version", required = false)
    private String version;
}
