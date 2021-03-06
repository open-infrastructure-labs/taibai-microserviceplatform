package com.taibai.admin.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taibai.admin.api.entity.RoleGroupRole;
import com.taibai.admin.mapper.RoleGroupRoleMapper;
import com.taibai.admin.service.IRoleGroupRoleService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 角色表 服务实现类
 * </p>
 *
 * @author Taibai
 * @since 2019-11-16
 */
@Slf4j
@Service
@AllArgsConstructor
public class RoleGroupRoleServiceImpl extends ServiceImpl<RoleGroupRoleMapper, RoleGroupRole>
        implements IRoleGroupRoleService {

    private final RoleGroupRoleMapper roleGroupRelationMapper;

}
