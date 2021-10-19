
package com.fitmgr.admin.api.vo;

import java.io.Serializable;
import java.util.List;

import com.fitmgr.admin.api.entity.Project;
import com.fitmgr.admin.api.entity.Role;
import com.fitmgr.admin.api.entity.Tenant;
import com.fitmgr.admin.api.entity.User;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Fitmgr
 * @date 2017/10/29
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserVO extends User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色列表（一对多模型）
     */
    private List<Role> roleList;

    private List<Role> sysRoleList;

    /**
     * project列表（一对多模型）
     */
    private List<Project> projectList;

    private List<Tenant> tenantList;

    /**
     * 用户数量
     */
    private Integer userNumber;

}
