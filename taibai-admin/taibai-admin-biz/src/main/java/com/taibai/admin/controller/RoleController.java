package com.taibai.admin.controller;

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
import com.taibai.activiti.api.feign.RemoteEditorService;
import com.taibai.admin.api.dto.SwitchUserVdcDTO;
import com.taibai.admin.api.entity.Role;
import com.taibai.admin.api.entity.RoleGroupRole;
import com.taibai.admin.api.entity.User;
import com.taibai.admin.api.validation.Save;
import com.taibai.admin.service.IRoleGroupRoleService;
import com.taibai.admin.service.IRoleService;
import com.taibai.common.core.constant.SecurityConstants;
import com.taibai.common.core.constant.enums.BusinessEnum;
import com.taibai.common.core.util.R;
import com.taibai.common.log.annotation.SysLog;
import com.taibai.common.security.service.FitmgrUser;
import com.taibai.common.security.util.SecurityUtils;
import com.taibai.template.api.feign.RemoteServiceModelService;
import com.taibai.webpush.api.feign.RemoteWebpushService;

import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;

/**
 * <p>
 * ????????? ???????????????
 * </p>
 *
 * @author Taibai
 * @since 2019-11-16
 */
@RestController
@AllArgsConstructor
@RequestMapping("/role")
@Api(value = "role", tags = "????????????")
public class RoleController {

    private final static String SYS_ROLE = "0";

    private final IRoleService iRoleService;
    private final RemoteServiceModelService remoteServiceModelService;
    private final RemoteEditorService remoteEditorService;
    private final RemoteWebpushService remoteWebpushService;
    private final IRoleGroupRoleService iroleGroupRoleService;

    @ApiOperation(value = "??????????????????????????????")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "query", name = "page", dataType = "Page", required = false, value = "????????????"),
            @ApiImplicitParam(paramType = "query", name = "role", dataType = "Role", required = false, value = "????????????") })
    @GetMapping("/page")
    public R getRolePage(Page page, Role role) {
        return R.ok(iRoleService.page(page, Wrappers.query(role).orderByDesc("create_time")));
    }

    @ApiOperation(value = "????????????????????????")
    @GetMapping("/role-list")
    public R getRoles() {
        return R.ok(iRoleService.list(new QueryWrapper<Role>().lambda().eq(Role::getDelFlag, 0)));
    }

    @ApiOperation(value = "???????????????????????????????????????")
    @ApiImplicitParams(value = @ApiImplicitParam(paramType = "query", name = "tenantId", dataType = "Integer", required = false, value = "??????id"))
    @GetMapping("/config-list")
    public R<List<Role>> getRoleList(@RequestParam(value = "tenantId", required = false) Integer tenantId) {
        // ?????????????????????????????????id
        FitmgrUser user = SecurityUtils.getUser();
        Integer newTenantId = tenantId != null ? tenantId : user.getDefaultTenantId();
        if (null != user.getId()) {
            return R.ok(iRoleService.getRoleList(user.getId(), newTenantId));
        }
        return R.failed(BusinessEnum.NOT_LOGIN);
    }

    @ApiOperation(value = "????????????????????????????????????")
    @ApiImplicitParams(value = @ApiImplicitParam(paramType = "query", name = "level", dataType = "String", required = false, value = "level"))
    @GetMapping("/list")
    public R getList(@RequestParam(value = "level") String level) {
        // ?????????????????????????????????id
        return R.ok(iRoleService.getList(level));
    }

    @ApiOperation(value = "????????????????????????")
    @GetMapping("/all/list")
    public R getAllList() {
        // ?????????????????????????????????id
        FitmgrUser user = SecurityUtils.getUser();
        if (null != user) {
            return R.ok(iRoleService.getAllList());
        }
        return R.failed(BusinessEnum.NOT_LOGIN);
    }

    @ApiOperation(value = "????????????????????????")
    @GetMapping("/sys-list")
    @ApiImplicitParams(value = @ApiImplicitParam(paramType = "query", name = "level", dataType = "Integer", required = false, value = "level"))
    public R getSysList(@RequestParam(value = "level", required = false) Integer level) {
        FitmgrUser user = SecurityUtils.getUser();
        if (null == user) {
            return R.failed(BusinessEnum.NOT_LOGIN);
        }
        // ??????????????????
        LambdaQueryWrapper<Role> queryWrapper = Wrappers.<Role>lambdaQuery();
        queryWrapper.eq(Role::getSysRole, 0);
        if (level != null) {
            queryWrapper.eq(Role::getLevel, level);
        }
        return R.ok(iRoleService.list(queryWrapper));

    }

    @ApiOperation(value = "????????????id??????????????????")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "id", dataType = "Integer", required = true, value = "??????id") })
    @GetMapping("/{id}")
    public R getByRole(@PathVariable(name = "id") Integer id) {
        if (null != id) {
            return R.ok(iRoleService.getById(id));
        }
        return R.failed(BusinessEnum.PARAMETER_NULL);
    }

    @GetMapping("/inner/{id}")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "id", dataType = "Integer", required = true, value = "??????id") })
    public R getInnerByRole(@PathVariable(name = "id") Integer id, @RequestHeader(SecurityConstants.FROM) String from) {
        if (null != id) {
            return R.ok(iRoleService.getById(id));
        }
        return R.failed(BusinessEnum.PARAMETER_NULL);
    }

    @SysLog(value = "????????????", cloudResType = "??????", resNameArgIndex = 0, resNameLocation = "arg.roleName")
    @ApiOperation(value = "????????????")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "role", dataType = "Role", required = true, value = "????????????") })
    @PostMapping
    public R saveRole(@Validated(Save.class) @RequestBody Role role) {
        return iRoleService.saveRole(role);
    }

    @SysLog(value = "????????????", cloudResType = "??????", resIdArgIndex = 0, resIdLocation = "arg")
    @ApiOperation(value = "????????????")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "id", dataType = "Integer", required = true, value = "??????id") })
    @DeleteMapping("/{id}")
    public R deletRole(@PathVariable(name = "id") Integer id) {
        if (null != id) {
            if (iRoleService.countUser(id) > 0) {
                return R.failed(BusinessEnum.ROLE_DELET);
            }
            Role role = iRoleService.getById(id);
            // ??????????????????????????????????????????????????????????????? ???????????????
            R r = remoteEditorService.checkRoleById(role.getRoleCode());
            if (r.getCode() == 1) {
                return R.failed("?????????????????????????????????????????????");
            }
            R<Boolean> rw = remoteWebpushService.getRoleUsed(role.getRoleCode());
            if (rw.getData()) {
                return R.failed("?????????????????????????????????????????????");
            }
            // ??????????????????????????????
            List<RoleGroupRole> list = iroleGroupRoleService
                    .list(new QueryWrapper<RoleGroupRole>().lambda().eq(RoleGroupRole::getRoleId, id));
            if (list != null && list.size() > 0) {
                return R.failed("?????????????????????????????????????????????");
            }
            if (SYS_ROLE.equals(role.getSysRole())) {
                return R.failed("???????????????????????????");
            }
            remoteServiceModelService.deleteModelRole(id);
            iRoleService.deleteRoleById(id);
            return R.ok();
        }
        return R.failed(BusinessEnum.PARAMETER_NULL);
    }

    @SysLog(value = "????????????", cloudResType = "??????", resNameArgIndex = 0, resNameLocation = "arg.roleName", resIdArgIndex = 0, resIdLocation = "arg.id")
    @ApiOperation(value = "????????????")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "role", dataType = "Role", required = true, value = "????????????") })
    @PutMapping
    public R updateRole(@Valid @RequestBody Role role) {
        return iRoleService.updateRole(role);
    }

    @SysLog(value = "????????????????????????VDC", cloudResType = "??????", resIdArgIndex = 0, resIdLocation = "arg")
    @ApiOperation(value = "????????????????????????VDC")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "query", name = "tenantId", dataType = "Integer", required = true, value = "??????id"),
            @ApiImplicitParam(paramType = "query", name = "roleId", dataType = "Integer", required = true, value = "??????id"),
            @ApiImplicitParam(paramType = "header", name = "authHeader", dataType = "String", required = true, value = "authHeader") })
    @PutMapping("/switch-role")
    public R switchRole(@RequestParam(name = "tenantId") Integer tenantId,
            @RequestParam(name = "roleId") Integer roleId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        // ?????????????????????????????????id
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
     * ???????????????project??????????????????
     *
     * @return R
     */
    @GetMapping("/project-role-list")
    @ApiOperation(value = "???????????????project??????????????????")
    public R projectRoleList() {
        return iRoleService.projectRoleList();
    }

    @ApiOperation(value = "????????????id???projectId?????????????????????project???????????????")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "query", name = "userId", dataType = "Integer", required = true, value = "??????id"),
            @ApiImplicitParam(paramType = "query", name = "projectId", dataType = "Integer", required = true, value = "??????id") })
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
