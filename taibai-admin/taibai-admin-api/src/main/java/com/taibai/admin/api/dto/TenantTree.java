package com.taibai.admin.api.dto;

import java.time.LocalDateTime;

import com.taibai.admin.api.entity.Tenant;
import com.taibai.common.core.util.TreeNode;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 创建人   mhp
 * 创建时间 2019/11/14
 * 描述
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class TenantTree extends TreeNode {

    /**
     * 租户名称
     */
    private String name;

    /**
     * 租户类型id
     */
    private Integer typeId;

    /**
     * 企业描述
     */
    private String description;

    /**
     * 状态 0-启用 1-禁用
     */
    private String status;


    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删 0-正常 1-删除
     */
    private String delFlag;

    private Integer level;

    /**
     * 是否能创建project  0-可以  1-不可以
     */
    private String createProject;

    public TenantTree() {}

    public TenantTree(Tenant tenant){
        this.id = tenant.getId();
        this.name = tenant.getName();
        this.parentId = tenant.getParentId();
        this.typeId = tenant.getTypeId();
        this.description = tenant.getDescription();
        this.status = tenant.getStatus();
        this.createTime = tenant.getCreateTime();
        this.updateTime = tenant.getUpdateTime();
        this.delFlag = tenant.getDelFlag();
        this.level = tenant.getLevel();
        this.createProject = tenant.getCreateProject();
    }
}
