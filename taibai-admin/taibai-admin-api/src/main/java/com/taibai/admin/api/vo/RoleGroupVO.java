package com.taibai.admin.api.vo;

import java.io.Serializable;
import java.util.List;

import com.taibai.admin.api.entity.Role;
import com.taibai.admin.api.entity.RoleGroup;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RoleGroupVO extends RoleGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色分组角色列表
     */
    private List<Role> roles;

}
