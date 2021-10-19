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
 * 租户服务表
 * </p>
 *
 * @author Fitmgr
 * @since 2021-01-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TenantService implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "id", name = "id", required = false)
    private Integer id;

    /**
     * 菜单ID
     */
    @ApiModelProperty(value = "菜单IDd", name = "menuId", required = false)
    private String serviceId;

    /**
     * 租户ID
     */
    @ApiModelProperty(value = "租户ID", name = "tenantId", required = false)
    private Integer tenantId;
}
