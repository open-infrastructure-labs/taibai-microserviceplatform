package com.fitmgr.admin.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fitmgr.admin.api.dto.CheckHrefInfoForRetrievePwd;
import com.fitmgr.admin.api.dto.ModifyInfoForRetrievePwd;
import com.fitmgr.admin.api.dto.UserDTO;
import com.fitmgr.admin.api.dto.UserInfo;
import com.fitmgr.admin.api.dto.VerifyInfoForFindPwd;
import com.fitmgr.admin.api.entity.Project;
import com.fitmgr.admin.api.entity.Role;
import com.fitmgr.admin.api.entity.User;
import com.fitmgr.admin.api.entity.UserCount;
import com.fitmgr.admin.api.vo.ImportUserVo;
import com.fitmgr.admin.api.vo.PreviewInfoVO;
import com.fitmgr.admin.api.vo.ProjectRoleVO;
import com.fitmgr.admin.api.vo.TenantOrProjectVO;
import com.fitmgr.admin.api.vo.TenantRoleVO;
import com.fitmgr.admin.api.vo.UserVO;
import com.fitmgr.common.core.util.R;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
public interface IUserService extends IService<User> {

    /**
     * 查询用户登录UserInfo的详细信息
     *
     * @param user 用户
     * @return 当前用户登录UserInfo的详细信息
     */
    UserInfo findUserInfo(User user);

    /**
     * 新增/添加用户信息
     *
     * @param userDTO 用户传输对象
     * @return true/false
     */
    Boolean saveUser(UserDTO userDTO);

    /**
     * 重置密码
     *
     * @param userDTO userDTO
     * @return Boolean
     */
    Boolean resetPassword(UserDTO userDTO);

    /**
     * 通过userId删除用户
     *
     * @param userId 用户ID
     * @return true/false
     */
    Boolean removeUserById(Integer userId);

    /**
     * 更新（当前/其他）用户基本信息
     *
     * @param userDTO 用户信息
     * @return true/false
     */
    Boolean updateUserInfo(UserDTO userDTO);

    /**
     * 更新默认用户账号信息
     *
     * @param userDTO 用户信息
     * @return true/false
     */
    Boolean renameUser(UserDTO userDTO);

    /**
     * 异常：理应由前端进行密码加密(解决明文密码暴露问题) 当前状态：1、前端传入原始密码与新密码都是明文 修改当前用户登录密码
     *
     * @param userDTO 用户信息 userDTO.getToken() 用户登录 token - insert 20.05.02
     * @return true/false
     */

    Boolean updateUserPassword(UserDTO userDTO);

    /**
     * 更新当前用户状态
     *
     * @param userDTO 用户信息
     * @return true/false
     */
    Boolean updateUserStatus(UserDTO userDTO);

    /**
     * 修改用户头像
     *
     * @param userDTO 用户接收入参DTO
     * @return true/false
     */
    Boolean updateUserAvatar(UserDTO userDTO);

    /**
     * 通过userId查询UserVO详情信息
     *
     * @param userId 用户id
     * @return UserVO 用户展示对象
     * @throws NullPointerException
     */
    UserVO queryDetailsByUserId(Integer userId) throws NullPointerException;

    /**
     * queryTenantRoleByUserId
     * 
     * @param userId userId
     * @return List<TenantRoleVO>
     */
    List<TenantRoleVO> queryTenantRoleByUserId(Integer userId);

    /**
     * queryProjectRoleByUserId
     * 
     * @param userId userId
     * @return List<ProjectRoleVO>
     */
    List<ProjectRoleVO> queryProjectRoleByUserId(Integer userId);

    /**
     * 通过唯一username查询用户详情信息
     *
     * @param username 唯一用户账号
     * @return 用户详情信息
     */
    UserVO queryDetailsByUsername(String username);

    /**
     * selectUserListByEmailOrPhone
     * 
     * @param emailOrPhone emailOrPhone
     * @return List<User>
     */
    List<User> selectUserListByEmailOrPhone(String emailOrPhone);

    /**
     * 查询用户列表(无参)
     *
     * @return UserList用户列表
     */
    List<User> selectUserList();

    /**
     * 据条件分页查询UserVO列表
     *
     * @param page    分页条件
     * @param userDTO 分页查询参数DTO
     * @param userId  登录后获取的用户id
     * @return UserVO列表
     */
    IPage<UserVO> selectListByCondition(Page page, UserDTO userDTO, Integer userId);

    /**
     * 分页查询UserDTO列表--无关权限
     * 
     * @param page    page
     * @param userDTO userDTO
     * @return IPage<UserVO>
     */
    IPage<UserVO> selectUsersListByCondition(Page page, UserDTO userDTO);

    /**
     * 通过用户id查询该用户所属角色列表信息
     *
     * @param userId 用户id
     * @return List<Role> 角色列表
     */
    List<Role> queryRoleByUserId(Integer userId);

    /**
     * queryRoleByUserIdAndTenantId
     * 
     * @param userId   userId
     * @param tenantId tenantId
     * @return List<Map<String, Integer>>
     */
    List<Map<String, Integer>> queryRoleByUserIdAndTenantId(Integer userId, Integer tenantId);

    /**
     * selectRoleByTenantId
     * 
     * @param tenantId tenantId
     * @return List<Role>
     */
    List<Role> selectRoleByTenantId(Integer tenantId);

    /**
     * selectRoleByTenantIdAndRoleCode
     * 
     * @param tenantId tenantId
     * @return List<Role>
     */
    List<Role> selectRoleByTenantIdAndRoleCode(Integer tenantId);

    /**
     * 通过用户id查询该用户所在project列表信息
     *
     * @param userId 用户id
     * @return List<Project> 项目列表
     */
    List<Project> queryProjectByUserId(Integer userId);

    /**
     * 通过用户id查询对应租户下的用户信息列表
     *
     * @param userId 用户id
     * @return List<User>
     */
    List<User> queryTenantInfoByUserId(Integer userId);

    /**
     * 通过用户id查询对应租户下的用户信息列表
     * 
     * @param userId    userId
     * @param page      page
     * @param queryName queryName
     * @return IPage<UserVO>
     */
    IPage<UserVO> queryPageTenantInfoByUserId(Integer userId, Page page, String queryName);

    /**
     * 1、通过tenantId查询所对应的tenant下的所有用户信息列表 2、根据当前user查询，该user所在的project列表
     *
     * @param tenantId  租户id
     * @param queryName 模糊查询名称
     * @param page      page
     * @return 用户信息列表
     */
    List<UserVO> queryUserByTenantId(Integer tenantId, String queryName, Page page);

    /**
     * 1、通过tenantId列表查询所对应的tenant下的所有用户信息列表 2、根据当前user查询，该user所在的project列表
     *
     * @param tenantIdList 租户id列表
     * @return 用户信息列表
     */
    List<UserVO> queryUserByTenantIdList(List<Integer> tenantIdList);

    /**
     * 1、通过tenantId查询所对应的tenant下的所有用户信息列表 2、根据当前user查询，该user所在的project列表
     *
     * @param tenantId 租户id
     * @return 用户信息列表
     */
    List<UserVO> queryUserListByTenantId(Integer tenantId);

    /**
     * 通过tenantId列表查询每个tenant下的用户数
     *
     * @param tenantIdList 租户id列表
     * @return 用户信息列表
     */
    List<UserCount> queryUserCountByTenantIdList(List<Integer> tenantIdList);

    /**
     * 通过projectId列表查询每个project下的用户数
     *
     * @param tenantIdList 租户id列表
     * @return 用户信息列表
     */
    List<UserCount> queryUserCountByProjectIdList(List<Integer> projectIdList);

    /**
     * 通过projectId查询用户信息列表
     *
     * @param projectId 项目id
     * @return List<UserVO> 用户信息列表
     */
    List<UserVO> queryUserByProjectId(Integer projectId);

    /**
     * 通过projectId查询该peiject对应的tenant下的所有用户信息列表
     *
     * @param projectId 项目id
     * @param queryName 模糊查询名称
     * @return List<UserVO> 用户信息列表
     */
    List<UserVO> queryTenantUserByProjectId(Integer projectId, String queryName);

    /**
     * 通过用户id查询角色所属类型
     *
     * @param userId 用户id
     * @return UserVO 角色所属类型
     */
    UserVO queryAffiliationType(Integer userId);

    /**
     * 用户中心信息预览
     *
     * @param userDTO userDTO.tenantId 租户id（默认查询所有信息）
     * @return PreviewInfoVO 用户中心预览信息
     */
    PreviewInfoVO previewInformation(UserDTO userDTO);

    /**
     * 查询用户租户project关联信息
     *
     * @return UserVO
     */
    UserVO userTenantProjectRelation();

    /**
     * 通过角色code获取拥有该角色的所有用户
     *
     * @param roleCode 角色唯一Code
     * @return List<UserVO> 用户展示对象集合
     */
    List<UserVO> queryUserInfoByRoleCodeAndTenantId(String roleCode);

    /**
     * queryUserInfoByRoleCodeAndTenantId
     * 
     * @param tenantId tenantId
     * @param roleCode roleCode
     * @return List<UserVO>
     */
    List<UserVO> queryUserInfoByRoleCodeAndTenantId(Integer tenantId, String roleCode);

    /**
     * queryUserInfoByRoleCodesAndTenantId
     * 
     * @param tenantId tenantId
     * @param roleCode roleCode
     * @return List<UserVO>
     */
    List<UserVO> queryUserInfoByRoleCodesAndTenantId(Integer tenantId, List<String> roleCode);

    /**
     * 通过projectId和roleCode查询用户信息
     *
     * @param projectId projectId
     * @param roleCode  roleCode
     * @return List<UserVO>
     */
    List<UserVO> queryUserInfoByProjectIdAndRoleCode(Integer projectId, String roleCode);

    /**
     * 通过角色code获取拥有该角色的所有用户
     *
     * @param roleCode 角色唯一Code
     * @return List<UserVO> 用户展示对象集合
     */
    List<UserVO> queryUserInfoByRoleCode(String roleCode);

    /**
     * 通过角色code获取拥有该角色的所有用户(分页)
     * 
     * @param page     page
     * @param roleCode roleCode
     * @param name     name
     * @return IPage<UserVO>
     */
    IPage<UserVO> queryUserInfoByRoleCodePage(Page page, String roleCode, String name);

    /**
     * 通过用户集合查询用户基本信息
     *
     * @param userIds 用户id集合
     * @return List<UserVO> 用户集合
     */
    List<UserVO> queryUserMsg(List<Integer> userIds);

    /**
     * 通过用户名查询用户信息
     *
     * @param usernames usernames
     * @return List<UserVO>
     */
    List<UserVO> queryUserByUsernames(List<String> usernames);

    /**
     * updateUserLoginTime
     * 
     * @param id id
     * @return Boolean
     */
    Boolean updateUserLoginTime(Integer id);

    /**
     * 通过角色code获取租户配额管理员或project配额管理员的的用户信息
     *
     * @param roleCode 角色唯一Code
     * @param tpId     租户或project的Id
     * @return R<List<User>>
     */
    R<List<User>> getUserByTenOrPro(String roleCode, Integer tpId);

    /**
     * 根据当前用户的角色获取租户列表或者project列表
     *
     * @param tenantId    租户id
     * @param defaultRole 角色id
     * @param id          用户id
     * @return R<TenantOrProjectVO>
     */
    R<TenantOrProjectVO> getTenOrProList(Integer tenantId, List<Integer> defaultRole, Integer id);

    /**
     * updateTenantId
     */
    void updateTenantId();

    /**
     * updateOrdinary
     */
    void updateOrdinary();

    /**
     * mergeUser
     */
    void mergeUser();

    /**
     * updateProjectTenant
     */
    void updateProjectTenant();

    /**
     * clearRedisCache
     */
    void clearRedisCache();

    /**
     * 条件查询用户列表
     *
     * @param userDTO userDTO
     * @return List<User>
     */
    List<User> userLists(UserDTO userDTO);

    /**
     * updateDefaultTenant
     * 
     * @param userDTO userDTO
     */
    void updateDefaultTenant(UserDTO userDTO);

    /**
     * refreshUrp
     */
    void refreshUrp();

    /**
     * importUser
     * 
     * @param importUserVo importUserVo
     * @return R
     */
    R importUser(ImportUserVo importUserVo);

    /**
     * downloadFail
     * 
     * @param response response
     * @param bucket   bucket
     * @param fileName fileName
     * @throws Exception
     */
    void downloadFail(HttpServletResponse response, String bucket, String fileName) throws Exception;

    /**
     * queryProgress
     * 
     * @param bucket   bucket
     * @param fileName fileName
     * @return ImportUserVo
     */
    ImportUserVo queryProgress(String bucket, String fileName);

    /**
     * queryLogs
     * 
     * @return List<ImportUserVo>
     */
    List<ImportUserVo> queryLogs();

    /**
     * checkLoginNeedValidateCode
     * 
     * @param userName userName
     * @return boolean
     */
    boolean checkLoginNeedValidateCode(String userName);

    /**
     * verifyIdentity
     * 
     * @param verifyInfoForFindPwd verifyInfoForFindPwd
     * @return R
     */
    R verifyIdentity(VerifyInfoForFindPwd verifyInfoForFindPwd);

    /**
     * modifyPwdForRetrievePwd
     * 
     * @param modifyInfoForRetrievePwd modifyInfoForRetrievePwd
     * @return R
     */
    R modifyPwdForRetrievePwd(ModifyInfoForRetrievePwd modifyInfoForRetrievePwd);

    /**
     * checkHrefForRetrievePwd
     * 
     * @param checkHrefInfoForRetrievePwd checkHrefInfoForRetrievePwd
     * @return R
     */
    R checkHrefForRetrievePwd(CheckHrefInfoForRetrievePwd checkHrefInfoForRetrievePwd);
}
