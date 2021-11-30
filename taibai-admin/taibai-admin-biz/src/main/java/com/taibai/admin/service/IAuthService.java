package com.taibai.admin.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taibai.admin.api.entity.Auth;
import com.taibai.admin.api.entity.AuthCheck;
import com.taibai.admin.api.vo.AuthVO;
import com.taibai.common.core.util.R;

/**
 * <p>
 * 操作权限表 服务类
 * </p>
 *
 * @author Taibai
 * @since 2019-11-14
 */
public interface IAuthService extends IService<Auth> {

    /**
     * 通过操作id和角色id获取所有权限
     *
     * @param functionId 操作id
     * @param roleId     角色id
     * @return AuthVO
     */
    AuthVO getRoleMenuByAuth(Integer functionId, Integer roleId);

    /**
     * checkByUrlAndMethod
     * 
     * @param apiUrl          apiUrl
     * @param httpMethod      httpMethod
     * @param userId          userId
     * @param defaultTenantId defaultTenantId
     * @return R
     */
    R checkByUrlAndMethod(String apiUrl, String httpMethod, Integer userId, Integer defaultTenantId);

    /**
     * 通过用户id和功能code获取功能权限和数据范围
     *
     * @param code        code
     * @param defaultRole 角色id
     * @return AuthVO
     */
    AuthVO getUserAuth(String code, Integer defaultRole);

    /**
     * 新增/修改角色权限
     *
     * @param auth 权限集合
     * @return R
     */
    R saveAuth(Auth auth);

    /**
     * api接口的数据权限校验
     * 
     * @param functionCode functionCode
     * @param userId       userId
     * @return R
     */
    R authCheck(String functionCode, Integer userId);

    /**
     * newAuthCheck
     * 
     * @param functionCode    functionCode
     * @param userId          userId
     * @param defaultTenantId defaultTenantId
     * @param resTenantId     resTenantId
     * @param resProjectId    resProjectId
     * @param resUserId       resUserId
     * @return R<AuthCheck>
     */
    R<AuthCheck> newAuthCheck(String functionCode, Integer userId, Integer defaultTenantId, Integer resTenantId,
            Integer resProjectId, Integer resUserId);

    /**
     * 添加新角色继承系统角色所有功能权限和数据权限
     *
     * @param roleId        新角色id
     * @param inheritRoleId 继承角色id
     * @return Integer
     */
    Integer inheritAuth(Integer roleId, Integer inheritRoleId);

    /**
     * 通过菜单id获取当前用户的所有权限
     *
     * @param menuId 菜单id
     * @return List<AuthVO>
     */
    List<AuthVO> getMenuByAuth(String menuId);

    /**
     * processAuth
     * 
     * @param roleId roleId
     */
    void processAuth(Integer roleId);

    /**
     * getFunctionIdByAuths
     * 
     * @param functionId functionId
     * @param roleIds    roleIds
     * @return AuthVO
     */
    AuthVO getFunctionIdByAuths(Integer functionId, List<Integer> roleIds);
}
