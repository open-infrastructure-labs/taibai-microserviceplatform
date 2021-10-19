package com.fitmgr.admin.api.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 租户、project、用户VO
 */
@Data
public class TenantProjectUserVO implements Serializable {

    /**
     * 租户id
     */
    private Integer tenantId;

    /**
     * 租户名称
     */
    private String tenantName;


    /**
     * projectid
     */
    private Integer projectId;

    /**
     * project名称
     */
    private String projectName;


    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 用户名称
     */
    private String userName;
}
