package com.taibai.admin.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taibai.admin.api.entity.Auth;
import com.taibai.admin.api.entity.AuthCheck;
import com.taibai.admin.api.entity.ResInfo;
import com.taibai.admin.api.vo.AuthCheckVO;
import com.taibai.admin.api.vo.AuthVO;
import com.taibai.admin.service.IAuthService;
import com.taibai.admin.service.IUserService;
import com.taibai.common.core.constant.enums.BusinessEnum;
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
 * <p>
 * 操作权限表 前端控制器check-by-url-method
 * </p>
 *
 * @author Taibai
 * @since 2019-11-14
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/auth")
@Api(value = "auth", tags = "权限模块")
public class AuthController {

    private final IAuthService iAuthService;
    private final IUserService userService;

    /**
     * 当前用户通过菜单id获取所有操作的功能权限+数据权限
     *
     * @param menuId 菜单id
     * @return
     */
    @ApiOperation(value = "当前用户通过菜单id获取所有操作的功能权限+数据权限")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "menuId", dataType = "String", required = true, value = "菜单id") })
    @GetMapping("/menu-auth/{menuId}")
    public R getMenuByAuth(@PathVariable String menuId) {
        // 获取当前用户的默认角色id
        FitmgrUser user = SecurityUtils.getUser();
        if (null != user) {
            return R.ok(iAuthService.getMenuByAuth(menuId));
        }
        return R.failed(BusinessEnum.NOT_LOGIN);
    }

    /**
     * 通过操作id和角色id获取所有权限
     *
     * @param functionId 操作id
     * @param roleId     角色id
     * @return
     */
    @ApiOperation(value = "通过操作id和角色id获取所有权限")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "query", name = "functionId", dataType = "Integer", required = true, value = "操作id"),
            @ApiImplicitParam(paramType = "query", name = "roleId", dataType = "Integer", required = true, value = "角色id") })
    @GetMapping("/role-menu")
    public R getRoleMenuByAuth(@RequestParam(name = "functionId") Integer functionId,
            @RequestParam(name = "roleId") Integer roleId) {
        AuthVO roleMenuByAuth = iAuthService.getRoleMenuByAuth(functionId, roleId);
        if (roleMenuByAuth != null) {
            return R.ok(roleMenuByAuth);
        }
        return R.ok(new AuthVO());
    }

    @ApiOperation(value = "当前用户通过操作code获取功能权限和数据范围")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "code", dataType = "String", required = true, value = "操作code") })
    @GetMapping("/code-auth/{code}")
    public R getUserAuth(@PathVariable(name = "code") String code) {
        // 获取当前用户的默认角色id
        FitmgrUser user = SecurityUtils.getUser();
        if (null != user.getId()) {
            return R.ok(iAuthService.getUserAuth(code, user.getId()));
        }
        return R.failed(BusinessEnum.NOT_LOGIN);
    }

    @ApiOperation(value = "当前用户通过操作id查询所有权限")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "functionId", dataType = "Integer", required = true, value = "操作id") })
    @GetMapping("/function/{functionId}")
    public R getAuthByFunctionId(@PathVariable(name = "functionId") Integer functionId) {
        // 获取当前用户的默认角色id
        FitmgrUser user = SecurityUtils.getUser();
        if (null != user) {
            List<Integer> roleIds = new ArrayList<Integer>();
            List<Map<String, Integer>> roleList = userService.queryRoleByUserIdAndTenantId(user.getId(),
                    user.getDefaultTenantId());
            for (Map<String, Integer> map : roleList) {
                roleIds.add(map.get("role_id"));
            }
            return R.ok(iAuthService.getFunctionIdByAuths(functionId, roleIds));
        }
        return R.failed(BusinessEnum.NOT_LOGIN);
    }

    @SysLog(value = "通过角色id配置功能权限", cloudResType = "权限", resIdArgIndex = 0, resIdLocation = "arg.roleId")
    @ApiOperation(value = "通过角色id配置功能权限")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "auth", dataType = "Auth", required = true, value = "权限集合") })
    @PostMapping
    public R saveAuth(@RequestBody Auth auth) {
        return iAuthService.saveAuth(auth);
    }

    /**
     * api接口的数据权限校验
     *
     * @param functionCode 操作唯一编码
     * @return
     */
    @ApiOperation(value = "api接口的数据权限校验")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "functionCode", dataType = "String", required = true, value = "操作唯一编码") })
    @GetMapping("/auth-check/{functionCode}")
    public R authCheck(@PathVariable(name = "functionCode") String functionCode) {
        // 获取当前用户的默认角色id
        FitmgrUser user = SecurityUtils.getUser();
        if (null != user) {
            return iAuthService.authCheck(functionCode, user.getId());
        }
        return R.failed(BusinessEnum.NOT_LOGIN);
    }

    @GetMapping("/new-auth-check/{functionCode}")
    public R<AuthCheck> newAuthCheck(@PathVariable(name = "functionCode") String functionCode,
            @RequestParam(name = "resTenantId", required = false) Integer resTenantId,
            @RequestParam(name = "resProjectId", required = false) Integer resProjectId,
            @RequestParam(name = "resUserId", required = false) Integer resUserId) {
        // 获取当前用户的默认角色id
        FitmgrUser user = SecurityUtils.getUser();
        if (null != user) {
            return iAuthService.newAuthCheck(functionCode, user.getId(), user.getDefaultTenantId(), resTenantId,
                    resProjectId, resUserId);
        }
        return R.failed(BusinessEnum.NOT_LOGIN);
    }

    @PostMapping("/new-auth-checks/{functionCode}")
    public R<List<AuthCheckVO>> newAuthChecks(@PathVariable(name = "functionCode") String functionCode,
            @RequestBody List<ResInfo> resInfos) {
        // 获取当前用户的默认角色id
        FitmgrUser user = SecurityUtils.getUser();
        if (null != user) {
            List<AuthCheckVO> list = new ArrayList<AuthCheckVO>();
            // 通过将List放入Set进行自动去重（即使用到ResInfo的equals与hashCode方法）
            Set<ResInfo> userSet = new HashSet<>(resInfos);
            // 然后重新放回List中即可
            resInfos = new ArrayList<>(userSet);
            for (ResInfo resInfo : resInfos) {
                AuthCheck authCheck = iAuthService.newAuthCheck(functionCode, user.getId(), user.getDefaultTenantId(),
                        resInfo.getResTenantId(), resInfo.getResProjectId(), resInfo.getResUserId()).getData();
                AuthCheckVO authCheckVO = new AuthCheckVO();
                authCheckVO.setResInfo(resInfo);
                authCheckVO.setAuthCheck(authCheck);
                list.add(authCheckVO);
            }
            return R.ok(list);
        }
        return R.failed(BusinessEnum.NOT_LOGIN);
    }

    @SysLog(value = "添加新角色继承系统角色所有功能权限和数据权限", cloudResType = "权限", resIdArgIndex = 0, resIdLocation = "arg")
    @ApiOperation(value = "添加新角色继承系统角色所有功能权限和数据权限")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "roleId", dataType = "Integer", required = true, value = "新角色id"),
            @ApiImplicitParam(paramType = "path", name = "inheritRoleId", dataType = "Integer", required = true, value = "继承角色id") })
    @PostMapping("/inherit-auth")
    public R inheritAuth(@PathVariable(name = "roleId") Integer roleId,
            @PathVariable(name = "inheritRoleId") Integer inheritRoleId) {
        return R.ok(iAuthService.inheritAuth(roleId, inheritRoleId));
    }

    @ApiOperation(value = "通过url和http方法获取权限")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "query", name = "functionId", dataType = "Integer", required = true, value = "操作id"),
            @ApiImplicitParam(paramType = "query", name = "roleId", dataType = "Integer", required = true, value = "角色id") })
    @GetMapping("/check-by-url-method")
    public R checkByUrlAndHttpMethod(@RequestParam(name = "apiUrl") String apiUrl,
            @RequestParam(name = "httpMethod") String httpMethod, @RequestParam(name = "userId") Integer userId,
            @RequestParam(name = "defaultTenantId") Integer defaultTenantId) {
        return iAuthService.checkByUrlAndMethod(apiUrl, httpMethod, userId, defaultTenantId);
    }

    @ApiOperation(value = "根据roleId更新权限")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "roleId", dataType = "Integer", required = true, value = "角色id") })
    @GetMapping("/process-auth/{roleId}")
    public R processAuth(@PathVariable(name = "roleId") Integer roleId) {
        iAuthService.processAuth(roleId);
        return R.ok();
    }

}
