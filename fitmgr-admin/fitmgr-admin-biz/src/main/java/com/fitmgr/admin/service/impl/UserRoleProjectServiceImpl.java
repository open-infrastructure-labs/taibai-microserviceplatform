package com.fitmgr.admin.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitmgr.admin.api.entity.UserRoleProject;
import com.fitmgr.admin.mapper.UserRoleProjectMapper;
import com.fitmgr.admin.service.IUserRoleProjectService;

import lombok.AllArgsConstructor;

/**
 * <p>
 * 用户角色表 服务实现类
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
@Service
@AllArgsConstructor
public class UserRoleProjectServiceImpl extends ServiceImpl<UserRoleProjectMapper, UserRoleProject>
        implements IUserRoleProjectService {

    private UserRoleProjectMapper userRoleProjectMapper;

    /**
     * 根据用户Id删除该用户的角色关系
     * 
     * @param userId 用户ID
     * @return boolean
     */
    @Override
    public Boolean deleteByUserId(Integer userId) {
        return userRoleProjectMapper.deleteByUserId(userId);
    }

}
