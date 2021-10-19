package com.fitmgr.admin.controller;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitmgr.admin.api.entity.ResourceMenu;
import com.fitmgr.admin.api.vo.ResourceFunctionVO;
import com.fitmgr.admin.service.IResourceMenuService;
import com.fitmgr.common.core.util.R;
import com.fitmgr.common.log.annotation.SysLog;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;

/**
 * <p>
 * 资源菜单表 前端控制器
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
@RestController
@AllArgsConstructor
@RequestMapping("/resource-menu")
@Api(value = "resourceMenu", tags = "资源菜单模块")
public class ResourceMenuController {

    private final IResourceMenuService iResourceMenuService;

    @ApiOperation(value = "返回资源树形菜单集合")
    @GetMapping(value = "/tree")
    public R getResourceMenu() {
        return R.ok(iResourceMenuService.getResourceMenu());
    }

    @SysLog(value = "删除资源菜单", cloudResType = "资源菜单", resIdArgIndex = 0, resIdLocation = "arg")
    @ApiOperation(value = "删除资源菜单")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "id", dataType = "Integer", required = true, value = "资源id") })
    @DeleteMapping("/{id}")
    public R deletResourceMenu(@PathVariable(name = "id") Integer id) {
        return R.ok(iResourceMenuService.removeById(id));
    }

    @SysLog(value = "修改资源菜单", cloudResType = "资源菜单", resIdArgIndex = 0, resIdLocation = "arg.id", resNameArgIndex = 0, resNameLocation = "arg.menuName")
    @ApiOperation(value = "修改资源菜单")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "resourceMenu", dataType = "ResourceMenu", required = true, value = "资源对象") })
    @PutMapping
    public R updateResourceMenu(@Valid @RequestBody ResourceMenu resourceMenu) {
        return R.ok(iResourceMenuService.updateById(resourceMenu));
    }

    @SysLog(value = "添加资源菜单", cloudResType = "资源菜单", resNameArgIndex = 0, resNameLocation = "arg.menuName")
    @ApiOperation(value = "添加资源菜单")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "resourceMenu", dataType = "ResourceMenu", required = true, value = "资源对象") })
    @PostMapping
    public R saveResourceMenu(@Valid @RequestBody ResourceMenu resourceMenu) {
        return R.ok(iResourceMenuService.save(resourceMenu));
    }

    @SysLog(value = "添加资源菜单和对应所有操作项", cloudResType = "资源菜单")
    @ApiOperation(value = "添加资源菜单和对应所有操作项")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "resourceFunctionVO", dataType = "ResourceFunctionVO", required = true, value = "资源操作VO") })
    @PostMapping("/save-resource-function")
    public R saveResourceByFunction(@Valid @RequestBody ResourceFunctionVO resourceFunctionVO) {
        return iResourceMenuService.saveResourceByFunction(resourceFunctionVO);
    }

    @SysLog(value = "通过code修改资源菜单", cloudResType = "资源菜单", resIdArgIndex = 0, resIdLocation = "arg.menuCode")
    @ApiOperation(value = "修改资源菜单")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "resourceMenu", dataType = "ResourceMenu", required = true, value = "资源对象") })
    @PutMapping("/update-menu-code")
    public R updateCodeResourceMenu(@Valid @RequestBody ResourceMenu resourceMenu) {
        return R.ok(iResourceMenuService.update(resourceMenu,
                Wrappers.<ResourceMenu>lambdaUpdate().eq(StringUtils.isNotEmpty(resourceMenu.getMenuCode()),
                        ResourceMenu::getMenuCode, resourceMenu.getMenuCode())));
    }

    @SysLog(value = "通过code删除资源菜单", cloudResType = "资源菜单", resIdArgIndex = 0, resIdLocation = "arg")
    @ApiOperation(value = "通过code删除资源菜单")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "menuCode", dataType = "String", required = true, value = "资源code") })
    @DeleteMapping("/delet-menu-code")
    public R deletCodeResourceMenu(@PathVariable(name = "menuCode") String menuCode) {
        return iResourceMenuService.deletCodeResourceMenu(menuCode);
    }
}
