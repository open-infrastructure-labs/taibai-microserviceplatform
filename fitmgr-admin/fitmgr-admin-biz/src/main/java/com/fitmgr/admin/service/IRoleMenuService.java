package com.fitmgr.admin.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fitmgr.admin.api.entity.RoleMenu;

/**
 * <p>
 * 角色菜单表 服务类
 * </p>
 *
 * @author Fitmgr
 * @since 2020-04-21
 */
public interface IRoleMenuService extends IService<RoleMenu> {
    /**
     * batchInsert
     * 
     * @param roleMenus roleMenus
     * @return boolean
     */
    boolean batchInsert(List<RoleMenu> roleMenus);

}
