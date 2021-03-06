
package com.taibai.admin.controller;

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
import com.taibai.admin.api.dto.MenuDTO;
import com.taibai.admin.api.entity.Menu;
import com.taibai.admin.api.entity.MenuOrder;
import com.taibai.admin.api.entity.RoleMenu;
import com.taibai.admin.api.entity.Subpage;
import com.taibai.admin.api.entity.TenantService;
import com.taibai.admin.api.vo.MenuVO;
import com.taibai.admin.mapper.TenantServiceMapper;
import com.taibai.admin.service.IMenuService;
import com.taibai.admin.service.IRoleMenuService;
import com.taibai.admin.service.IUserService;
import com.taibai.common.core.constant.CommonConstants;
import com.taibai.common.core.constant.enums.BusinessEnum;
import com.taibai.common.core.util.IdGen;
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
 * @author Fitmgr
 * @date 2017/10/31
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/menu")
@Api(value = "menu", tags = "??????????????????")
public class MenuController {

    private final String ROOT_MENU_ID = "0";

    private final IMenuService iMenuService;

    private final IRoleMenuService iRoleMenuService;

    private final IUserService userService;

    private final TenantServiceMapper tenantServiceMapper;

    /**
     * ??????????????????????????????
     *
     * @return
     */
    @ApiOperation(value = "?????????????????????????????????????????????+???????????????")
    @GetMapping("/role-menu")
    public R getUserMenu() {
        // ?????????????????????????????????id
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
     * ????????????id???????????????
     *
     * @return
     */
    @ApiOperation(value = "????????????id??????????????????????????????+???????????????")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "roleId", dataType = "Integer", required = true, value = "??????id") })
    @GetMapping("/role-id-menu/{roleId}")
    public R getRoleIdMenu(@PathVariable(name = "roleId") Integer roleId) {
        return R.ok(iMenuService.getRoleMenu(roleId));
    }

    /**
     * ????????????????????????list
     *
     * @return
     */
    @ApiOperation(value = "????????????????????????list")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "roleId", dataType = "Integer", required = true, value = "??????id") })
    @GetMapping("/list/{roleId}")
    public R getList(@PathVariable(name = "roleId") Integer roleId) {
        return new R(iMenuService.getList(roleId));
    }

    /**
     * ??????url????????????
     *
     * @return
     */
    @ApiOperation(value = "??????url????????????")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "query", name = "url", dataType = "String", required = true, value = "??????url") })
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
        return R.failed("?????????url???????????????");
    }

    /**
     * ??????templateId????????????
     *
     * @return
     */
    @ApiOperation(value = "??????templateId????????????")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "templateId", dataType = "Integer", required = true, value = "??????id") })
    @GetMapping("/templateId/{templateId}")
    public R<Menu> getMenuByTemplateId(@PathVariable(name = "templateId") Integer templateId) {
        Menu menus = iMenuService.getOne(Wrappers.<Menu>query().lambda().eq(Menu::getTemplateId, templateId));
        if (null == menus) {
            return R.failed("???????????????ID???????????????");
        } else {
            return R.ok(menus);
        }
    }

    /**
     * ?????????????????????????????????
     *
     * @return ????????????
     */
    @ApiOperation(value = "????????????????????????????????????????????????+???????????????")
    @GetMapping(value = "/get-tree")
    public R getTree() {
        return R.ok(iMenuService.getTree());
    }

    /**
     * ??????????????????????????????
     *
     * @return ??????????????????
     */
    @ApiOperation(value = "??????????????????????????????-????????????")
    @GetMapping(value = "/get-sys-tree")
    public R getSysTree() {
        return R.ok(iMenuService.getSysTree());
    }

    /**
     * ????????????ID?????????????????????????????????
     *
     * @param menuId ??????ID
     * @return ??????????????????
     */
    @ApiOperation(value = "??????ID?????????????????????????????????")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "menuId", dataType = "String", required = true, value = "??????id") })
    @GetMapping("/sys-menu/{menuId}")
    public R getById(@PathVariable(name = "menuId") String menuId) {
        if (StringUtils.isNotBlank(menuId)) {
            return R.ok(iMenuService.getOne(new QueryWrapper<Menu>().lambda().eq(Menu::getMenuId, menuId)
                    .eq(Menu::getType, CommonConstants.SYSTEM_MENU)));
        }
        return R.failed(BusinessEnum.PARAMETER_NULL);
    }

    /**
     * ??????????????????
     *
     * @param roleMenus ??????????????????
     * @return ??????????????????
     */
    @SysLog(value = "??????????????????", cloudResType = "??????")
    @ApiOperation(value = "??????????????????/??????????????????(????????????)")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "roleMenus", dataType = "List", required = true, value = "??????????????????") })
    @PostMapping("/role-menu-auth")
    public R configMenuAuth(@RequestBody List<RoleMenu> roleMenus) {
        if (roleMenus.size() > 0) {
            if (null != roleMenus.get(0).getRoleId() && 1 == roleMenus.get(0).getRoleId()) {
                // ?????????????????????
                List<String> newHalfMenuIds = new ArrayList<String>();
                for (int i = 0; i < roleMenus.size(); i++) {
                    if (roleMenus.get(i).getHalf()) {
                        newHalfMenuIds.add(roleMenus.get(i).getMenuId());
                    }
                }
                List<MenuVO> listmv = iMenuService.getList(1);
                // ?????????????????????
                List<String> oldHalfMenuIds = new ArrayList<String>();
                for (int i = 0; i < listmv.size(); i++) {
                    if (listmv.get(i).getHalf()) {
                        oldHalfMenuIds.add(listmv.get(i).getMenuId());
                    }
                }
                newHalfMenuIds.removeAll(oldHalfMenuIds);
                // ????????????????????????????????????????????????
                if (newHalfMenuIds.size() > 0) {
                    return R.failed("???????????????????????????????????????");
                }
            }
            iRoleMenuService.remove(Wrappers.<RoleMenu>lambdaQuery().eq(null != roleMenus.get(0).getRoleId(),
                    RoleMenu::getRoleId, roleMenus.get(0).getRoleId()));
            return R.ok(iRoleMenuService.batchInsert(roleMenus));
        }
        return R.failed(BusinessEnum.PARAMETER_NULL);
    }

    /**
     * ????????????
     *
     * @param menuDTO ????????????
     * @return success/false
     */
    @SysLog(value = "??????????????????", cloudResType = "??????", resNameArgIndex = 0, resNameLocation = "arg.name")
    @ApiOperation(value = "??????????????????")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "menuDTO", dataType = "MenuDTO", required = true, value = "????????????") })
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
        // ????????????????????????????????????????????????
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
     * ????????????
     *
     * @param menuId ??????ID
     * @return success/false
     */
    @SysLog(value = "????????????", cloudResType = "??????", resIdArgIndex = 0, resIdLocation = "arg")
    @ApiOperation(value = "????????????")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "menuId", dataType = "String", required = true, value = "??????id") })
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
     * ????????????
     *
     * @param menuDTO ????????????
     * @return
     */
    @SysLog(value = "????????????", cloudResType = "??????", resIdArgIndex = 0, resIdLocation = "arg.id", resNameArgIndex = 0, resNameLocation = "arg.name")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "menuDTO", dataType = "MenuDTO", required = true, value = "????????????") })
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
     * ??????????????????
     *
     * @param menuOrder ??????????????????
     * @return
     */
    @SysLog(value = "??????????????????", cloudResType = "??????", resIdArgIndex = 0, resIdLocation = "arg.menuId")
    @ApiOperation(value = "??????????????????")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "menu", dataType = "MenuOrder", required = true, value = "??????????????????") })
    @PutMapping("/order/config")
    public R update(@Valid @RequestBody MenuOrder menuOrder) {
        return R.ok(iMenuService.updateMenuOrder(menuOrder));
    }

    /**
     * ???????????????
     *
     * @param subpage ???????????????
     * @return
     */
    @SysLog(value = "???????????????", cloudResType = "??????", resIdArgIndex = 0, resIdLocation = "arg.menuId")
    @ApiOperation(value = "???????????????")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "subpage", dataType = "Subpage", required = true, value = "???????????????") })
    @PostMapping("/subpage")
    public R addSubpage(@Valid @RequestBody Subpage subpage) {
        return iMenuService.addSubpage(subpage);
    }

    /**
     * ???????????????
     *
     * @param subpageId ?????????ID
     * @return
     */
    @SysLog(value = "???????????????", cloudResType = "??????", resIdArgIndex = 0, resIdLocation = "arg")
    @ApiOperation(value = "???????????????")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "subpageId", dataType = "Integer", required = true, value = "?????????id") })
    @DeleteMapping("/subpage/{subpageId}")
    public R removeSubpageById(@PathVariable(name = "subpageId") Integer subpageId) {
        return iMenuService.deleteSubpage(subpageId);
    }

    /**
     * ???????????????
     *
     * @param subpage ???????????????
     * @return
     */
    @SysLog(value = "???????????????", cloudResType = "??????", resIdArgIndex = 0, resIdLocation = "arg.id", resNameArgIndex = 0, resNameLocation = "arg.name")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "subpage", dataType = "Subpage", required = true, value = "???????????????"),
            @ApiImplicitParam(paramType = "path", name = "subpageId", dataType = "Integer", required = true, value = "?????????ID") })
    @PutMapping("/subpage/{subpageId}")
    public R update(@Valid @RequestBody Subpage subpage) {
        return iMenuService.modifySubpage(subpage);
    }

    /**
     * ?????????????????????????????????
     *
     * @param menuId ??????Id
     * @return
     */
    @ApiOperation(value = "?????????????????????????????????")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "menuId", dataType = "String", required = true, value = "??????id") })
    @GetMapping("/{menuId}/subpage")
    public R getSubpageByMenuId(@PathVariable(name = "menuId") String menuId) {
        if (StringUtils.isNotBlank(menuId)) {
            return R.ok(iMenuService.getSubpageByMenuId(menuId));
        }
        return R.failed(BusinessEnum.PARAMETER_NULL);
    }
}
