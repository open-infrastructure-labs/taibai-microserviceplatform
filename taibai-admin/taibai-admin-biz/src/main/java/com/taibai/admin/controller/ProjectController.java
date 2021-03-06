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
 * ????????? mhp ???????????? 2019/11/15 ??????
 **/
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/project")
@Api(value = "project", tags = "??????????????????")
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
     * ??????project
     *
     * @param projectDTO DTO??????
     * @return R
     */
    @SysLog(value = "????????????", cloudResType = "??????", resNameArgIndex = 0, resNameLocation = "arg.name")
    @PostMapping
    @ApiOperation(value = "??????project")
    @ApiImplicitParams(@ApiImplicitParam(name = "projectDTO", value = "??????projectDTO??????", dataType = "ProjectDTO", paramType = "body", required = true))
    public R saveProject(@Validated(Save.class) @RequestBody ProjectDTO projectDTO) {
        // ?????????????????????????????????project
        Tenant tenant = tenantService.getById(projectDTO.getTenantId());
        if (tenant == null) {
            return R.failed("?????????????????????");
        }
        if (TENANT_STATUS_OFF.equals(tenant.getStatus())) {
            return R.failed("??????????????????");
        }
        if (TENANT_CREAT_PROJECT_OFF.equals(tenant.getCreateProject())) {
            return R.failed("??????????????????project");
        }

        int count = projectService.count(new QueryWrapper<Project>().eq("name", projectDTO.getName()));
        if (count > 0) {
            return R.failed("??????????????????");
        }
        boolean b = projectService.saveProject(projectDTO);
        return b ? R.ok() : R.failed();
    }

    /**
     * ??????id??????project
     *
     * @param id projectId
     * @return R
     */
    @SysLog(value = "????????????", cloudResType = "??????", resIdArgIndex = 0, resIdLocation = "arg")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "??????project")
    @ApiImplicitParams(@ApiImplicitParam(name = "id", value = "projectId", dataType = "Integer", paramType = "path", required = true))
    public R deleteProject(@PathVariable("id") Integer id) {
        if (id == null) {
            return R.failed("id??????????????????");
        }
        int count = projectService.count(new QueryWrapper<Project>().lambda().eq(Project::getId, id));
        if (count < 1) {
            return R.failed("???????????????");
        }
        boolean b = projectService.deleteProject(id);
        return b ? R.ok() : R.failed();
    }

    /**
     * ??????project
     *
     * @param projectDTO DTO??????
     * @return R
     */
    @SysLog(value = "????????????", cloudResType = "??????", resIdArgIndex = 0, resIdLocation = "arg.id", resNameArgIndex = 0, resNameLocation = "arg.name")
    @PutMapping
    @ApiOperation(value = "??????project")
    @ApiImplicitParams(@ApiImplicitParam(name = "projectDTO", value = "projectDTO??????", dataType = "ProjectDTO", paramType = "body", required = true))
    public R updateProject(@Validated(Update.class) @RequestBody ProjectDTO projectDTO) {
        int count = projectService.count(new QueryWrapper<Project>().lambda().eq(Project::getId, projectDTO.getId()));
        if (count < 1) {
            return R.failed("???????????????");
        }

        Project project = projectService.getOne(new QueryWrapper<Project>().eq("name", projectDTO.getName()));
        if (project != null && !project.getId().equals(projectDTO.getId())) {
            return R.failed("??????????????????");
        }

        return projectService.updateProject(projectDTO);
    }

    /**
     * ??????id??????projectVO
     *
     * @param id projectId
     * @return R
     */
    @GetMapping("/detail/{id}")
    @ApiOperation(value = "??????id??????project??????")
    @ApiImplicitParams(@ApiImplicitParam(name = "id", value = "??????id", dataType = "Integer", paramType = "path", required = true))
    public R selectProject(@PathVariable("id") Integer id) {
        if (id == null) {
            return R.failed("id??????????????????");
        }
        ProjectVO projectVO = projectService.selectById(id);
        return projectVO == null ? R.failed("project?????????") : R.ok(projectVO);
    }

    /**
     * ??????????????????project????????????
     *
     * @param page       ??????????????????
     * @param projectDTO DTO??????
     * @return R
     */
    @GetMapping("/list/page")
    @ApiOperation(value = "??????????????????project????????????")
    @ApiImplicitParams({ @ApiImplicitParam(name = "page", value = "??????????????????", dataType = "Page", paramType = "query"),
            @ApiImplicitParam(name = "projectDTO", value = "??????????????????", dataType = "ProjectDTO", paramType = "query") })
    public R pageList(Page page, ProjectDTO projectDTO) {

        // ?????????????????????????????????id
        FitmgrUser user = SecurityUtils.getUser();
        IPage<ProjectVO> iPage = projectService.pageList(page, projectDTO, user.getId());
        return R.ok(iPage);
    }

    @GetMapping("/member-add/users/{tenantId}/{projectId}")
    @ApiOperation(value = "??????????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tenantId", value = "??????id", allowMultiple = true, dataType = "Integer", paramType = "path", required = true),
            @ApiImplicitParam(name = "projectId", value = "projectId", allowMultiple = true, dataType = "Integer", paramType = "path", required = true),
            @ApiImplicitParam(name = "queryName", value = "queryName", allowMultiple = true, dataType = "String", paramType = "String", required = true) })
    public R addMemberQueryUser(Page page, @PathVariable("tenantId") Integer tenantId,
            @PathVariable("projectId") Integer projectId,
            @RequestParam(value = "queryName", required = false) String queryName) {
        IPage<UserVO> iPage = projectService.queryUserForAddMember(page, tenantId, projectId, queryName);
        return R.ok(iPage);
    }

    /**
     * project ????????????
     *
     * @param list userRoleProject??????
     * @return R
     */
    @SysLog(value = "??????????????????", cloudResType = "??????")
    @PostMapping("/member-add")
    @ApiOperation(value = "project????????????")
    @ApiImplicitParams(@ApiImplicitParam(name = "list", value = "userProject????????????", allowMultiple = true, dataType = "UserRoleProject", paramType = "body", required = true))
    public R addMember(@Valid @RequestBody List<UserRoleProject> list) {

        if (list.isEmpty()) {
            return R.failed("??????????????????");
        }
        Set<Integer> projectIdSet = new HashSet<>();
        Set<Integer> userIdSet = new HashSet<>();
        Integer projectId = null;
        for (UserRoleProject userRoleProject : list) {
            projectId = userRoleProject.getProjectId();
            if (projectId == null) {
                return R.failed("project id??????????????????");
            }
            projectIdSet.add(projectId);
            Integer userId = userRoleProject.getUserId();
            if (userId == null) {
                return R.failed("?????? id??????????????????");
            }
            userIdSet.add(userId);
        }
        if (projectIdSet.size() != 1) {
            return R.failed("project id???????????????????????????????????????");
        }
        Project project = projectService.getById(projectId);
        if (project == null) {
            return R.failed("???????????????");
        }
        if (PROJECT_STATUS_OFF.equals(project.getStatus())) {
            return R.failed("??????????????????");
        }
        if (userIdSet.size() < list.size()) {
            return R.failed("?????? id???????????????");
        }

        for (Integer id : userIdSet) {
            User user = userService.getById(id);
            if (user == null) {
                return R.failed("??????userId = [ " + id + "] ?????????");
            }
            if (USER_STATUS_OFF.equals(user.getStatus())) {
                return R.failed("??????userId = [ " + id + "] ?????????");
            }
        }

        List<Member> members = projectService.listMember(projectId);
        // project??????????????? ????????????????????????
        if (members.size() > 0) {
            // project????????????????????????????????????????????????project???

            for (UserRoleProject userRoleProject : list) {
                for (Member member : members) {
                    if (member.getUserId().equals(userRoleProject.getUserId())) {

                        return R.failed("????????????????????? id = [" + userRoleProject.getUserId() + "]????????????project????????????????????????");
                    }
                }
            }

        }
        projectService.addMember(list);
        return R.ok();
    }

    /**
     * project????????????
     *
     * @param userRoleProject userRoleProject
     * @return R
     */
    @SysLog(value = "??????????????????", cloudResType = "??????", resIdArgIndex = 0, resIdLocation = "arg.projectId")
    @DeleteMapping("/member-remove")
    @ApiOperation(value = "project????????????")
    @ApiImplicitParams(@ApiImplicitParam(name = "userRoleProject", value = "userRoleProject", dataType = "UserRoleProject", paramType = "body", required = true))
    public R removeMember(@RequestBody UserRoleProject userRoleProject) {
        if (userRoleProject.getProjectId() == null) {
            return R.failed("project id??????????????????");
        }
        Project project = projectService.getById(userRoleProject.getProjectId());
        if (project == null) {
            return R.failed("???????????????");
        }
        if (userRoleProject.getUserId() == null) {
            return R.failed("?????? id??????????????????");
        }
        if (PROJECT_STATUS_OFF.equals(project.getStatus())) {
            return R.failed("??????????????????");
        }
        User user = userService.getById(userRoleProject.getUserId());
        if (user == null) {
            return R.failed("???????????????");
        }
        if (USER_STATUS_OFF.equals(user.getStatus())) {
            return R.failed("??????????????????");
        }
        int count = userRoleProjectService.count(new QueryWrapper<UserRoleProject>().lambda()
                .eq(UserRoleProject::getProjectId, userRoleProject.getProjectId())
                .eq(UserRoleProject::getUserId, userRoleProject.getUserId()));
        if (count < 1) {
            return R.failed("????????????????????????");
        }
        projectService.removeMember(userRoleProject);
        return R.ok();
    }

    /**
     * ????????????project??????
     *
     * @return ??????project??????
     */
    @GetMapping("/list")
    @ApiOperation(value = "????????????project??????")
    @ApiImplicitParams(@ApiImplicitParam(name = "projectDTO", value = "??????????????????", dataType = "ProjectDTO", paramType = "query"))
    public R getList(ProjectDTO projectDTO) {
        List<Project> list = projectService.listCondition(projectDTO);
        return R.ok(list);
    }

    @PostMapping("/list_inner")
    @ApiOperation(value = "????????????project??????")
    @ApiImplicitParams(@ApiImplicitParam(name = "projectDTO", value = "??????????????????", dataType = "ProjectDTO", paramType = "body"))
    public R getInnerProjectList(@RequestBody ProjectDTO projectDTO,
            @RequestHeader(SecurityConstants.FROM) String from) {
        List<Project> list = projectService.listConditionInnerProject(projectDTO, from);
        return R.ok(list);
    }

    /**
     * ????????????project????????????
     *
     * @param page ????????????
     * @param id   ??????id
     * @return R
     */
    @GetMapping("/member-list/page/{id}")
    @ApiOperation(value = "????????????project????????????")
    @ApiImplicitParams({ @ApiImplicitParam(name = "page", value = "????????????", dataType = "Page", paramType = "query"),
            @ApiImplicitParam(name = "id", value = "??????id", dataType = "Integer", paramType = "path") })
    public R listMember(Page page, @PathVariable("id") Integer id,
            @RequestParam(value = "name", required = false) String name) {
        if (id == null) {
            return R.failed("project id??????????????????");
        }
        Project project = projectService.getById(id);
        if (project == null) {
            return R.failed("???????????????");
        }
        IPage iPage = projectService.listMember(page, id, name);
        return R.ok(iPage);
    }

    /**
     * ??????project????????????
     *
     * @param id ??????id
     * @return R
     */
    @GetMapping("/member-list/{id}")
    @ApiOperation(value = "??????project????????????")
    @ApiImplicitParams({ @ApiImplicitParam(name = "id", value = "??????id", dataType = "Integer", paramType = "path") })
    public R<List<Member>> listMember(@PathVariable("id") Integer id) {
        if (id == null) {
            return R.failed("project id??????????????????");
        }
        Project project = projectService.getById(id);
        if (project == null) {
            return R.failed("???????????????");
        }
        return R.ok(projectService.listMember(id));
    }

    /**
     * ??????project????????????
     *
     * @param userRoleProject UserRoleProject??????
     * @return R
     */
    @SysLog(value = "????????????????????????", cloudResType = "project", resIdArgIndex = 0, resIdLocation = "arg.projectId")
    @PutMapping("/member-role")
    @ApiOperation(value = "??????project????????????")
    @ApiImplicitParams(@ApiImplicitParam(name = "userRoleProject", value = "UserRoleProject??????", dataType = "UserRoleProject", paramType = "body"))
    public R updateMemberRole(@RequestBody UserRoleProject userRoleProject) {
        return projectService.updateMemberRole(userRoleProject);
    }

    /**
     * ??????project??????????????????
     *
     * @param project project??????
     * @return R
     */
    @SysLog(value = "??????????????????????????????", cloudResType = "project", resIdArgIndex = 0, resIdLocation = "arg.id")
    @ApiOperation(value = "??????project??????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "project", value = "project??????", dataType = "Project", paramType = "body", required = true) })
    @PutMapping("/status")
    public R updateStatus(@RequestBody Project project) {
        if (project.getId() == null) {
            return R.failed("project id ??????????????????");
        }
        Project project1 = projectService.getById(project.getId());
        if (null == project1) {
            return R.failed("???????????????");
        }
        if (null == project.getStatus()) {
            return R.failed("????????????????????????????????????");
        }
        boolean b = project.getStatus().matches("[01]");
        if (!b) {
            return R.failed("??????[status]???????????????????????????0?????????1");
        }
        Tenant tenant = tenantService.getById(project1.getTenantId());
        if (TENANT_STATUS_OFF.equals(tenant.getStatus())) {
            return R.failed("???????????????????????????");
        }
        int i = projectService.updateStatus(project);
        return i == 1 ? R.ok() : R.failed("??????????????????");
    }

    /**
     * ??????TenantId ??????ProjectList??????
     *
     * @param id TenantId
     * @return ProjectList
     */
    @GetMapping("/project-list/{id}")
    @ApiOperation(value = "??????TenantId ??????ProjectList??????")
    @ApiImplicitParams({ @ApiImplicitParam(name = "id", value = "??????id", dataType = "Integer", paramType = "path") })
    public R projectListByTenantId(@PathVariable("id") Integer id) {
        if (null != id) {
            return R.ok(projectService.projectListByTenantId(id));
        }
        return R.failed(BusinessEnum.PARAMETER_NULL);
    }

    @ApiOperation(value = "??????userId,roleId ??????ProjectList??????")
    @ApiImplicitParams({ @ApiImplicitParam(name = "userId", value = "??????Id", dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "roleId", value = "??????Id", dataType = "Integer", paramType = "path") })
    @GetMapping("/by/{userId}/{roleId}")
    public R<List<UserRoleProject>> getProjectBy(@PathVariable("userId") Integer userId,
            @PathVariable("roleId") Integer roleId) {
        List<UserRoleProject> userRoleProjects = userRoleProjectService.list(new QueryWrapper<UserRoleProject>()
                .lambda().eq(UserRoleProject::getUserId, userId).eq(UserRoleProject::getRoleId, roleId));
        return R.ok(userRoleProjects);
    }

    @ApiOperation(value = "????????????")
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
     * ??????name??????project??????
     *
     * @return R
     */
    @GetMapping("/inclusion/order/project")
    @ApiOperation(value = "??????name??????project??????")
    @ApiImplicitParams(@ApiImplicitParam(name = "name", value = "??????", dataType = "String", paramType = "query"))
    public R findProjectByName(@RequestParam("name") String name) {

        ProjectDTO projectDTO = projectService.findProject(name);
        return R.ok(projectDTO);
    }

    /**
     * ??????name??????project??????
     *
     * @return R
     */
    @PostMapping("/inclusion/order/project")
    @ApiOperation(value = "??????name??????project??????")
    @ApiImplicitParams(@ApiImplicitParam(name = "name", value = "??????", dataType = "String", paramType = "body"))
    public R findProjectByNameWithBody(@RequestBody String name) {
        ProjectDTO projectDTO = projectService.findProject(name);
        return R.ok(projectDTO);
    }

    /**
     * ??????project????????????
     */
    @SysLog(value = "????????????????????????", cloudResType = "??????", resIdArgIndex = 0, resIdLocation = "arg")
    @PutMapping("/update/project/tenant")
    @ApiOperation(value = "??????Project????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectId", value = "projectId", dataType = "Integer", paramType = "query"),
            @ApiImplicitParam(name = "tenantId", value = "??????id", dataType = "Integer", paramType = "query") })
    public R updateProjectTenantId(@RequestParam("projectId") Integer projectId,
            @RequestParam("tenantId") Integer tenantId) {

        // ??????project??????tenantId
        if (null == projectId || null == tenantId) {
            return R.failed("project???tenant Id????????????");
        }
        ProjectVO projectVO = projectService.selectById(projectId);
        if (null == projectVO) {
            return R.failed("???????????????");
        }
        TenantVO tenantVO = tenantService.selectTenantVoById(tenantId);
        if (null == tenantVO) {
            return R.failed("vdc?????????");
        }
        int count = projectService.updateProjectTenantId(projectId, tenantId);
        if (count < 1) {
            return R.failed("??????????????????");
        }
        // ?????????????????????????????????
        return null;
    }

    @GetMapping("/keyword")
    @ApiOperation(value = "??????????????????projectList")
    @ApiImplicitParams(@ApiImplicitParam(name = "name", value = "??????", dataType = "String", paramType = "query"))
    public R<List<Project>> findProjectListBykeyword(@RequestParam("name") String name) {
        log.info("name={}", name);
        final LambdaQueryWrapper<Project> wrapper = Wrappers.<Project>lambdaQuery()
                .like(StringUtils.isNotBlank(name), Project::getName, name)
                .eq(Project::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus());
        return R.ok(projectMapper.selectList(wrapper));
    }

    /**
     * ????????????project??????(quota??????)
     *
     * @return project??????
     */
    @PostMapping("/list/quota")
    @ApiOperation(value = "????????????project??????")
    @ApiImplicitParams(@ApiImplicitParam(name = "projectDTO", value = "project??????", dataType = "ProjectDTO", paramType = "body"))
    public R getLists(@RequestBody ProjectDTO projectDTO) {
        List<Project> list = projectService.projectList(projectDTO);
        return R.ok(list);
    }

    /**
     * ?????????project??????(quota??????)
     * 
     * @return project??????
     */
    @GetMapping("/list/quotaForAudit")
    @ApiOperation(value = "??????project??????")
    public R getProjectInfo() {
        List<ProjectVO> list = projectMapper.getProjectListInfo();
        return R.ok(list);
    }
}
