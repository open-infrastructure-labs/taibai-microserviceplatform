package com.fitmgr.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fitmgr.admin.api.entity.UserRoleProject;

/**
 * <p>
 * 用户角色表 服务类
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
public interface IUserRoleProjectService extends IService<UserRoleProject> {

    /**
     * 根据用户Id删除该用户的角色关系
     * 
     * @param userId 用户ID
     * @return boolean
     */
    Boolean deleteByUserId(Integer userId);

}
