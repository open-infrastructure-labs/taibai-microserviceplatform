package com.fitmgr.admin.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
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
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitmgr.activiti.api.feign.RemoteEditorService;
import com.fitmgr.admin.api.dto.SwitchUserVdcDTO;
import com.fitmgr.admin.api.entity.Role;
import com.fitmgr.admin.api.entity.RoleGroupRole;
import com.fitmgr.admin.api.entity.User;
import com.fitmgr.admin.api.validation.Save;
import com.fitmgr.admin.service.IRoleGroupRoleService;
import com.fitmgr.admin.service.IRoleService;
import com.fitmgr.common.core.constant.SecurityConstants;
import com.fitmgr.common.core.constant.enums.BusinessEnum;
import com.fitmgr.common.core.util.R;
import com.fitmgr.common.log.annotation.SysLog;
import com.fitmgr.common.security.service.FitmgrUser;
import com.fitmgr.common.security.util.SecurityUtils;
import com.fitmgr.template.api.feign.RemoteServiceModelService;
import com.fitmgr.webpush.api.feign.RemoteWebpushService;

import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;

/**
 * <p>
 * 角色表 前端控制器
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
@RestController
@AllArgsConstructor
@RequestMapping("/role")
@Api(value = "role", tags = "角色模块")
public class RoleController {

    private final static String SYS_ROLE = "0";

    private final IRoleService iRoleService;
    private final RemoteServiceModelService remoteServiceModelService;
    private final RemoteEditorService remoteEditorService;
    private final RemoteWebpushService remoteWebpushService;
    private final IRoleGroupRoleService iroleGroupRoleService;

    @ApiOperation(value = "分页条件查询角色列表")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "query", name = "page", dataType = "Page", required = false, value = "分页对象"),
            @ApiImplicitParam(paramType = "query", name = "role", dataType = "Role", required = false, value = "条件对象") })
    @GetMapping("/page")
    public R getRolePage(Page page, Role role) {
        return R.ok(iRoleService.page(page, Wrappers.query(role).orderByDesc("create_time")));
    }

    @ApiOperation(value = "获取所有角色列表")
    @GetMapping("/role-list")
    public R getRoles() {
        return R.ok(iRoleService.list(new QueryWrapper<Role>().lambda().eq(Role::getDelFlag, 0)));
    }

    @ApiOperation(value = "获取当前登录用户的角色列表")
    @ApiImplicitParams(value = @ApiImplicitParam(paramType = "query", name = "tenantId", dataType = "Integer", required = false, value = "租户id"))
    @GetMapping("/config-list")
    public R<List<Role>> getRoleList(@RequestParam(value = "tenantId", required = false) Integer tenantId) {
        // 获取当前用户的默认用户id
        FitmgrUser user = SecurityUtils.getUser();
        Integer newTenantId = tenantId != null ? tenantId : user.getDefaultTenantId();
        if (null != user.getId()) {
            return R.ok(iRoleService.getRoleList(user.getId(), newTenantId));
        }
        return R.failed(BusinessEnum.NOT_LOGIN);
    }

    @ApiOperation(value = "当前角色可分配的角色列表")
    @ApiImplicitParams(value = @ApiImplicitParam(paramType = "query", name = "level", dataType = "String", required = false, value = "level"))
    @GetMapping("/list")
    public R getList(@RequestParam(value = "level") String level) {
        // 获取当前用户的默认角色id
        return R.ok(iRoleService.getList(level));
    }

    @ApiOperation(value = "获取所有角色列表")
    @GetMapping("/all/list")
    public R getAllList() {
        // 获取当前用户的默认角色id
        FitmgrUser user = SecurityUtils.getUser();
        if (null != user) {
            return R.ok(iRoleService.getAllList());
        }
        return R.failed(BusinessEnum.NOT_LOGIN);
    }

    @ApiOperation(value = "获取系统角色列表")
    @GetMapping("/sys-list")
    @ApiImplicitParams(value = @ApiImplicitParam(paramType = "query", name = "level", dataType = "Integer", required = false, value = "level"))
    public R getSysList(@RequestParam(value = "level", required = false) Integer level) {
        FitmgrUser user = SecurityUtils.getUser();
        if (null == user) {
            return R.failed(BusinessEnum.NOT_LOGIN);
        }
        // 根据级别过滤
        LambdaQueryWrapper<Role> queryWrapper = Wrappers.<Role>lambdaQuery();
        queryWrapper.eq(Role::getSysRole, 0);
        if (level != null) {
            queryWrapper.eq(Role::getLevel, level);
        }
        return R.ok(iRoleService.list(queryWrapper));

    }

    @ApiOperation(value = "通过角色id获取角色详情")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "id", dataType = "Integer", required = true, value = "角色id") })
    @GetMapping("/{id}")
    public R getByRole(@PathVariable(name = "id") Integer id) {
        if (null != id) {
            return R.ok(iRoleService.getById(id));
        }
        return R.failed(BusinessEnum.PARAMETER_NULL);
    }

    @GetMapping("/inner/{id}")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "id", dataType = "Integer", required = true, value = "角色id") })
    public R getInnerByRole(@PathVariable(name = "id") Integer id, @RequestHeader(SecurityConstants.FROM) String from) {
        if (null != id) {
            return R.ok(iRoleService.getById(id));
        }
        return R.failed(BusinessEnum.PARAMETER_NULL);
    }

    @SysLog(value = "添加角色", cloudResType = "角色", resNameArgIndex = 0, resNameLocation = "arg.roleName")
    @ApiOperation(value = "添加角色")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "role", dataType = "Role", required = true, value = "功能对象") })
    @PostMapping
    public R saveRole(@Validated(Save.class) @RequestBody Role role) {
        return iRoleService.saveRole(role);
    }

    @SysLog(value = "删除角色", cloudResType = "角色", resIdArgIndex = 0, resIdLocation = "arg")
    @ApiOperation(value = "删除角色")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "id", dataType = "Integer", required = true, value = "角色id") })
    @DeleteMapping("/{id}")
    public R deletRole(@PathVariable(name = "id") Integer id) {
        if (null != id) {
            if (iRoleService.countUser(id) > 0) {
                return R.failed(BusinessEnum.ROLE_DELET);
            }
            Role role = iRoleService.getById(id);
            // 模型管理使用了该角色、消息类型使用了该角色 不允许删除
            R r = remoteEditorService.checkRoleById(role.getRoleCode());
            if (r.getCode() == 1) {
                return R.failed("该角色被模型管理占用，无法删除");
            }
            R<Boolean> rw = remoteWebpushService.getRoleUsed(role.getRoleCode());
            if (rw.getData()) {
                return R.failed("该角色被消息类型占用，无法删除");
            }
            // 角色分组使用了该角色
            List<RoleGroupRole> list = iroleGroupRoleService
                    .list(new QueryWrapper<RoleGroupRole>().lambda().eq(RoleGroupRole::getRoleId, id));
            if (list != null && list.size() > 0) {
                return R.failed("该角色被角色分组占用，无法删除");
            }
            if (SYS_ROLE.equals(role.getSysRole())) {
                return R.failed("系统角色不允许删除");
            }
            remoteServiceModelService.deleteModelRole(id);
            iRoleService.deleteRoleById(id);
            return R.ok();
        }
        return R.failed(BusinessEnum.PARAMETER_NULL);
    }

    @SysLog(value = "修改角色", cloudResType = "角色", resNameArgIndex = 0, resNameLocation = "arg.roleName", resIdArgIndex = 0, resIdLocation = "arg.id")
    @ApiOperation(value = "修改角色")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "role", dataType = "Role", required = true, value = "角色对象") })
    @PutMapping
    public R updateRole(@Valid @RequestBody Role role) {
        return iRoleService.updateRole(role);
    }

    @SysLog(value = "切换当前用户默认VDC", cloudResType = "用户", resIdArgIndex = 0, resIdLocation = "arg")
    @ApiOperation(value = "切换当前用户默认VDC")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "query", name = "tenantId", dataType = "Integer", required = true, value = "租户id"),
            @ApiImplicitParam(paramType = "query", name = "roleId", dataType = "Integer", required = true, value = "角色id"),
            @ApiImplicitParam(paramType = "header", name = "authHeader", dataType = "String", required = true, value = "authHeader") })
    @PutMapping("/switch-role")
    public R switchRole(@RequestParam(name = "tenantId") Integer tenantId,
            @RequestParam(name = "roleId") Integer roleId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        // 获取当前用户的默认角色id
        FitmgrUser user = SecurityUtils.getUser();
        if (StrUtil.isBlank(authHeader) && null != user.getId()) {
            return R.failed(BusinessEnum.NOT_LOGIN);
        }
        User user1 = new User();
        user1.setId(user.getId());
        user1.setUsername(user.getUsername());
        user1.setDefaultTenantId(tenantId);
        String token = authHeader.replace(OAuth2AccessToken.BEARER_TYPE, StrUtil.EMPTY).trim();
        return R.ok(iRoleService.switchRole(token, user1, roleId));
    }

    @PostMapping("/switch-user-vdc")
    public R switchUserVdc(@RequestBody SwitchUserVdcDTO switchUserVdcDTO) {
        return iRoleService.switchUserVdc(switchUserVdcDTO);
    }

    /**
     * 获取所有跟project相关角色列表
     *
     * @return R
     */
    @GetMapping("/project-role-list")
    @ApiOperation(value = "获取所有跟project相关角色列表")
    public R projectRoleList() {
        return iRoleService.projectRoleList();
    }

    @ApiOperation(value = "通过用户id，projectId获取当前用户在project的所属角色")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "query", name = "userId", dataType = "Integer", required = true, value = "用户id"),
            @ApiImplicitParam(paramType = "query", name = "projectId", dataType = "Integer", required = true, value = "项目id") })
    @GetMapping("/role-list/byTwoId")
    public R getRoleListByTwoId(@RequestParam(name = "userId") Integer userId,
            @RequestParam(name = "projectId") Integer projectId) {
        if (null != userId && null != projectId) {
            List<Role> roleListByTwoId = iRoleService.getRoleListByTwoId(userId, projectId);
            return R.ok(roleListByTwoId);
        }
        return R.failed(BusinessEnum.PARAMETER_NULL);
    }
}
