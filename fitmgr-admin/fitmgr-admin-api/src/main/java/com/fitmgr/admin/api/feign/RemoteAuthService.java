package com.fitmgr.admin.api.feign;

import java.util.List;

import javax.validation.Valid;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.fitmgr.admin.api.config.AdminFeignConfig;
import com.fitmgr.admin.api.entity.AuthCheck;
import com.fitmgr.admin.api.entity.Function;
import com.fitmgr.admin.api.entity.ResInfo;
import com.fitmgr.admin.api.entity.ResourceMenu;
import com.fitmgr.admin.api.vo.AuthCheckVO;
import com.fitmgr.admin.api.vo.AuthVO;
import com.fitmgr.admin.api.vo.ResourceFunctionVO;
import com.fitmgr.common.core.constant.ServiceNameConstants;
import com.fitmgr.common.core.util.R;

/**
 * @Classname RemoteAuthService
 * @Description 权限feign接口
 * @Date 2019/11/19 15:18
 * @Created by DZL
 */
@FeignClient(contextId = "remoteAuthService", value = ServiceNameConstants.UMPS_SERVICE, configuration = AdminFeignConfig.class)
public interface RemoteAuthService {

    /**
     * 当前用户通过功能code获取权限
     *
     * @param code code
     * @return AuthVO
     */
    @GetMapping("/auth/code-auth/{code}")
    AuthVO getUserAuth(@PathVariable("code") String code);

    /**
     * 功能权限+数据权限校验
     *
     * @param functionCode 操作唯一编码
     * @return R
     */
    @GetMapping("/auth/auth-check/{functionCode}")
    R authCheck(@PathVariable("functionCode") String functionCode);

    /**
     * （新）功能权限+数据权限校验
     * 
     * @param functionCode functionCode
     * @param resTenantId  resTenantId
     * @param resProjectId resProjectId
     * @param resUserId    resUserId
     * @return R
     */
    @GetMapping("/auth/new-auth-check/{functionCode}")
    R<AuthCheck> newAuthCheck(@PathVariable("functionCode") String functionCode,
            @RequestParam("resTenantId") Integer resTenantId, @RequestParam("resProjectId") Integer resProjectId,
            @RequestParam("resUserId") Integer resUserId);

    /**
     * （新）批量功能权限+数据权限校验
     * 
     * @param functionCode functionCode
     * @param resInfos     resInfos
     * @return R
     */
    @PostMapping("/auth/new-auth-checks/{functionCode}")
    R<List<AuthCheckVO>> newAuthChecks(@PathVariable(name = "functionCode") String functionCode,
            @RequestBody List<ResInfo> resInfos);

    /**
     * 添加资源菜单和对应所有操作项
     *
     * @param resourceFunctionVO resourceFunctionVO
     * @return R
     */
    @PostMapping("/resource-menu/save-resource-function")
    R saveResourceByFunction(@RequestBody ResourceFunctionVO resourceFunctionVO);

    /**
     * 通过code修改资源菜单
     *
     * @param resourceMenu 资源对象
     * @return R
     */
    @PutMapping("/resource-menu/update-menu-code")
    R updateCodeResourceMenu(@Valid @RequestBody ResourceMenu resourceMenu);

    /**
     * 通过code删除资源菜单
     *
     * @param menuCode 资源code
     * @return R
     */
    @DeleteMapping("/resource-menu/delet-menu-code")
    R deletCodeResourceMenu(@PathVariable(name = "menuCode") String menuCode);

    /**
     * 通过code修改操作
     *
     * @param functions 操作list
     * @return R
     */
    @PutMapping("/resource-menu/update-function-code")
    R updateCodefunction(@RequestBody List<Function> functions);

    /**
     * 通过code删除操作
     *
     * @param functionCode 操作code
     * @return R
     */
    @DeleteMapping("/resource-menu/delet-function-code")
    R deletCodefunction(@PathVariable(name = "functionCode") String functionCode);

}
