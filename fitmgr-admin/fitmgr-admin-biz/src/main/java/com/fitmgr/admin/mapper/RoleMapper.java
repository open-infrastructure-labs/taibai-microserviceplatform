package com.fitmgr.admin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitmgr.admin.api.entity.Role;

/**
 * <p>
 * distributableRoles 角色表 Mapper 接口
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 通过角色统计用户数量
     *
     * @param roleId 角色id
     * @return Integer
     */
    Integer countUser(@Param("roleId") Integer roleId);

    /**
     * 根据当前用户获取角色列表
     *
     * @param userId 用户id
     * @return List<Role>
     */
    List<Role> getRoleList(@Param("userId") Integer userId);

    /**
     * getRoleListByTwoId
     * 
     * @param userId    userId
     * @param projectId projectId
     * @return List<Role>
     */
    List<Role> getRoleListByTwoId(@Param("userId") Integer userId, @Param("projectId") Integer projectId);

    /**
     * getRoleListByUserIdAndTenantId
     * 
     * @param userId   userId
     * @param tenantId tenantId
     * @return List<Role>
     */
    List<Role> getRoleListByUserIdAndTenantId(@Param("userId") Integer userId, @Param("tenantId") Integer tenantId);

    /**
     * getProjectRoleListByUserIdAndTenantId
     * 
     * @param userId   userId
     * @param tenantId tenantId
     * @return List<Role>
     */
    List<Role> getProjectRoleListByUserIdAndTenantId(@Param("userId") Integer userId,
            @Param("tenantId") Integer tenantId);

    /**
     * deleteByRoleId
     * 
     * @param roleId roleId
     */
    void deleteByRoleId(@Param("roleId") Integer roleId);
}
