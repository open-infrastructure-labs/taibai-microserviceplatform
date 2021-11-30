package com.taibai.admin.api.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TenantRole extends Tenant {
    private static final long serialVersionUID = -2411958749009905917L;
    private Integer roleId;

    private String roleName;
}
