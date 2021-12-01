package com.taibai.admin.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taibai.admin.api.dto.ProjectDTO;
import com.taibai.admin.api.dto.UserDTO;
import com.taibai.admin.api.entity.AuthCheck;
import com.taibai.admin.api.entity.Project;
import com.taibai.admin.api.entity.ProjectOperatingRange;
import com.taibai.admin.api.entity.Role;
import com.taibai.admin.api.entity.User;
import com.taibai.admin.api.entity.UserRoleProject;
import com.taibai.admin.api.feign.RemoteTokenService;
import com.taibai.admin.api.vo.Member;
import com.taibai.admin.api.vo.ProjectVO;
import com.taibai.admin.api.vo.UserVO;
import com.taibai.admin.exceptions.UserCenterException;
import com.taibai.admin.mapper.ProjectMapper;
import com.taibai.admin.mapper.RoleMapper;
import com.taibai.admin.mapper.TenantMapper;
import com.taibai.admin.mapper.UserMapper;
import com.taibai.admin.mapper.UserRoleProjectMapper;
import com.taibai.admin.service.IAuthService;
import com.taibai.admin.service.IProjectService;
import com.taibai.common.core.constant.CommonConstants;
import com.taibai.common.core.constant.SecurityConstants;
import com.taibai.common.core.constant.enums.ApiEnum;
import com.taibai.common.core.constant.enums.BusinessEnum;
import com.taibai.common.core.constant.enums.OperatingRangeEnum;
import com.taibai.common.core.constant.enums.ResponseCodeEnum;
import com.taibai.common.core.exception.BusinessException;
import com.taibai.common.core.util.R;
import com.taibai.common.security.service.FitmgrUser;
import com.taibai.common.security.util.SecurityUtils;
import com.taibai.quota.api.entity.QuotaRelationProject;
import com.taibai.quota.api.feign.RemoteQuotaService;
import com.taibai.resource.api.feign.RemoteCmdbService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * project表 服务实现类
 * </p>
 *
 * @author Taibai
 * @since 2019-11-15
 */
@Slf4j
@Service
@AllArgsConstructor
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements IProjectService {

    private final String PROJECT_STATUS_OFF = "1";
    private final String USER_STATUS_OFF = "1";

    private final ProjectMapper projectMapper;

    private final TenantMapper tenantMapper;

    private final UserRoleProjectMapper userRoleProjectMapper;

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final IAuthService authService;
    private final RemoteTokenService remoteTokenService;
    private final RedisTemplate<String, String> redisTemplate;
    private final RemoteCmdbService remoteCmdbService;
    private final RemoteQuotaService remoteQuotaService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveProject(ProjectDTO projectDTO) {

        Project project = new Project();
        BeanUtils.copyProperties(projectDTO, project);
        project.setCreateTime(LocalDateTime.now());
        project.setUpdateTime(project.getCreateTime());
        project.setDelFlag(CommonConstants.STATUS_NORMAL);
        project.setStatus(CommonConstants.STATUS_NORMAL);
        project.setIsDefault("1");

        // 查询所属租户 是否是 航信租户
        int i = projectMapper.insert(project);
        if (i != 1) {
            throw new RuntimeException("add project operate is error!");
        }
        Integer code = saveProjectInfoToQuota(projectDTO);
        if (code.equals(1)) {
            log.info("quota add project operate is error!");
        }
        redisTemplate.opsForValue().set(CommonConstants.PROJECT_PREFIX + project.getId(), project.getName());
        return true;

    }

    public Integer saveProjectInfoToQuota(ProjectDTO projectDTO) {
        ProjectDTO result = findProject(projectDTO.getName());
        ProjectVO projectVO = selectById(result.getId());
        QuotaRelationProject quotaRelationProject = new QuotaRelationProject();
        quotaRelationProject.setCreateTime(projectVO.getCreateTime());
        quotaRelationProject.setName(projectVO.getName());
        quotaRelationProject.setProjectId(projectVO.getId());
        quotaRelationProject.setTenantName(projectVO.getTenantName());
        quotaRelationProject.setTenantId(projectDTO.getTenantId());

        R r = remoteQuotaService.addProjectRelation(quotaRelationProject);
        return r.getCode();
    }

    public R deleteProjectInfoToQuota(Integer projectId) {
        return remoteQuotaService.deleteProjectRelation(projectId);
    }

    public Integer updateProjectInfoToQuota(ProjectDTO projectDTO) {
        QuotaRelationProject quotaRelationProject = new QuotaRelationProject();
        ProjectVO projectVO = selectById(projectDTO.getId());
        quotaRelationProject.setName(projectDTO.getName());
        quotaRelationProject.setTenantName(projectVO.getTenantName());
        quotaRelationProject.setProjectId(projectDTO.getId());
        R r = remoteQuotaService.updateProjectRelation(quotaRelationProject);
        return r.getCode();
    }

    @Override
    public boolean deleteProject(Integer projectId) {
        Integer count = userRoleProjectMapper
                .selectCount(new QueryWrapper<UserRoleProject>().lambda().eq(UserRoleProject::getProjectId, projectId));
        if (count > 0) {
            throw new UserCenterException("项目下仍然有成员，不能删除该项目");
        }
        R<Boolean> customerHasResource = remoteCmdbService.isCustomerHasResource("1", projectId);
        if (ResponseCodeEnum.SUCCESS.getCode() != customerHasResource.getCode()) {
            throw new BusinessException("检查项目资源时发生异常");
        }
        if (customerHasResource.getData()) {
            throw new BusinessException("项目下存在资源未释放，不能删除该项目");
        }
        R r = deleteProjectInfoToQuota(projectId);
        if (r == null || r.getCode() == 1) {
            log.error("quota delete project operate is error!");
            throw new BusinessException(r == null ? "删除项目配额失败" : r.getMsg());
        }
        int i = projectMapper.deleteById(projectId);
        if (i != 1) {
            log.error("delete project operate is error!");
            throw new RuntimeException("delete project operate is error!");
        }
        redisTemplate.delete(CommonConstants.PROJECT_PREFIX + projectId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R updateProject(ProjectDTO projectDTO) {
        Project project = new Project();
        BeanUtils.copyProperties(projectDTO, project);
        project.setUpdateTime(LocalDateTime.now());
        projectMapper.updateById(project);
        Integer code = updateProjectInfoToQuota(projectDTO);
        if (code.equals(1)) {
            log.info("quota update project operate is error!");
            return R.failed();
        }
        redisTemplate.opsForValue().set(CommonConstants.PROJECT_PREFIX + project.getId(), project.getName());
        return R.ok();
    }

    @Override
    public ProjectVO selectById(Integer projectId) {
        ProjectVO projectVO = projectMapper.selectProjectVoById(projectId);
        return projectVO;
    }

    @Override
    public IPage<ProjectVO> pageList(Page page, ProjectDTO projectDTO, Integer userId) {
        // TODO 获取数据权限
        R<AuthCheck> r = authService.newAuthCheck(ApiEnum.SELECT_PROJECT_LIST.getCode(), userId,
                SecurityUtils.getUser().getDefaultTenantId(), null, null, null);
        log.info(">>>>>>>> r {}", JSONObject.toJSONString(r));
        if (r.getCode() == 0 && r.getData().isStatus()) {
            if (OperatingRangeEnum.ALL_CODE.equals(r.getData().getOperatingRange())) {
                // 全局
                IPage<ProjectVO> iPage = projectMapper.listByCondition(page, projectDTO);
                return processPageResult(iPage);
            } else if (OperatingRangeEnum.TENANT_CODE.equals(r.getData().getOperatingRange())) {
                // 租户级别
                projectDTO.setTenantIds(r.getData().getTenantIds());
                IPage<ProjectVO> iPage = projectMapper.listByCondition(page, projectDTO);
                return processPageResult(iPage);
            } else if (OperatingRangeEnum.PROJECT_CODE.equals(r.getData().getOperatingRange())) {
                // project 级别
                List<Integer> projectIds = new ArrayList<Integer>();
                if (CollectionUtils.isNotEmpty(r.getData().getProjectIds())) {
                    projectIds.addAll(r.getData().getProjectIds());
                }
                if (CollectionUtils.isNotEmpty(r.getData().getProjectOperatingRanges())) {
                    for (ProjectOperatingRange projectOperatingRange : r.getData().getProjectOperatingRanges()) {
                        projectIds.add(projectOperatingRange.getProjectId());
                    }
                }
                projectDTO.setIds(projectIds);
                IPage<ProjectVO> iPage = projectMapper.listByCondition(page, projectDTO);
                return processPageResult(iPage);
            } else if (OperatingRangeEnum.SELF_CODE.equals(r.getData().getOperatingRange())) {
                throw new UserCenterException(BusinessEnum.AUTH_NOT);
            }

            throw new UserCenterException(BusinessEnum.AUTH_CONFIG);
        }
        throw new UserCenterException(BusinessEnum.AUTH_NOT);
    }

    private IPage<ProjectVO> processPageResult(IPage<ProjectVO> iPage) {
        if (CollectionUtils.isEmpty(iPage.getRecords())) {
            return iPage;
        }
        return iPage;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addMember(List<UserRoleProject> list) {

        // 查询角色表，获取普通用户角色 的 id
        Role role = roleMapper.selectOne(new QueryWrapper<Role>().eq("role_code", ApiEnum.PROJECT_USER.getCode()));

        // 新加入的成员默认是普通成员
        for (UserRoleProject userRoleProject : list) {
            userRoleProject.setRoleId(role.getId());
        }

        // 设置project所属的tenant
        for (UserRoleProject userRoleProject : list) {
            Integer projectId = userRoleProject.getProjectId();
            if (null == projectId) {
                continue;
            }
            Project project = projectMapper.selectById(projectId);
            if (null == project) {
                continue;
            }
            userRoleProject.setTenantId(project.getTenantId());
        }
        projectMapper.addMember(list);
    }

    @Override
    public void removeMember(UserRoleProject userRoleProject) {
        projectMapper.removeMember(userRoleProject);
    }

    @Override
    public List<Project> listCondition(ProjectDTO projectDTO) {

        FitmgrUser user = SecurityUtils.getUser();
        R<AuthCheck> r = authService.newAuthCheck("select_project_list_nopage", user.getId(), user.getDefaultTenantId(),
                null, null, null);
        log.info("r.getMsg() value is {}", r.getMsg());
        if (r.getCode() == 0 && r.getData().isStatus()) {
            if (OperatingRangeEnum.ALL_CODE.equals(r.getData().getOperatingRange())) {
                // 全局
                return projectMapper.listNoPageByCondition(projectDTO);
            } else if (OperatingRangeEnum.TENANT_CODE.equals(r.getData().getOperatingRange())) {
                // 租户级别
                projectDTO.setTenantIds(r.getData().getTenantIds());
                return projectMapper.listNoPageByCondition(projectDTO);
            } else if (OperatingRangeEnum.PROJECT_CODE.equals(r.getData().getOperatingRange())) {
                // project 级别
                List<Integer> projectIds = new ArrayList<Integer>();
                if (CollectionUtils.isNotEmpty(r.getData().getProjectIds())) {
                    projectIds.addAll(r.getData().getProjectIds());
                }
                if (CollectionUtils.isNotEmpty(r.getData().getProjectOperatingRanges())) {
                    for (ProjectOperatingRange projectOperatingRange : r.getData().getProjectOperatingRanges()) {
                        projectIds.add(projectOperatingRange.getProjectId());
                    }
                }
                projectDTO.setProjectIds(projectIds);
                return projectMapper.listNoPageByCondition(projectDTO);
            } else if (OperatingRangeEnum.SELF_CODE.equals(r.getData().getOperatingRange())) {
                projectDTO.setUserId(SecurityUtils.getUser().getId());
                return projectMapper.listNoPageByCondition(projectDTO);
            }
            throw new UserCenterException(BusinessEnum.AUTH_CONFIG);
        }
        throw new UserCenterException(BusinessEnum.AUTH_NOT);
    }

    @Override
    public List<Project> listConditionInnerProject(ProjectDTO projectDTO, String from) {
        R<AuthCheck> r;
        if (StringUtils.isNoneBlank(from) && SecurityConstants.FROM_IN.equals(from)) {
            AuthCheck authCheck = new AuthCheck();
            authCheck.setOperatingRange("0");
            r = R.ok(authCheck);
        } else {
            FitmgrUser user = SecurityUtils.getUser();
            r = authService.newAuthCheck("select_project_list_nopage", user.getId(), user.getDefaultTenantId(), null,
                    null, null);
        }
        log.info("r.getMsg() value is {}", r.getMsg());
        if (r.getCode() == 0 && r.getData().isStatus()) {
            if (OperatingRangeEnum.ALL_CODE.equals(r.getData().getOperatingRange())) {
                // 全局
                return projectMapper.listNoPageByCondition(projectDTO);
            } else if (OperatingRangeEnum.TENANT_CODE.equals(r.getData().getOperatingRange())) {
                // 租户级别
                projectDTO.setTenantIds(r.getData().getTenantIds());
                return projectMapper.listNoPageByCondition(projectDTO);
            } else if (OperatingRangeEnum.PROJECT_CODE.equals(r.getData().getOperatingRange())) {
                // project 级别
                List<Integer> projectIds = new ArrayList<Integer>();
                if (CollectionUtils.isNotEmpty(r.getData().getProjectIds())) {
                    projectIds.addAll(r.getData().getProjectIds());
                }
                if (CollectionUtils.isNotEmpty(r.getData().getProjectOperatingRanges())) {
                    for (ProjectOperatingRange projectOperatingRange : r.getData().getProjectOperatingRanges()) {
                        projectIds.add(projectOperatingRange.getProjectId());
                    }
                }
                projectDTO.setProjectIds(projectIds);
                return projectMapper.listNoPageByCondition(projectDTO);
            } else if (OperatingRangeEnum.SELF_CODE.equals(r.getData().getOperatingRange())) {
                projectDTO.setUserId(SecurityUtils.getUser().getId());
                return projectMapper.listNoPageByCondition(projectDTO);
            }
            throw new UserCenterException(BusinessEnum.AUTH_CONFIG);
        }
        throw new UserCenterException(BusinessEnum.AUTH_NOT);
    }

    @Override
    public List<Project> projectListByTenantId(Integer id) {
        LambdaQueryWrapper<Project> queryWrapper = Wrappers.<Project>lambdaQuery().eq(null != id, Project::getTenantId,
                id);
        return projectMapper.selectList(queryWrapper);
    }

    public IPage listMember(Page page, Integer projectId, String name) {
        IPage iPage = null;
        if (StringUtils.isEmpty(name)) {
            iPage = projectMapper.listMemberPage(page, projectId);
        } else {
            iPage = projectMapper.listMemberByName(page, projectId, name);
        }
        Map<Integer, Member> memberRoleMap = findMemRoleMap(projectId);
        if (iPage.getRecords() != null) {
            for (int i = 0; i < iPage.getRecords().size(); i++) {
                Member member = (Member) iPage.getRecords().get(i);
                Member member2 = memberRoleMap.get(member.getUserId());
                if (member2 != null) {
                    member.setRoleName(member2.getRoleName());
                    member.setRoleId(member2.getRoleId());
                    member.setRoles(member2.getRoles());
                }
            }
        }
        return iPage;
    }

    @Override
    public List<Member> listMember(Integer projectId) {
        Map<Integer, Member> memberRoleMap = findMemRoleMap(projectId);
        List<Member> members = new ArrayList<>();
        members.addAll(memberRoleMap.values());
        return members;
    }

    @Override
    public IPage<UserVO> queryUserForAddMember(Page page, Integer tenantId, Integer projectId, String queryName) {
        // 将该Project已经配置了的用户过滤，只返回未配置的用户
        LambdaQueryWrapper<UserRoleProject> wrapperQuery = Wrappers.<UserRoleProject>lambdaQuery().eq(true,
                UserRoleProject::getProjectId, projectId);
        List<UserRoleProject> userRoleProjects = userRoleProjectMapper.selectList(wrapperQuery);
        Set<Integer> userIds = new HashSet<>();
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(userRoleProjects)) {
            userRoleProjects.forEach(userRoleProject -> userIds.add(userRoleProject.getUserId()));
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setUserIds(new ArrayList<>(userIds));
        userDTO.setTenantId(tenantId);
        userDTO.setName(queryName);
        userDTO.setStatus("0");
        return userMapper.selectListByCondition(page, userDTO);
    }

    private Map<Integer, Member> findMemRoleMap(Integer projectId) {
        List<Member> initMembers = projectMapper.listMember(projectId);
        Map<Integer, Member> memberRoleMap = new HashMap<>();
        if (initMembers != null) {
            for (Member member : initMembers) {
                Member newMember = memberRoleMap.get(member.getUserId());
                if (newMember == null) {
                    newMember = new Member();
                    newMember.setName(member.getName());
                    newMember.setPhone(member.getPhone());
                    newMember.setUserId(member.getUserId());
                    newMember.setRoleId(member.getRoleId());
                    newMember.setRoleName(member.getRoleName());
                    List<Role> roles = new ArrayList<>();
                    newMember.setRoles(roles);
                    memberRoleMap.put(member.getUserId(), newMember);
                }
                Role role = new Role();
                role.setId(member.getRoleId());
                role.setRoleName(member.getRoleName());
                newMember.getRoles().add(role);
            }
        }
        return memberRoleMap;
    }

    @Override
    public R updateMemberRole(UserRoleProject userRoleProject) {
        if (userRoleProject.getProjectId() == null) {
            return R.failed("id参数不能为空");
        }
        Project project = projectMapper.selectById(userRoleProject.getProjectId());
        if (project == null) {
            return R.failed("指定的项目不存在");
        }
        if (PROJECT_STATUS_OFF.equals(project.getStatus())) {
            return R.failed("项目已被禁用");
        }
        if (userRoleProject.getUserId() == null) {
            return R.failed("用户id参数不能为空");
        }
        User user = userMapper.selectById(userRoleProject.getUserId());
        if (user == null) {
            return R.failed("指定的用户不存在");
        }

        if (USER_STATUS_OFF.equals(user.getStatus())) {
            return R.failed("用户已被禁用");
        }

        List<UserRoleProject> one = userRoleProjectMapper.selectList(new QueryWrapper<UserRoleProject>().lambda()
                .eq(UserRoleProject::getProjectId, userRoleProject.getProjectId())
                .eq(UserRoleProject::getUserId, userRoleProject.getUserId()));

        if (CollectionUtils.isEmpty(one)) {
            return R.failed("用户未加入该项目");
        }

        if (CollectionUtils.isEmpty(userRoleProject.getRoleIds())) {
            Role role = roleMapper.selectById(userRoleProject.getRoleId());
            if (role == null) {
                return R.failed("指定的角色不存在");
            }
            projectMapper.updateMemberRole(userRoleProject);
        } else {
            userRoleProjectMapper.delete(new QueryWrapper<UserRoleProject>().eq("user_id", userRoleProject.getUserId())
                    .eq("project_id", userRoleProject.getProjectId()));

            for (Integer roleId : userRoleProject.getRoleIds()) {
                UserRoleProject userRoleProjectTemp = new UserRoleProject();
                userRoleProjectTemp.setUserId(userRoleProject.getUserId());
                userRoleProjectTemp.setRoleId(roleId);
                userRoleProjectTemp.setProjectId(userRoleProject.getProjectId());
                userRoleProjectTemp.setTenantId(project.getTenantId());
                userRoleProjectMapper.insert(userRoleProjectTemp);
            }
        }
        return R.ok();
    }

    @Override
    public int updateStatus(Project project) {
        return projectMapper.updateById(project);
    }

    @Override
    public ProjectDTO findProject(String name) {
        Project project = projectMapper
                .selectOne(new QueryWrapper<Project>().lambda().eq(true, Project::getName, name));
        ProjectDTO projectDTO = new ProjectDTO();
        if (null != project) {
            BeanUtils.copyProperties(project, projectDTO);
            return projectDTO;
        } else {
            return null;
        }
    }

    @Override
    public int updateProjectTenantId(Integer projectId, Integer tenantId) {
        Project project = new Project();
        project.setId(projectId);
        project.setTenantId(tenantId);
        return projectMapper.updateById(project);
    }

    @Override
    public List<Project> projectList(ProjectDTO projectDTO) {
        LambdaQueryWrapper<Project> queryWrapper = Wrappers.<Project>lambdaQuery()
                .in(null != projectDTO.getProjectIds(), Project::getId, projectDTO.getProjectIds());
        return projectMapper.selectList(queryWrapper);
    }
}
