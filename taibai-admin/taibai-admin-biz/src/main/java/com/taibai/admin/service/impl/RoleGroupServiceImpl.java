package com.taibai.admin.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taibai.admin.api.entity.RoleGroup;
import com.taibai.admin.mapper.RoleGroupMapper;
import com.taibai.admin.service.IRoleGroupService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 角色分组表 服务实现类
 * </p>
 *
 * @author Taibai
 * @since 2019-11-16
 */
@Slf4j
@Service
@AllArgsConstructor
public class RoleGroupServiceImpl extends ServiceImpl<RoleGroupMapper, RoleGroup> implements IRoleGroupService {

    private final RoleGroupMapper roleGroupMapper;

}
