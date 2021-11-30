package com.taibai.admin.api.vo;

import com.taibai.admin.api.entity.Project;
import com.taibai.admin.api.entity.Tenant;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TenantOrProjectVO implements Serializable {

    /**
     * 租户集合
     */
    private List<Tenant> tenants;

    /**
     * project集合
     */
    private List<Project> projects;

    /**
     * 类型
     */
    private String type;

}