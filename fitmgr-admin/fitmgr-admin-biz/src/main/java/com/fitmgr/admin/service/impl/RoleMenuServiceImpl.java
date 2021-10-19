package com.fitmgr.admin.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitmgr.admin.api.entity.Role;
import com.fitmgr.admin.api.entity.RoleMenu;
import com.fitmgr.admin.api.vo.UserVO;
import com.fitmgr.admin.mapper.RoleMapper;
import com.fitmgr.admin.mapper.RoleMenuMapper;
import com.fitmgr.admin.mapper.UserMapper;
import com.fitmgr.admin.service.IRoleMenuService;
import com.fitmgr.admin.threadpool.InheritableRequestContextTaskWrapper;
import com.fitmgr.admin.utils.AdminUtils;

/**
 * <p>
 * 角色菜单表 服务实现类
 * </p>
 *
 * @author Fitmgr
 * @since 2020-04-21
 */
@Service
public class RoleMenuServiceImpl extends ServiceImpl<RoleMenuMapper, RoleMenu> implements IRoleMenuService {

    @Autowired
    private AdminUtils adminUtils;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ThreadPoolTaskExecutor threadPoola;

    @Override
    public boolean batchInsert(List<RoleMenu> roleMenus) {
        boolean res = saveBatch(roleMenus);
        if (res) {
            InheritableRequestContextTaskWrapper wrapper = new InheritableRequestContextTaskWrapper();
            threadPoola.submit(() -> {
                wrapper.lambda2(() -> {
                    try {
                        Role role = roleMapper.selectById(roleMenus.get(0).getRoleId());
                        if (role != null) {
                            List<UserVO> userVos = userMapper.queryUserInfoByRoleCode(role.getRoleCode());
                            if (CollectionUtils.isNotEmpty(userVos)) {
                                List<Integer> userIds = userVos.stream().map(UserVO::getId)
                                        .collect(Collectors.toList());
                                List<String> emails = userVos.stream().map(UserVO::getEmail)
                                        .collect(Collectors.toList());
                                adminUtils.batchSendEmail(userIds, "修改角色的权限", "modify-role-authority",
                                        "roleName:" + role.getRoleName(), emails);
                            }
                        }
                    } catch (Throwable e) {
                        log.error("Error occurred in async tasks", e);
                    }
                }).accept();
            });
        }
        return res;
    }
}
