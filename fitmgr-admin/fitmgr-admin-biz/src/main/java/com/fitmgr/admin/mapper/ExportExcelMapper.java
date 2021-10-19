package com.fitmgr.admin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitmgr.admin.api.entity.ExcelLevelMenu;
import com.fitmgr.admin.api.entity.Function;
import com.fitmgr.admin.api.entity.Menu;
import com.fitmgr.admin.api.entity.Role;
import com.fitmgr.admin.api.entity.RoleFunction;

/**
 * @author Fitmgr
 * @date ：Created in 2021/1/11 14:26
 * @modified By：
 */
public interface ExportExcelMapper extends BaseMapper {
    /**
     * findRoleMenu
     * 
     * @return List<Menu>
     */
    @Select({ " SELECT " + " role.role_name AS `name`, " + " menu.title " + " FROM " + " menu "
            + " LEFT JOIN role_menu ON menu.menu_id = role_menu.menu_id "
            + " LEFT JOIN role ON role.id = role_menu.role_id " + " WHERE "
            + " menu.del_flag = 0 AND role.del_flag = 0 " })
    List<Menu> findRoleMenu();

    /**
     * findParentId
     * 
     * @return List<Menu>
     */
    @Select({ "select menu_id ,parent_id,title from menu  " })
    List<Menu> findParentId();

    /**
     * findRoleNameList
     * 
     * @return List<String>
     */
    @Select({ " SELECT role_name from role where del_flag = 0 " })
    List<String> findRoleNameList();

    /**
     * findRoleIdList
     * 
     * @return List<Role>
     */
    @Select({ "select id,role_name from role where del_flag = 0 " })
    List<Role> findRoleIdList();

    /**
     * findMenuTitle
     * 
     * @return List<String>
     */
    @Select({ "SELECT title from menu WHERE del_flag = 0" })
    List<String> findMenuTitle();

    /**
     * findAllFirstMenu
     * 
     * @return List<String>
     */
    @Select({ "SELECT title from menu where parent_id = '0' and del_flag = 0" })
    List<String> findAllFirstMenu();

    /**
     * findLevelMenuList
     * 
     * @return List<ExcelLevelMenu>
     */
    @Select({ "SELECT" + " a.title,a.menu_id,a.parent_id,b.title as parent_title" + " FROM" + " menu AS a"
            + " LEFT JOIN menu AS b ON a.parent_id = b.menu_id" + " WHERE a.del_flag = 0 and b.del_flag = 0" })
    List<ExcelLevelMenu> findLevelMenuList();

    /**
     * findRoleFunction
     * 
     * @return List<RoleFunction>
     */
    @Select({ "SELECT " + " `function`.`name` as function_name,role.role_name,auth.operating_range " + " FROM "
            + " `function` " + " LEFT JOIN auth ON `function`.id = auth.function_id "
            + " LEFT JOIN role ON role.id = auth.role_id " + " where auth.`status` = 0" + " AND `function`.del_flag = 0"
            + " AND auth.del_flag = 0 " + " and role.del_flag = 0 " + " ORDER BY `function`.`name` " })
    List<RoleFunction> findRoleFunction();

    /**
     * findFunction
     * 
     * @return List<Function>
     */
    @Select({ " SELECT `function`.`name`,`function`.function_code FROM `function` where del_flag = 0 " })
    List<Function> findFunction();

}
