package com.fitmgr.admin.api.entity;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class TenantResourcePool implements Serializable {
    /**
     * 主键id
     */
    private Integer id;

    /**
     * 租户id
     */
    @NotNull(message = "未指定租户")
    private Integer tenantId;

    /**
     * 资源池组件code
     */
    @NotBlank(message = "未指定资源池标识")
    private String resourcePoolCode;

    /**
     * 资源池id
     */
    @NotNull(message = "未指定资源池")
    private String resourcePoolId;
}
