package com.fitmgr.admin.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitmgr.admin.api.dto.MenuTree;
import com.fitmgr.admin.api.entity.Auth;
import com.fitmgr.admin.api.entity.Function;
import com.fitmgr.admin.api.entity.Menu;
import com.fitmgr.admin.api.entity.MenuOrder;
import com.fitmgr.admin.api.entity.RoleMenu;
import com.fitmgr.admin.api.entity.Subpage;
import com.fitmgr.admin.api.entity.TenantService;
import com.fitmgr.admin.api.vo.MenuVO;
import com.fitmgr.admin.api.vo.MetaMenuVO;
import com.fitmgr.admin.api.vo.SubpageVO;
import com.fitmgr.admin.mapper.AuthMapper;
import com.fitmgr.admin.mapper.FunctionMapper;
import com.fitmgr.admin.mapper.MenuMapper;
import com.fitmgr.admin.mapper.RoleMenuMapper;
import com.fitmgr.admin.mapper.SubpageMapper;
import com.fitmgr.admin.mapper.TenantServiceMapper;
import com.fitmgr.admin.service.IMenuService;
import com.fitmgr.common.core.constant.CommonConstants;
import com.fitmgr.common.core.util.ObjectCopyUtils;
import com.fitmgr.common.core.util.R;
import com.fitmgr.common.core.util.SysTreeNode;
import com.fitmgr.common.core.util.SysTreeUtil;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 菜单表 服务实现类
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-18
 */
@Slf4j
@Service
@AllArgsConstructor
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements IMenuService {

    private final String ROOT_MENU_ID = "0";

    private final MenuMapper menuMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final SubpageMapper subpageMapper;
    private final FunctionMapper functionMapper;
    private final AuthMapper authMapper;
    private final TenantServiceMapper tenantServiceMapper;

    /**
     * 获取当前用户的菜单树
     *
     * @param roleId 角色id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<MenuTree> getUserMenu(Integer roleId) {
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<MenuTree> getUserMenus(List<Integer> roleId, List<TenantService> filterTenantServices) {
        // 根据角色获取菜单
        List<MenuVO> menuVos = menuMapper.getSysMenus(roleId);
        List<RoleMenu> filterRoleMenus = roleMenuMapper
                .selectList(new QueryWrapper<RoleMenu>().in("role_id", roleId).groupBy("menu_id"));
        return getMenuTrees(menuVos, filterRoleMenus, filterTenantServices, getAllSubpageMap());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<MenuTree> getRoleMenu(Integer roleId) {
        // 根据角色获取菜单
        List<MenuVO> menuVos = menuMapper.getSysMenu(roleId);
        List<RoleMenu> filterRoleMenus = roleMenuMapper.selectList(new QueryWrapper<RoleMenu>().eq("role_id", roleId));
        return getMenuTrees(menuVos, filterRoleMenus, null, getAllSubpageMap());
    }

    /**
     * 返回所有的树形菜单集合
     *
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<MenuTree> getTree() {
        List<Menu> menus1 = menuMapper.selectList(new QueryWrapper<>());
        // 转VO集合
        List<MenuVO> menus = ObjectCopyUtils.copy(menus1, MenuVO.class);

        return getMenuTrees(menus, null, null, getAllSubpageMap());
    }

    private Map<String, List<Subpage>> getAllSubpageMap() {
        Map<String, List<Subpage>> subPageMap = new HashMap<>();
        List<Subpage> subpages = subpageMapper.selectList(new QueryWrapper<Subpage>());
        if (CollectionUtils.isNotEmpty(subpages)) {
            for (Subpage subpage : subpages) {
                List<Subpage> subpagesTemp = subPageMap.get(subpage.getMenuId());
                if (subpagesTemp == null) {
                    subpagesTemp = new ArrayList<>();
                    subPageMap.put(subpage.getMenuId(), subpagesTemp);
                }
                subpagesTemp.add(subpage);
            }
        }
        return subPageMap;
    }

    /**
     * 返回系统树形菜单集合-系统菜单
     *
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<MenuTree> getSysTree() {
        List<Menu> menus1 = menuMapper
                .selectList(Wrappers.<Menu>lambdaQuery().eq(Menu::getType, CommonConstants.SYSTEM_MENU));
        // 转VO集合
        List<MenuVO> menus = ObjectCopyUtils.copy(menus1, MenuVO.class);
        List<MenuTree> trees = new ArrayList<>();
        MenuTree node;
        for (MenuVO menu : menus) {
            node = new MenuTree();
            node.setMenuId(menu.getMenuId());
            node.setParentId(menu.getParentId());
            node.setType(menu.getType());
            node.setName(menu.getName());
            node.setPath(menu.getPath());
            node.setLabel(menu.getName());
            node.setComponent(menu.getComponent());
            node.setSort(menu.getSort());
            node.setDelFlag(menu.getDelFlag());
            node.setIcon(menu.getIcon());
            node.setKeepAlive(menu.getKeepAlive());
            node.setHidden(menu.getHidden());
            node.setAlwaysShow(menu.getAlwaysShow());
            node.setRedirect(menu.getRedirect());
            node.setIsExternal(menu.getIsExternal());
            node.setExternalPwd(menu.getExternalPwd());
            node.setExternalLoginUrl(menu.getExternalLoginUrl());
            node.setExternalUsername(menu.getExternalUsername());
            if (menu.getMeta() != null) {
                node.setMeta(menu.getMeta());
            }
            node.setMenuOrder(menu.getMenuOrder());
            node.setStatus(menu.getStatus());
            trees.add(node);
        }
        List<MenuTree> build = SysTreeUtil.build(trees, "0");

        try {
            sortTree(build);
        } catch (Throwable th) {
            log.error("sort fail", th);
        }

        return build;
    }

    private void sortTree(List<MenuTree> menuTrees) {
        if (CollectionUtils.isEmpty(menuTrees)) {
            return;
        }
        menuTrees.sort(new MenuComparator());
        for (MenuTree menuTree : menuTrees) {
            sortSysTree(menuTree.getChildren());
        }
    }

    private void sortSysTree(List<SysTreeNode> menuTrees) {
        if (CollectionUtils.isEmpty(menuTrees)) {
            return;
        }

        menuTrees.sort(new MenuComparator());
        for (SysTreeNode sysTreeNode : menuTrees) {
            sortSysTree(sysTreeNode.getChildren());
        }
    }

    /**
     * 根据角色id配置菜单权限
     *
     * @param menuIds 菜单list
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R configMenuAuth(Integer roleId, List<String> menuIds) {
        return R.ok(menuMapper.configMenuAuth(roleId, menuIds));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<MenuVO> getList(Integer roleId) {
        List<Menu> menus = menuMapper.selectList(new QueryWrapper<>());
        // 获取角色系统菜单
        List<MenuVO> menuVos = menuMapper.getSysMenu(roleId);
        List<String> list = new ArrayList<String>();
        for (MenuVO menuVo : menuVos) {
            list.add(menuVo.getMenuId());
        }
        // 根据全量菜单重新构建该角色的菜单
        JSONArray array = new JSONArray();
        structure(menus, array, list, "0");
        menuVos = JSONObject.parseArray(array.toJSONString(), MenuVO.class);
        return menuVos;
    }

    public static String structure(List<Menu> menus, JSONArray array, List<String> list, String parentId) {
        List<String> halfs = new ArrayList<String>();
        for (Menu menu : menus) {
            if (menu.getParentId().equals(parentId)) {
                String res = structure(menus, array, list, menu.getMenuId());
                if (res != null && "half".equals(res)) {
                    // 有选有不选有半选为半选
                    JSONObject json = new JSONObject();
                    json.put("menuId", menu.getMenuId());
                    json.put("half", true);
                    array.add(json);
                    halfs.add("half");
                } else if (res != null && "in".equals(res)) {
                    // 全都选为全选
                    JSONObject json = new JSONObject();
                    json.put("menuId", menu.getMenuId());
                    json.put("half", false);
                    array.add(json);
                    halfs.add("in");
                } else if (res != null && "notin".equals(res)) {
                    // 全都不选为不选
                    halfs.add("notin");
                } else if (list.contains(menu.getMenuId())) {
                    // 不包含但之前已选为全选
                    JSONObject json = new JSONObject();
                    json.put("menuId", menu.getMenuId());
                    json.put("half", false);
                    array.add(json);
                    halfs.add("in");
                } else {
                    halfs.add("notin");
                }
            }
        }
        if ((halfs.contains("in") && halfs.contains("half") && halfs.contains("notin"))
                || (halfs.contains("in") && halfs.contains("half")) || (halfs.contains("in") && halfs.contains("notin"))
                || (halfs.contains("half") && halfs.contains("notin")) || halfs.contains("half")) {
            // 有选有不选有半选为半选
            return "half";
        } else if (halfs.contains("in")) {
            // 全都选为全选
            return "in";
        } else if (halfs.contains("notin")) {
            // 全都不选为不选
            return "notin";
        } else if (list.contains(parentId)) {
            // 不包含但之前已选为全选
            return "in";
        }
        return null;
    }

    private void configMenuHalf(MenuVO menuVO, Map<String, MenuVO> menuVoMap) {
        if (menuVO == null) {
            return;
        }

        if (StringUtils.isEmpty(menuVO.getParentId())) {
            return;
        }

        MenuVO parentMenuVO = menuVoMap.get(menuVO.getParentId());
        if (parentMenuVO != null) {
            parentMenuVO.setHalf(true);
            configMenuHalf(parentMenuVO, menuVoMap);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R updateMenuOrder(MenuOrder menuOrder) {
        Menu menu = menuMapper.selectOne(new QueryWrapper<Menu>().eq("menu_id", menuOrder.getMenuId()));
        if (menu == null) {
            return R.failed("菜单不存在");
        }

        menu.setMenuOrder(menuOrder.getMenuOrder());
        menuMapper.updateById(menu);
        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R addSubpage(Subpage subpage) {
        subpageMapper.insert(subpage);
        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R modifySubpage(Subpage subpage) {
        subpageMapper.updateById(subpage);
        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R deleteSubpage(Integer id) {
        subpageMapper.deleteById(id);
        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R deleteMenu(String menuId) {
        List<Function> functions = functionMapper
                .selectList(new QueryWrapper<Function>().lambda().eq(Function::getMenuId, menuId));
        List<Integer> functionIds = functions.stream().map(Function::getId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(functionIds)) {
            authMapper.delete(new QueryWrapper<Auth>().lambda().in(Auth::getFunctionId, functionIds));
        }
        functionMapper.deleteBatchIds(functionIds);
        roleMenuMapper.delete(new QueryWrapper<RoleMenu>().lambda().eq(RoleMenu::getMenuId, menuId));
        subpageMapper.delete(new QueryWrapper<Subpage>().lambda().eq(Subpage::getMenuId, menuId));
        menuMapper.delete(new QueryWrapper<Menu>().lambda().eq(Menu::getMenuId, menuId));
        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Subpage> getSubpageByMenuId(String menuId) {
        return subpageMapper.selectList(new QueryWrapper<Subpage>().lambda().eq(Subpage::getMenuId, menuId));
    }

    /**
     * 菜单树封装
     *
     * @param menuVOS              菜单VO集合
     * @param filterRoleMenus      角色过滤
     * @param filterTenantServices 租户过滤
     * @param subpageMap           接收服务菜单R
     * @return
     */
    private List<MenuTree> getMenuTrees(List<MenuVO> menuVos, List<RoleMenu> filterRoleMenus,
            List<TenantService> filterTenantServices, Map<String, List<Subpage>> subpageMap) {
        // 前端路由处理
        cloudMenu(menuVos);
        // 生成菜单树返回
        List<MenuTree> trees = new ArrayList<>();
        MenuTree node;
        Map<String, RoleMenu> roleMenuMap = null;
        if (filterRoleMenus != null) {
            roleMenuMap = filterRoleMenus.stream().collect(Collectors.toMap(RoleMenu::getMenuId, roleMenu -> roleMenu));
        }

        Map<String, TenantService> tenantServiceMap = null;
        if (filterTenantServices != null) {
            tenantServiceMap = filterTenantServices.stream()
                    .collect(Collectors.toMap(TenantService::getServiceId, tenantService -> tenantService));
        }

        Map<String, MenuTree> menuTreeMap = new HashMap<>();
        Map<Integer, SubpageVO> subpageVoMap = new HashMap<>();
        for (MenuVO menu : menuVos) {
            if (roleMenuMap != null) {
                if (!roleMenuMap.containsKey(menu.getMenuId())) {
                    continue;
                }

            }
            if (menu.getIsGlobal().equals(CommonConstants.NOT_GLOBAL) && tenantServiceMap != null) {
                if (!tenantServiceMap.containsKey(menu.getMenuId())) {
                    continue;
                }
            }

            node = new MenuTree();
            node.setMenuId(menu.getMenuId());
            node.setParentId(menu.getParentId());
            node.setType(menu.getType());
            node.setName(menu.getName());
            node.setPath(menu.getPath());
            node.setLabel(menu.getName());
            node.setComponent(menu.getComponent());
            node.setSort(menu.getSort());
            node.setDelFlag(menu.getDelFlag());
            node.setIcon(menu.getIcon());
            node.setKeepAlive(menu.getKeepAlive());
            node.setHidden(menu.getHidden());
            node.setAlwaysShow(menu.getAlwaysShow());
            node.setRedirect(menu.getRedirect());
            node.setIsExternal(menu.getIsExternal());
            node.setExternalLoginUrl(menu.getExternalLoginUrl());
            node.setExternalUsername(menu.getExternalUsername());
            node.setExternalPwd(menu.getExternalPwd());

            node.setStatus(menu.getStatus());

            List<Integer> tenants = new ArrayList<>();
            List<TenantService> tenantServices = tenantServiceMapper
                    .selectList(new QueryWrapper<TenantService>().eq("service_id", menu.getMenuId()));
            for (TenantService tenantService : tenantServices) {
                tenants.add(tenantService.getTenantId());
            }
            node.setTenantIds(tenants);

            if (menu.getIsGlobal() != null) {
                node.setIsGlobal(menu.getIsGlobal());
            }
            if (menu.getTemplateId() != null) {
                node.setTemplateId(menu.getTemplateId());
            }
            if (menu.getMeta() != null) {
                node.setMeta(menu.getMeta());
            }
            node.setMenuOrder(menu.getMenuOrder());

            menuTreeMap.put(node.getMenuId(), node);
            if (node.getParentMenus() == null) {
                node.setParentMenus(new ArrayList<>());
            }
            configParentMenus(node, menuTreeMap, node.getParentMenus());

            List<Subpage> subpageTemp = subpageMap.get(node.getMenuId());
            if (CollectionUtils.isNotEmpty(subpageTemp)) {
                List<MenuTree> subPageParentMenus = new ArrayList<>();
                subPageParentMenus.addAll(node.getParentMenus());
                MenuTree temp = new MenuTree();
                temp.setMenuId(node.getMenuId());
                temp.setName(node.getName());
                temp.setLabel(node.getLabel());
                temp.setMeta(node.getMeta());
                temp.setPath(node.getPath());
                temp.setMenuOrder(node.getMenuOrder());
                temp.setCode(node.getCode());
                temp.setComponent(node.getComponent());
                temp.setRedirect(node.getRedirect());
                temp.setType(node.getType());
                temp.setSort(node.getSort());
                temp.setIsExternal(node.getIsExternal());
                temp.setExternalPwd(node.getExternalPwd());
                temp.setExternalLoginUrl(node.getExternalLoginUrl());
                temp.setExternalUsername(node.getExternalUsername());
                subPageParentMenus.add(temp);
                List<SubpageVO> subpageVos = new ArrayList<>();
                for (Subpage subpage : subpageTemp) {
                    SubpageVO subpageVO = new SubpageVO();
                    BeanUtils.copyProperties(subpage, subpageVO);
                    subpageVO.setMenuName(menu.getName());
                    subpageVO.setParentMenus(subPageParentMenus);
                    subpageVoMap.put(subpageVO.getId(), subpageVO);
                    if (subpageVO.getParentId() == -1) {
                        subpageVos.add(subpageVO);
                    } else {
                        SubpageVO parentSubpageVO = subpageVoMap.get(subpageVO.getParentId());
                        if (parentSubpageVO != null) {
                            if (parentSubpageVO.getChildren() == null) {
                                parentSubpageVO.setChildren(new ArrayList<>());
                            }
                            parentSubpageVO.getChildren().add(subpageVO);
                        }
                    }
                }
                node.setSubpageList(subpageVos);
            }
            trees.add(node);
        }

        List<MenuTree> build = SysTreeUtil.build(trees, "0");
        try {
            sortTree(build);
        } catch (Throwable th) {
            log.error("sort fail", th);
        }
        return build;
    }

    private void configParentMenus(MenuTree menuTree, Map<String, MenuTree> menuTreeMap, List<MenuTree> parentMenus) {
        if (menuTree.getParentId() != null && !ROOT_MENU_ID.equals(menuTree.getParentId())) {
            MenuTree parentMenu = menuTreeMap.get(menuTree.getParentId());
            if (parentMenu != null) {
                configParentMenus(parentMenu, menuTreeMap, parentMenus);
                MenuTree temp = new MenuTree();
                temp.setMenuId(parentMenu.getMenuId());
                temp.setName(parentMenu.getName());
                temp.setLabel(parentMenu.getLabel());
                temp.setMeta(parentMenu.getMeta());
                temp.setPath(parentMenu.getPath());
                temp.setMenuOrder(parentMenu.getMenuOrder());
                temp.setCode(parentMenu.getCode());
                temp.setComponent(parentMenu.getComponent());
                temp.setRedirect(parentMenu.getRedirect());
                temp.setType(parentMenu.getType());
                temp.setSort(parentMenu.getSort());
                temp.setIsExternal(parentMenu.getIsExternal());
                temp.setExternalUsername(parentMenu.getExternalUsername());
                temp.setExternalLoginUrl(parentMenu.getExternalLoginUrl());
                temp.setExternalPwd(parentMenu.getExternalPwd());
                parentMenus.add(temp);
            }
        }
    }

    /**
     * 为了可怕的动态路由生成页面等
     *
     * @param menuVOS 菜单VO
     */
    private void cloudMenu(List<MenuVO> menuVos) {
        for (MenuVO menuVO : menuVos) {
            // 云服务
            MetaMenuVO meta = menuVO.getMeta();
            if (meta != null) {
                continue;
            }
            meta = new MetaMenuVO();
            if (menuVO.getIcon() != null) {
                meta.setIcon(menuVO.getIcon());
            }
            meta.setTitle(menuVO.getTitle());
            menuVO.setMeta(meta);
        }

    }

    @Override
    public void hidden(Menu menu) {
        if (menu.getHidden()) {
            if (menuMapper.selectList(Wrappers.<Menu>lambdaQuery().eq(Menu::getParentId, menu.getMenuId()))
                    .size() > 0) {
                List<Menu> menus = menuMapper
                        .selectList(Wrappers.<Menu>lambdaQuery().eq(Menu::getParentId, menu.getMenuId()));
                for (Menu sonMenu : menus) {
                    sonMenu.setHidden(true);
                    menuMapper.update(sonMenu, Wrappers.<Menu>lambdaQuery().eq(Menu::getMenuId, sonMenu.getMenuId()));
                    hidden(sonMenu);
                }
            }
        }
    }
}
