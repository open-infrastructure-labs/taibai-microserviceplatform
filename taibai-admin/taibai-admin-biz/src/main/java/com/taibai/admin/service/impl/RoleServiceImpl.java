package com.taibai.admin.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.codingapi.tx.annotation.TxTransaction;
import com.taibai.admin.api.constants.RoleLevelEnum;
import com.taibai.admin.api.dto.SwitchUserVdcDTO;
import com.taibai.admin.api.dto.TokenDTO;
import com.taibai.admin.api.entity.Role;
import com.taibai.admin.api.entity.RoleMenu;
import com.taibai.admin.api.entity.Tenant;
import com.taibai.admin.api.entity.User;
import com.taibai.admin.api.entity.UserRoleProject;
import com.taibai.admin.api.feign.RemoteTokenService;
import com.taibai.admin.api.vo.UserVO;
import com.taibai.admin.mapper.RoleMapper;
import com.taibai.admin.mapper.TenantMapper;
import com.taibai.admin.mapper.UserMapper;
import com.taibai.admin.mapper.UserRoleProjectMapper;
import com.taibai.admin.service.IAuthService;
import com.taibai.admin.service.IRoleMenuService;
import com.taibai.admin.service.IRoleService;
import com.taibai.admin.threadpool.InheritableRequestContextTaskWrapper;
import com.taibai.admin.utils.AdminUtils;
import com.taibai.common.core.constant.SecurityConstants;
import com.taibai.common.core.constant.enums.BusinessEnum;
import com.taibai.common.core.exception.BusinessException;
import com.taibai.common.core.util.R;
import com.taibai.common.security.service.FitmgrUser;
import com.taibai.common.security.util.SecurityUtils;
import com.taibai.template.api.feign.RemoteServiceModelService;
import com.google.common.collect.Lists;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * ????????? ???????????????
 * </p>
 *
 * @author Taibai
 * @since 2019-11-16
 */
@Slf4j
@Service
@AllArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {

    private final RemoteTokenService remoteTokenService;
    private final UserMapper userMapper;
    private final TenantMapper tenantMapper;
    private final RemoteServiceModelService remoteServiceModelService;
    private final IAuthService authService;
    private final IRoleMenuService roleMenuService;
    private final UserRoleProjectMapper userRoleProjectMapper;

    private final RoleMapper roleMapper;

    private final AdminUtils adminUtils;

    private final ThreadPoolTaskExecutor threadPoola;

    /**
     * ?????????
     */
    static List<Role> childMenu = new ArrayList<Role>();

    /**
     * ??????????????????????????????
     *
     * @param roleId ??????id
     * @return
     */
    @Override
    public Integer countUser(Integer roleId) {
        return baseMapper.countUser(roleId);
    }

    /**
     * ????????????????????????????????????
     *
     * @returnm
     */
    @Override
    public List<Role> getList(String level) {
        if (StringUtils.isNotEmpty(level)) {
            String[] levels = level.split(",");
            List<Integer> intLevels = new ArrayList<>();
            for (String str : levels) {
                intLevels.add(Integer.valueOf(str));
            }
            return baseMapper.selectList(new QueryWrapper<Role>().lambda().in(Role::getLevel, intLevels));
        } else {
            return baseMapper.selectList(new QueryWrapper<>());
        }
    }

    @Override
    public List<Role> getAllList() {
        return baseMapper.selectList(new QueryWrapper<>());
    }

    @Override
    public List<Role> getRoleList(Integer userId, Integer tenantId) {
        log.info("userId value {},tenantId value {}", userId, tenantId);
        if (tenantId == -1) {
            return baseMapper.getRoleListByUserIdAndTenantId(userId, tenantId);
        } else {
            // ?????????????????????
            List<Role> tRoleList = baseMapper.getRoleListByUserIdAndTenantId(userId, tenantId);
            // ???????????????????????????project??????
            List<Role> pRoleList = baseMapper.getProjectRoleListByUserIdAndTenantId(userId, tenantId);
            Set<Role> allRoleSet = new HashSet<>();
            allRoleSet.addAll(tRoleList);
            allRoleSet.addAll(pRoleList);
            List<Role> allRoleList = new ArrayList<>(allRoleSet);
            return allRoleList;
        }
    }

    /**
     * ??????????????????
     *
     * @param token  token
     * @param roleId ??????id
     * @return
     */
    @Override
    @SneakyThrows
    public R switchRole(String token, User user, Integer roleId) {
        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setToken(token);
        tokenDTO.setUser(user);
        // ??????token???????????????
        return remoteTokenService.updateRdisToken(tokenDTO, SecurityConstants.FROM_IN);
    }

    @Override
    public R switchUserVdc(SwitchUserVdcDTO switchUserVdcDTO) {
        User dbUser = userMapper
                .selectOne(new QueryWrapper<User>().lambda().eq(User::getUsername, switchUserVdcDTO.getUserName()));
        if (dbUser == null) {
            return R.failed("???????????????");
        }

        User user = new User();
        if (!"-1".equals(switchUserVdcDTO.getVdcName())) {
            Tenant tenant = tenantMapper
                    .selectOne(new QueryWrapper<Tenant>().lambda().eq(Tenant::getName, switchUserVdcDTO.getVdcName()));
            if (tenant == null) {
                return R.failed("VDC?????????");
            }
            user.setDefaultTenantId(tenant.getId());
        } else {
            user.setDefaultTenantId(-1);
        }

        user.setId(dbUser.getId());
        user.setUsername(dbUser.getUsername());

        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setToken(switchUserVdcDTO.getToken());
        tokenDTO.setUser(user);
        R updateTokenr = remoteTokenService.updateRdisToken(tokenDTO, SecurityConstants.FROM_IN);
        if (updateTokenr.getCode() != 0) {
            return updateTokenr;
        }

        return R.ok(user.getDefaultTenantId());
    }

    /**
     * ????????????
     */
    @TxTransaction
    @Transactional(rollbackFor = Exception.class)
    @Override
    public R saveRole(Role role) {
        // ????????????code????????????
        Integer count = baseMapper.selectCount(Wrappers.<Role>lambdaQuery()
                .eq(StringUtils.isNoneBlank(role.getRoleCode()), Role::getRoleCode, role.getRoleCode()));
        Integer count2 = baseMapper.selectCount(Wrappers.<Role>lambdaQuery()
                .eq(StringUtils.isNoneBlank(role.getRoleName()), Role::getRoleName, role.getRoleName()));
        if (count > 0) {
            return R.failed(BusinessEnum.ROLE_CODE);
        }
        if (count2 > 0) {
            return R.failed(BusinessEnum.ROLE_NAME);
        }
        // ????????????
        boolean save = this.save(role);
        if (save) {
            if (null != role.getInheritId()) {
                // ???????????????????????????????????????????????????
                authService.inheritAuth(role.getId(), role.getInheritId());
                // ?????????????????????????????????
                List<RoleMenu> list = roleMenuService.list(Wrappers.<RoleMenu>lambdaQuery()
                        .eq(null != role.getInheritId(), RoleMenu::getRoleId, role.getInheritId()));
                list.stream().forEach(roleMenu -> roleMenu.setRoleId(role.getId()));
                roleMenuService.saveBatch(list);
            }
            InheritableRequestContextTaskWrapper wrapper = new InheritableRequestContextTaskWrapper();
            threadPoola.submit(() -> {
                wrapper.lambda2(() -> {
                    try {
                        FitmgrUser loginUser = SecurityUtils.getUser();
                        List<UserRoleProject> userRoleProjects = userRoleProjectMapper
                                .selectList(new QueryWrapper<UserRoleProject>().lambda().in(UserRoleProject::getRoleId,
                                        Lists.newArrayList(1, 88)));
                        if (CollectionUtils.isNotEmpty(userRoleProjects)) {
                            List<Integer> userIds = userRoleProjects.stream().map(UserRoleProject::getUserId)
                                    .collect(Collectors.toList());
                            List<User> users = userMapper
                                    .selectList(new QueryWrapper<User>().lambda().in(User::getId, userIds));
                            if (CollectionUtils.isNotEmpty(users)) {
                                List<String> emails = users.stream().map(User::getEmail).collect(Collectors.toList());
                                adminUtils.batchSendEmail(userIds, "????????????", "create-role",
                                        "roleName:" + role.getRoleName(), emails);
                            }
                        }
                    } catch (Throwable e) {
                        log.error("Error occurred in async tasks", e);
                    }
                }).accept();
            });
            return R.ok();
        }
        throw new BusinessException(BusinessEnum.ADD_FAIL);
    }

    /**
     * ???????????????project??????????????????
     *
     * @return
     */
    @Override
    public R projectRoleList() {
        List<Role> roles = baseMapper
                .selectList(Wrappers.<Role>lambdaQuery().eq(Role::getLevel, RoleLevelEnum.PROJECT.getCode()));
        return R.ok(roles);
    }

    @Override
    public List<Role> getRoleListByTwoId(Integer userId, Integer projectId) {
        List<Role> roleList = baseMapper.getRoleListByTwoId(userId, projectId);
        return roleList;
    }

    @Override
    public R updateRole(Role role) {
        Role oldRole = this.getById(role.getId());
        if (oldRole == null) {
            return R.failed("???????????????id");
        }
        Integer count2 = baseMapper.selectCount(Wrappers.<Role>lambdaQuery().ne(Role::getId, role.getId())
                .eq(StringUtils.isNoneBlank(role.getRoleName()), Role::getRoleName, role.getRoleName()));
        if (count2 > 0) {
            return R.failed("??????????????????");
        }
        role.setCreateTime(oldRole.getCreateTime());
        role.setUpdateTime(LocalDateTime.now());
        boolean res = updateById(role);
        if (res) {
            InheritableRequestContextTaskWrapper wrapper = new InheritableRequestContextTaskWrapper();
            threadPoola.submit(() -> {
                wrapper.lambda2(() -> {
                    try {
                        List<Integer> userIds = new ArrayList<Integer>();
                        List<String> emails = new ArrayList<String>();
                        List<UserVO> userVos = userMapper.queryUserInfoByRoleCode(role.getRoleCode());
                        if (CollectionUtils.isNotEmpty(userVos)) {
                            userIds = userVos.stream().map(UserVO::getId).collect(Collectors.toList());
                            emails = userVos.stream().map(UserVO::getEmail).collect(Collectors.toList());
                        }
                        Role inheritRole = this.getById(role.getInheritId());
                        String parameters = "roleName:" + oldRole.getRoleName() + "," + "newRoleName:"
                                + role.getRoleName();
                        if (inheritRole != null) {
                            parameters = parameters + "????????????????????????" + inheritRole.getRoleName();
                        }
                        adminUtils.batchSendEmail(userIds, "????????????", "modify-role", parameters, emails);
                    } catch (Throwable e) {
                        log.error("Error occurred in async tasks", e);
                    }
                }).accept();
            });
        }
        return R.ok();
    }

    @Override
    public void deleteRoleById(Integer roleId) {
        if (null == roleId) {
            return;
        }
        Role role = roleMapper.selectOne(Wrappers.<Role>lambdaQuery().eq(Role::getId, roleId));
        if (null == role) {
            return;
        }
        roleMapper.deleteByRoleId(roleId);
    }

    /**
     * ?????????????????????????????????????????????
     *
     * @param menuList ?????????list
     * @param pid      ?????????id
     * @return
     */
    public static List<Role> treeMenuList(List<Role> menuList, int pid) {
        for (Role mu : menuList) {
            // ????????????id???????????????id???add??????????????????
            if (mu.getParentId() == pid) {
                // ?????????????????????
                treeMenuList(menuList, mu.getId());
                childMenu.add(mu);
            }
        }
        return childMenu;
    }
}
