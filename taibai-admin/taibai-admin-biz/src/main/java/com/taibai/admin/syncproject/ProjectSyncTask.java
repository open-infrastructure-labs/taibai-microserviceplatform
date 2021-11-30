package com.taibai.admin.syncproject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.taibai.admin.api.constants.ProjectDefaultEnum;
import com.taibai.admin.api.constants.ProjectStatusEnum;
import com.taibai.admin.api.entity.Project;
import com.taibai.admin.api.entity.Role;
import com.taibai.admin.api.entity.Tenant;
import com.taibai.admin.api.entity.User;
import com.taibai.admin.api.entity.UserRoleProject;
import com.taibai.admin.mapper.ProjectMapper;
import com.taibai.admin.mapper.RoleMapper;
import com.taibai.admin.mapper.TenantMapper;
import com.taibai.admin.mapper.UserMapper;
import com.taibai.admin.mapper.UserRoleProjectMapper;
import com.taibai.admin.syncproject.model.CreateTokenReq;
import com.taibai.admin.syncproject.model.CreateTokenResp;
import com.taibai.admin.syncproject.model.HxBusiInfo;
import com.taibai.admin.syncproject.model.QueryHxBusiInfoResp;
import com.taibai.common.core.constant.CommonConstants;
import com.taibai.common.core.constant.enums.ResponseCodeEnum;
import com.taibai.common.core.util.R;
import com.taibai.resource.api.feign.RemoteCmdbService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ProjectSyncTask {

    private final String CANCELLATION = "注销";
    private final String TO_CANCELLATION = "待注销";
    private final String LOCK = "锁定";

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private TenantMapper tenantMapper;

    @Autowired
    private UserRoleProjectMapper userRoleProjectMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private RemoteCmdbService remoteCmdbService;

    @Resource(name = "HxInternalRestTemplate")
    private RestTemplate restTemplate;

    private static final String MANAGER = "project_admin";
    private static final String MANAGER_OP = "manager_op";
    private static final String MANAGER_OP_B = "manager_op_b";
    private static final String APP_MANAGER = "app_manager";

    private static final String LOCK_KEY = "project_sync";

    @Value("${projectsync.appId}")
    private String appId;

    @Value("${projectsync.secret}")
    private String secret;

    @Value("${projectsync.hxIp}")
    private String hxIp;

    public void sync() {
        log.info("start sync project");
        boolean isLock = LockManager.tryLock(LOCK_KEY, 1, TimeUnit.MINUTES.toSeconds(10));
        if (!isLock) {
            return;
        }
        try {
            CreateTokenReq createTokenReq = new CreateTokenReq();
            createTokenReq.setAppId(appId);
            createTokenReq.setAppSecret(secret);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity("https://" + hxIp + "/auth/oauth/token",
                    createTokenReq, String.class);

            CreateTokenResp createTokenResp = JSON.parseObject(responseEntity.getBody(), CreateTokenResp.class);
            if (createTokenResp == null || createTokenResp.getDetail() == null
                    || StringUtils.isEmpty(createTokenResp.getDetail().getAccessToken())) {
                return;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "bearer " + createTokenResp.getDetail().getAccessToken());
            ResponseEntity<String> responseEntity2 = restTemplate.exchange("https://" + hxIp + "/cmdb/business/tenant",
                    HttpMethod.GET, new HttpEntity<String>(headers), String.class, new HashMap<>());

            QueryHxBusiInfoResp queryHxBusiInfoResp = JSON.parseObject(responseEntity2.getBody(),
                    QueryHxBusiInfoResp.class);
            if (queryHxBusiInfoResp == null || CollectionUtils.isEmpty(queryHxBusiInfoResp.getDetail())) {
                return;
            }

            List<HxBusiInfo> hxBusiInfos = queryHxBusiInfoResp.getDetail();
            Map<String, List<HxBusiInfo>> hxBusiInfoMap = new HashMap<>();
            for (HxBusiInfo hxBusiInfo : hxBusiInfos) {
                if (StringUtils.isEmpty(hxBusiInfo.getName())) {
                    continue;
                }
                List<HxBusiInfo> hxBusiInfosTemp = hxBusiInfoMap.get("中国民航信息集团");
                if (hxBusiInfosTemp == null) {
                    hxBusiInfosTemp = new ArrayList<>();
                    hxBusiInfoMap.put("中国民航信息集团", hxBusiInfosTemp);
                }
                hxBusiInfosTemp.add(hxBusiInfo);
            }

            List<Role> roles = roleMapper.selectList(new QueryWrapper<>());
            Map<String, Role> roleMap = roles.stream().collect(Collectors.toMap(Role::getRoleCode, role -> role));

            for (Map.Entry<String, List<HxBusiInfo>> entry : hxBusiInfoMap.entrySet()) {
                List<HxBusiInfo> hxBusiInfosTemp = entry.getValue();
                String tenantName = entry.getKey();
                QueryWrapper<Tenant> tenantQueryWrapper = new QueryWrapper<>();
                tenantQueryWrapper.eq("name", tenantName);
                Tenant tenant = tenantMapper.selectOne(tenantQueryWrapper);
                if (tenant == null) {
                    tenant = tenantMapper
                            .selectOne(new QueryWrapper<Tenant>().lambda().eq(Tenant::getName, "中国民航信息集团"));
                }

                for (HxBusiInfo hxBusiInfo : hxBusiInfosTemp) {
                    List<Project> existProjects = projectMapper.selectList(new QueryWrapper<Project>().lambda()
                            .eq(Project::getExProjectId, hxBusiInfo.getId()).eq(Project::getDelFlag, 0));
                    if (CollectionUtils.isEmpty(existProjects)) {
                        processProjectNotExist(roleMap, tenant, hxBusiInfo);
                    } else {
                        processProjectExist(roleMap, tenant, hxBusiInfo, existProjects.get(0));
                    }
                }
            }
        } finally {
            LockManager.unlock(LOCK_KEY);
        }
    }

    private void processProjectExist(Map<String, Role> roleMap, Tenant tenant, HxBusiInfo hxBusiInfo,
            Project existProject) {
        if (CANCELLATION.equals(hxBusiInfo.getStatus())) {
            R<Boolean> customerHasResource = remoteCmdbService.isCustomerHasResource("1", existProject.getId());
            if (ResponseCodeEnum.SUCCESS.getCode() != customerHasResource.getCode()) {
                log.error("check project has resource fail. projectId={}", existProject.getId());
                return;
            }

            if (customerHasResource.getData()) {
                log.error("project has resources. projectId={}", existProject.getId());
                return;
            }

            userRoleProjectMapper.delete(new QueryWrapper<UserRoleProject>().eq("project_id", existProject.getId()));
            projectMapper.deleteById(existProject.getId());
            return;
        }

        if (TO_CANCELLATION.equals(hxBusiInfo.getStatus()) || LOCK.equals(hxBusiInfo.getStatus())) {
            existProject.setStatus(ProjectStatusEnum.DISABLE.getCode());
            existProject.setUpdateTime(LocalDateTime.now());
            existProject.setCossId(hxBusiInfo.getCossId());
            projectMapper.updateById(existProject);
        }

        if (!StringUtils.equals(hxBusiInfo.getName(), existProject.getName()) || existProject.getCossId() == null) {
            existProject.setName(hxBusiInfo.getName());
            existProject.setUpdateTime(LocalDateTime.now());
            existProject.setCossId(hxBusiInfo.getCossId());
            projectMapper.updateById(existProject);
        }

        List<UserRoleProject> userRoleProjects = userRoleProjectMapper
                .selectList(new QueryWrapper<UserRoleProject>().eq("project_id", existProject.getId()));
        Map<Integer, List<UserRoleProject>> userRoleProjectMap = new HashMap<>();
        for (UserRoleProject userRoleProject : userRoleProjects) {
            List<UserRoleProject> temp = userRoleProjectMap.get(userRoleProject.getRoleId());
            if (temp == null) {
                temp = new ArrayList<>();
                userRoleProjectMap.put(userRoleProject.getRoleId(), temp);
            }
            temp.add(userRoleProject);
        }

        processUserRoleProject(roleMap, tenant, hxBusiInfo, existProject, userRoleProjectMap, MANAGER,
                hxBusiInfo.getManager());
        processUserRoleProject(roleMap, tenant, hxBusiInfo, existProject, userRoleProjectMap, MANAGER_OP,
                hxBusiInfo.getManagerOp());
        processUserRoleProject(roleMap, tenant, hxBusiInfo, existProject, userRoleProjectMap, MANAGER_OP_B,
                hxBusiInfo.getManagerOpb());
        processUserRoleProject(roleMap, tenant, hxBusiInfo, existProject, userRoleProjectMap, APP_MANAGER,
                hxBusiInfo.getAppManager());
    }

    private void processProjectNotExist(Map<String, Role> roleMap, Tenant tenant, HxBusiInfo hxBusiInfo) {
        if (CANCELLATION.equals(hxBusiInfo.getStatus())) {
            return;
        }

        Project newProject = new Project();
        newProject.setName(hxBusiInfo.getName());
        newProject.setTenantId(tenant.getId());
        if (TO_CANCELLATION.equals(hxBusiInfo.getStatus()) || LOCK.equals(hxBusiInfo.getStatus())) {
            newProject.setStatus(ProjectStatusEnum.DISABLE.getCode());
        } else {
            newProject.setStatus(ProjectStatusEnum.ENABLE.getCode());
        }
        newProject.setIsDefault(ProjectDefaultEnum.NO.getCode());
        newProject.setCreateTime(LocalDateTime.now());
        newProject.setUpdateTime(newProject.getCreateTime());
        newProject.setDelFlag(CommonConstants.STATUS_NORMAL);
        newProject.setExProjectId(hxBusiInfo.getId());
        newProject.setCossId(hxBusiInfo.getCossId());
        projectMapper.insert(newProject);

        addUserRoleProject(roleMap, tenant, newProject, hxBusiInfo.getManager(), MANAGER);
        addUserRoleProject(roleMap, tenant, newProject, hxBusiInfo.getManagerOp(), MANAGER_OP);
        addUserRoleProject(roleMap, tenant, newProject, hxBusiInfo.getManagerOpb(), MANAGER_OP_B);
        addUserRoleProject(roleMap, tenant, newProject, hxBusiInfo.getAppManager(), APP_MANAGER);
    }

    private void processUserRoleProject(Map<String, Role> roleMap, Tenant tenant, HxBusiInfo hxBusiInfo,
            Project existProject, Map<Integer, List<UserRoleProject>> userRoleProjectMap, String roleCode,
            String hxUser) {
        List<User> users = null;
        String[] hxUserArr = null;
        if (StringUtils.isNotEmpty(hxUser)) {
            hxUserArr = hxUser.split(" ");
            String email = hxUserArr[hxUserArr.length - 1];
            users = userMapper.selectList(new QueryWrapper<User>().eq("email", email));
        }
        List<UserRoleProject> userRoleProjectsTemp = userRoleProjectMap.get(roleMap.get(roleCode).getId());
        if (CollectionUtils.isEmpty(userRoleProjectsTemp)) {
            if (CollectionUtils.isNotEmpty(users)) {
                for (User user : users) {
                    List<UserRoleProject> userRoleProjects = userRoleProjectMapper.selectList(
                            new QueryWrapper<UserRoleProject>().lambda().eq(UserRoleProject::getUserId, user.getId())
                                    .eq(UserRoleProject::getTenantId, tenant.getId()));
                    if (CollectionUtils.isNotEmpty(userRoleProjects)) {
                        UserRoleProject userRoleProject2 = new UserRoleProject();
                        userRoleProject2.setProjectId(existProject.getId());
                        userRoleProject2.setRoleId(roleMap.get(roleCode).getId());
                        userRoleProject2.setUserId(user.getId());
                        userRoleProjectMapper.insert(userRoleProject2);
                    }
                }
            } else {
                if (hxUserArr != null) {
                    users = userMapper.selectList(new QueryWrapper<User>().eq("name", hxUserArr[0]));
                    if (CollectionUtils.isNotEmpty(users)) {
                        for (User user : users) {
                            if (hxUserArr[hxUserArr.length - 1].equalsIgnoreCase(user.getEmail())) {
                                List<UserRoleProject> userRoleProjects = userRoleProjectMapper
                                        .selectList(new QueryWrapper<UserRoleProject>().lambda()
                                                .eq(UserRoleProject::getUserId, user.getId())
                                                .eq(UserRoleProject::getTenantId, tenant.getId()));
                                if (CollectionUtils.isNotEmpty(userRoleProjects)) {
                                    UserRoleProject userRoleProject2 = new UserRoleProject();
                                    userRoleProject2.setProjectId(existProject.getId());
                                    userRoleProject2.setRoleId(roleMap.get(roleCode).getId());
                                    userRoleProject2.setUserId(user.getId());
                                    userRoleProjectMapper.insert(userRoleProject2);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            if (CollectionUtils.isNotEmpty(users)) {
                for (User user : users) {
                    List<UserRoleProject> userRoleProjects = userRoleProjectMapper.selectList(
                            new QueryWrapper<UserRoleProject>().lambda().eq(UserRoleProject::getUserId, user.getId())
                                    .eq(UserRoleProject::getTenantId, tenant.getId()));
                    if (CollectionUtils.isNotEmpty(userRoleProjects)) {
                        boolean isUserInProjectRoles = isUserInProjectRoles(roleMap.get(roleCode).getId(), user.getId(),
                                existProject.getId(), userRoleProjectsTemp);
                        if (isUserInProjectRoles) {
                            continue;
                        }
                        UserRoleProject userRoleProject2 = new UserRoleProject();
                        userRoleProject2.setProjectId(existProject.getId());
                        userRoleProject2.setRoleId(roleMap.get(roleCode).getId());
                        userRoleProject2.setUserId(user.getId());
                        userRoleProjectMapper.insert(userRoleProject2);
                    }
                }
            } else {
                if (hxUserArr != null) {
                    users = userMapper.selectList(new QueryWrapper<User>().eq("name", hxUserArr[0]));
                    if (CollectionUtils.isNotEmpty(users)) {
                        for (User user : users) {
                            if (hxUserArr[hxUserArr.length - 1].equalsIgnoreCase(user.getEmail())) {
                                List<UserRoleProject> userRoleProjects = userRoleProjectMapper
                                        .selectList(new QueryWrapper<UserRoleProject>().lambda()
                                                .eq(UserRoleProject::getUserId, user.getId())
                                                .eq(UserRoleProject::getTenantId, tenant.getId()));
                                if (CollectionUtils.isNotEmpty(userRoleProjects)) {
                                    boolean isUserInProjectRoles = isUserInProjectRoles(roleMap.get(roleCode).getId(),
                                            user.getId(), existProject.getId(), userRoleProjectsTemp);
                                    if (isUserInProjectRoles) {
                                        continue;
                                    }
                                    UserRoleProject userRoleProject2 = new UserRoleProject();
                                    userRoleProject2.setProjectId(existProject.getId());
                                    userRoleProject2.setRoleId(roleMap.get(roleCode).getId());
                                    userRoleProject2.setUserId(user.getId());
                                    userRoleProjectMapper.insert(userRoleProject2);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isUserInProjectRoles(Integer roleId, Integer userId, Integer projectId,
            List<UserRoleProject> userRoleProjectsTemp) {
        for (UserRoleProject userRoleProject : userRoleProjectsTemp) {
            if (roleId.equals(userRoleProject.getRoleId()) && userId.equals(userRoleProject.getUserId())
                    && projectId.equals(userRoleProject.getProjectId())) {
                return true;
            }
        }
        return false;
    }

    private void addUserRoleProject(Map<String, Role> roleMap, Tenant tenant, Project newProject, String hxUser,
            String hxRoleCode) {
        if (roleMap.get(hxRoleCode) == null) {
            return;
        }
        if (StringUtils.isNotEmpty(hxUser)) {
            String[] hxUserArr = hxUser.split(" ");
            String email = hxUserArr[hxUserArr.length - 1];
            List<User> users = userMapper.selectList(new QueryWrapper<User>().eq("email", email));
            if (CollectionUtils.isNotEmpty(users)) {
                for (User user : users) {
                    List<UserRoleProject> userRoleProjects = userRoleProjectMapper.selectList(
                            new QueryWrapper<UserRoleProject>().lambda().eq(UserRoleProject::getUserId, user.getId())
                                    .eq(UserRoleProject::getTenantId, tenant.getId()));
                    if (CollectionUtils.isNotEmpty(userRoleProjects)) {
                        UserRoleProject userRoleProject2 = new UserRoleProject();
                        userRoleProject2.setProjectId(newProject.getId());
                        userRoleProject2.setRoleId(roleMap.get(hxRoleCode).getId());
                        userRoleProject2.setUserId(user.getId());
                        userRoleProjectMapper.insert(userRoleProject2);
                    }
                }
            } else {
                users = userMapper.selectList(new QueryWrapper<User>().eq("name", hxUserArr[0]));
                if (CollectionUtils.isNotEmpty(users)) {
                    for (User user : users) {
                        if (email.equalsIgnoreCase(user.getEmail())) {
                            List<UserRoleProject> userRoleProjects = userRoleProjectMapper
                                    .selectList(new QueryWrapper<UserRoleProject>().lambda()
                                            .eq(UserRoleProject::getUserId, user.getId())
                                            .eq(UserRoleProject::getTenantId, tenant.getId()));
                            if (CollectionUtils.isNotEmpty(userRoleProjects)) {
                                UserRoleProject userRoleProject2 = new UserRoleProject();
                                userRoleProject2.setProjectId(newProject.getId());
                                userRoleProject2.setRoleId(roleMap.get(hxRoleCode).getId());
                                userRoleProject2.setUserId(user.getId());
                                userRoleProjectMapper.insert(userRoleProject2);
                            }
                        }
                    }
                }
            }
        }
    }
}
