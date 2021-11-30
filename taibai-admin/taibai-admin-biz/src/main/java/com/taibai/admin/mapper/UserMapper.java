package com.taibai.admin.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taibai.admin.api.dto.UserDTO;
import com.taibai.admin.api.entity.AuthCheck;
import com.taibai.admin.api.entity.Project;
import com.taibai.admin.api.entity.ProjectRole;
import com.taibai.admin.api.entity.Role;
import com.taibai.admin.api.entity.Tenant;
import com.taibai.admin.api.entity.TenantRole;
import com.taibai.admin.api.entity.User;
import com.taibai.admin.api.entity.UserCount;
import com.taibai.admin.api.vo.ProjectVO;
import com.taibai.admin.api.vo.RoleVO;
import com.taibai.admin.api.vo.TenantVO;
import com.taibai.admin.api.vo.UserVO;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author Taibai
 * @since 2019-11-16
 */
public interface UserMapper extends BaseMapper<User> {

    /**
     * 通过userId查询UserVO详情信息
     *
     * @param userId 用户id
     * @return UserVO
     */
    UserVO queryDetailsByUserId(@Param("userId") Integer userId);

    /**
     * queryRoleByUserId
     * 
     * @param userId userId
     * @return List<Role>
     */
    List<Role> queryRoleByUserId(@Param("userId") Integer userId);

    /**
     * queryRoleByUserIdAndTenantId
     * 
     * @param userId   userId
     * @param tenantId tenantId
     * @return List<Map<String, Integer>>
     */
    List<Map<String, Integer>> queryRoleByUserIdAndTenantId(@Param("userId") Integer userId,
            @Param("tenantId") Integer tenantId);

    /**
     * queryRoleByUserIdAndRoleLevel
     * 
     * @param userId    userId
     * @param roleLevel roleLevel
     * @return List<Role>
     */
    List<Role> queryRoleByUserIdAndRoleLevel(@Param("userId") Integer userId, @Param("roleLevel") Integer roleLevel);

    /**
     * 通过租户id查询该租户下所有角色列表信息
     *
     * @param tenantId 租户id
     * @return 角色信息
     */
    List<Role> selectRoleByTenantId(@Param("tenantId") Integer tenantId);

    /**
     * 通过用户id查询该用户所在project列表信息
     *
     * @param userId 用户id
     * @return List<Project> 项目集合
     */
    List<Project> queryProjectByUserId(@Param("userId") Integer userId);

    /**
     * 通过租户id查询该租户下所有角色列表信息
     *
     * @param tenantId 租户id
     * @return 角色信息
     */
    List<Role> selectRoleByTenantIdAndRoleCode(@Param("tenantId") Integer tenantId);

    /**
     * 通过用户id查询该用户所在Tenant列表信息
     *
     * @param userId 用户id
     * @return List<Tenant> 项目集合
     */
    List<Tenant> queryTenantByUserId(@Param("userId") Integer userId);

    /**
     * 根据条件分页查询 UserVO 列表
     *
     * @param page    分页参数
     * @param userDTO 查询条件
     * @return 用户列表
     */
    IPage<UserVO> selectListByCondition(@Param("page") Page page, @Param("userDTO") UserDTO userDTO);

    /**
     * selectListByProjectCondition
     * 
     * @param page      page
     * @param userDTO   userDTO
     * @param authCheck authCheck
     * @return IPage<UserVO>
     */
    IPage<UserVO> selectListByProjectCondition(@Param("page") Page page, @Param("userDTO") UserDTO userDTO,
            @Param("authCheck") AuthCheck authCheck);

    /**
     * selectUsersListNoPageByCondition
     * 
     * @param userDTO userDTO
     * @return List<User>
     */
    List<User> selectUsersListNoPageByCondition(@Param("userDTO") UserDTO userDTO);

    /**
     * 通过tenantId对应的tenant下的所有用户信息列表
     *
     * @param tenantId  租户id
     * @param queryName 模糊查询名称
     * @return 用户列表
     */
    List<UserVO> queryUserByTenantId(@Param("tenantId") Integer tenantId, @Param("queryName") String queryName);

    /**
     * 通过tenantId对应的tenant下的所有用户信息列表
     *
     * @param tenantIdList 租户id列表
     * @param queryName    模糊查询名称
     * @return 用户列表
     */
    List<UserVO> queryUserByTenantIdList(@Param("tenantIdList") List<Integer> tenantIdList);

    /**
     * 通过tenantId对应的tenant下的所有用户信息列表
     *
     * @param tenantId 租户id
     * @return 用户列表
     */
    List<UserVO> queryUserListByTenantId(@Param("tenantId") Integer tenantId);

    /**
     * 通过tenantId列表查询每个tenant下的用户数
     *
     * @param tenantIdList 租户id列表
     * @param queryName    模糊查询名称
     * @return 用户列表
     */
    List<UserCount> queryUserCountByTenantIdList(@Param("tenantIdList") List<Integer> tenantIdList);

    /**
     * 通过projectId列表查询每个project下的用户数
     *
     * @param tenantIdList 租户id列表
     * @param queryName    模糊查询名称
     * @return 用户列表
     */
    List<UserCount> queryUserCountByProjectIdList(@Param("projectIdList") List<Integer> projectIdList);

    /**
     * 通过projectId查询user用户列表
     *
     * @param projectId 项目id
     * @return 用户列表
     */
    List<UserVO> queryUserByProjectId(@Param("projectId") Integer projectId);

    /**
     * 通过projectId查询该peiject对应的tenant下的所有用户信息列表
     *
     * @param projectId 项目id
     * @param queryName 模糊查询名称
     * @return 用户列表
     */
    List<UserVO> queryTenantUserByProjectId(@Param("projectId") Integer projectId,
            @Param("queryName") String queryName);

    /**
     * 通过用户id查询角色所属类型
     *
     * @param userId 用户id
     * @return 用户展示对象
     */
    UserVO queryAffiliationType(@Param("userId") Integer userId);

    /**
     * 租户数量
     * 
     * @return TenantVO
     */
    TenantVO queryTenantNumber();

    /**
     * projecr数量
     * 
     * @param userDTO userDTO
     * @return ProjectVO
     */
    ProjectVO queryProjectNumber(@Param("userDTO") UserDTO userDTO);

    /**
     * 所有role数量
     * 
     * @return RoleVO
     */
    RoleVO queryAllRoleNumber();

    /**
     * role数量
     * 
     * @param userDTO userDTO
     * @return RoleVO
     */
    RoleVO queryRoleNumber(@Param("userDTO") UserDTO userDTO);

    /**
     * user数量
     * 
     * @param userDTO userDTO
     * @return UserVO
     */
    UserVO queryUserNumber(@Param("userDTO") UserDTO userDTO);

    /**
     * queryUserRecentlyNumber
     * 
     * @param userDTO userDTO
     * @return List<UserVO>
     */
    List<UserVO> queryUserRecentlyNumber(@Param("userDTO") UserDTO userDTO);

    /**
     * queryProjectUserRecentlyNumber
     * 
     * @param userDTO userDTO
     * @return List<UserVO>
     */
    List<UserVO> queryProjectUserRecentlyNumber(@Param("userDTO") UserDTO userDTO);

    /**
     * 通过角色code获取拥有该角色的所有用户
     *
     * @param roleCode 角色Code
     * @return List<UserVO> 用户集合
     */
    List<UserVO> queryUserInfoByRoleCode(@Param("roleCode") String roleCode);

    /**
     * 通过角色code+tenantId获取某个租户下拥有该角色的所有用户
     * 
     * @param roleCode roleCode
     * @param tenantId tenantId
     * @return List<UserVO>
     */
    List<UserVO> queryUserInfoByRoleCodeAndTenantId(@Param("roleCode") String roleCode,
            @Param("tenantId") Integer tenantId);

    /**
     * queryUserInfoByRoleCodesAndTenantId
     * 
     * @param sysRoleCodes sysRoleCodes
     * @param tenRoleCodes tenRoleCodes
     * @param tenantId     tenantId
     * @return List<UserVO>
     */
    List<UserVO> queryUserInfoByRoleCodesAndTenantId(@Param("sysRoleCodes") List<String> sysRoleCodes,
            @Param("tenRoleCodes") List<String> tenRoleCodes, @Param("tenantId") Integer tenantId);

    /**
     * queryUserInfoByProjectIdAndRoleCode
     * 
     * @param roleCode  roleCode
     * @param projectId projectId
     * @return List<UserVO>
     */
    List<UserVO> queryUserInfoByProjectIdAndRoleCode(@Param("roleCode") String roleCode,
            @Param("projectId") Integer projectId);

    /**
     * queryUserInfoByRoleIdAndTenantId
     * 
     * @param roleId   roleId
     * @param tenantId tenantId
     * @return List<UserVO>
     */
    List<UserVO> queryUserInfoByRoleIdAndTenantId(@Param("roleId") Integer roleId, @Param("tenantId") Integer tenantId);

    /**
     * 通过角色code获取拥有该角色的所有用户(分页)
     * 
     * @param page     page
     * @param name     name
     * @param tenantId tenantId
     * @return IPage<UserVO>
     */
    IPage<UserVO> queryUserInfoByName(@Param("page") Page page, @Param("name") String name,
            @Param("tenantId") Integer tenantId);

    /**
     * 通过用户id，查询对应的用户角色信息
     * 
     * @param userIdLst userIdLst
     * @return List<UserVO>
     */
    List<UserVO> queryUserAndRoleInfoByUserIdList(@Param("userIdLst") List<Integer> userIdLst);

    /**
     * 通过角色code获取拥有该角色的所有用户(分页)
     * 
     * @param page     page
     * @param roleCode roleCode
     * @param name     name
     * @param tenantId tenantId
     * @return IPage<UserVO>
     */
    IPage<UserVO> queryUserInfoByRoleCodePage(@Param("page") Page page, @Param("roleCode") String roleCode,
            @Param("name") String name, @Param("tenantId") Integer tenantId);

    /**
     * 通过用户集合查询用户基本信息
     *
     * @param userIds 用户id集合
     * @return List<UserVO> 用户集合
     */
    List<UserVO> queryUserMsg(@Param("userIds") List<Integer> userIds);

    /**
     * 通过用户名查询用户信息
     *
     * @param usernames usernames
     * @return List<UserVO>
     */
    List<UserVO> queryUserByUsernames(@Param("usernames") List<String> usernames);

    /**
     * updateUserLoginTime
     * 
     * @param id id
     * @return Boolean
     */
    Boolean updateUserLoginTime(@Param("id") Integer id);

    /**
     * 通过角色code获取租户配额管理员的用户信息
     *
     * @param roleCode roleCode
     * @param tpId     tpId
     * @return List<User>
     */
    List<User> getUserByTenant(@Param("roleCode") String roleCode, @Param("tpId") Integer tpId);

    /**
     * 通过角色code获取project配额管理员的的用户信息
     *
     * @param roleCode roleCode
     * @param tpId     tpId
     * @return List<User>
     */
    List<User> getUserByProject(@Param("roleCode") String roleCode, @Param("tpId") Integer tpId);

    /**
     * queryTenantRoleByUserId
     * 
     * @param userId userId
     * @return List<TenantRole>
     */
    List<TenantRole> queryTenantRoleByUserId(@Param("userId") Integer userId);

    /**
     * queryProjectRoleByUserId
     * 
     * @param userId userId
     * @return List<ProjectRole>
     */
    List<ProjectRole> queryProjectRoleByUserId(@Param("userId") Integer userId);
}
