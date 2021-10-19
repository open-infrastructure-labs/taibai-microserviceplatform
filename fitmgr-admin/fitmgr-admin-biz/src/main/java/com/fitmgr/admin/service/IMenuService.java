package com.fitmgr.admin.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fitmgr.admin.api.dto.MenuTree;
import com.fitmgr.admin.api.entity.Menu;
import com.fitmgr.admin.api.entity.MenuOrder;
import com.fitmgr.admin.api.entity.Subpage;
import com.fitmgr.admin.api.entity.TenantService;
import com.fitmgr.admin.api.vo.MenuVO;
import com.fitmgr.common.core.util.R;

/**
 * <p>
 * 菜单表 服务类
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-18
 */
public interface IMenuService extends IService<Menu> {

    /**
     * 获取当前用户的菜单树（系统菜单+服务菜单）
     *
     * @param roleId roleId
     * @return List<MenuTree>
     */
    List<MenuTree> getUserMenu(Integer roleId);

    /**
     * 获取该角色ID的菜单树（系统菜单+服务菜单）
     *
     * @param roleId 角色id
     * @return List<MenuTree>
     */
    List<MenuTree> getRoleMenu(Integer roleId);

    /**
     * 获取当前角色的菜单树（系统菜单+服务菜单）
     * 
     * @param roleId               roleId
     * @param filterTenantServices filterTenantServices
     * @return List<MenuTree>
     */
    List<MenuTree> getUserMenus(List<Integer> roleId, List<TenantService> filterTenantServices);

    /**
     * 返回所有的树形菜单集合（系统菜单+服务菜单）
     *
     * @return List<MenuTree>
     */
    List<MenuTree> getTree();

    /**
     * 返回系统树形菜单集合-系统菜单
     *
     * @return List<MenuTree>
     */
    List<MenuTree> getSysTree();

    /**
     * 根据角色配置菜单权限（功能权限联动修改）
     * 
     * @param defaultRole defaultRole
     * @param menuIds     menuIds
     * @return R
     */
    R configMenuAuth(Integer defaultRole, List<String> menuIds);

    /**
     * 获取角色系统菜单list
     *
     * @param roleId 角色id
     * @return List<MenuVO>
     */
    List<MenuVO> getList(Integer roleId);

    /**
     * 设置menu顺序
     * 
     * @param menuOrder menuOrder
     * @return R
     */
    public R updateMenuOrder(MenuOrder menuOrder);

    /**
     * 新增子页面
     *
     * @param subpage 子页面对象
     * @return R
     */
    R addSubpage(Subpage subpage);

    /**
     * 修改子页面
     *
     * @param subpage 子页面对象
     * @return R
     */
    R modifySubpage(Subpage subpage);

    /**
     * 删除子页面
     *
     * @param id 子页面ID
     * @return R
     */
    R deleteSubpage(Integer id);

    /**
     * 删除菜单
     *
     * @param menuId menuId
     * @return R
     */
    R deleteMenu(String menuId);

    /**
     * 查询指定菜单下的子页面
     *
     * @param menuId 菜单Id
     * @return List<Subpage>
     */
    List<Subpage> getSubpageByMenuId(String menuId);

    /**
     * 递归隐藏菜单，若上层菜单隐藏，下层菜单也随之隐藏
     * 
     * @param menu menu
     */
    void hidden(Menu menu);
}
