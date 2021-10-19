package com.fitmgr.admin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitmgr.admin.api.entity.User;
import com.fitmgr.admin.api.entity.UserRoleProject;

/**
 * <p>
 * 用户角色表 Mapper 接口
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
public interface UserRoleProjectMapper extends BaseMapper<UserRoleProject> {

    /**
     * 根据用户Id删除该用户的角色关系
     *
     * @param userId userId
     * @return Boolean
     */
    Boolean deleteByUserId(@Param("userId") Integer userId);

    /**
     * 删除某一用户在某个project中的某一角色
     *
     * @param userRoleProject userRoleProject
     */
    void deleteUserRole(UserRoleProject userRoleProject);

    /**
     * 切换默认角色
     * 
     * @param projcetId projcetId
     * @param userId    userId
     * @param roleId    roleId
     * @return List<User>
     */
    List<User> selectSwitchUser(@Param("projcetId") Integer projcetId, @Param("userId") Integer userId,
            @Param("roleId") Integer roleId);
}
