
package com.fitmgr.admin.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitmgr.admin.api.dto.MenuDTO;
import com.fitmgr.admin.api.entity.Menu;
import com.fitmgr.admin.api.entity.MenuOrder;
import com.fitmgr.admin.api.entity.RoleMenu;
import com.fitmgr.admin.api.entity.Subpage;
import com.fitmgr.admin.api.entity.TenantService;
import com.fitmgr.admin.api.vo.MenuVO;
import com.fitmgr.admin.mapper.TenantServiceMapper;
import com.fitmgr.admin.service.IMenuService;
import com.fitmgr.admin.service.IRoleMenuService;
import com.fitmgr.admin.service.IUserService;
import com.fitmgr.common.core.constant.CommonConstants;
import com.fitmgr.common.core.constant.enums.BusinessEnum;
import com.fitmgr.common.core.util.IdGen;
import com.fitmgr.common.core.util.R;
import com.fitmgr.common.log.annotation.SysLog;
import com.fitmgr.common.security.service.FitmgrUser;
import com.fitmgr.common.security.util.SecurityUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Fitmgr
 * @date 2017/10/31
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/menu")
@Api(value = "menu", tags = "菜单管理模块")
public class MenuController {

    private final String ROOT_MENU_ID = "0";

    private final IMenuService iMenuService;

    private final IRoleMenuService iRoleMenuService;

    private final IUserService userService;

    private final TenantServiceMapper tenantServiceMapper;

    /**
     * 获取当前角色的菜单树
     *
     * @return
     */
    @ApiOperation(value = "获取当前角色的菜单树（系统菜单+服务菜单）")
    @GetMapping("/role-menu")
    public R getUserMenu() {
        // 获取当前用户的默认角色id
        FitmgrUser user = SecurityUtils.getUser();
        if (null != user.getDefaultTenantId()) {
            List<Map<String, Integer>> roleList = userService.queryRoleByUserIdAndTenantId(user.getId(),
                    user.getDefaultTenantId());
            List<Integer> list = new ArrayList<>();
            for (Map<String, Integer> role : roleList) {
                list.add(role.get("role_id"));
            }

            List<TenantService> filterTenantServices;
            if (user.getDefaultTenantId().equals(-1)) {
                filterTenantServices = null;
            } else {
                filterTenantServices = tenantServiceMapper
                        .selectList(new QueryWrapper<TenantService>().eq("tenant_id", user.getDefaultTenantId()));
            }

            return R.ok(iMenuService.getUserMenus(list, filterTenantServices));
        }
        return R.failed(BusinessEnum.NOT_LOGIN);
    }

    /**
     * 通过角色id获取菜单树
     *
     * @return
     */
    @ApiOperation(value = "通过角色id获取菜单树（系统菜单+服务菜单）")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "roleId", dataType = "Integer", required = true, value = "角色id") })
    @GetMapping("/role-id-menu/{roleId}")
    public R getRoleIdMenu(@PathVariable(name = "roleId") Integer roleId) {
        return R.ok(iMenuService.getRoleMenu(roleId));
    }

    /**
     * 获取角色系统菜单list
     *
     * @return
     */
    @ApiOperation(value = "获取角色系统菜单list")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "roleId", dataType = "Integer", required = true, value = "角色id") })
    @GetMapping("/list/{roleId}")
    public R getList(@PathVariable(name = "roleId") Integer roleId) {
        return new R(iMenuService.getList(roleId));
    }

    /**
     * 通过url获取菜单
     *
     * @return
     */
    @ApiOperation(value = "通过url获取菜单")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "query", name = "url", dataType = "String", required = true, value = "菜单url") })
    @GetMapping("/template/url")
    public R<Menu> getListByUrl(@RequestParam(name = "url") String url) {
        List<Menu> menus = iMenuService
                .list(Wrappers.<Menu>query().lambda().eq(Menu::getType, CommonConstants.SERVICE_MENU));
        String newUrl;
        for (Menu menu : menus) {
            if (url.startsWith("https://") || url.startsWith("http://")) {
                String url1 = "";
                if (url.startsWith("https://")) {
                    url1 = url.replaceAll("https://", "");
                }
                if (url.startsWith("http://")) {

                    url1 = url.replaceAll("http://", "");
                }

                String url2 = url1.replaceAll("\\d+\\.\\d+\\.\\d+\\.\\d+\\:\\d+\\/", "");
                newUrl = "/" + url2;
            } else {
                newUrl = url;
            }
            if (newUrl.equals(menu.getPath())) {
                return R.ok(menu);
            }
        }
        return R.failed("未找到url对应的菜单");
    }

    /**
     * 通过templateId获取菜单
     *
     * @return
     */
    @ApiOperation(value = "通过templateId获取菜单")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "templateId", dataType = "Integer", required = true, value = "模板id") })
    @GetMapping("/templateId/{templateId}")
    public R<Menu> getMenuByTemplateId(@PathVariable(name = "templateId") Integer templateId) {
        Menu menus = iMenuService.getOne(Wrappers.<Menu>query().lambda().eq(Menu::getTemplateId, templateId));
        if (null == menus) {
            return R.failed("未找到服务ID对应的菜单");
        } else {
            return R.ok(menus);
        }
    }

    /**
     * 返回所有的树形菜单集合
     *
     * @return 树形菜单
     */
    @ApiOperation(value = "返回所有的树形菜单集合（系统菜单+服务菜单）")
    @GetMapping(value = "/get-tree")
    public R getTree() {
        return R.ok(iMenuService.getTree());
    }

    /**
     * 返回系统树形菜单集合
     *
     * @return 系统树形菜单
     */
    @ApiOperation(value = "返回系统树形菜单集合-系统菜单")
    @GetMapping(value = "/get-sys-tree")
    public R getSysTree() {
        return R.ok(iMenuService.getSysTree());
    }

    /**
     * 通过菜单ID查询系统菜单的详细信息
     *
     * @param menuId 菜单ID
     * @return 菜单详细信息
     */
    @ApiOperation(value = "通过ID查询系统菜单的详细信息")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "menuId", dataType = "String", required = true, value = "菜单id") })
    @GetMapping("/sys-menu/{menuId}")
    public R getById(@PathVariable(name = "menuId") String menuId) {
        if (StringUtils.isNotBlank(menuId)) {
            return R.ok(iMenuService.getOne(new QueryWrapper<Menu>().lambda().eq(Menu::getMenuId, menuId)
                    .eq(Menu::getType, CommonConstants.SYSTEM_MENU)));
        }
        return R.failed(BusinessEnum.PARAMETER_NULL);
    }

    /**
     * 修改菜单权限
     *
     * @param roleMenus 角色菜单对象
     * @return 菜单详细信息
     */
    @SysLog(value = "修改菜单权限", cloudResType = "菜单")
    @ApiOperation(value = "通过角色添加/修改菜单权限(批量操作)")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "roleMenus", dataType = "List", required = true, value = "角色菜单对象") })
    @PostMapping("/role-menu-auth")
    public R configMenuAuth(@RequestBody List<RoleMenu> roleMenus) {
        if (roleMenus.size() > 0) {
            if (null != roleMenus.get(0).getRoleId() && 1 == roleMenus.get(0).getRoleId()) {
                // 新的未勾选菜单
                List<String> newHalfMenuIds = new ArrayList<String>();
                for (int i = 0; i < roleMenus.size(); i++) {
                    if (roleMenus.get(i).getHalf()) {
                        newHalfMenuIds.add(roleMenus.get(i).getMenuId());
                    }
                }
                List<MenuVO> listmv = iMenuService.getList(1);
                // 旧的未勾选菜单
                List<String> oldHalfMenuIds = new ArrayList<String>();
                for (int i = 0; i < listmv.size(); i++) {
                    if (listmv.get(i).getHalf()) {
                        oldHalfMenuIds.add(listmv.get(i).getMenuId());
                    }
                }
                newHalfMenuIds.removeAll(oldHalfMenuIds);
                // 新的未勾选菜单多于旧的未勾选菜单
                if (newHalfMenuIds.size() > 0) {
                    return R.failed("超级管理员菜单权限不可取消");
                }
            }
            iRoleMenuService.remove(Wrappers.<RoleMenu>lambdaQuery().eq(null != roleMenus.get(0).getRoleId(),
                    RoleMenu::getRoleId, roleMenus.get(0).getRoleId()));
            return R.ok(iRoleMenuService.batchInsert(roleMenus));
        }
        return R.failed(BusinessEnum.PARAMETER_NULL);
    }

    /**
     * 新增菜单
     *
     * @param menuDTO 菜单信息
     * @return success/false
     */
    @SysLog(value = "新增系统菜单", cloudResType = "菜单", resNameArgIndex = 0, resNameLocation = "arg.name")
    @ApiOperation(value = "新增系统菜单")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "menuDTO", dataType = "MenuDTO", required = true, value = "菜单对象") })
    @PostMapping
    public R save(@Valid @RequestBody MenuDTO menuDTO) {
        int count = iMenuService.count(Wrappers.<Menu>lambdaQuery().eq(StringUtils.isNotBlank(menuDTO.getName()),
                Menu::getName, menuDTO.getName()));
        if (count > 0) {
            return R.failed(BusinessEnum.MENU_CODE_REPEAT);
        }

        int count2 = iMenuService.count(Wrappers.<Menu>lambdaQuery().eq(StringUtils.isNotBlank(menuDTO.getPath()),
                Menu::getPath, menuDTO.getPath()));
        if (count2 > 0) {
            return R.failed(BusinessEnum.MENU_PATH_REPEAT);
        }

        if (StringUtils.isBlank(menuDTO.getMenuId())) {
            menuDTO.setMenuId(IdGen.uuid());
        }
        // 添加一个子菜单，将父菜单设置半选
        if (!(menuDTO.getParentId().equals(ROOT_MENU_ID))) {
            iRoleMenuService.update(Wrappers.<RoleMenu>lambdaUpdate().eq(RoleMenu::getMenuId, menuDTO.getParentId())
                    .set(RoleMenu::getHalf, true));
        }
        Menu menu = new Menu();
        BeanUtils.copyProperties(menuDTO, menu);
        if ((CommonConstants.SERVICE_MENU).equals(menu.getType()) && null != menuDTO.getTenantIds()
                && menuDTO.getIsGlobal().equals(CommonConstants.NOT_GLOBAL)) {
            if (menuDTO.getTenantIds().size() > 0 && !menuDTO.getTenantIds().isEmpty()) {
                tenantServiceMapper.insertBatch(menuDTO.getTenantIds(), menu.getMenuId());
            }
        }

        return R.ok(iMenuService.save(menu));
    }

    /**
     * 删除菜单
     *
     * @param menuId 菜单ID
     * @return success/false
     */
    @SysLog(value = "删除菜单", cloudResType = "菜单", resIdArgIndex = 0, resIdLocation = "arg")
    @ApiOperation(value = "删除菜单")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "menuId", dataType = "String", required = true, value = "菜单id") })
    @DeleteMapping("/{menuId}")
    public R removeById(@PathVariable(name = "menuId") String menuId) {
        if (iMenuService.getOne(
                Wrappers.<Menu>lambdaQuery().eq(StringUtils.isNotBlank(menuId), Menu::getMenuId, menuId)) != null) {
            return R.ok(iMenuService
                    .remove(Wrappers.<Menu>lambdaQuery().eq(StringUtils.isNotBlank(menuId), Menu::getMenuId, menuId)));
        } else {
            return R.failed(BusinessEnum.MENU_NOT_FIND);
        }
    }

    /**
     * 更新菜单
     *
     * @param menuDTO 菜单对象
     * @return
     */
    @SysLog(value = "修改菜单", cloudResType = "菜单", resIdArgIndex = 0, resIdLocation = "arg.id", resNameArgIndex = 0, resNameLocation = "arg.name")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "menuDTO", dataType = "MenuDTO", required = true, value = "菜单对象") })
    @PutMapping
    public R update(@Valid @RequestBody MenuDTO menuDTO) {
        Menu menu = iMenuService.getOne(Wrappers.<Menu>lambdaQuery().eq(Menu::getMenuId, menuDTO.getMenuId()));
        if (!menuDTO.getName().equals(menu.getName())) {
            int count = iMenuService.count(Wrappers.<Menu>lambdaQuery().eq(StringUtils.isNotBlank(menuDTO.getName()),
                    Menu::getName, menuDTO.getName()));
            if (count > 0) {
                return R.failed(BusinessEnum.MENU_CODE_REPEAT);
            }
        }

        if (!menuDTO.getPath().equals(menu.getPath())) {
            int count2 = iMenuService.count(Wrappers.<Menu>lambdaQuery().eq(StringUtils.isNotBlank(menuDTO.getPath()),
                    Menu::getPath, menuDTO.getPath()));
            if (count2 > 0) {
                return R.failed(BusinessEnum.MENU_PATH_REPEAT);
            }
        }
        if ((CommonConstants.SERVICE_MENU).equals(menu.getType()) && null != menuDTO.getTenantIds()
                && menuDTO.getIsGlobal().equals(CommonConstants.NOT_GLOBAL)) {
            if (menuDTO.getTenantIds().size() > 0 && !menuDTO.getTenantIds().isEmpty()) {
                tenantServiceMapper.deleteByServiceId(menuDTO.getMenuId());
                tenantServiceMapper.insertBatch(menuDTO.getTenantIds(), menuDTO.getMenuId());
            }
        }

        Menu newMenu = new Menu();
        BeanUtils.copyProperties(menuDTO, newMenu);

        iMenuService.hidden(newMenu);

        return R.ok(
                iMenuService.update(newMenu, Wrappers.<Menu>lambdaUpdate().eq(Menu::getMenuId, menuDTO.getMenuId())));
    }

    /**
     * 设置菜单顺序
     *
     * @param menuOrder 菜单顺序对象
     * @return
     */
    @SysLog(value = "设置菜单顺序", cloudResType = "菜单", resIdArgIndex = 0, resIdLocation = "arg.menuId")
    @ApiOperation(value = "设置菜单顺序")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "menu", dataType = "MenuOrder", required = true, value = "菜单顺序对象") })
    @PutMapping("/order/config")
    public R update(@Valid @RequestBody MenuOrder menuOrder) {
        return R.ok(iMenuService.updateMenuOrder(menuOrder));
    }

    /**
     * 新增子页面
     *
     * @param subpage 子页面对象
     * @return
     */
    @SysLog(value = "新增子页面", cloudResType = "菜单", resIdArgIndex = 0, resIdLocation = "arg.menuId")
    @ApiOperation(value = "新增子页面")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "subpage", dataType = "Subpage", required = true, value = "子页面对象") })
    @PostMapping("/subpage")
    public R addSubpage(@Valid @RequestBody Subpage subpage) {
        return iMenuService.addSubpage(subpage);
    }

    /**
     * 删除子页面
     *
     * @param subpageId 子页面ID
     * @return
     */
    @SysLog(value = "删除子页面", cloudResType = "菜单", resIdArgIndex = 0, resIdLocation = "arg")
    @ApiOperation(value = "删除子页面")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "subpageId", dataType = "Integer", required = true, value = "子页面id") })
    @DeleteMapping("/subpage/{subpageId}")
    public R removeSubpageById(@PathVariable(name = "subpageId") Integer subpageId) {
        return iMenuService.deleteSubpage(subpageId);
    }

    /**
     * 修改子页面
     *
     * @param subpage 子页面对象
     * @return
     */
    @SysLog(value = "修改子页面", cloudResType = "菜单", resIdArgIndex = 0, resIdLocation = "arg.id", resNameArgIndex = 0, resNameLocation = "arg.name")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "subpage", dataType = "Subpage", required = true, value = "子页面对象"),
            @ApiImplicitParam(paramType = "path", name = "subpageId", dataType = "Integer", required = true, value = "子页面ID") })
    @PutMapping("/subpage/{subpageId}")
    public R update(@Valid @RequestBody Subpage subpage) {
        return iMenuService.modifySubpage(subpage);
    }

    /**
     * 查询指定菜单下的子页面
     *
     * @param menuId 菜单Id
     * @return
     */
    @ApiOperation(value = "查询指定菜单下的子页面")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "menuId", dataType = "String", required = true, value = "菜单id") })
    @GetMapping("/{menuId}/subpage")
    public R getSubpageByMenuId(@PathVariable(name = "menuId") String menuId) {
        if (StringUtils.isNotBlank(menuId)) {
            return R.ok(iMenuService.getSubpageByMenuId(menuId));
        }
        return R.failed(BusinessEnum.PARAMETER_NULL);
    }
}
