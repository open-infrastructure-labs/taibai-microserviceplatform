package com.taibai.admin.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taibai.admin.api.constants.RoleLevelEnum;
import com.taibai.admin.api.constants.UserLockStatus;
import com.taibai.admin.api.dto.CheckHrefInfoForRetrievePwd;
import com.taibai.admin.api.dto.ModifyInfoForRetrievePwd;
import com.taibai.admin.api.dto.TokenDTO;
import com.taibai.admin.api.dto.UserDTO;
import com.taibai.admin.api.dto.UserInfo;
import com.taibai.admin.api.dto.VerifyInfoForFindPwd;
import com.taibai.admin.api.entity.LockedAccountRecord;
import com.taibai.admin.api.entity.LoginFailRecord;
import com.taibai.admin.api.entity.Project;
import com.taibai.admin.api.entity.RelationRole;
import com.taibai.admin.api.entity.Role;
import com.taibai.admin.api.entity.Tenant;
import com.taibai.admin.api.entity.User;
import com.taibai.admin.api.entity.UserCount;
import com.taibai.admin.api.entity.UserLoginRecord;
import com.taibai.admin.api.entity.UserRoleProject;
import com.taibai.admin.api.feign.RemoteTokenService;
import com.taibai.admin.api.validation.Password;
import com.taibai.admin.api.validation.Save;
import com.taibai.admin.api.validation.Update;
import com.taibai.admin.api.vo.ImportUserVo;
import com.taibai.admin.api.vo.PreviewInfoVO;
import com.taibai.admin.api.vo.ProjectRoleVO;
import com.taibai.admin.api.vo.TenantOrProjectVO;
import com.taibai.admin.api.vo.TenantRoleVO;
import com.taibai.admin.api.vo.UserVO;
import com.taibai.admin.exceptions.UserCenterException;
import com.taibai.admin.mapper.TenantMapper;
import com.taibai.admin.service.ILockedAccountRecordService;
import com.taibai.admin.service.ILoginFailRecordService;
import com.taibai.admin.service.IProjectService;
import com.taibai.admin.service.IRoleService;
import com.taibai.admin.service.IUserService;
import com.taibai.admin.service.UserLoginRecordService;
import com.taibai.admin.service.impl.UserRoleProjectServiceImpl;
import com.taibai.admin.syncproject.UserImport;
import com.taibai.admin.utils.AdminUtils;
import com.taibai.common.core.constant.CacheConstants;
import com.taibai.common.core.constant.CommonConstants;
import com.taibai.common.core.constant.SecurityConstants;
import com.taibai.common.core.constant.enums.BusinessEnum;
import com.taibai.common.core.constant.enums.ResponseCodeEnum;
import com.taibai.common.core.util.R;
import com.taibai.common.log.annotation.SysLog;
import com.taibai.common.log.util.SysLogUtils;
import com.taibai.common.security.annotation.Inner;
import com.taibai.common.security.service.FitmgrUser;
import com.taibai.common.security.util.AuthUtils;
import com.taibai.common.security.util.SecurityUtils;
import com.taibai.log.api.entity.OperateLog;
import com.taibai.log.api.feign.RemoteOperateLogService;

import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author Taibai
 * @since 2019-11-16
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/user")
@Api(value = "user", tags = "用户管理模块")
public class UserController {

    private final String INTENAL_ADMIN = "internal_admin";

    public static final String 通过角色_CODE_获取拥有该角色的所有用户 = "通过角色code获取拥有该角色的所有用户";
    private final IUserService userService;
    private final TenantMapper tenantMapper;
    private final IProjectService projectService;
    /**
     * token处理
     */
    private final RemoteTokenService remoteTokenService;
    /**
     * 直接操作Redis
     */
    private final RedisTemplate<String, String> redisTemplate;

    private final UserLoginRecordService userLoginRecordService;
    private final IRoleService iRoleService;
    private final UserRoleProjectServiceImpl userRoleProjectService;

    private final ILockedAccountRecordService iLockedAccountRecordService;
    private final ILoginFailRecordService iLoginFailRecordService;

    private final RemoteOperateLogService remoteOperateLogService;
    private final Registration registration;

    @Autowired
    private UserImport userImport;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private AdminUtils adminUtils;

    /**
     * 获取当前用户全部信息
     *
     * @return 用户信息
     */
    @ApiOperation(value = "获取当前用户全部信息")
    @GetMapping(value = { "/info" })
    public R infos(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        FitmgrUser user1 = SecurityUtils.getUser();
        if (StrUtil.isBlank(authHeader) && null != user1.getId()) {
            return R.failed(BusinessEnum.NOT_LOGIN);
        }
        String username = user1.getUsername();
        User user = userService.getOne(Wrappers.<User>query().lambda().eq(User::getUsername, username));
        if (user == null) {
            return R.failed(Boolean.FALSE, "获取当前用户信息失败");
        }
        UserInfo userInfo = userService.findUserInfo(user);
        FitmgrUser fitmgrUser = AuthUtils.getFitmgrUserFromReidsAuthentication();
        if (fitmgrUser != null) {
            userInfo.getUser().setDefaultTenantId(fitmgrUser.getDefaultTenantId());
        }
        Boolean flag = false;
        for (Tenant tenant : userInfo.getTenantList()) {
            if (tenant.getId().equals(userInfo.getUser().getDefaultTenantId())) {
                flag = true;
            }
        }
        if (!flag) {
            // 当前用户不在默认VDC下时，改为系统平台
            User user2 = new User();
            user2.setId(userInfo.getUser().getId());
            user2.setUsername(userInfo.getUser().getUsername());
            user2.setDefaultTenantId(-1);
            String token = authHeader.replace(OAuth2AccessToken.BEARER_TYPE, StrUtil.EMPTY).trim();

            TokenDTO tokenDTO = new TokenDTO();
            tokenDTO.setToken(token);
            tokenDTO.setUser(user2);
            remoteTokenService.updateRdisToken(tokenDTO, SecurityConstants.FROM_IN);

            userInfo.getUser().setDefaultTenantId(-1);
        }
        return R.ok(userInfo);
    }

    /**
     * 获取指定用户全部信息（底层框架登录源码）
     *
     * @return 用户信息
     */
    @ApiOperation(value = "获取指定用户全部信息")
    @ApiImplicitParams(@ApiImplicitParam(paramType = "path", name = "username", dataType = "String", required = true, value = "用户名"))
    @Inner
    @GetMapping("/info/{username}")
    public R info(@PathVariable(name = "username") String username) {
        User user = userService.getOne(Wrappers.<User>query().lambda().eq(User::getUsername, username));
        if (user == null) {
            return R.failed(Boolean.FALSE, String.format("用户信息为空 %s", username));
        }
        if (StrUtil.isNotBlank(username)) {
            Set<String> keys = redisTemplate.keys(CacheConstants.USER_DETAILS_PREFIX + username + "*");
            redisTemplate.delete(keys);
        }
        /** ------------20.05.04-add用户禁用状态拦截 start-------------- */
        userService.updateUserLoginTime(user.getId());
        UserInfo userInfo = userService.findUserInfo(user);

        FitmgrUser fitmgrUser = AuthUtils.getFitmgrUserFromReidsAuthentication();
        if (fitmgrUser != null) {
            userInfo.getUser().setDefaultTenantId(fitmgrUser.getDefaultTenantId());
        }
        return R.ok(userInfo);
    }

    @ApiOperation(value = "获取登录用户全部信息")
    @ApiImplicitParams(@ApiImplicitParam(paramType = "path", name = "username", dataType = "String", required = true, value = "用户名"))
    @Inner
    @GetMapping("/loginInfo/{username}")
    public R loginInfo(@PathVariable(name = "username") String username) {
        User user = userService.getOne(Wrappers.<User>query().lambda().eq(User::getUsername, username));
        if (user == null) {
            log.error("user == null. username={}", username);
            return R.failed(Boolean.FALSE, String.format("用户信息为空 %s", username));
        }
        if (StrUtil.isNotBlank(username)) {
            Set<String> keys = redisTemplate.keys(CacheConstants.USER_DETAILS_PREFIX + username + "*");
            redisTemplate.delete(keys);
        }
        /** ------------20.05.04-add用户禁用状态拦截 start-------------- */
        userService.updateUserLoginTime(user.getId());
        UserInfo userInfo = userService.findUserInfo(user);

        FitmgrUser fitmgrUser = AuthUtils.getFitmgrUserFromReidsAuthentication();
        if (fitmgrUser != null) {
            userInfo.getUser().setDefaultTenantId(fitmgrUser.getDefaultTenantId());
        }
        return R.ok(userInfo);
    }

    /**
     * 添加用户
     *
     * @param userDTO 用户接收入参DTO
     * @return R
     */
    @ApiOperation(value = "添加用户")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "userDTO", dataType = "UserDTO", required = true, value = "用户对象") })
    @PostMapping
    public R saveUser(@Validated(Save.class) @RequestBody UserDTO userDTO) {
        boolean flag = userService.saveUser(userDTO);
        if (flag) {
            FitmgrUser user = SecurityUtils.getUser();
            OperateLog operateLog = SysLogUtils.getSysLog();
            operateLog.setTitle("添加用户");
            operateLog.setOperateObjType("用户");
            operateLog.setUserId(user.getId());
            operateLog.setCreateBy(user.getUsername());
            operateLog.setOperateObjName(userDTO.getUsername());
            operateLog.setServiceId(registration.getServiceId());
            operateLog.setResultCode(ResponseCodeEnum.SUCCESS.getDesc());
            operateLog.setTenantId(user.getDefaultTenantId());
            userDTO.setPassword(null);
            operateLog.setParams(JSON.toJSONString(userDTO));
            remoteOperateLogService.saveLog(operateLog, SecurityConstants.FROM_IN);
        }
        return R.ok(flag);
    }

    /**
     * 删除用户
     *
     * @param userId 用户id
     * @return R
     */
    @SysLog(value = "删除用户", cloudResType = "用户", resIdArgIndex = 0, resIdLocation = "arg")
    @ApiOperation(value = "删除用户")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "userId", dataType = "Integer", required = true, value = "用户id") })
    @DeleteMapping("/{userId}")
    public R deleteById(@PathVariable(name = "userId") Integer userId) {
        if (null != userId) {
            return R.ok(userService.removeUserById(userId));
        }
        return R.failed(BusinessEnum.PARAMETER_ID_NULL);
    }

    /**
     * 修改用户信息
     *
     * @param userDTO 用户接收入参DTO
     * @return R
     */
    @SysLog(value = "修改用户基础信息", cloudResType = "用户", resNameArgIndex = 0, resNameLocation = "arg.name", resIdArgIndex = 0, resIdLocation = "arg.id")
    @ApiOperation(value = "修改用户基础信息")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "userDTO", dataType = "UserDTO", required = true, value = "用户对象") })
    @PutMapping
    public R updateUser(@Validated(Update.class) @RequestBody UserDTO userDTO) {
        if (null != userDTO.getId()) {
            return R.ok(userService.updateUserInfo(userDTO));
        }
        return R.failed(BusinessEnum.PARAMETER_ID_NULL);
    }

    /**
     * 默认用户修改用户名
     *
     * @param userDTO 用户接收入参DTO
     * @return R
     */
    @SysLog(value = "修改默认用户账号信息", cloudResType = "用户", resNameArgIndex = 0, resNameLocation = "arg.name", resIdArgIndex = 0, resIdLocation = "arg.id")
    @ApiOperation(value = "修改默认用户账号信息")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "userDTO", dataType = "UserDTO", required = true, value = "用户对象") })
    @PutMapping("/rename")
    public R renameUser(@Validated(Update.class) @RequestBody UserDTO userDTO) {
        OperateLog operateLog = SysLogUtils.getSysLog();
        FitmgrUser fitmgrUser = SecurityUtils.getUser();
        try {
            if (null != userDTO.getId()) {
                Boolean result = userService.renameUser(userDTO);
                if (result) {
                    operateLog.setResultCode(ResponseCodeEnum.SUCCESS.getDesc());
                    return R.ok(result);
                }
                operateLog.setResultCode(ResponseCodeEnum.ERROR.getDesc());
                return R.failed(!result);
            }
            operateLog.setResultCode(ResponseCodeEnum.ERROR.getDesc());
            return R.failed(BusinessEnum.PARAMETER_ID_NULL);
        } catch (Exception e) {
            log.error("修改用户账户失败，user={}", userDTO, e);
            operateLog.setResultCode(ResponseCodeEnum.ERROR.getDesc());
            return R.failed(e.getMessage());
        } finally {
            try {
                operateLog.setTitle("修改当前账户");
                operateLog.setOperateObjType("用户");
                operateLog.setUserId(fitmgrUser.getId());
                operateLog.setCreateBy(fitmgrUser.getUsername());
                operateLog.setServiceId(registration.getServiceId());
                operateLog.setTenantId(userDTO.getDefaultTenantId());
                userDTO.setPassword(null);
                userDTO.setNewpassword1(null);
                operateLog.setParams("userId=" + fitmgrUser.getId() + ", " + fitmgrUser.getUsername() + "->"
                        + userDTO.getUsername());
                remoteOperateLogService.saveLog(operateLog, SecurityConstants.FROM_IN);
            } catch (Exception e) {
                log.error("【修改账户】记录操作日志失败", e);
            }
        }
    }

    /**
     * 修改当前用户密码
     *
     * @param userDTO 用户接收入参DTO
     * @return R
     */
    @ApiOperation(value = "修改当前用户密码")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "userDTO", dataType = "UserDTO", required = true, value = "用户对象") })
    @PutMapping("/password")
    public R updateUserPassword(@Validated(Password.class) @RequestBody UserDTO userDTO,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        if (!StrUtil.isNotBlank(authHeader)) {
            throw new UserCenterException("当前用户token信息传参异常");
        }
        String decodePassword = userDTO.getPassword().trim();
        String decodeNewPassword = userDTO.getNewpassword1().trim();
        if (StrUtil.isNotBlank(decodePassword) && StrUtil.isNotBlank(decodeNewPassword)) {
            if (decodePassword.equals(decodeNewPassword)) {
                throw new UserCenterException("原始密码与新密码重复,请重新输入");
            }
        }
        FitmgrUser user = SecurityUtils.getUser();
        Boolean aBoolean = userService.updateUserPassword(userDTO);
        // 修改密码成功后，方进行数据缓存的处理
        if (aBoolean) {
            adminUtils.kickoutByUserIdExcludeToken(user.getId(), AuthUtils.getToken(), "用户密码已被修改, 请重新登录");

            /** ---------------修改密码后，直接删除对应用户信息的key，处理Redis缓存导致的异常-20.05.02------------- */
            if (StrUtil.isNotBlank(user.getUsername())) {
                // 清缓存信息
                Set<String> keys = redisTemplate.keys(CacheConstants.USER_DETAILS_PREFIX + user.getUsername() + "*");
                redisTemplate.delete(keys);
            }
            OperateLog operateLog = SysLogUtils.getSysLog();
            operateLog.setTitle("修改当前用户密码");
            operateLog.setOperateObjType("用户");
            operateLog.setUserId(user.getId());
            operateLog.setCreateBy(user.getUsername());
            operateLog.setServiceId(registration.getServiceId());
            operateLog.setResultCode(ResponseCodeEnum.SUCCESS.getDesc());
            operateLog.setTenantId(user.getDefaultTenantId());
            operateLog.setOperateObjId(String.valueOf(user.getId()));
            operateLog.setOperateObjName(user.getUsername());
            userDTO.setPassword(null);
            userDTO.setNewpassword1(null);
            operateLog.setParams(JSON.toJSONString(userDTO));
            remoteOperateLogService.saveLog(operateLog, SecurityConstants.FROM_IN);
        }
        return R.ok(aBoolean);
    }

    @ApiOperation(value = "修改当前用户密码")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "userDTO", dataType = "UserDTO", required = true, value = "用户对象") })
    @PutMapping("/first-login/password")
    public R updateUserPasswordForFirstLogin(@Validated(Password.class) @RequestBody UserDTO userDTO,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        if (!StrUtil.isNotBlank(authHeader)) {
            throw new UserCenterException("当前用户token信息传参异常");
        }
        String decodePassword = userDTO.getPassword().trim();
        String decodeNewPassword = userDTO.getNewpassword1().trim();
        if (StrUtil.isNotBlank(decodePassword) && StrUtil.isNotBlank(decodeNewPassword)) {
            if (decodePassword.equals(decodeNewPassword)) {
                throw new UserCenterException("原始密码与新密码重复,请重新输入");
            }
        }
        FitmgrUser user = SecurityUtils.getUser();
        Boolean aBoolean = userService.updateUserPassword(userDTO);
        // 修改密码成功后，方进行数据缓存的处理
        if (aBoolean) {

            adminUtils.kickoutByUserIdExcludeToken(user.getId(), AuthUtils.getToken(), null);

            adminUtils.kickoutByToken(AuthUtils.getToken(), null);

            /** ---------------修改密码后，直接删除对应用户信息的key，处理Redis缓存导致的异常-20.05.02------------- */
            if (StrUtil.isNotBlank(user.getUsername())) {
                // 清缓存信息
                Set<String> keys = redisTemplate.keys(CacheConstants.USER_DETAILS_PREFIX + user.getUsername() + "*");
                redisTemplate.delete(keys);
            }
            userLoginRecordService.addUserLoginRecord(user.getUsername());

            OperateLog operateLog = SysLogUtils.getSysLog();
            operateLog.setTitle("修改当前用户密码");
            operateLog.setOperateObjType("用户");
            operateLog.setUserId(user.getId());
            operateLog.setCreateBy(user.getUsername());
            operateLog.setServiceId(registration.getServiceId());
            operateLog.setResultCode(ResponseCodeEnum.SUCCESS.getDesc());
            operateLog.setTenantId(user.getDefaultTenantId());
            operateLog.setOperateObjId(String.valueOf(user.getId()));
            operateLog.setOperateObjName(user.getUsername());
            userDTO.setPassword(null);
            userDTO.setNewpassword1(null);
            operateLog.setParams(JSON.toJSONString(userDTO));
            remoteOperateLogService.saveLog(operateLog, SecurityConstants.FROM_IN);
        }
        return R.ok(aBoolean);
    }

    /**
     * 修改用户启用/禁用状态
     *
     * @param userDTO 用户接收入参DTO
     * @return R
     */
    @SysLog(value = "修改用户启用/禁用状态", cloudResType = "用户", resIdArgIndex = 0, resIdLocation = "arg.id")
    @ApiOperation(value = "修改用户启用/禁用状态")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "userDTO", dataType = "UserDTO", required = true, value = "用户对象") })
    @PutMapping("/status")
    public R updateUserStatus(@RequestBody UserDTO userDTO) {
        if (null != userDTO.getId()) {
            if (CommonConstants.STATUS_NORMAL.equals(userDTO.getStatus())
                    || CommonConstants.STATUS_DEL.equals(userDTO.getStatus())) {
                return R.ok(userService.updateUserStatus(userDTO));
            } else {
                return R.failed(BusinessEnum.USER_STATUS_FAULT);
            }
        }
        return R.failed(BusinessEnum.PARAMETER_ID_NULL);
    }

    /**
     * 修改用户头像
     *
     * @param userDTO 用户接收入参DTO
     * @return R
     */
    @SysLog(value = "修改用户头像", cloudResType = "用户", resIdArgIndex = 0, resIdLocation = "arg.id")
    @ApiOperation(value = "修改用户头像")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "userDTO", dataType = "UserDTO", required = true, value = "用户对象") })
    @PutMapping("/avatar")
    public R updateUserAvatar(@RequestBody UserDTO userDTO) {
        if (StrUtil.isNotBlank(userDTO.getAvatar()) && null != userDTO.getId()) {
            return R.ok(userService.updateUserAvatar(userDTO));
        }
        return R.failed(BusinessEnum.PARAMETER_FAULT);
    }

    /**
     * 通过id查询用户详情信息
     *
     * @param userId 用户id
     * @return 用户详情信息
     */
    @ApiOperation(value = "通过id查询用户详情信息")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "userId", dataType = "Integer", required = true, value = "用户id") })
    @GetMapping("/user-info/{userId}")
    public R getById(@PathVariable(name = "userId") Integer userId) {
        if (null != userId) {
            UserVO userVO = userService.queryDetailsByUserId(userId);
            return R.ok(userVO);
        }
        return R.failed(BusinessEnum.PARAMETER_ID_NULL);
    }

    @ApiOperation(value = "通过id查询用户的租户角色信息")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "userId", dataType = "Integer", required = true, value = "用户id") })
    @GetMapping("/user-info/{userId}/tenant-role")
    public R getTenantRoleById(@PathVariable(name = "userId") Integer userId) {
        if (null != userId) {
            List<TenantRoleVO> tenantRoleVos = userService.queryTenantRoleByUserId(userId);
            return R.ok(tenantRoleVos);
        }
        return R.failed(BusinessEnum.PARAMETER_ID_NULL);
    }

    @ApiOperation(value = "通过id查询用户的project角色信息")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "userId", dataType = "Integer", required = true, value = "用户id") })
    @GetMapping("/user-info/{userId}/project-role")
    public R getProjectRoleById(@PathVariable(name = "userId") Integer userId) {
        if (null != userId) {
            List<ProjectRoleVO> projectRoleVos = userService.queryProjectRoleByUserId(userId);
            return R.ok(projectRoleVos);
        }
        return R.failed(BusinessEnum.PARAMETER_ID_NULL);
    }

    @ApiOperation(value = "通过唯一username查询用户详情信息")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "username", dataType = "String", required = true, value = "唯一用户账号") })
    @GetMapping("/user-info/username/{username}")
    public R queryDetailsByUsername(@PathVariable(name = "username") String username) {
        if (StrUtil.isNotBlank(username)) {
            return R.ok(userService.queryDetailsByUsername(username));
        }
        return R.failed(BusinessEnum.PARAMETER_NULL);
    }

    @GetMapping("/user-summary/username/{username}")
    public R<User> querySummaryByUsername(@PathVariable(name = "username") String username) {
        if (StrUtil.isNotBlank(username)) {
            return R.ok(userService.getOne(new QueryWrapper<User>().lambda().eq(User::getUsername, username)));
        }
        return R.failed(BusinessEnum.PARAMETER_NULL);
    }

    @ApiOperation(value = "通过邮箱查询用户详情信息")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "email", dataType = "String", required = true, value = "邮箱") })
    @GetMapping("/user-info/emailOrPhone/{emailOrPhone}")
    public R queryDetailsByEmail(@PathVariable(name = "emailOrPhone") String emailOrPhone) {
        if (StrUtil.isNotBlank(emailOrPhone)) {
            return R.ok(userService.selectUserListByEmailOrPhone(emailOrPhone));
        }
        return R.failed(BusinessEnum.PARAMETER_NULL);
    }

    /**
     * 查询用户列表
     *
     * @return 用户列表
     */
    @ApiOperation(value = "查询用户列表")
    @GetMapping("/user-lists")
    public R selectUserList() {
        List<User> userList = userService.selectUserList();
        return R.ok(userList);
    }

    /**
     * 分页条件查询用户列表
     *
     * @param userDTO 条件
     * @return 用户列表
     */
    @ApiOperation(value = "分页条件查询用户列表")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "query", name = "page", dataType = "Page", required = false, value = "分页条件"),
            @ApiImplicitParam(paramType = "query", name = "userDTO", dataType = "UserDTO", required = false, value = "条件查询对象") })
    @GetMapping("/page")
    public R selectListByCondition(Page page, UserDTO userDTO) {
        try {
            // 获取当前用户的默认角色id,用户辅助控制数据权限
            FitmgrUser user = SecurityUtils.getUser();
            if (null != user) {
                IPage<UserVO> iPage = userService.selectListByCondition(page, userDTO, user.getId());
                return R.ok(iPage);
            }
            return R.failed(BusinessEnum.NOT_LOGIN);
        } catch (Throwable th) {
            log.error("fail", th);
            return R.failed();
        }
    }

    /**
     * 根据条件分页查询用户列表--无关权限
     */
    @PostMapping("/tenantId/page")
    @ApiOperation(value = "根据条件分页查询用户列表--无关权限", notes = "根据条件分页查询用户列表--无关权限")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "query", name = "current", dataType = "Long", required = true, value = "current"),
            @ApiImplicitParam(paramType = "query", name = "size", dataType = "long", required = true, value = "size"),
            @ApiImplicitParam(paramType = "body", name = "userDTO", dataType = "UserDTO", required = true, value = "userDTO") })
    public R selectUserListByCondition(@RequestParam(value = "current") Long current,
            @RequestParam(value = "size") long size, @RequestBody UserDTO userDTO) {
        Page page = new Page();
        page.setCurrent(current);
        page.setSize(size);
        IPage<UserVO> iPage = userService.selectUsersListByCondition(page, userDTO);
        return R.ok(iPage);
    }

    /**
     * 通过用户id查询对应租户下的用户信息列表
     *
     * @param userId 用户id
     * @return 租户下的用户信息列表
     */
    @ApiOperation(value = "通过用户id查询对应租户下的用户信息列表")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "userId", dataType = "Integer", required = true, value = "用户id") })
    @GetMapping("/last-tenant/{userId}")
    public R selectTenantById(@PathVariable(name = "userId") Integer userId) {
        if (null != userId) {
            List<User> userList = userService.queryTenantInfoByUserId(userId);
            return R.ok(userList);
        }
        return R.failed(BusinessEnum.PARAMETER_ID_NULL);
    }

    /**
     * 通过用户id查询对应租户下的用户信息列表
     *
     * @param userId 用户id
     * @return 租户下的用户信息列表
     */
    @ApiOperation(value = "通过用户id,分页查询对应租户下的用户信息")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "userId", dataType = "Integer", required = true, value = "用户id"),
            @ApiImplicitParam(paramType = "query", name = "page", dataType = "Page", required = true, value = "page") })
    @GetMapping("/last-tenant-page/{userId}")
    public R selectPageTenantById(Page page, @PathVariable(name = "userId") Integer userId,
            @RequestParam(value = "queryName", required = false) String queryName) {
        if (null != userId) {
            IPage<UserVO> userPage = userService.queryPageTenantInfoByUserId(userId, page, queryName);
            return R.ok(userPage);
        }
        return R.failed(BusinessEnum.PARAMETER_ID_NULL);
    }

    /**
     * 通过用户id查询该用户所在project列表信息
     *
     * @param userId 用户id
     * @return project信息
     */
    @ApiOperation(value = "通过用户id查询该用户所在project列表信息")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "userId", dataType = "Integer", required = true, value = "用户id") })
    @GetMapping("/project-info/{userId}")
    public R selectProjectById(@PathVariable(name = "userId") Integer userId) {
        if (null != userId) {
            List<Project> projectList = userService.queryProjectByUserId(userId);
            return R.ok(projectList);
        }
        return R.failed(BusinessEnum.PARAMETER_ID_NULL);
    }

    /**
     * 通过用户id查询该用户所属角色列表信息
     *
     * @param userId 用户id
     * @return 角色信息
     */
    @ApiOperation(value = "通过用户id查询该用户所属角色列表信息")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "userId", dataType = "Integer", required = true, value = "用户id") })
    @GetMapping("/role-list/{userId}")
    public R selectRoleById(@PathVariable(name = "userId") Integer userId) {
        if (null != userId) {
            List<Role> roleList = userService.queryRoleByUserId(userId);
            return R.ok(roleList);
        }
        return R.failed(BusinessEnum.PARAMETER_ID_NULL);
    }

    @ApiOperation(value = "通过租户id查询该租户下所有角色列表信息")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "tenantId", dataType = "Integer", required = true, value = "租户id") })
    @GetMapping("/role-list/tenant/{tenantId}")
    public R selectRoleByTenantId(@PathVariable(name = "tenantId") Integer tenantId) {
        List<Role> roleList = userService.selectRoleByTenantIdAndRoleCode(tenantId);
        return R.ok(roleList);
    }

    /**
     * 通过tenantId查询所对应的tenant下的所有用户信息列表
     *
     * @param tenantId  租户id
     * @param queryName 模糊查询名称
     * @return 用户信息列表
     */
    @ApiOperation(value = "通过tenantId查询所对应的tenant下的所有用户信息列表")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "tenantId", dataType = "Integer", required = true, value = "租户id"),
            @ApiImplicitParam(paramType = "path", name = "queryName", dataType = "String", required = false, value = "条件查询对象"),
            @ApiImplicitParam(paramType = "query", name = "page", dataType = "Page", required = true, value = "page") })
    @GetMapping("/user-lists/{tenantId}")
    public R queryUserByTenantId(Page page, @PathVariable(name = "tenantId") Integer tenantId,
            @RequestParam(value = "queryName", required = false) String queryName) {
        if (null != tenantId) {
            if (page.getCurrent() == 0)
                page.setCurrent(1);
            if (page.getSize() == 0)
                page.setSize(10);
            List<UserVO> userVos = userService.queryUserByTenantId(tenantId, queryName, page);
            return R.ok(userVos);
        }
        return R.failed(BusinessEnum.PARAMETER_ID_NULL);
    }

    /**
     * 通过tenantId列表查询所对应的tenant下的所有用户信息列表
     *
     * @param tenantIdList 租户id列表
     * @return 用户信息列表
     */
    @ApiOperation(value = "通过tenantId查询所对应的tenant下的所有用户信息列表")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "tenantIdList", dataType = "List", required = true, value = "tenantIdList") })
    @PostMapping("/user-lists/tenantId-list")
    public R queryUserByTenantIdList(@RequestBody List<Integer> tenantIdList) {
        if (null != tenantIdList && tenantIdList.size() > 0) {
            List<UserVO> userVos = userService.queryUserByTenantIdList(tenantIdList);
            return R.ok(userVos);
        }
        return R.failed(BusinessEnum.PARAMETER_ID_NULL);
    }

    @PostMapping("/update-user/default-tenant")
    public R updateUserDefaultTenant(@RequestBody UserDTO userDTO) {
        userService.updateDefaultTenant(userDTO);
        return R.ok();
    }

    /**
     * 通过tenantId列表查询每个tenant下的用户数
     *
     * @param tenantIdList 租户id列表
     * @return 用户信息列表
     */
    @ApiOperation(value = "通过tenantId列表查询每个tenant下的用户数")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "tenantIdList", dataType = "List", required = true, value = "tenantIdList") })
    @PostMapping("/userCount-lists/tenantId-list")
    public R<List<UserCount>> queryUserCountByTenantIdList(@RequestBody List<Integer> tenantIdList) {
        List<UserCount> userVos = userService.queryUserCountByTenantIdList(tenantIdList);
        return R.ok(userVos);
    }

    /**
     * 通过projectId列表查询每个project下的用户数
     *
     * @param tenantIdList 租户id列表
     * @return 用户信息列表
     */
    @ApiOperation(value = "通过projectId列表查询每个project下的用户数")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "tenantIdList", dataType = "List", required = true, value = "tenantIdList") })
    @PostMapping("/userCount-lists/projectId-list")
    public R<List<UserCount>> queryUserCountByProjectIdList(@RequestBody List<Integer> projectIdList) {
        List<UserCount> userVos = userService.queryUserCountByProjectIdList(projectIdList);
        return R.ok(userVos);
    }

    /**
     * 通过projectId查询用户信息列表
     *
     * @param projectId 项目id
     * @return 用户信息列表
     */
    @ApiOperation(value = "通过projectId查询用户信息")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "projectId", dataType = "Integer", required = true, value = "项目Id") })
    @GetMapping("/user-list/{projectId}")
    public R queryUserByProjectId(@PathVariable(name = "projectId") Integer projectId) {
        if (null != projectId) {
            List<UserVO> userVos = userService.queryUserByProjectId(projectId);
            return R.ok(userVos);
        }
        return R.failed(BusinessEnum.PARAMETER_ID_NULL);
    }

    /**
     * 通过projectId查询该peiject对应的tenant下的所有用户信息列表
     *
     * @param projectId 项目id
     * @param queryName 模糊查询名称
     * @return 用户信息列表
     */
    @ApiOperation(value = "通过projectId查询该project对应的tenant下的所有用户信息列表")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "projectId", dataType = "Integer", required = true, value = "项目id"),
            @ApiImplicitParam(paramType = "query", name = "queryName", dataType = "String", required = false, value = "条件查询对象") })
    @GetMapping("/tenant-user-list/{projectId}")
    public R queryTenantUserByProjectId(@PathVariable(name = "projectId") Integer projectId, String queryName) {
        if (null != projectId) {
            List<UserVO> userVos = userService.queryTenantUserByProjectId(projectId, queryName);
            return R.ok(userVos);
        }
        return R.failed(BusinessEnum.PARAMETER_ID_NULL);
    }

    /**
     * 用户中心信息预览
     *
     * @param userDTO 用户传输对象
     * @return PreviewInfoVO 用户中心信息预览展示信息
     */
    @ApiOperation(value = "用户中心信息预览")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "userDTO", dataType = "UserDTO", required = true, value = "条件查询对象") })
    @PostMapping("/preview-information")
    public R previewInformation(@RequestBody UserDTO userDTO) {
        if (null != userDTO.getId()) {
            PreviewInfoVO previewInfoVO = userService.previewInformation(userDTO);
            return R.ok(previewInfoVO);
        }
        return R.failed(BusinessEnum.PARAMETER_FAULT);
    }

    @ApiOperation(value = "当前登录用户租户project关联信息")
    @GetMapping("/user-tenant-project/relation")
    public R userTenantProjectRelation() {
        UserVO userVO = userService.userTenantProjectRelation();
        if (null != userVO.getId()) {
            // impl层业务逻辑（queryDetailsByUserId）
            UserVO userVo2 = userService.queryDetailsByUserId(userVO.getId());
            userVO.setTenantList(userVo2.getTenantList());
            if (null != userVo2.getProjectList() && !userVo2.getProjectList().isEmpty()) {
                userVO.setProjectList(userVo2.getProjectList());
            }
            return R.ok(userVO);
        }
        return R.failed(BusinessEnum.NOT_LOGIN);
    }

    @ApiOperation(value = "通过角色code获取当前登录用户所在租户下拥有该角色的所有用户")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "roleCode", dataType = "String", required = true, value = "角色唯一Code") })
    @GetMapping("/user-info-list/{roleCode}")
    public R queryUserInfoByRoleCode(@PathVariable(name = "roleCode") String roleCode) {
        if (StrUtil.isNotBlank(roleCode)) {
            List<UserVO> userVos = userService.queryUserInfoByRoleCodeAndTenantId(roleCode);
            return R.ok(userVos);
        }
        return R.failed(BusinessEnum.USER_ROLE_CODE_FAULT);
    }

    @ApiOperation(value = "通过角色code获取当前登录用户所在租户下拥有该角色的所有用户")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "roleCode", dataType = "String", required = true, value = "角色唯一Code") })
    @GetMapping("/user-info-list/inner/{roleCode}")
    public R queryInnerUserInfoByRoleCode(@PathVariable(name = "roleCode") String roleCode,
            @RequestHeader(SecurityConstants.FROM) String from) {
        if (StrUtil.isNotBlank(roleCode)) {
            List<UserVO> userVos = userService.queryUserInfoByRoleCodeAndTenantId(roleCode);
            return R.ok(userVos);
        }
        return R.failed(BusinessEnum.USER_ROLE_CODE_FAULT);
    }

    @GetMapping("/user-info-list/inner/{tenantId}/{roleCode}")
    R<List<UserVO>> queryInnerUserInfoByTenantIdAndRoleCode(@PathVariable(name = "tenantId") Integer tenantId,
            @PathVariable(name = "roleCode") String roleCode, @RequestHeader(SecurityConstants.FROM) String from) {
        if (StrUtil.isNotBlank(roleCode)) {
            List<UserVO> userVos = userService.queryUserInfoByRoleCodeAndTenantId(tenantId, roleCode);
            return R.ok(userVos);
        }
        return R.failed(BusinessEnum.USER_ROLE_CODE_FAULT);
    }

    @GetMapping("/user-info-list/inner/roleCodes/{tenantId}")
    R<List<UserVO>> queryInnerUserInfoByTenantIdAndRoleCodes(@PathVariable(name = "tenantId") Integer tenantId,
            @RequestParam(name = "roleCodes") List<String> roleCodes,
            @RequestHeader(SecurityConstants.FROM) String from) {
        if (roleCodes != null && roleCodes.size() > 0) {
            List<UserVO> userVos = userService.queryUserInfoByRoleCodesAndTenantId(tenantId, roleCodes);
            return R.ok(userVos);
        }
        return R.failed(BusinessEnum.USER_ROLE_CODE_FAULT);
    }

    /**
     * 通过角色code和ProjectId获取当前登录用户所在Project下拥有该角色的所有用户
     *
     * @param roleCode
     * @return
     */
    @GetMapping("/project/user/list")
    @ApiOperation(value = "通过角色code和ProjectId获取当前登录用户所在Project下拥有该角色的所有用户", notes = "通过角色code和ProjectId获取当前登录用户所在Project下拥有该角色的所有用户")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "query", name = "projectId", dataType = "Integer", required = true, value = "projectId"),
            @ApiImplicitParam(paramType = "query", name = "roleCode", dataType = "String", required = true, value = "roleCode") })
    public R queryUserInfoByProjectIdAndRoleCode(@RequestParam("projectId") Integer projectId,
            @RequestParam("roleCode") String roleCode) {
        return R.ok(userService.queryUserInfoByProjectIdAndRoleCode(projectId, roleCode));
    }

    /**
     * 通过角色code和ProjectId获取当前登录用户所在Project下拥有该角色的所有用户, 内部调用
     *
     * @param roleCode
     * @return
     */
    @GetMapping("/inner/project/user/list")
    public R queryInnerUserInfoByProjectIdAndRoleCode(@RequestParam("projectId") Integer projectId,
            @RequestParam("roleCode") String roleCode, @RequestHeader(SecurityConstants.FROM) String from) {
        return R.ok(userService.queryUserInfoByProjectIdAndRoleCode(projectId, roleCode));
    }

    @ApiOperation(value = "通过角色code获取拥有该角色的所有用户")
    @GetMapping("/user-list/bycode/{roleCode}")
    @ApiImplicitParams(value = @ApiImplicitParam(paramType = "query", name = "roleCode", dataType = "String", required = true, value = "roleCode"))
    public R queryUserListByRoleCode(@PathVariable(name = "roleCode") String roleCode) {
        if (StrUtil.isNotBlank(roleCode)) {
            List<UserVO> userVos = userService.queryUserInfoByRoleCode(roleCode);
            return R.ok(userVos);
        }
        return R.failed(BusinessEnum.USER_ROLE_CODE_FAULT);
    }

    @ApiOperation(value = "通过角色code获取拥有该角色的所有用户（分页）")
    @PostMapping("/user-info-list/page")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "page", dataType = "Page", required = true, value = "page"),
            @ApiImplicitParam(paramType = "query", name = "roleCode", dataType = "String", required = true, value = "roleCode"),
            @ApiImplicitParam(paramType = "query", name = "name", dataType = "String", required = true, value = "name") })
    public R queryUserInfoByRoleCodePage(@RequestBody Page page,
            @RequestParam(name = "roleCode", required = false) String roleCode,
            @RequestParam(name = "name", required = false) String name) {
        // 默认展示第一页，初始值为10
        if (page.getCurrent() <= 0) {
            page.setCurrent(1L);
        }
        if (page.getSize() <= 0) {
            page.setSize(10L);
        }
        IPage<UserVO> userVoiPage = userService.queryUserInfoByRoleCodePage(page, roleCode, name);
        return R.ok(userVoiPage);
    }

    /**
     * 通过用户集合查询用户基本信息
     *
     * @param userIds 用户id集合
     * @return 用户基本信息List
     */
    @ApiImplicitParam(paramType = "body", name = "userIds", dataType = "List", required = true, value = "用户id集合")
    @PostMapping("/user-msg")
    @ApiOperation(value = "通过用户集合查询用户基本信息", notes = "通过用户集合查询用户基本信息")
    public R<List<UserVO>> queryUserMsg(@RequestBody List<Integer> userIds) {
        return R.ok(userService.queryUserMsg(userIds));
    }

    /**
     * 通过用户名集合查询用户信息
     *
     * @param usernames
     * @return
     */
    @ApiImplicitParam(paramType = "body", name = "usernames", dataType = "List", required = true, value = "用户名称集合")
    @PostMapping("/find/users")
    @ApiOperation(value = "通过用户名集合查询用户信息", notes = "通过用户名集合查询用户信息")
    public R<List<UserVO>> queryUserByUsernames(@RequestBody List<String> usernames) {
        return R.ok(userService.queryUserByUsernames(usernames));
    }

    @ApiOperation(value = "通过角色code获取租户配额管理员或project配额管理员的的用户信息")
    @GetMapping("/ten-pro-usr")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "query", name = "roleCode", dataType = "String", required = true, value = "角色唯一Code"),
            @ApiImplicitParam(paramType = "query", name = "tpId", dataType = "Integer", required = true, value = "租户或project的Id") })
    public R<List<User>> getUserByTenOrPro(@RequestParam("roleCode") String roleCode,
            @RequestParam("tpId") Integer tpId) {
        return userService.getUserByTenOrPro(roleCode, tpId);
    }

    @GetMapping("/importUser")
    @ApiOperation(value = "importUser")
    public R syncProject() {
        try {
            userImport.process("/app/user.xlsx");
        } catch (Throwable th) {
            log.error("process fail", th);
        }
        return R.ok();
    }

    @ApiOperation(value = "根据当前用户的角色获取租户列表或者project列表")
    @GetMapping("/current-user")
    public R<TenantOrProjectVO> getTenOrProList() {
        FitmgrUser user = SecurityUtils.getUser();
        if (user != null) {
            List<Integer> roleIds = new ArrayList<Integer>();
            List<Map<String, Integer>> roleList = userService.queryRoleByUserIdAndTenantId(user.getId(),
                    user.getDefaultTenantId());
            for (Map<String, Integer> map : roleList) {
                roleIds.add(map.get("role_id"));
            }
            return userService.getTenOrProList(user.getDefaultTenantId(), roleIds, user.getId());
        }
        return R.failed(BusinessEnum.NOT_LOGIN);
    }

    @SysLog(value = "重置密码", cloudResType = "用户", resIdArgIndex = 0, resIdLocation = "arg.id")
    @ApiOperation(value = "重置密码")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "userDTO", dataType = "UserDTO", required = true, value = "用户对象") })
    @PostMapping("/password/reset")
    public R resetPassword(@RequestBody UserDTO userDTO) {
        return R.ok(userService.resetPassword(userDTO));
    }

    @ApiOperation(value = "查询用户登录次数")
    @GetMapping("/login-count/{username}")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "username", dataType = "String", required = false, value = "username") })
    public R<UserLoginRecord> queryUserLoginCount(@PathVariable(name = "username") String username) {
        return R.ok(userLoginRecordService.queryUserCount(username));
    }

    @ApiOperation(value = "用户登录次数加1")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "userLoginRecord", dataType = "UserLoginRecord", required = false, value = "用户对象") })
    @PostMapping("/login-count/add-count/{username}")
    public R addLoginCount(@PathVariable(name = "username") String username) {
        userLoginRecordService.addUserLoginRecord(username);
        return R.ok();
    }

    @GetMapping("/updateTenantId/updateTenantId")
    @ApiOperation(value = "更新租户id")
    public R queryUserLoginCount() {
        userService.updateTenantId();
        return R.ok();
    }

    /**
     * 刷新租户普通租户数据
     * 
     * @return
     */
    @GetMapping("/updateTenantId/updateOrdinary")
    @ApiOperation(value = "刷新租户普通租户数据")
    public R updateOrdinary() {
        userService.updateOrdinary();
        return R.ok();
    }

    /**
     * 刷新project对应的tenant
     * 
     * @return
     */
    @GetMapping("/updateTenantId/updateprojecttenant")
    @ApiOperation(value = "刷新project对应的tenant")
    public R updateTenant() {
        userService.updateProjectTenant();
        return R.ok();
    }

    @GetMapping("/mergeUser/mergeUser")
    @ApiOperation(value = "mergeUser")
    public R mergeUser() {
        userService.mergeUser();
        return R.ok();
    }

    @GetMapping("/clearRedisCache/clearRedisCache")
    @ApiOperation(value = "清空缓存")
    public R clearRedisCache() {
        userService.clearRedisCache();
        return R.ok();
    }

    @GetMapping("/accountTotal/accountTotal")
    @ApiOperation(value = "总数")
    public R accountTotal() {
        userImport.countTotal("/app/user.xlsx");
        return R.ok();
    }

    /**
     * 条件查询用户列表(quota使用)
     *
     * @return 用户列表
     */
    @PostMapping("/list/quota")
    @ApiOperation(value = "条件获取用户列表")
    @ApiImplicitParams(value = @ApiImplicitParam(paramType = "body", name = "userDTO", dataType = "UserDTO", required = true, value = "userDTO"))
    public R<List<User>> getLists(@RequestBody UserDTO userDTO) {
        List<User> list = userService.userLists(userDTO);
        return R.ok(list);
    }

    @GetMapping("/resetUserTenant/resetUserTenant")
    @ApiOperation(value = "恢复用户租户")
    public R resetUserTenant() {
        userImport.resetUserTenant();
        return R.ok();
    }

    @GetMapping("/envtest")
    public R selectEnv() {
        SecurityUtils.getUser();
        List<String> services = discoveryClient.getServices();
        Map<String, Object> data = new HashMap<>();
        log.info("services={}", services);
        data.put("services", services);
        for (String service : services) {
            log.info("service={}, instances={}", service, discoveryClient.getInstances(service));
            data.put(service + "-instances", discoveryClient.getInstances(service));
        }
        return R.ok(data);
    }

    @GetMapping("/refreshUrp/refreshUrp")
    @ApiOperation(value = "刷新URP")
    public R refreshUrp() {
        userService.refreshUrp();
        return R.ok();
    }

    /**
     * 批量关联角色
     *
     * @param userDTO 用户接收入参DTO
     * @return R
     */
    @SysLog(value = "批量关联角色", cloudResType = "用户")
    @ApiOperation(value = "批量关联角色")
    @PostMapping("/batchRelationRole")
    public R batchRelationRole(@RequestBody RelationRole relationRole) {
        List<UserRoleProject> userRoleList = new ArrayList<UserRoleProject>();
        List<Role> roles = iRoleService
                .list(new QueryWrapper<Role>().lambda().in(Role::getId, relationRole.getRoleIds()));
        if ((relationRole.getTenantId() == null || relationRole.getTenantId() == -1)) {
            if (SecurityUtils.getUser().getDefaultTenantId() != -1) {
                return R.failed("VDC管理员不可关联系统角色");
            }
            for (Role role : roles) {
                if (!role.getLevel().equals(RoleLevelEnum.SYSTEM.getCode())) {
                    return R.failed("不可关联非系统级别角色");
                }
            }
        }
        if (relationRole.getTenantId() != null && relationRole.getTenantId() != -1) {
            if (relationRole.getProjectId() == null || relationRole.getProjectId() == -1) {
                for (Role role : roles) {
                    if (!role.getLevel().equals(RoleLevelEnum.TENANT.getCode())) {
                        return R.failed("不可关联非VDC级别角色");
                    }
                }
            }
        }
        if (relationRole.getTenantId() != null && relationRole.getTenantId() != -1) {
            if (relationRole.getProjectId() != null && relationRole.getProjectId() != -1) {
                for (Role role : roles) {
                    if (!role.getLevel().equals(RoleLevelEnum.PROJECT.getCode())) {
                        return R.failed("不可关联非项目级别角色");
                    }
                }
            }
        }
        Map<Integer, List<Integer>> userIds = new HashMap<Integer, List<Integer>>();
        List<UserRoleProject> list = userRoleProjectService.list(
                new QueryWrapper<UserRoleProject>().lambda().in(UserRoleProject::getRoleId, relationRole.getRoleIds())
                        .in(UserRoleProject::getUserId, relationRole.getUserIds())
                        .eq(UserRoleProject::getTenantId, relationRole.getTenantId())
                        .eq(UserRoleProject::getProjectId, relationRole.getProjectId()));
        for (UserRoleProject userRoleProject : list) {
            if (userIds.get(userRoleProject.getUserId()) == null) {
                List<Integer> roleIds = new ArrayList<Integer>();
                roleIds.add(userRoleProject.getRoleId());
                userIds.put(userRoleProject.getUserId(), roleIds);
            } else {
                List<Integer> roleIds = userIds.get(userRoleProject.getUserId());
                roleIds.add(userRoleProject.getRoleId());
                userIds.put(userRoleProject.getUserId(), roleIds);
            }
        }
        for (Integer userId : relationRole.getUserIds()) {
            for (Integer roleId : relationRole.getRoleIds()) {
                if (userIds.get(userId) == null || !userIds.get(userId).contains(roleId)) {
                    UserRoleProject userRoleProject = new UserRoleProject();
                    userRoleProject.setUserId(userId);
                    userRoleProject.setRoleId(roleId);
                    userRoleProject.setProjectId(relationRole.getProjectId());
                    userRoleProject.setTenantId(relationRole.getTenantId());
                    userRoleList.add(userRoleProject);
                }
            }
        }
        boolean res = userRoleProjectService.saveBatch(userRoleList);
        return R.ok(res);
    }

    /**
     * 批量删除角色
     *
     * @param userDTO 用户接收入参DTO
     * @return R
     */
    @SysLog(value = "批量删除角色", cloudResType = "用户")
    @ApiOperation(value = "批量删除角色")
    @PostMapping("/batchDeleteRole")
    public R batchDeleteRole(@RequestBody RelationRole relationRole) {
        List<Role> roles = iRoleService
                .list(new QueryWrapper<Role>().lambda().in(Role::getId, relationRole.getRoleIds()));
        if ((relationRole.getTenantId() == null || relationRole.getTenantId() == -1)) {
            if (SecurityUtils.getUser().getDefaultTenantId() != -1) {
                return R.failed("VDC管理员不可删除系统角色");
            }
            for (Role role : roles) {
                if (!role.getLevel().equals(RoleLevelEnum.SYSTEM.getCode())) {
                    return R.failed("不可删除非系统级别角色");
                }
            }
        }
        if (relationRole.getTenantId() != null && relationRole.getTenantId() != -1) {
            if (relationRole.getProjectId() == null || relationRole.getProjectId() == -1) {
                for (Role role : roles) {
                    if (!role.getLevel().equals(RoleLevelEnum.TENANT.getCode())) {
                        return R.failed("不可删除非VDC级别角色");
                    }
                }
            }
        }
        if (relationRole.getTenantId() != null && relationRole.getTenantId() != -1) {
            if (relationRole.getProjectId() != null && relationRole.getProjectId() != -1) {
                for (Role role : roles) {
                    if (!role.getLevel().equals(RoleLevelEnum.PROJECT.getCode())) {
                        return R.failed("不可删除非项目级别角色");
                    }
                }
            }
        }
        List<UserRoleProject> listAll = userRoleProjectService.list(
                new QueryWrapper<UserRoleProject>().lambda().in(UserRoleProject::getUserId, relationRole.getUserIds())
                        .eq(UserRoleProject::getTenantId, relationRole.getTenantId())
                        .eq(UserRoleProject::getProjectId, relationRole.getProjectId()));
        Map<Integer, List<Integer>> mapAll = new HashMap<Integer, List<Integer>>();
        for (UserRoleProject userRoleProject : listAll) {
            if (mapAll.get(userRoleProject.getUserId()) == null) {
                List<Integer> list = new ArrayList<Integer>();
                mapAll.put(userRoleProject.getUserId(), list);
            }
            List<Integer> list = mapAll.get(userRoleProject.getUserId());
            list.add(userRoleProject.getRoleId());
            mapAll.put(userRoleProject.getUserId(), list);
        }
        List<UserRoleProject> listRemove = userRoleProjectService.list(
                new QueryWrapper<UserRoleProject>().lambda().in(UserRoleProject::getRoleId, relationRole.getRoleIds())
                        .in(UserRoleProject::getUserId, relationRole.getUserIds())
                        .eq(UserRoleProject::getTenantId, relationRole.getTenantId())
                        .eq(UserRoleProject::getProjectId, relationRole.getProjectId()));
        Map<Integer, List<Integer>> mapRemove = new HashMap<Integer, List<Integer>>();
        for (UserRoleProject userRoleProject : listRemove) {
            if (mapRemove.get(userRoleProject.getUserId()) == null) {
                List<Integer> list = new ArrayList<Integer>();
                mapRemove.put(userRoleProject.getUserId(), list);
            }
            List<Integer> list = mapRemove.get(userRoleProject.getUserId());
            list.add(userRoleProject.getRoleId());
            mapRemove.put(userRoleProject.getUserId(), list);
        }
        List<Integer> userIds = relationRole.getUserIds();
        List<Integer> addUserIds = new ArrayList<Integer>();
        for (Entry<Integer, List<Integer>> entry : mapAll.entrySet()) {
            List<Integer> roleIds = entry.getValue();
            if (mapRemove.get(entry.getKey()) != null) {
                roleIds.removeAll(mapRemove.get(entry.getKey()));
            }
            if (roleIds.size() == 0) {
                addUserIds.add(entry.getKey());
            }
        }
        boolean res = true;
        if (userIds.size() > 0) {
            res = userRoleProjectService.remove(new QueryWrapper<UserRoleProject>().lambda()
                    .in(UserRoleProject::getRoleId, relationRole.getRoleIds()).in(UserRoleProject::getUserId, userIds)
                    .eq(UserRoleProject::getTenantId, relationRole.getTenantId())
                    .eq(UserRoleProject::getProjectId, relationRole.getProjectId()));
        }
        List<UserRoleProject> userRoleList = new ArrayList<UserRoleProject>();
        for (Integer userId : addUserIds) {
            UserRoleProject userRoleProject = new UserRoleProject();
            userRoleProject.setUserId(userId);
            if ((relationRole.getTenantId() == null || relationRole.getTenantId() == -1)) {
                List<Role> sDefaultRoles = iRoleService.list(new QueryWrapper<Role>().lambda()
                        .eq(Role::getLevel, RoleLevelEnum.SYSTEM.getCode()).eq(Role::getSystemDefaultRole, true));
                userRoleProject.setRoleId(sDefaultRoles.get(0).getId());
            }
            if (relationRole.getTenantId() != null && relationRole.getTenantId() != -1) {
                if (relationRole.getProjectId() == null || relationRole.getProjectId() == -1) {
                    List<Role> tDefaultRoles = iRoleService.list(new QueryWrapper<Role>().lambda()
                            .eq(Role::getLevel, RoleLevelEnum.TENANT.getCode()).eq(Role::getTenantDefaultRole, true));
                    userRoleProject.setRoleId(tDefaultRoles.get(0).getId());
                }
            }
            if (relationRole.getTenantId() != null && relationRole.getTenantId() != -1) {
                if (relationRole.getProjectId() != null && relationRole.getProjectId() != -1) {
                    List<Role> pDefaultRoles = iRoleService.list(new QueryWrapper<Role>().lambda()
                            .eq(Role::getLevel, RoleLevelEnum.PROJECT.getCode()).eq(Role::getProjectDefaultRole, true));
                    userRoleProject.setRoleId(pDefaultRoles.get(0).getId());
                }
            }
            userRoleProject.setProjectId(relationRole.getProjectId());
            userRoleProject.setTenantId(relationRole.getTenantId());
            userRoleList.add(userRoleProject);
        }
        res = userRoleProjectService.saveBatch(userRoleList);
        return R.ok(res);
    }

    /**
     * 修改用户锁定状态
     *
     * @param userId 用户id
     * @return R
     */
    @ApiOperation(value = "修改用户锁定状态")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "userId", dataType = "Integer", required = true, value = "用户id") })
    @PutMapping("/lockById/{userId}")
    public R lockById(@PathVariable(name = "userId") Integer userId,
            @RequestParam(value = "lockState", required = true) String lockState) {
        if (null != userId) {
            if (lockState.equals(UserLockStatus.UN_LOCK.getCode())) {
                // 删除登录失败及锁定记录
                iLoginFailRecordService
                        .remove(new QueryWrapper<LoginFailRecord>().lambda().eq(LoginFailRecord::getUserId, userId));
                iLockedAccountRecordService.remove(
                        new QueryWrapper<LockedAccountRecord>().lambda().eq(LockedAccountRecord::getUserId, userId));
            }
            User user = userService.getOne(new QueryWrapper<User>().lambda().eq(User::getId, userId));
            user.setLockState(lockState);
            boolean flag = userService.updateById(user);

            FitmgrUser user1 = SecurityUtils.getUser();
            if (!INTENAL_ADMIN.equals(user1.getUsername())) {
                OperateLog operateLog = SysLogUtils.getSysLog();
                operateLog.setTitle("修改用户锁定状态");
                operateLog.setOperateObjType("用户");
                operateLog.setUserId(user1.getId());
                operateLog.setCreateBy(user1.getUsername());
                operateLog.setOperateObjName(user.getUsername());
                operateLog.setServiceId(registration.getServiceId());
                if (flag) {
                    operateLog.setResultCode(ResponseCodeEnum.SUCCESS.getDesc());
                } else {
                    operateLog.setResultCode(ResponseCodeEnum.ERROR.getDesc());
                }
                operateLog.setTenantId(user1.getDefaultTenantId());
                operateLog.setParams(String.valueOf(userId));
                remoteOperateLogService.saveLog(operateLog, SecurityConstants.FROM_IN);
            }

            return R.ok(flag);
        }
        return R.failed(BusinessEnum.PARAMETER_ID_NULL);
    }

    @ApiOperation(value = "录入用户")
    @PostMapping(value = { "/import" })
    public R improt(@RequestBody ImportUserVo importUserVo) {
        return userService.importUser(importUserVo);
    }

    @ApiOperation(value = "下载录入失败的用户")
    @GetMapping(value = { "/downloadFail" })
    public void downloadFail(HttpServletResponse response,
            @RequestParam(value = "bucket", required = true) String bucket,
            @RequestParam(value = "fileName", required = true) String fileName) throws Exception {
        userService.downloadFail(response, bucket, fileName);
    }

    @ApiOperation(value = "获取用户录入进度")
    @GetMapping(value = { "/queryProgress" })
    public R<ImportUserVo> queryProgress(@RequestParam(value = "bucket", required = true) String bucket,
            @RequestParam(value = "fileName", required = true) String fileName) throws Exception {
        ImportUserVo importUserVo = userService.queryProgress(bucket, fileName);
        return R.ok(importUserVo);
    }

    @ApiOperation(value = "查看历史录入记录")
    @GetMapping(value = { "/queryLogs" })
    public R<List<ImportUserVo>> queryLogs() throws Exception {
        List<ImportUserVo> importUserVo = userService.queryLogs();
        return R.ok(importUserVo);
    }

    @GetMapping("/checkNeedValidateCode/{userName}")
    public R checkLoginNeedValidateCode(@PathVariable(name = "userName") String userName) {
        return R.ok(userService.checkLoginNeedValidateCode(userName));
    }

    @SysLog(value = "用户自助重置密码验证身份", cloudResType = "用户", resNameLocation = "arg.userName")
    @ApiOperation(value = "用户自助重置密码验证身份")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "verifyInfoForFindPwd", dataType = "VerifyInfoForFindPwd", required = true, value = "验证对象") })
    @PostMapping("/retrieve-password/verify")
    public R verifyIdentity(@RequestBody VerifyInfoForFindPwd verifyInfoForFindPwd) {
        return userService.verifyIdentity(verifyInfoForFindPwd);
    }

    @SysLog(value = "用户自助重置密码修改密码", cloudResType = "用户", resIdLocation = "arg.userId")
    @ApiOperation(value = "用户自助重置密码修改密码")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "modifyInfoForRetrievePwd", dataType = "ModifyInfoForRetrievePwd", required = true, value = "修改密码对象") })
    @PostMapping("/retrieve-password/modify-password")
    public R modifyPwdForRetrievePwd(@RequestBody ModifyInfoForRetrievePwd modifyInfoForRetrievePwd) {
        return userService.modifyPwdForRetrievePwd(modifyInfoForRetrievePwd);
    }

    @SysLog(value = "用户自助重置密码校验链接是否有效", cloudResType = "用户", resIdLocation = "arg.userId")
    @ApiOperation(value = "用户自助重置密码校验链接是否有效")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "checkHrefInfoForRetrievePwd", dataType = "CheckHrefInfoForRetrievePwd", required = true, value = "校验对象") })
    @PostMapping("/retrieve-password/check-href")
    public R checkHrefForRetrievePwd(@RequestBody CheckHrefInfoForRetrievePwd checkHrefInfoForRetrievePwd) {
        return userService.checkHrefForRetrievePwd(checkHrefInfoForRetrievePwd);
    }
}
