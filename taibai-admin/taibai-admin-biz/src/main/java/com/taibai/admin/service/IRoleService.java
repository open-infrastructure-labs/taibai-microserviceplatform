package com.taibai.admin.service;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taibai.admin.api.dto.SwitchUserVdcDTO;
import com.taibai.admin.api.entity.Role;
import com.taibai.admin.api.entity.User;
import com.taibai.common.core.util.R;

/**
 * <p>
 * 角色表 服务类
 * </p>
 *
 * @author Taibai
 * @since 2019-11-16
 */
public interface IRoleService extends IService<Role> {

    /**
     * 通过角色统计用户数量
     *
     * @param roleId 角色id
     * @return Integer
     */
    Integer countUser(Integer roleId);

    /**
     * 当前用户可分配的角色列表
     * 
     * @param level level
     * @return List<Role>
     */
    List<Role> getList(String level);

    /**
     * 获取所有角色列表
     *
     * @return List<Role>
     */
    List<Role> getAllList();

    /**
     * 根据当前用户获取角色列表
     * 
     * @param userId   userId
     * @param tenantId tenantId
     * @return List<Role>
     */
    List<Role> getRoleList(Integer userId, Integer tenantId);

    /**
     * 切换当前用户默认VDC
     * 
     * @param token  token
     * @param user   user
     * @param roleId roleId
     * @return R
     */
    R switchRole(String token, User user, Integer roleId);

    /**
     * switchUserVdc
     * 
     * @param switchUserVdcDTO switchUserVdcDTO
     * @return R
     */
    R switchUserVdc(SwitchUserVdcDTO switchUserVdcDTO);

    /**
     * saveRole
     * 
     * @param role role
     * @return R
     */
    R saveRole(Role role);

    /**
     * 获取所有跟project相关角色列表
     *
     * @return R
     */
    R projectRoleList();

    /**
     * getRoleListByTwoId
     * 
     * @param userId    userId
     * @param projectId projectId
     * @return List<Role>
     */
    List<Role> getRoleListByTwoId(@Param("roleId") Integer userId, @Param("projectId") Integer projectId);

    /**
     * updateRole
     * 
     * @param role role
     * @return R
     */
    R updateRole(Role role);

    /**
     * 删除角色
     *
     * @param roleId role
     */
    void deleteRoleById(Integer roleId);
}
