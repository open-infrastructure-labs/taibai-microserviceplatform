package com.taibai.admin.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taibai.admin.api.entity.Role;
import com.taibai.admin.api.entity.RoleGroup;
import com.taibai.admin.api.entity.RoleGroupRole;
import com.taibai.admin.api.validation.Save;
import com.taibai.admin.api.vo.RoleGroupVO;
import com.taibai.admin.service.IRoleGroupRoleService;
import com.taibai.admin.service.IRoleGroupService;
import com.taibai.admin.service.IRoleService;
import com.taibai.common.core.constant.enums.BusinessEnum;
import com.taibai.common.core.util.R;
import com.taibai.common.log.annotation.SysLog;
import com.taibai.webpush.api.feign.RemoteWebpushService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;

/**
 * <p>
 * 角色分组表 前端控制器
 * </p>
 *
 * @author Taibai
 * @since 2019-11-16
 */
@RestController
@AllArgsConstructor
@RequestMapping("/roleGroup")
@Api(value = "roleGroup", tags = "角色分组模块")
public class RoleGroupController {

    private final IRoleService iRoleService;
    private final IRoleGroupService iRoleGroupService;
    private final IRoleGroupRoleService iRoleGroupRelationService;
    private final RemoteWebpushService remoteWebpushService;

    @ApiOperation(value = "分页条件查询角色分组列表")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "query", name = "page", dataType = "Page", required = false, value = "分页对象"),
            @ApiImplicitParam(paramType = "query", name = "roleGroup", dataType = "RoleGroup", required = false, value = "条件对象") })
    @GetMapping("/page")
    public R<Page<RoleGroup>> getRolePage(Page<RoleGroup> page, RoleGroup roleGroup) {
        if (roleGroup != null && roleGroup.getName() != null) {
            return R.ok(iRoleGroupService.page(page, new QueryWrapper<RoleGroup>().lambda()
                    .like(RoleGroup::getName, roleGroup.getName()).orderByDesc(RoleGroup::getCreateTime)));
        } else {
            return R.ok(iRoleGroupService.page(page, Wrappers.query(roleGroup).orderByDesc("create_time")));
        }
    }

    @ApiOperation(value = "获取所有角色分组列表")
    @GetMapping("/list")
    public R<List<RoleGroup>> getRoles() {
        return R.ok(iRoleGroupService.list(new QueryWrapper<RoleGroup>().lambda()));
    }

    @ApiOperation(value = "通过角色id获取角色分组详情")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "id", dataType = "Integer", required = true, value = "角色分组id") })
    @GetMapping("/{id}")
    public R<RoleGroupVO> getByRole(@PathVariable(name = "id") Integer id) {
        if (null != id) {
            RoleGroup roleGroup = iRoleGroupService.getById(id);
            List<Integer> roleIds = new ArrayList<Integer>();
            List<RoleGroupRole> list = iRoleGroupRelationService
                    .list(new QueryWrapper<RoleGroupRole>().lambda().eq(RoleGroupRole::getRoleGroupId, id));
            for (RoleGroupRole roleGroupRelation : list) {
                roleIds.add(roleGroupRelation.getRoleId());
            }
            List<Role> roles = iRoleService.list(new QueryWrapper<Role>().lambda().in(Role::getId, roleIds));
            RoleGroupVO roleGroupVO = JSON.toJavaObject((JSONObject) JSON.toJSON(roleGroup), RoleGroupVO.class);
            roleGroupVO.setRoles(roles);
            return R.ok(roleGroupVO);
        }
        return R.failed(BusinessEnum.PARAMETER_NULL);
    }

    @ApiOperation(value = "通过角色分组名称获取角色分组详情")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "name", dataType = "String", required = true, value = "角色分组名称") })
    @GetMapping("/getByName/{name}")
    public R<RoleGroupVO> getByName(@PathVariable(name = "name") String name) {
        if (null != name) {
            RoleGroup roleGroup = iRoleGroupService
                    .getOne(new QueryWrapper<RoleGroup>().lambda().eq(RoleGroup::getName, name));
            if (roleGroup == null) {
                return R.failed("角色分组不存在");
            }
            List<Integer> roleIds = new ArrayList<Integer>();
            List<RoleGroupRole> list = iRoleGroupRelationService.list(
                    new QueryWrapper<RoleGroupRole>().lambda().eq(RoleGroupRole::getRoleGroupId, roleGroup.getId()));
            for (RoleGroupRole roleGroupRelation : list) {
                roleIds.add(roleGroupRelation.getRoleId());
            }
            List<Role> roles = iRoleService.list(new QueryWrapper<Role>().lambda().in(Role::getId, roleIds));
            RoleGroupVO roleGroupVO = JSON.toJavaObject((JSONObject) JSON.toJSON(roleGroup), RoleGroupVO.class);
            roleGroupVO.setRoles(roles);
            return R.ok(roleGroupVO);
        }
        return R.failed(BusinessEnum.PARAMETER_NULL);
    }

    @SysLog(value = "添加角色分组", cloudResType = "角色分组", resNameArgIndex = 0, resNameLocation = "arg.name")
    @ApiOperation(value = "添加角色分组")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "roleGroupVO", dataType = "RoleGroupVO", required = true, value = "功能对象") })
    @PostMapping
    public R<Boolean> saveRole(@Validated(Save.class) @RequestBody RoleGroupVO roleGroupVO) {
        List<RoleGroup> roleGroups = iRoleGroupService
                .list(new QueryWrapper<RoleGroup>().lambda().eq(RoleGroup::getName, roleGroupVO.getName()));
        if (roleGroups != null && roleGroups.size() > 0) {
            return R.failed(roleGroupVO.getName() + "已存在");
        } else {
            LocalDateTime localDateTime = LocalDateTime.now();
            roleGroupVO.setCreateTime(localDateTime);
            roleGroupVO.setUpdateTime(localDateTime);
            boolean flag = iRoleGroupService.save(roleGroupVO);
            if (flag) {
                RoleGroup roleGroup = iRoleGroupService
                        .getOne(new QueryWrapper<RoleGroup>().lambda().eq(RoleGroup::getName, roleGroupVO.getName()));
                List<Role> roles = roleGroupVO.getRoles();
                for (Role role : roles) {
                    RoleGroupRole roleGroupRelation = new RoleGroupRole();
                    roleGroupRelation.setRoleId(role.getId());
                    roleGroupRelation.setRoleGroupId(roleGroup.getId());
                    roleGroupRelation.setCreateTime(localDateTime);
                    iRoleGroupRelationService.save(roleGroupRelation);
                }
            }
            return R.ok(flag);
        }
    }

    @SysLog(value = "删除角色分组", cloudResType = "角色分组", resIdArgIndex = 0, resIdLocation = "arg")
    @ApiOperation(value = "删除角色分组")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "id", dataType = "Integer", required = true, value = "角色id") })
    @DeleteMapping("/{id}")
    public R<Boolean> deletRole(@PathVariable(name = "id") Integer id) {
        if (null != id) {
            // TODO 没有被使用的角色分组才可以删除
            boolean flag = false;
            flag = remoteWebpushService.getIsUsed(id).getData();
            if (flag) {
                return R.failed("角色分组被消息类型使用，无法删除");
            }
            iRoleGroupRelationService
                    .remove(new QueryWrapper<RoleGroupRole>().lambda().eq(RoleGroupRole::getRoleGroupId, id));
            return R.ok(iRoleGroupService.removeById(id));
        }
        return R.failed(BusinessEnum.PARAMETER_NULL);
    }

    @SysLog(value = "修改角色分组", cloudResType = "角色分组", resNameArgIndex = 0, resNameLocation = "arg.name", resIdArgIndex = 0, resIdLocation = "arg.id")
    @ApiOperation(value = "修改角色分组")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "roleGroupVO", dataType = "RoleGroupVO", required = true, value = "角色分组对象") })
    @PutMapping
    public R<Boolean> updateRole(@Valid @RequestBody RoleGroupVO roleGroupVO) {
        List<RoleGroup> roleGroups = iRoleGroupService
                .list(new QueryWrapper<RoleGroup>().lambda().eq(RoleGroup::getName, roleGroupVO.getName()));
        if (roleGroups != null && roleGroups.size() > 0 && !roleGroups.get(0).getId().equals(roleGroupVO.getId())) {
            return R.failed(roleGroupVO.getName() + "已存在");
        } else {
            iRoleGroupRelationService.remove(
                    new QueryWrapper<RoleGroupRole>().lambda().eq(RoleGroupRole::getRoleGroupId, roleGroupVO.getId()));
            LocalDateTime localDateTime = LocalDateTime.now();
            List<Role> roles = roleGroupVO.getRoles();
            for (Role role : roles) {
                RoleGroupRole roleGroupRelation = new RoleGroupRole();
                roleGroupRelation.setRoleId(role.getId());
                roleGroupRelation.setRoleGroupId(roleGroupVO.getId());
                roleGroupRelation.setCreateTime(localDateTime);
                iRoleGroupRelationService.save(roleGroupRelation);
            }
            roleGroupVO.setUpdateTime(localDateTime);
            RoleGroup roleGroup = roleGroupVO;
            return R.ok(iRoleGroupService.updateById(roleGroup));
        }
    }
}
