package com.fitmgr.admin.api.vo;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * 创建人 mhp 创建时间 2019/12/11 描述
 **/
@Data
public class MemberVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer userId;

    private String name;

    private String phone;

    private String email;

    private List<TenantRoleVO> tenantRoles;

    private Integer roleId;

    private String roleName;

    public MemberVO(Member member) {
        super();
        this.userId = member.getUserId();
        this.name = member.getName();
        this.phone = member.getPhone();
        this.email = member.getEmail();
        this.roleId = member.getRoleId();
        this.roleName = member.getRoleName();
    }
}
