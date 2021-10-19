package com.fitmgr.admin.api.vo;

import java.util.List;

import com.fitmgr.admin.api.entity.Role;
import com.fitmgr.admin.api.entity.Tenant;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TenantRoleVO extends TenantVO {
    private static final long serialVersionUID = 1065044456103032769L;
    private List<Role> roles;
    
    public TenantRoleVO(Tenant tenant) {
        super();
        this.setCreateProject(tenant.getCreateProject());
        this.setCreateTime(tenant.getCreateTime());
        this.setDelFlag(tenant.getDelFlag());
        this.setDescription(tenant.getDescription());
        this.setEnglishName(tenant.getEnglishName());
        this.setId(tenant.getId());
        this.setLevel(tenant.getLevel());
        this.setName(tenant.getName());
        this.setParentId(tenant.getParentId());
        this.setStatus(tenant.getStatus());
        this.setTypeId(tenant.getTypeId());
        this.setUpdateTime(tenant.getUpdateTime());
    }

    public TenantRoleVO() {
        super();
    }
}
