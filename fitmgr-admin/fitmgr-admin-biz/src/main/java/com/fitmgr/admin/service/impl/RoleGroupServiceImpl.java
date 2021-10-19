package com.fitmgr.admin.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitmgr.admin.api.entity.RoleGroup;
import com.fitmgr.admin.mapper.RoleGroupMapper;
import com.fitmgr.admin.service.IRoleGroupService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 角色分组表 服务实现类
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
@Slf4j
@Service
@AllArgsConstructor
public class RoleGroupServiceImpl extends ServiceImpl<RoleGroupMapper, RoleGroup> implements IRoleGroupService {

    private final RoleGroupMapper roleGroupMapper;

}