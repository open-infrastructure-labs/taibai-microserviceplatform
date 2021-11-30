package com.taibai.admin.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taibai.admin.api.dto.ProjectDTO;
import com.taibai.admin.api.entity.Project;
import com.taibai.admin.api.entity.Tenant;
import com.taibai.admin.api.entity.User;
import com.taibai.admin.api.entity.UserRoleProject;
import com.taibai.admin.api.validation.Save;
import com.taibai.admin.api.validation.Update;
import com.taibai.admin.api.vo.Member;
import com.taibai.admin.api.vo.ProjectVO;
import com.taibai.admin.api.vo.TenantVO;
import com.taibai.admin.api.vo.UserVO;
import com.taibai.admin.mapper.ProjectMapper;
import com.taibai.admin.service.IProjectService;
import com.taibai.admin.service.ITenantService;
import com.taibai.admin.service.IUserRoleProjectService;
import com.taibai.admin.service.IUserService;
import com.taibai.admin.syncproject.ProjectSyncTask;
import com.taibai.common.core.constant.SecurityConstants;
import com.taibai.common.core.constant.enums.BusinessEnum;
import com.taibai.common.core.constant.enums.DeleteFlagStatusEnum;
import com.taibai.common.core.util.R;
import com.taibai.common.log.annotation.SysLog;
import com.taibai.common.security.service.FitmgrUser;
import com.taibai.common.security.util.SecurityUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 创建人 mhp 创建时间 2019/11/15 描述
 **/
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/project")
@Api(value = "project", tags = "项目管理模块")
public class ProjectController {
    private final String TENANT_STATUS_OFF = "1";
    private final String TENANT_CREAT_PROJECT_OFF = "1";
    private final String PROJECT_STATUS_OFF = "1";
    private final String USER_STATUS_OFF = "1";

    private final ProjectMapper projectMapper;
    private final IProjectService projectService;
    private final ITenantService tenantService;
    private final IUserRoleProjectService userRoleProjectService;
    private final IUserService userService;
    @Autowired
    private ProjectSyncTask projectSyncTask;

    /**
     * 添加project
     *
     * @param projectDTO DTO对象
     * @return R
     */
    @SysLog(value = "添加项目", cloudResType = "项目", resNameArgIndex = 0, resNameLocation = "arg.name")
    @PostMapping
    @ApiOperation(value = "添加project")
    @ApiImplicitParams(@ApiImplicitParam(name = "projectDTO", value = "新增projectDTO对象", dataType = "ProjectDTO", paramType = "body", required = true))
    public R saveProject(@Validated(Save.class) @RequestBody ProjectDTO projectDTO) {
        // 查询所属租户是否可创建project
        Tenant tenant = tenantService.getById(projectDTO.getTenantId());
        if (tenant == null) {
            return R.failed("所选租户不存在");
        }
        if (TENANT_STATUS_OFF.equals(tenant.getStatus())) {
            return R.failed("租户已被禁用");
        }
        if (TENANT_CREAT_PROJECT_OFF.equals(tenant.getCreateProject())) {
            return R.failed("租户不可创建project");
        }

        int count = projectService.count(new QueryWrapper<Project>().eq("name", projectDTO.getName()));
        if (count > 0) {
            return R.failed("项目名称重复");
        }
        boolean b = projectService.saveProject(projectDTO);
        return b ? R.ok() : R.failed();
    }

    /**
     * 根据id删除project
     *
     * @param id projectId
     * @return R
     */
    @SysLog(value = "删除项目", cloudResType = "项目", resIdArgIndex = 0, resIdLocation = "arg")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除project")
    @ApiImplicitParams(@ApiImplicitParam(name = "id", value = "projectId", dataType = "Integer", paramType = "path", required = true))
    public R deleteProject(@PathVariable("id") Integer id) {
        if (id == null) {
            return R.failed("id参数不能为空");
        }
        int count = projectService.count(new QueryWrapper<Project>().lambda().eq(Project::getId, id));
        if (count < 1) {
            return R.failed("项目不存在");
        }
        boolean b = projectService.deleteProject(id);
        return b ? R.ok() : R.failed();
    }

    /**
     * 修改project
     *
     * @param projectDTO DTO对象
     * @return R
     */
    @SysLog(value = "修改项目", cloudResType = "项目", resIdArgIndex = 0, resIdLocation = "arg.id", resNameArgIndex = 0, resNameLocation = "arg.name")
    @PutMapping
    @ApiOperation(value = "更新project")
    @ApiImplicitParams(@ApiImplicitParam(name = "projectDTO", value = "projectDTO对象", dataType = "ProjectDTO", paramType = "body", required = true))
    public R updateProject(@Validated(Update.class) @RequestBody ProjectDTO projectDTO) {
        int count = projectService.count(new QueryWrapper<Project>().lambda().eq(Project::getId, projectDTO.getId()));
        if (count < 1) {
            return R.failed("项目不存在");
        }

        Project project = projectService.getOne(new QueryWrapper<Project>().eq("name", projectDTO.getName()));
        if (project != null && !project.getId().equals(projectDTO.getId())) {
            return R.failed("项目名称重复");
        }

        return projectService.updateProject(projectDTO);
    }

    /**
     * 根据id查询projectVO
     *
     * @param id projectId
     * @return R
     */
    @GetMapping("/detail/{id}")
    @ApiOperation(value = "根据id获取project详情")
    @ApiImplicitParams(@ApiImplicitParam(name = "id", value = "项目id", dataType = "Integer", paramType = "path", required = true))
    public R selectProject(@PathVariable("id") Integer id) {
        if (id == null) {
            return R.failed("id参数不能为空");
        }
        ProjectVO projectVO = projectService.selectById(id);
        return projectVO == null ? R.failed("project不存在") : R.ok(projectVO);
    }

    /**
     * 根据条件查询project详情列表
     *
     * @param page       分页条件对象
     * @param projectDTO DTO对象
     * @return R
     */
    @GetMapping("/list/page")
    @ApiOperation(value = "条件分页查询project详情列表")
    @ApiImplicitParams({ @ApiImplicitParam(name = "page", value = "分页条件对象", dataType = "Page", paramType = "query"),
            @ApiImplicitParam(name = "projectDTO", value = "查询条件对象", dataType = "ProjectDTO", paramType = "query") })
    public R pageList(Page page, ProjectDTO projectDTO) {

        // 获取当前用户的默认角色id
        FitmgrUser user = SecurityUtils.getUser();
        IPage<ProjectVO> iPage = projectService.pageList(page, projectDTO, user.getId());
        return R.ok(iPage);
    }

    @GetMapping("/member-add/users/{tenantId}/{projectId}")
    @ApiOperation(value = "租户添加成员查询用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tenantId", value = "租户id", allowMultiple = true, dataType = "Integer", paramType = "path", required = true),
            @ApiImplicitParam(name = "projectId", value = "projectId", allowMultiple = true, dataType = "Integer", paramType = "path", required = true),
            @ApiImplicitParam(name = "queryName", value = "queryName", allowMultiple = true, dataType = "String", paramType = "String", required = true) })
    public R addMemberQueryUser(Page page, @PathVariable("tenantId") Integer tenantId,
            @PathVariable("projectId") Integer projectId,
            @RequestParam(value = "queryName", required = false) String queryName) {
        IPage<UserVO> iPage = projectService.queryUserForAddMember(page, tenantId, projectId, queryName);
        return R.ok(iPage);
    }

    /**
     * project 添加成员
     *
     * @param list userRoleProject集合
     * @return R
     */
    @SysLog(value = "项目添加成员", cloudResType = "项目")
    @PostMapping("/member-add")
    @ApiOperation(value = "project添加成员")
    @ApiImplicitParams(@ApiImplicitParam(name = "list", value = "userProject对象集合", allowMultiple = true, dataType = "UserRoleProject", paramType = "body", required = true))
    public R addMember(@Valid @RequestBody List<UserRoleProject> list) {

        if (list.isEmpty()) {
            return R.failed("参数不能为空");
        }
        Set<Integer> projectIdSet = new HashSet<>();
        Set<Integer> userIdSet = new HashSet<>();
        Integer projectId = null;
        for (UserRoleProject userRoleProject : list) {
            projectId = userRoleProject.getProjectId();
            if (projectId == null) {
                return R.failed("project id参数不能为空");
            }
            projectIdSet.add(projectId);
            Integer userId = userRoleProject.getUserId();
            if (userId == null) {
                return R.failed("用户 id参数不能为空");
            }
            userIdSet.add(userId);
        }
        if (projectIdSet.size() != 1) {
            return R.failed("project id参数错误，应指定同一个项目");
        }
        Project project = projectService.getById(projectId);
        if (project == null) {
            return R.failed("项目不存在");
        }
        if (PROJECT_STATUS_OFF.equals(project.getStatus())) {
            return R.failed("项目已被禁用");
        }
        if (userIdSet.size() < list.size()) {
            return R.failed("用户 id参数有重复");
        }

        for (Integer id : userIdSet) {
            User user = userService.getById(id);
            if (user == null) {
                return R.failed("用户userId = [ " + id + "] 不存在");
            }
            if (USER_STATUS_OFF.equals(user.getStatus())) {
                return R.failed("用户userId = [ " + id + "] 被禁用");
            }
        }

        List<Member> members = projectService.listMember(projectId);
        // project中没有成员 ，直接添加所有人
        if (members.size() > 0) {
            // project中已有成员，如果参数中成员已经在project中

            for (UserRoleProject userRoleProject : list) {
                for (Member member : members) {
                    if (member.getUserId().equals(userRoleProject.getUserId())) {

                        return R.failed("添加失败，用户 id = [" + userRoleProject.getUserId() + "]已经在该project中，不能重复添加");
                    }
                }
            }

        }
        projectService.addMember(list);
        return R.ok();
    }

    /**
     * project删除成员
     *
     * @param userRoleProject userRoleProject
     * @return R
     */
    @SysLog(value = "项目移除成员", cloudResType = "项目", resIdArgIndex = 0, resIdLocation = "arg.projectId")
    @DeleteMapping("/member-remove")
    @ApiOperation(value = "project删除成员")
    @ApiImplicitParams(@ApiImplicitParam(name = "userRoleProject", value = "userRoleProject", dataType = "UserRoleProject", paramType = "body", required = true))
    public R removeMember(@RequestBody UserRoleProject userRoleProject) {
        if (userRoleProject.getProjectId() == null) {
            return R.failed("project id参数不能为空");
        }
        Project project = projectService.getById(userRoleProject.getProjectId());
        if (project == null) {
            return R.failed("项目不存在");
        }
        if (userRoleProject.getUserId() == null) {
            return R.failed("用户 id参数不能为空");
        }
        if (PROJECT_STATUS_OFF.equals(project.getStatus())) {
            return R.failed("项目已被禁用");
        }
        User user = userService.getById(userRoleProject.getUserId());
        if (user == null) {
            return R.failed("用户不存在");
        }
        if (USER_STATUS_OFF.equals(user.getStatus())) {
            return R.failed("用户已被禁用");
        }
        int count = userRoleProjectService.count(new QueryWrapper<UserRoleProject>().lambda()
                .eq(UserRoleProject::getProjectId, userRoleProject.getProjectId())
                .eq(UserRoleProject::getUserId, userRoleProject.getUserId()));
        if (count < 1) {
            return R.failed("用户不在该项目中");
        }
        projectService.removeMember(userRoleProject);
        return R.ok();
    }

    /**
     * 条件查询project列表
     *
     * @return 返回project列表
     */
    @GetMapping("/list")
    @ApiOperation(value = "条件获取project列表")
    @ApiImplicitParams(@ApiImplicitParam(name = "projectDTO", value = "查询条件对象", dataType = "ProjectDTO", paramType = "query"))
    public R getList(ProjectDTO projectDTO) {
        List<Project> list = projectService.listCondition(projectDTO);
        return R.ok(list);
    }

    @PostMapping("/list_inner")
    @ApiOperation(value = "条件获取project列表")
    @ApiImplicitParams(@ApiImplicitParam(name = "projectDTO", value = "查询条件对象", dataType = "ProjectDTO", paramType = "body"))
    public R getInnerProjectList(@RequestBody ProjectDTO projectDTO,
            @RequestHeader(SecurityConstants.FROM) String from) {
        List<Project> list = projectService.listConditionInnerProject(projectDTO, from);
        return R.ok(list);
    }

    /**
     * 分页查询project成员列表
     *
     * @param page 分页条件
     * @param id   项目id
     * @return R
     */
    @GetMapping("/member-list/page/{id}")
    @ApiOperation(value = "分页查询project成员列表")
    @ApiImplicitParams({ @ApiImplicitParam(name = "page", value = "分页条件", dataType = "Page", paramType = "query"),
            @ApiImplicitParam(name = "id", value = "项目id", dataType = "Integer", paramType = "path") })
    public R listMember(Page page, @PathVariable("id") Integer id,
            @RequestParam(value = "name", required = false) String name) {
        if (id == null) {
            return R.failed("project id参数不能为空");
        }
        Project project = projectService.getById(id);
        if (project == null) {
            return R.failed("项目不存在");
        }
        IPage iPage = projectService.listMember(page, id, name);
        return R.ok(iPage);
    }

    /**
     * 查询project成员列表
     *
     * @param id 项目id
     * @return R
     */
    @GetMapping("/member-list/{id}")
    @ApiOperation(value = "查询project成员列表")
    @ApiImplicitParams({ @ApiImplicitParam(name = "id", value = "项目id", dataType = "Integer", paramType = "path") })
    public R<List<Member>> listMember(@PathVariable("id") Integer id) {
        if (id == null) {
            return R.failed("project id参数不能为空");
        }
        Project project = projectService.getById(id);
        if (project == null) {
            return R.failed("项目不存在");
        }
        return R.ok(projectService.listMember(id));
    }

    /**
     * 修改project成员角色
     *
     * @param userRoleProject UserRoleProject对象
     * @return R
     */
    @SysLog(value = "修改项目成员角色", cloudResType = "project", resIdArgIndex = 0, resIdLocation = "arg.projectId")
    @PutMapping("/member-role")
    @ApiOperation(value = "修改project成员角色")
    @ApiImplicitParams(@ApiImplicitParam(name = "userRoleProject", value = "UserRoleProject对象", dataType = "UserRoleProject", paramType = "body"))
    public R updateMemberRole(@RequestBody UserRoleProject userRoleProject) {
        return projectService.updateMemberRole(userRoleProject);
    }

    /**
     * 修改project启用禁用状态
     *
     * @param project project对象
     * @return R
     */
    @SysLog(value = "修改项目启用禁用状态", cloudResType = "project", resIdArgIndex = 0, resIdLocation = "arg.id")
    @ApiOperation(value = "修改project启用禁用状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "project", value = "project对象", dataType = "Project", paramType = "body", required = true) })
    @PutMapping("/status")
    public R updateStatus(@RequestBody Project project) {
        if (project.getId() == null) {
            return R.failed("project id 参数不能为空");
        }
        Project project1 = projectService.getById(project.getId());
        if (null == project1) {
            return R.failed("项目不存在");
        }
        if (null == project.getStatus()) {
            return R.failed("未指定将要修改的状态参数");
        }
        boolean b = project.getStatus().matches("[01]");
        if (!b) {
            return R.failed("参数[status]错误，取值范围字符0或字符1");
        }
        Tenant tenant = tenantService.getById(project1.getTenantId());
        if (TENANT_STATUS_OFF.equals(tenant.getStatus())) {
            return R.failed("项目所属租户被禁用");
        }
        int i = projectService.updateStatus(project);
        return i == 1 ? R.ok() : R.failed("修改状态失败");
    }

    /**
     * 通过TenantId 查询ProjectList列表
     *
     * @param id TenantId
     * @return ProjectList
     */
    @GetMapping("/project-list/{id}")
    @ApiOperation(value = "通过TenantId 查询ProjectList列表")
    @ApiImplicitParams({ @ApiImplicitParam(name = "id", value = "租户id", dataType = "Integer", paramType = "path") })
    public R projectListByTenantId(@PathVariable("id") Integer id) {
        if (null != id) {
            return R.ok(projectService.projectListByTenantId(id));
        }
        return R.failed(BusinessEnum.PARAMETER_NULL);
    }

    @ApiOperation(value = "通过userId,roleId 查询ProjectList列表")
    @ApiImplicitParams({ @ApiImplicitParam(name = "userId", value = "用户Id", dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "roleId", value = "角色Id", dataType = "Integer", paramType = "path") })
    @GetMapping("/by/{userId}/{roleId}")
    public R<List<UserRoleProject>> getProjectBy(@PathVariable("userId") Integer userId,
            @PathVariable("roleId") Integer roleId) {
        List<UserRoleProject> userRoleProjects = userRoleProjectService.list(new QueryWrapper<UserRoleProject>()
                .lambda().eq(UserRoleProject::getUserId, userId).eq(UserRoleProject::getRoleId, roleId));
        return R.ok(userRoleProjects);
    }

    @ApiOperation(value = "异步接口")
    @GetMapping("/syncProject")
    public R syncProject() {
        try {
            projectSyncTask.sync();
        } catch (Throwable th) {
            log.error("sync fail", th);
        }
        return R.ok();
    }

    /**
     * 根据name查询project详情
     *
     * @return R
     */
    @GetMapping("/inclusion/order/project")
    @ApiOperation(value = "根据name查询project详情")
    @ApiImplicitParams(@ApiImplicitParam(name = "name", value = "名称", dataType = "String", paramType = "query"))
    public R findProjectByName(@RequestParam("name") String name) {

        ProjectDTO projectDTO = projectService.findProject(name);
        return R.ok(projectDTO);
    }

    /**
     * 根据name查询project详情
     *
     * @return R
     */
    @PostMapping("/inclusion/order/project")
    @ApiOperation(value = "根据name查询project详情")
    @ApiImplicitParams(@ApiImplicitParam(name = "name", value = "名称", dataType = "String", paramType = "body"))
    public R findProjectByNameWithBody(@RequestBody String name) {
        ProjectDTO projectDTO = projectService.findProject(name);
        return R.ok(projectDTO);
    }

    /**
     * 更新project所属租户
     */
    @SysLog(value = "更新项目所属租户", cloudResType = "项目", resIdArgIndex = 0, resIdLocation = "arg")
    @PutMapping("/update/project/tenant")
    @ApiOperation(value = "更新Project所属租户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectId", value = "projectId", dataType = "Integer", paramType = "query"),
            @ApiImplicitParam(name = "tenantId", value = "租户id", dataType = "Integer", paramType = "query") })
    public R updateProjectTenantId(@RequestParam("projectId") Integer projectId,
            @RequestParam("tenantId") Integer tenantId) {

        // 更新project所属tenantId
        if (null == projectId || null == tenantId) {
            return R.failed("project或tenant Id不能为空");
        }
        ProjectVO projectVO = projectService.selectById(projectId);
        if (null == projectVO) {
            return R.failed("项目不存在");
        }
        TenantVO tenantVO = tenantService.selectTenantVoById(tenantId);
        if (null == tenantVO) {
            return R.failed("vdc不存在");
        }
        int count = projectService.updateProjectTenantId(projectId, tenantId);
        if (count < 1) {
            return R.failed("项目更新失败");
        }
        // 更新配额及计量相关信息
        return null;
    }

    @GetMapping("/keyword")
    @ApiOperation(value = "通过名称查询projectList")
    @ApiImplicitParams(@ApiImplicitParam(name = "name", value = "名称", dataType = "String", paramType = "query"))
    public R<List<Project>> findProjectListBykeyword(@RequestParam("name") String name) {
        log.info("name={}", name);
        final LambdaQueryWrapper<Project> wrapper = Wrappers.<Project>lambdaQuery()
                .like(StringUtils.isNotBlank(name), Project::getName, name)
                .eq(Project::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus());
        return R.ok(projectMapper.selectList(wrapper));
    }

    /**
     * 条件查询project列表(quota使用)
     *
     * @return project列表
     */
    @PostMapping("/list/quota")
    @ApiOperation(value = "条件获取project列表")
    @ApiImplicitParams(@ApiImplicitParam(name = "projectDTO", value = "project对象", dataType = "ProjectDTO", paramType = "body"))
    public R getLists(@RequestBody ProjectDTO projectDTO) {
        List<Project> list = projectService.projectList(projectDTO);
        return R.ok(list);
    }

    /**
     * 查询租project列表(quota使用)
     * 
     * @return project列表
     */
    @GetMapping("/list/quotaForAudit")
    @ApiOperation(value = "获取project列表")
    public R getProjectInfo() {
        List<ProjectVO> list = projectMapper.getProjectListInfo();
        return R.ok(list);
    }
}
