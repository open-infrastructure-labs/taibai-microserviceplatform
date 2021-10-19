package com.fitmgr.admin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitmgr.admin.api.entity.Auth;
import com.fitmgr.admin.api.vo.AuthVO;

/**
 * <p>
 * 功能权限表 Mapper 接口
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
public interface AuthMapper extends BaseMapper<Auth> {

    /**
     * 通过资源id获取所有权限(当前角色)
     *
     * @param resourceId 资源id
     * @param roleId     角色id
     * @return List<Integer>
     */
    List<Integer> getResourceByAuth(@Param("resourceId") Integer resourceId, @Param("roleId") Integer roleId);

    /**
     * api接口的数据权限校验
     *
     * @param functionCode 操作唯一编码
     * @param defaultRole  角色id
     * @return AuthVO
     */
    AuthVO getFunctionCodeByAuth(@Param("functionCode") String functionCode, @Param("defaultRole") Integer defaultRole);

    /**
     * getFunctionCodeByAuths
     * 
     * @param functionCode functionCode
     * @param roleIds      roleIds
     * @return List<AuthVO>
     */
    List<AuthVO> getFunctionCodeByAuths(@Param("functionCode") String functionCode,
            @Param("roleIds") List<Integer> roleIds);

    /**
     * getFunctionIdByAuths
     * 
     * @param functionId functionId
     * @param roleIds    roleIds
     * @return AuthVO
     */
    AuthVO getFunctionIdByAuths(@Param("functionId") Integer functionId, @Param("roleIds") List<Integer> roleIds);

    /**
     * 当前角色通过菜单id获取功能按钮和所有权限
     * 
     * @param menuId  menuId
     * @param roleIds roleIds
     * @return List<AuthVO>
     */
    List<AuthVO> getMenuByAuth(@Param("menuId") String menuId, @Param("roleIds") List<Integer> roleIds);

    /**
     * 删除当前角色所有权限
     *
     * @param roleId roleId
     * @return int
     */
    int remove(@Param("roleId") Integer roleId);

    /**
     * 新增/修改角色权限
     *
     * @param roleId 角色id
     * @param auths  权限集合
     * @return int
     */
    int saveAuth(@Param("roleId") Integer roleId, @Param("list") List<Auth> auths);

    /**
     * 通过角色id和操作code获取所有权限
     *
     * @param roleId     资源id
     * @param functionId 操作id
     * @return AuthVO
     */
    AuthVO getRoleIdByAuths(@Param("functionId") Integer functionId, @Param("roleId") Integer roleId);

    /**
     * 新增权限
     *
     * @param roleId     roleId
     * @param menuByAuth menuByAuth
     */
    void addAuths(@Param("roleId") Integer roleId, @Param("list") List<AuthVO> menuByAuth);
}
