package com.taibai.admin.api.vo;

import com.taibai.admin.api.entity.Role;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建人   mhp
 * 创建时间 2019/12/11
 * 描述
 **/
@Data
public class Member implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer userId;

    private String name;

    private String phone;

    private String email;

    private List<Role> roles;

    private Integer roleId;

    private String roleName;
}
