package com.taibai.admin.api.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class TenantResourcePoolVO implements Serializable {

    /**
     * 租户id
     */
    private Integer tenantId;

    /**
     * 资源类型id
     */
    private String resourcePoolCode;


    /**
     * 资源池id
     */
    private String resourcePoolId;

    /**
     * 资源池名
     */
    private String resourcePoolName;

    /**
     * 独享或共享 0-共享 1-独享
     */
    private String isShare;
}
