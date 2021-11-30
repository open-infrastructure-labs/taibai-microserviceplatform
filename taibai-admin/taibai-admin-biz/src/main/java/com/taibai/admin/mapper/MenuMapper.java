package com.taibai.admin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taibai.admin.api.entity.Menu;
import com.taibai.admin.api.vo.MenuVO;

/**
 * <p>
 * 菜单表 Mapper 接口
 * </p>
 *
 * @author Taibai
 * @since 2019-11-18
 */
public interface MenuMapper extends BaseMapper<Menu> {
    /**
     * getSysMenu
     * 
     * @param roleId roleId
     * @return List<MenuVO>
     */
    List<MenuVO> getSysMenu(@Param("roleId") Integer roleId);

    /**
     * getSysMenus
     * 
     * @param roleIds roleIds
     * @return List<MenuVO>
     */
    List<MenuVO> getSysMenus(@Param("roleIds") List<Integer> roleIds);

    /**
     * configMenuAuth
     * 
     * @param roleId roleId
     * @param list   list
     * @return int
     */
    int configMenuAuth(@Param("roleId") Integer roleId, @Param("list") List<String> list);

    /**
     * deleteMenuAuth
     * 
     * @param roleId roleId
     * @return int
     */
    int deleteMenuAuth(@Param("roleId") Integer roleId);
}
