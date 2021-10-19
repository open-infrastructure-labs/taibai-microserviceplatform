package com.fitmgr.admin.api.vo;

import com.fitmgr.admin.api.entity.Role;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProjectRoleVO extends ProjectVO{
    private static final long serialVersionUID = 4174169301743430542L;
    private List<Role> roles;
}
