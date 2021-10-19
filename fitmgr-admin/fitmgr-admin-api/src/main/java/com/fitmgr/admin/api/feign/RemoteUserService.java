package com.fitmgr.admin.api.feign;

import java.util.List;

import javax.validation.Valid;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitmgr.admin.api.config.AdminFeignConfig;
import com.fitmgr.admin.api.dto.UserDTO;
import com.fitmgr.admin.api.dto.UserInfo;
import com.fitmgr.admin.api.entity.Project;
import com.fitmgr.admin.api.entity.Role;
import com.fitmgr.admin.api.entity.SysUser;
import com.fitmgr.admin.api.entity.User;
import com.fitmgr.admin.api.entity.UserCount;
import com.fitmgr.admin.api.entity.UserLoginRecord;
import com.fitmgr.admin.api.vo.PreviewInfoVO;
import com.fitmgr.admin.api.vo.UserVO;
import com.fitmgr.common.core.constant.SecurityConstants;
import com.fitmgr.common.core.constant.ServiceNameConstants;
import com.fitmgr.common.core.util.R;

/**
 * @author Fitmgr
 * @date 2018/6/22
 */
@FeignClient(contextId = "remoteUserService", value = ServiceNameConstants.UMPS_SERVICE, configuration = AdminFeignConfig.class)
public interface RemoteUserService {

    /**
     * 获取当前用户全部信息
     * 
     * @param authHeader authHeader
     * @return R<UserInfo>
     */
    @GetMapping(value = { "/user/info" })
    R<UserInfo> infos(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader);

    /**
     * 通过用户名查询用户、角色信息
     *
     * @param username 用户名
     * @param from     调用标志
     * @return R<UserInfo>
     */
    @GetMapping("/user/info/{username}")
    R<UserInfo> info(@PathVariable("username") String username, @RequestHeader(SecurityConstants.FROM) String from);

    /**
     * loginInfo
     * 
     * @param username username
     * @param from     from
     * @return R<UserInfo>
     */
    @GetMapping("/user/loginInfo/{username}")
    R<UserInfo> loginInfo(@PathVariable("username") String username,
            @RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 添加用户
     *
     * @param userDTO 用户接收入参DTO
     * @return R
     */
    @PostMapping("/user")
    R saveUser(@Valid @RequestBody UserDTO userDTO);

    /**
     * 删除用户
     *
     * @param userId 用户id
     * @return R
     */
    @DeleteMapping("/user/{userId}")
    R deleteById(@PathVariable(name = "userId") Integer userId);

    /**
     * 修改用户信息
     *
     * @param userDTO 用户接收入参DTO
     * @return R
     */
    @PutMapping("/user")
    R updateUser(@RequestBody UserDTO userDTO);

    /**
     * 修改当前用户密码
     * 
     * @param userDTO    userDTO
     * @param authHeader authHeader
     * @return R
     */
    @PutMapping("/user/password")
    R updateUserPassword(@RequestBody UserDTO userDTO,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader);

    /**
     * 修改用户启用/禁用状态
     *
     * @param userDTO 用户接收入参DTO
     * @return R
     */
    @PutMapping("/user/status")
    R updateUserStatus(@RequestBody UserDTO userDTO);

    /**
     * 通过id查询用户详情信息
     *
     * @param userId 用户id
     * @return 用户详情信息
     */
    @GetMapping("/user/user-info/{userId}")
    R<UserVO> getById(@PathVariable(name = "userId") Integer userId);

    /**
     * getByIdForFrom
     * 
     * @param userId userId
     * @param from   from
     * @return R<UserVO>
     */
    @GetMapping("/user/user-info/{userId}")
    R<UserVO> getByIdForFrom(@PathVariable(name = "userId") Integer userId,
            @RequestHeader(SecurityConstants.FROM) String from);

    @GetMapping("/user-summary/username/{username}")
    R<User> querySummaryByUsername(@PathVariable(name = "username") String username);

    /**
     * 通过唯一username查询用户详情信息
     *
     * @param username 唯一用户账号
     * @return 用户详情信息
     */
    @GetMapping("/user/user-info/username/{username}")
    R<UserVO> queryDetailsByUsername(@PathVariable(name = "username") String username);

    /**
     * queryDetailsByEmailOrPhone
     * 
     * @param emailOrPhone emailOrPhone
     * @param from         from
     * @return R<List<User>>
     */
    @GetMapping("/user/user-info/emailOrPhone/{emailOrPhone}")
    R<List<User>> queryDetailsByEmailOrPhone(@PathVariable(name = "emailOrPhone") String emailOrPhone,
            @RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 查询全部用户列表信息
     *
     * @return List<User>
     */
    @GetMapping("/user/user-lists")
    R<List<User>> selectUserList();

    /**
     * 获取用户列表(quota使用)
     *
     * @param userDTO
     * @return R<List<User>>
     */
    @PostMapping("/user/list/quota")
    R<List<User>> userLists(@RequestBody UserDTO userDTO);

    /**
     * 通过用户id查询对应租户下的用户信息列表
     *
     * @param userId 用户id
     * @return 租户下的用户信息列表
     */
    @GetMapping("/user/last-tenant/{userId}")
    R<List<User>> selectTenantById(@PathVariable(name = "userId") Integer userId);

    /**
     * 通过用户id查询该用户所在project列表信息
     *
     * @param userId 用户id
     * @return project信息
     */
    @GetMapping("/user/project-info/{userId}")
    R<List<Project>> selectProjectById(@PathVariable(name = "userId") Integer userId);

    /**
     * 通过用户id查询该用户所属角色列表信息
     *
     * @param userId 用户id
     * @return 角色信息
     */
    @GetMapping("/user/role-list/{userId}")
    R<List<Role>> selectRoleById(@PathVariable(name = "userId") Integer userId);

    /**
     * 通过tenantId查询所对应的tenant下的所有用户信息列表
     *
     * @param tenantId  租户id
     * @param queryName 模糊查询名称
     * @return 用户信息列表
     */
    @GetMapping("/user/user-lists/{tenantId}")
    R<List<UserVO>> queryUserByTenantId(@PathVariable(name = "tenantId") Integer tenantId,
            @RequestParam(value = "queryName", required = false) String queryName);

    /**
     * 通过tenantId列表查询所对应的tenant下的所有用户信息列表
     * 
     * @param tenantIdList tenantIdList
     * @param authHeader   authHeader
     * @return R<List<UserVO>>
     */
    @PostMapping("/user/user-lists/tenantId-list")
    R<List<UserVO>> queryUserByTenantIdList(@RequestBody List<Integer> tenantIdList,
            @RequestHeader(SecurityConstants.FROM) String authHeader);

    /**
     * 通过tenantId分页查询用户信息
     * 
     * @param current current
     * @param size    size
     * @param userDTO userDTO
     * @return R
     */
    @PostMapping("/user/tenantId/page")
    R selectUserListByCondition(@RequestParam(value = "current") Long current, @RequestParam(value = "size") Long size,
            @RequestBody UserDTO userDTO);

    /**
     * 通过租户id查询该租户下所有角色列表信息
     *
     * @param tenantId tenantId
     * @return R
     */
    @GetMapping("/user/role-list/tenant/{tenantId}")
    R selectRoleByTenantId(@PathVariable(name = "tenantId") Integer tenantId);

    /**
     * 通过tenantId列表查询每个tenant下的用户数
     *
     * @param tenantIdList 租户id列表
     * @return 用户信息列表
     */
    @PostMapping("/user/userCount-lists/tenantId-list")
    public R<List<UserCount>> queryUserCountByTenantIdList(@RequestBody List<Integer> tenantIdList);

    /**
     * 通过projectId列表查询每个project下的用户数
     *
     * @param tenantIdList 租户id列表
     * @return 用户信息列表
     */
    @PostMapping("/user/userCount-lists/projectId-list")
    public R<List<UserCount>> queryUserCountByProjectIdList(@RequestBody List<Integer> projectIdList);

    /**
     * 通过projectId查询用户信息列表
     *
     * @param projectId 项目id
     * @return 用户信息列表
     */
    @GetMapping("/user/user-list/{projectId}")
    R<List<UserVO>> queryUserByProjectId(@PathVariable(name = "projectId") Integer projectId);

    /**
     * 通过projectId查询该peiject对应的tenant下的所有用户信息列表
     *
     * @param projectId 项目id
     * @param queryName 模糊查询名称
     * @return 用户信息列表
     */
    @GetMapping("/user/tenant-user-list/{projectId}")
    R<List<UserVO>> queryTenantUserByProjectId(@PathVariable(name = "projectId") Integer projectId, String queryName);

    /**
     * 通过用户id查询角色所属类型
     *
     * @param userId 用户id
     * @return 角色所属类型
     */
    @GetMapping("/user/query-type/{userId}")
    R<UserVO> queryAffiliationType(@PathVariable("userId") Integer userId);

    /**
     * 用户中心信息预览
     *
     * @param userDTO 用户传输对象
     * @return PreviewInfoVO 用户中心信息预览展示信息
     */
    @PostMapping("/user/preview-information")
    R<PreviewInfoVO> previewInformation(@RequestBody UserDTO userDTO);

    /**
     * 用户租户project关联信息
     *
     * @return UserVO
     */
    @GetMapping("/user/user-tenant-project/relation")
    R<UserVO> userTenantProjectRelation();

    /**
     * 通过角色_CODE_获取拥有该角色的所有用户
     *
     * @param roleCode 角色Code
     * @return List<UserVO> 用户集合
     */
    @GetMapping("/user/user-info-list/{roleCode}")
    R<List<UserVO>> queryUserInfoByRoleCode(@PathVariable(name = "roleCode") String roleCode);

    /**
     * 通过角色_CODE_获取拥有该角色的所有用户，内部调用
     * 
     * @param roleCode roleCode
     * @param from     from
     * @return R<List<UserVO>>
     */
    @GetMapping("/user/user-info-list/inner/{roleCode}")
    R<List<UserVO>> queryInnerUserInfoByRoleCode(@PathVariable(name = "roleCode") String roleCode,
            @RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 通过租户id和角色code查询拥有该角色的所有用户，内部调用
     * 
     * @param tenantId tenantId
     * @param roleCode roleCode
     * @param from     from
     * @return R<List<UserVO>>
     */
    @GetMapping("/user/user-info-list/inner/{tenantId}/{roleCode}")
    R<List<UserVO>> queryInnerUserInfoByTenantIdAndRoleCode(@PathVariable(name = "tenantId") Integer tenantId,
            @PathVariable(name = "roleCode") String roleCode, @RequestHeader(SecurityConstants.FROM) String from);

    /**
     * queryInnerUserInfoByTenantIdAndRoleCodes
     * 
     * @param tenantId  tenantId
     * @param roleCodes roleCodes
     * @param from      from
     * @return R<List<UserVO>>
     */
    @GetMapping("/user/user-info-list/inner/roleCodes/{tenantId}")
    R<List<UserVO>> queryInnerUserInfoByTenantIdAndRoleCodes(@PathVariable(name = "tenantId") Integer tenantId,
            @RequestParam(name = "roleCodes") List<String> roleCodes,
            @RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 根据ProjectId和roleCode查询project对应的某个角色的所有用户
     * 
     * @param projectId projectId
     * @param roleCode  roleCode
     * @return R<List<UserVO>>
     */
    @GetMapping("/user/project/user/list")
    R<List<UserVO>> queryUserInfoByProjectIdAndRoleCode(@RequestParam("projectId") Integer projectId,
            @RequestParam("roleCode") String roleCode);

    /**
     * 据ProjectId和roleCode查询project对应的某个角色的所有用户, 内部调用
     * 
     * @param projectId projectId
     * @param roleCode  roleCode
     * @param from      from
     * @return R<List<UserVO>>
     */
    @GetMapping("/user/inner/project/user/list")
    R<List<UserVO>> queryInnerUserInfoByProjectIdAndRoleCode(@RequestParam("projectId") Integer projectId,
            @RequestParam("roleCode") String roleCode, @RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 通过角色_CODE_获取拥有该角色的所有用户
     * 
     * @param roleCode   roleCode
     * @param authHeader authHeader
     * @return R<List<UserVO>>
     */
    @GetMapping("/user/user-list/bycode/{roleCode}")
    R<List<UserVO>> queryUserListByRoleCode(@PathVariable(name = "roleCode") String roleCode,
            @RequestHeader(SecurityConstants.FROM) String authHeader);

    /**
     * 通过角色_CODE_获取拥有该角色的所有用户（分页）
     * 
     * @param page     page
     * @param roleCode roleCode
     * @param name     name
     * @return R
     */
    @PostMapping("/user/user-info-list/page")
    R queryUserInfoByRoleCodePage(@RequestBody Page page,
            @RequestParam(name = "roleCode", required = false) String roleCode,
            @RequestParam(name = "name", required = false) String name);

    /**
     * 通过社交账号或手机号查询用户、角色信息
     *
     * @param inStr appid@code
     * @param from  调用标志
     * @return R<UserInfo>
     */
    @GetMapping("/social/info/{inStr}")
    R<UserInfo> social(@PathVariable("inStr") String inStr, @RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 查询上级部门的用户信息(系统自带接口，已被删除-BJX希望留着,用途未知)
     *
     * @param username 用户名
     * @return R<List<SysUser>>
     */
    @GetMapping("/user/ancestor/{username}")
    R<List<SysUser>> ancestorUsers(@PathVariable("username") String username);

    /**
     * 通过用户集合查询用户基本信息
     *
     * @param userIds 用户id集合
     * @return 用户基本信息List
     */
    @PostMapping("/user/user-msg")
    R<List<UserVO>> queryUserMsg(@RequestBody List<Integer> userIds);

    /**
     * queryUserMsgForFrom
     * 
     * @param userIds userIds
     * @param from    from
     * @return R<List<UserVO>>
     */
    @PostMapping("/user/user-msg")
    R<List<UserVO>> queryUserMsgForFrom(@RequestBody List<Integer> userIds,
            @RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 通过用户usernames查询用户信息集合
     * 
     * @param usernames usernames
     * @return R<List<UserVO>>
     */
    @PostMapping("/user/find/users")
    R<List<UserVO>> queryUserByUsernames(@RequestBody List<String> usernames);

    /**
     * 通过角色code获取租户配额管理员或project配额管理员的的用户信息
     * 
     * @param roleCode roleCode
     * @param tpId     tpId
     * @return R<List<User>>
     */
    @GetMapping("/user/ten-pro-usr")
    R<List<User>> getUserBytenOrPro(@RequestParam("roleCode") String roleCode, @RequestParam("tpId") Integer tpId);

    /**
     * queryUserLoginCount
     * 
     * @param username username
     * @return R<UserLoginRecord>
     */
    @GetMapping("/user/login-count/{username}")
    R<UserLoginRecord> queryUserLoginCount(@PathVariable(name = "username") String username);

    /**
     * addLoginCount
     * 
     * @param username username
     * @return R
     */
    @PostMapping("/login-count/add-count/{username}")
    R addLoginCount(@PathVariable(name = "username") String username);

    /**
     * updateUserDefaultTenant
     * 
     * @param userDTO userDTO
     * @return R
     */
    @PostMapping("/user/update-user/default-tenant")
    R updateUserDefaultTenant(@RequestBody UserDTO userDTO);

    /**
     * 修改用户锁定状态
     * 
     * @param userId    userId
     * @param lockState lockState
     * @return R
     */
    @PutMapping("/user/lockById/{userId}")
    public R lockById(@PathVariable(name = "userId") Integer userId,
            @RequestParam(value = "lockState", required = true) String lockState);
}
