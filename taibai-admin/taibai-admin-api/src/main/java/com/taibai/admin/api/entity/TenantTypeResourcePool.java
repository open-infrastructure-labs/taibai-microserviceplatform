package com.taibai.admin.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 创建人   mhp
 * 创建时间 2019/11/29
 * 描述
 **/

@Data
public class TenantTypeResourcePool implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ApiModelProperty(value = "id", name = "id", required = false)
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 租户类型id
     */
    @ApiModelProperty(value = "租户类型id", name = "tenantTypeId", required = true)
    @NotNull(message = "租户类型不能为空")
    private Integer tenantTypeId;

    /**
     * 资源池id
     */
    @ApiModelProperty(value = "资源池id", name = "resourcePoolId", required = true)
    @NotNull(message = "资源池不能为空")
    private Integer resourcePoolId;

    /**
     * 资源类型id
     */
    @ApiModelProperty(value = "资源类型id", name = "resourceTypeId", required = true)
    @NotNull(message = "资源池类型不能为空")
    private Integer resourceTypeId;

    /**
     * 状态 0-启用 1-禁用
     */
    @ApiModelProperty(value = "状态", name = "status", required = false)
    private String status;
}
