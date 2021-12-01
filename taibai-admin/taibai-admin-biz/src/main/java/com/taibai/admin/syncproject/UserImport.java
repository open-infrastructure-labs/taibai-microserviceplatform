package com.taibai.admin.syncproject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.taibai.admin.api.constants.RoleLevelEnum;
import com.taibai.admin.api.dto.UserDTO;
import com.taibai.admin.api.entity.Role;
import com.taibai.admin.api.entity.Tenant;
import com.taibai.admin.api.entity.User;
import com.taibai.admin.api.entity.UserRoleProject;
import com.taibai.admin.exceptions.UserCenterException;
import com.taibai.admin.mapper.RoleMapper;
import com.taibai.admin.mapper.TenantMapper;
import com.taibai.admin.mapper.UserMapper;
import com.taibai.admin.mapper.UserRoleProjectMapper;
import com.taibai.admin.service.impl.UserRoleProjectServiceImpl;
import com.taibai.common.core.constant.CommonConstants;
import com.taibai.common.core.constant.enums.BusinessEnum;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UserImport {

    private final String SUPER_ADMIN = "超级管理员";

    @Autowired
    private TenantMapper tenantMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private UserRoleProjectMapper userRoleProjectMapper;

    @Autowired
    private UserRoleProjectServiceImpl userRoleProjectService;

    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();

    public void process(String url) {
        log.info("start import user");
        List<UserTenantRoleRow> userTenantRoleRows = ExcelReader.readExcel(url);
        for (UserTenantRoleRow userTenantRoleRow : userTenantRoleRows) {
            User user = userMapper
                    .selectOne(new QueryWrapper<User>().lambda().eq(User::getUsername, userTenantRoleRow.getAccount()));
            if (user != null) {
                processUserExist(userTenantRoleRow, user);
                continue;
            }
            processUserNotExist(userTenantRoleRow);
        }

        supplementRole();
        log.info("finish import user");
    }

    public void countTotal(String url) {
        List<UserTenantRoleRow> userTenantRoleRows = ExcelReader.readExcel(url);
        log.info("userTenantRoleRows.size()={}", userTenantRoleRows.size());
        int count = 0;
        Map<String, UserTenantRoleRow> accountSet = new HashMap<>();
        Map<String, List<UserTenantRoleRow>> duplicatAccount = new HashMap<>();
        Set<String> ids = new HashSet<>();
        int maxId = 0;
        for (UserTenantRoleRow userTenantRoleRow : userTenantRoleRows) {
            ids.add(userTenantRoleRow.getId());
            if (accountSet.containsKey(userTenantRoleRow.getAccount())) {
                List<UserTenantRoleRow> templist = duplicatAccount.get(userTenantRoleRow.getAccount());
                if (templist == null) {
                    templist = new ArrayList<>();
                    templist.add(accountSet.get(userTenantRoleRow.getAccount()));
                    duplicatAccount.put(userTenantRoleRow.getAccount(), templist);
                }
                templist.add(userTenantRoleRow);
            } else {
                accountSet.put(userTenantRoleRow.getAccount(), userTenantRoleRow);
            }
            if (Integer.parseInt(userTenantRoleRow.getId()) > maxId) {
                maxId = Integer.parseInt(userTenantRoleRow.getId());
            }
        }
        Set<Integer> notIds = new HashSet<>();
        for (int i = 1; i <= maxId; i++) {
            if (!ids.contains(String.valueOf(i))) {
                notIds.add(i);
            }
        }
        log.info("account total = {}", accountSet.size());
        log.info("notIds = {}", notIds);
        for (Map.Entry<String, List<UserTenantRoleRow>> entry : duplicatAccount.entrySet()) {
            log.info("duplicate account = {}", entry.getKey());
            for (UserTenantRoleRow userTenantRoleRow : entry.getValue()) {
                log.info("duplicate info = {}", userTenantRoleRow);
            }
        }
    }

    public void resetUserTenant() {
        List<User> allUsers = userMapper.selectList(new QueryWrapper<User>());
        for (User user : allUsers) {
            List<UserRoleProject> userRoleProjects = userRoleProjectMapper
                    .selectList(new QueryWrapper<UserRoleProject>().lambda()
                            .eq(UserRoleProject::getUserId, user.getId()).eq(UserRoleProject::getTenantId, 30));
            if (CollectionUtils.isEmpty(userRoleProjects)) {
                UserRoleProject userRoleProject = new UserRoleProject();
                userRoleProject.setUserId(user.getId());
                userRoleProject.setProjectId(-1);
                userRoleProject.setTenantId(30);
                userRoleProject.setRoleId(85);
                userRoleProjectMapper.insert(userRoleProject);
            } else {
                for (UserRoleProject userRoleProject : userRoleProjects) {
                    userRoleProjectMapper.deleteById(userRoleProject.getId());
                }
                UserRoleProject userRoleProject = new UserRoleProject();
                userRoleProject.setUserId(user.getId());
                userRoleProject.setProjectId(-1);
                userRoleProject.setTenantId(30);
                userRoleProject.setRoleId(85);
                userRoleProjectMapper.insert(userRoleProject);
            }
        }

    }

    private void supplementRole() {
        List<User> allUsers = userMapper.selectList(new QueryWrapper<User>());
        for (User user : allUsers) {
            List<UserRoleProject> userRoleProjects = userRoleProjectMapper.selectList(
                    new QueryWrapper<UserRoleProject>().lambda().eq(UserRoleProject::getUserId, user.getId()));
            if (CollectionUtils.isEmpty(userRoleProjects)) {
                Role role = roleMapper.selectOne(new QueryWrapper<Role>().lambda().eq(Role::getRoleName, "普通用户"));
                UserRoleProject userRoleProject = new UserRoleProject();
                userRoleProject.setUserId(user.getId());
                userRoleProject.setTenantId(-1);
                userRoleProject.setProjectId(-1);
                userRoleProject.setRoleId(role.getId());
                userRoleProjectMapper.insert(userRoleProject);
            } else {
                boolean hasSysRole = userRoleProjects.stream().anyMatch(userRoleProject -> {
                    if (userRoleProject.getTenantId() == null || userRoleProject.getTenantId() <= 0) {
                        if (userRoleProject.getProjectId() == null || userRoleProject.getProjectId() <= 0) {
                            return true;
                        }
                    }
                    return false;
                });
                if (!hasSysRole) {
                    Role role = roleMapper.selectOne(new QueryWrapper<Role>().lambda().eq(Role::getRoleName, "普通用户"));
                    UserRoleProject userRoleProject = new UserRoleProject();
                    userRoleProject.setUserId(user.getId());
                    userRoleProject.setTenantId(-1);
                    userRoleProject.setProjectId(-1);
                    userRoleProject.setRoleId(role.getId());
                    userRoleProjectMapper.insert(userRoleProject);
                }

                Map<Integer, List<UserRoleProject>> userRoleProjectMap = new HashMap<>();
                for (UserRoleProject userRoleProject : userRoleProjects) {
                    if (userRoleProject.getTenantId() != null && userRoleProject.getTenantId() > 0) {
                        List<UserRoleProject> userRoleProjects1 = userRoleProjectMap.get(userRoleProject.getTenantId());
                        if (userRoleProjects1 == null) {
                            userRoleProjects1 = new ArrayList<>();
                        }
                        userRoleProjects1.add(userRoleProject);
                        userRoleProjectMap.put(userRoleProject.getTenantId(), userRoleProjects1);
                    }
                }

                Role role = roleMapper.selectOne(new QueryWrapper<Role>().lambda().eq(Role::getRoleName, "租户普通用户"));
                if (userRoleProjectMap.size() > 0) {
                    for (Map.Entry<Integer, List<UserRoleProject>> entry : userRoleProjectMap.entrySet()) {
                        List<UserRoleProject> tempList = entry.getValue();
                        boolean hasOrdinaryRole = tempList.stream().anyMatch(userRoleProject -> {
                            if (role.getId().equals(userRoleProject.getRoleId())) {
                                return true;
                            }
                            return false;
                        });
                        if (!hasOrdinaryRole) {
                            UserRoleProject userRoleProject = new UserRoleProject();
                            userRoleProject.setUserId(user.getId());
                            userRoleProject.setTenantId(entry.getKey());
                            userRoleProject.setProjectId(-1);
                            userRoleProject.setRoleId(role.getId());
                            userRoleProjectMapper.insert(userRoleProject);
                        }
                    }
                }
            }
        }
    }

    private void processUserNotExist(UserTenantRoleRow userTenantRoleRow) {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(userTenantRoleRow.getEmail());
        userDTO.setUsername(userTenantRoleRow.getAccount());
        userDTO.setName(userTenantRoleRow.getName());
        userDTO.setPhone(userTenantRoleRow.getPhone());
        userDTO.setPassword(userTenantRoleRow.getEmail());
        Tenant tenant = null;
        if (StringUtils.isNotEmpty(userTenantRoleRow.getTenantName())) {
            tenant = tenantMapper.selectOne(
                    new QueryWrapper<Tenant>().lambda().eq(Tenant::getName, userTenantRoleRow.getTenantName()));
            if (tenant == null) {
                tenant = tenantMapper.selectOne(new QueryWrapper<Tenant>().lambda().eq(Tenant::getName, "中国民航信息集团"));
            }
        }
        if (tenant != null) {
            userDTO.setTenantId(tenant.getId());
        }
        if (SUPER_ADMIN.equals(userTenantRoleRow.getRole())) {
            Role computeRole = roleMapper.selectOne(new QueryWrapper<Role>().lambda().eq(Role::getRoleName, "计算管理员"));
            Role storageRole = roleMapper.selectOne(new QueryWrapper<Role>().lambda().eq(Role::getRoleName, "存储管理员"));
            List<Integer> roleIds = new ArrayList<>();
            roleIds.add(computeRole.getId());
            roleIds.add(storageRole.getId());
            userDTO.setRole(roleIds);
        } else {
            Role role = null;
            if (StringUtils.isEmpty(userTenantRoleRow.getRole())) {
                if (tenant == null) {
                    role = roleMapper.selectOne(new QueryWrapper<Role>().lambda().eq(Role::getRoleName, "普通用户"));
                } else {
                    role = roleMapper.selectOne(new QueryWrapper<Role>().lambda().eq(Role::getRoleName, "租户普通用户"));
                }
            } else {
                role = roleMapper.selectOne(
                        new QueryWrapper<Role>().lambda().eq(Role::getRoleName, userTenantRoleRow.getRole()));
                if (role == null) {
                    if (tenant == null) {
                        role = roleMapper.selectOne(new QueryWrapper<Role>().lambda().eq(Role::getRoleName, "普通用户"));
                    } else {
                        role = roleMapper.selectOne(new QueryWrapper<Role>().lambda().eq(Role::getRoleName, "租户普通用户"));
                    }
                }
            }
            List<Integer> roleIds = new ArrayList<>();
            roleIds.add(role.getId());
            userDTO.setRole(roleIds);
        }
        createUser(userDTO);
    }

    private void processUserExist(UserTenantRoleRow userTenantRoleRow, User user) {
        user.setNewImport(true);
        user.setEmail(userTenantRoleRow.getEmail());
        user.setPhone(userTenantRoleRow.getPhone());
        userMapper.updateById(user);
        Tenant tenant = null;
        if (StringUtils.isNotEmpty(userTenantRoleRow.getTenantName())) {
            tenant = tenantMapper.selectOne(
                    new QueryWrapper<Tenant>().lambda().eq(Tenant::getName, userTenantRoleRow.getTenantName()));
            if (tenant == null) {
                tenant = tenantMapper.selectOne(new QueryWrapper<Tenant>().lambda().eq(Tenant::getName, "中国民航信息集团"));
            }
        }
        if (SUPER_ADMIN.equals(userTenantRoleRow.getRole())) {
            Role computeRole = roleMapper.selectOne(new QueryWrapper<Role>().lambda().eq(Role::getRoleName, "计算管理员"));
            Role storageRole = roleMapper.selectOne(new QueryWrapper<Role>().lambda().eq(Role::getRoleName, "存储管理员"));
            List<UserRoleProject> userRoleProjects = userRoleProjectMapper.selectList(
                    new QueryWrapper<UserRoleProject>().lambda().eq(UserRoleProject::getUserId, user.getId())
                            .eq(UserRoleProject::getRoleId, computeRole.getId()));
            if (CollectionUtils.isEmpty(userRoleProjects)) {
                UserRoleProject userRoleProject = new UserRoleProject();
                userRoleProject.setUserId(user.getId());
                userRoleProject.setTenantId(-1);
                userRoleProject.setProjectId(-1);
                userRoleProject.setRoleId(computeRole.getId());
                userRoleProjectMapper.insert(userRoleProject);
            }

            userRoleProjects = userRoleProjectMapper.selectList(new QueryWrapper<UserRoleProject>().lambda()
                    .eq(UserRoleProject::getUserId, user.getId()).eq(UserRoleProject::getRoleId, storageRole.getId()));
            if (CollectionUtils.isEmpty(userRoleProjects)) {
                UserRoleProject userRoleProject = new UserRoleProject();
                userRoleProject.setUserId(user.getId());
                userRoleProject.setTenantId(-1);
                userRoleProject.setProjectId(-1);
                userRoleProject.setRoleId(storageRole.getId());
                userRoleProjectMapper.insert(userRoleProject);
            }

            if (tenant != null) {
                Role tenantOrdinaryRole = roleMapper
                        .selectOne(new QueryWrapper<Role>().lambda().eq(Role::getRoleName, "租户普通用户"));
                userRoleProjects = userRoleProjectMapper.selectList(new QueryWrapper<UserRoleProject>().lambda()
                        .eq(UserRoleProject::getUserId, user.getId()).eq(UserRoleProject::getTenantId, tenant.getId())
                        .eq(UserRoleProject::getRoleId, tenantOrdinaryRole.getId()));
                if (CollectionUtils.isEmpty(userRoleProjects)) {
                    UserRoleProject userRoleProject = new UserRoleProject();
                    userRoleProject.setUserId(user.getId());
                    userRoleProject.setTenantId(tenant.getId());
                    userRoleProject.setProjectId(-1);
                    userRoleProject.setRoleId(tenantOrdinaryRole.getId());
                    userRoleProjectMapper.insert(userRoleProject);
                }
            }
        } else {
            Role role = null;
            if (StringUtils.isEmpty(userTenantRoleRow.getRole())) {
                if (tenant == null) {
                    role = roleMapper.selectOne(new QueryWrapper<Role>().lambda().eq(Role::getRoleName, "普通用户"));
                } else {
                    role = roleMapper.selectOne(new QueryWrapper<Role>().lambda().eq(Role::getRoleName, "租户普通用户"));
                }
            } else {
                role = roleMapper.selectOne(
                        new QueryWrapper<Role>().lambda().eq(Role::getRoleName, userTenantRoleRow.getRole()));
                if (role == null) {
                    if (tenant == null) {
                        role = roleMapper.selectOne(new QueryWrapper<Role>().lambda().eq(Role::getRoleName, "普通用户"));
                    } else {
                        role = roleMapper.selectOne(new QueryWrapper<Role>().lambda().eq(Role::getRoleName, "租户普通用户"));
                    }
                }
            }

            if (role.getLevel() == RoleLevelEnum.TENANT.getCode()) {
                if (tenant != null) {
                    List<UserRoleProject> userRoleProjects = userRoleProjectMapper.selectList(
                            new QueryWrapper<UserRoleProject>().lambda().eq(UserRoleProject::getUserId, user.getId())
                                    .eq(UserRoleProject::getRoleId, role.getId())
                                    .eq(UserRoleProject::getTenantId, tenant.getId()));
                    if (CollectionUtils.isEmpty(userRoleProjects)) {
                        UserRoleProject userRoleProject = new UserRoleProject();
                        userRoleProject.setUserId(user.getId());
                        userRoleProject.setTenantId(tenant.getId());
                        userRoleProject.setProjectId(-1);
                        userRoleProject.setRoleId(role.getId());
                        userRoleProjectMapper.insert(userRoleProject);
                    }
                }
            } else if (role.getLevel() == RoleLevelEnum.SYSTEM.getCode()) {
                List<UserRoleProject> userRoleProjects = userRoleProjectMapper.selectList(
                        new QueryWrapper<UserRoleProject>().lambda().eq(UserRoleProject::getUserId, user.getId())
                                .eq(UserRoleProject::getRoleId, role.getId()));
                if (CollectionUtils.isEmpty(userRoleProjects)) {
                    UserRoleProject userRoleProject = new UserRoleProject();
                    userRoleProject.setUserId(user.getId());
                    userRoleProject.setTenantId(-1);
                    userRoleProject.setProjectId(-1);
                    userRoleProject.setRoleId(role.getId());
                    userRoleProjectMapper.insert(userRoleProject);
                }

                if (tenant != null) {
                    Role tenantOrdinaryRole = roleMapper
                            .selectOne(new QueryWrapper<Role>().lambda().eq(Role::getRoleName, "租户普通用户"));
                    userRoleProjects = userRoleProjectMapper.selectList(
                            new QueryWrapper<UserRoleProject>().lambda().eq(UserRoleProject::getUserId, user.getId())
                                    .eq(UserRoleProject::getTenantId, tenant.getId())
                                    .eq(UserRoleProject::getRoleId, tenantOrdinaryRole.getId()));
                    if (CollectionUtils.isEmpty(userRoleProjects)) {
                        UserRoleProject userRoleProject = new UserRoleProject();
                        userRoleProject.setUserId(user.getId());
                        userRoleProject.setTenantId(tenant.getId());
                        userRoleProject.setProjectId(-1);
                        userRoleProject.setRoleId(tenantOrdinaryRole.getId());
                        userRoleProjectMapper.insert(userRoleProject);
                    }
                }
            }
        }
    }

    private void createUser(UserDTO userDTO) {
        String tenantStatus = tenantMapper.getTenantStatus(userDTO.getTenantId());
        if (CommonConstants.STATUS_DEL.equals(tenantStatus)) {
            throw new UserCenterException(BusinessEnum.USER_TENANT_FORBIDDEN);
        }
        List<Integer> role = userDTO.getRole();
        if (CollectionUtils.isEmpty(role)) {
            throw new UserCenterException("用户角色信息不能为空，请核实后输入");
        }

        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        // 状态 0-启用 1-禁用 2-初次登陆(初次登陆，强制用户跳转修改自己的登录密码) - 200516
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(user.getCreateTime());
        user.setDelFlag(CommonConstants.STATUS_NORMAL);
        // 后端直接设置用户默认头像
        if (!StrUtil.isNotBlank(userDTO.getUrl())) {
            user.setAvatar(CommonConstants.USER_DEFAULT_SETAVATAR);
        }
        // 用户密码
        user.setPassword(ENCODER.encode(userDTO.getPassword()));
        // 修改default_role为该用户角色列表的第一个role
        if (null != userDTO.getRole() && userDTO.getRole().size() >= 1) {
            boolean hasSysRole = userDTO.getRole().stream().anyMatch(roleId -> {
                Role roleTemp = roleMapper.selectById(roleId);
                if (roleTemp.getLevel().equals(RoleLevelEnum.SYSTEM.getCode())) {
                    return true;
                }
                return false;
            });
            if (!hasSysRole) {
                List<Role> sDefaultRoles = roleMapper.selectList(new QueryWrapper<Role>().lambda()
                        .eq(Role::getLevel, RoleLevelEnum.SYSTEM.getCode()).eq(Role::getSystemDefaultRole, true));
                if (CollectionUtils.isEmpty(sDefaultRoles)) {
                    throw new UserCenterException("没有设置系统级别的默认角色");
                }
                userDTO.getRole().add(sDefaultRoles.get(0).getId());
            }

            if (userDTO.getTenantId() != null) {
                boolean hasTenRole = userDTO.getRole().stream().anyMatch(roleId -> {
                    Role roleTemp = roleMapper.selectById(roleId);
                    if (roleTemp.getLevel().equals(RoleLevelEnum.TENANT.getCode())) {
                        return true;
                    }
                    return false;
                });
                if (!hasTenRole) {
                    List<Role> tDefaultRoles = roleMapper.selectList(new QueryWrapper<Role>().lambda()
                            .eq(Role::getLevel, RoleLevelEnum.TENANT.getCode()).eq(Role::getTenantDefaultRole, true));
                    if (CollectionUtils.isEmpty(tDefaultRoles)) {
                        throw new UserCenterException("没有设置租户级别的默认角色");
                    }
                    userDTO.getRole().add(tDefaultRoles.get(0).getId());
                }
            }
        }
        user.setDefaultTenantId(-1);
        user.setNewImport(true);
        userMapper.insert(user);
        /*
         * ------------------------------------------tips
         * 2-------------------------------------------
         */
        // 将用户名写进redis
        redisTemplate.opsForValue().set(CommonConstants.USER_PREFIX + user.getId(), user.getName());
        // 批量新增用户 user与 role、project 之间的关系
        List<UserRoleProject> userRoleList = userDTO.getRole().stream().map(roleId -> {
            UserRoleProject userRoleProject = new UserRoleProject();
            Role roleTemp = roleMapper.selectById(roleId);
            if (roleTemp.getLevel().equals(RoleLevelEnum.SYSTEM.getCode())) {
                userRoleProject.setUserId(user.getId());
                userRoleProject.setRoleId(roleId);
                userRoleProject.setProjectId(-1);
                userRoleProject.setTenantId(-1);
            } else {
                userRoleProject.setUserId(user.getId());
                userRoleProject.setRoleId(roleId);
                userRoleProject.setTenantId(userDTO.getTenantId());
                userRoleProject.setProjectId(-1);
            }
            return userRoleProject;
        }).collect(Collectors.toList());
        userRoleProjectService.saveBatch(userRoleList);
    }
}
