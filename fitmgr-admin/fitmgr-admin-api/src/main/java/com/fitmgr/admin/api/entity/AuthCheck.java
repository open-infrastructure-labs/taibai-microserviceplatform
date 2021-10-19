package com.fitmgr.admin.api.entity;

import java.util.List;

import lombok.Data;

@Data
public class AuthCheck {
    
    /**
     * 是否有权限
     */
    private boolean status;

    /**
     * 数据范围
     */
    private String operatingRange;
    
    /**
     * 租户id集合
     */
    private List<Integer> tenantIds;
    
    /**
     * peojectId集合
     */
    private List<Integer> projectIds;
    
    /**
     * peoject权限集合
     */
    private List<ProjectOperatingRange> projectOperatingRanges;
    
    /**
     * 用户id
     */
    private Integer userId;
    
    
}
