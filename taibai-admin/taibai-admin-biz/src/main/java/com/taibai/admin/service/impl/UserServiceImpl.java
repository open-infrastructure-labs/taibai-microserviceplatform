package com.taibai.admin.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taibai.activiti.api.feign.RemoteProcessService;
import com.taibai.admin.api.constants.RoleLevelEnum;
import com.taibai.admin.api.dto.CheckHrefInfoForRetrievePwd;
import com.taibai.admin.api.dto.ModifyInfoForRetrievePwd;
import com.taibai.admin.api.dto.ProjectDTO;
import com.taibai.admin.api.dto.UserDTO;
import com.taibai.admin.api.dto.UserInfo;
import com.taibai.admin.api.dto.VerifyInfoForFindPwd;
import com.taibai.admin.api.entity.AuthCheck;
import com.taibai.admin.api.entity.ExcelData;
import com.taibai.admin.api.entity.LockedAccountRecord;
import com.taibai.admin.api.entity.LoginFailRecord;
import com.taibai.admin.api.entity.MaxHisPassCount;
import com.taibai.admin.api.entity.PassExpiration;
import com.taibai.admin.api.entity.Project;
import com.taibai.admin.api.entity.ProjectRole;
import com.taibai.admin.api.entity.RetrievePwdInfo;
import com.taibai.admin.api.entity.Role;
import com.taibai.admin.api.entity.Tenant;
import com.taibai.admin.api.entity.TenantRole;
import com.taibai.admin.api.entity.User;
import com.taibai.admin.api.entity.UserCount;
import com.taibai.admin.api.entity.UserPasswordLog;
import com.taibai.admin.api.entity.UserRoleProject;
import com.taibai.admin.api.feign.RemoteTokenService;
import com.taibai.admin.api.vo.ImportUserVo;
import com.taibai.admin.api.vo.PreviewInfoVO;
import com.taibai.admin.api.vo.ProjectRoleVO;
import com.taibai.admin.api.vo.ProjectVO;
import com.taibai.admin.api.vo.RoleVO;
import com.taibai.admin.api.vo.TenantOrProjectVO;
import com.taibai.admin.api.vo.TenantRoleVO;
import com.taibai.admin.api.vo.TenantVO;
import com.taibai.admin.api.vo.UserVO;
import com.taibai.admin.exceptions.UserCenterException;
import com.taibai.admin.mapper.LockedAccountRecordMapper;
import com.taibai.admin.mapper.LoginFailRecordMapper;
import com.taibai.admin.mapper.MaxHisPassCountMapper;
import com.taibai.admin.mapper.PassExpirationMapper;
import com.taibai.admin.mapper.ProjectMapper;
import com.taibai.admin.mapper.RetrievePwdMapper;
import com.taibai.admin.mapper.RoleMapper;
import com.taibai.admin.mapper.TenantMapper;
import com.taibai.admin.mapper.UserMapper;
import com.taibai.admin.mapper.UserRoleProjectMapper;
import com.taibai.admin.service.IAuthService;
import com.taibai.admin.service.IRoleService;
import com.taibai.admin.service.ISessionService;
import com.taibai.admin.service.IUserPasswordLogService;
import com.taibai.admin.service.IUserService;
import com.taibai.admin.utils.AdminUtils;
import com.taibai.admin.utils.DateUtils;
import com.taibai.admin.utils.ExcelUtil;
import com.taibai.common.core.constant.CacheConstants;
import com.taibai.common.core.constant.CommonConstants;
import com.taibai.common.core.constant.SecurityConstants;
import com.taibai.common.core.constant.enums.BusinessEnum;
import com.taibai.common.core.constant.enums.DeleteFlagStatusEnum;
import com.taibai.common.core.constant.enums.OperatingRangeEnum;
import com.taibai.common.core.constant.enums.UserTypeEnum;
import com.taibai.common.core.util.R;
import com.taibai.common.minio.service.MinioTemplate;
import com.taibai.common.security.service.FitmgrUser;
import com.taibai.common.security.util.AuthUtils;
import com.taibai.common.security.util.SecurityUtils;
import com.taibai.resource.api.feign.RemoteCmdbService;
import com.taibai.webpush.api.feign.RemoteWebpushService;
import com.google.common.collect.Lists;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * ????????? ???????????????
 * </p>
 *
 * @author Taibai
 * @since 2019-11-16
 */

@Slf4j
@Service
@AllArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final String SUPER_ADMIN = "super_admin";
    private final String TENANT_ADMIN = "tenant_admin";
    private final String TENANT_QUOTA_ADMIN = "tenant_quota_admin";
    private final String PROJECT_QUOTA_ADMIN = "project_quota_admin";
    private final String USERNAME = "^[-_!@#$%^&*a-zA-Z0-9]{1,16}$";
    private final String USER_STATUS_OFF = "1";
    private final String USER_STATUS_ON = "0";
    private final int IMPORT_MAX_SIZE = 10000;
    private final String NAME = "^[a-zA-Z\\u4e00-\\u9fa5][-_!@#$%^&*a-zA-Z0-9.\\u4e00-\\u9fa5]{1,31}$";
    private final String PHONE = "^1\\d{10}$";
    private final String EMAIL = "^[A-Za-z\\d]+([-_.][A-Za-z\\d]+)*@([A-Za-z\\d]+[-.])+[A-Za-z\\d]{2,4}$";
    private final int RECORD_IMPORT_LOG_SIZE = 10;
    private final int EXCEL_CELL_LENGTH = 1000;

    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();
    private final UserMapper userMapper;
    private final IRoleService roleService;
    private final TenantMapper tenantMapper;
    private final RoleMapper roleMapper;
    private final UserRoleProjectServiceImpl userRoleProjectService;
    private final IAuthService iAuthService;
    private final IUserPasswordLogService iUserPasswordLogService;
    private final MaxHisPassCountMapper maxHisPassCountMapper;
    /**
     * ????????????Redis
     */
    private final RedisTemplate<String, String> redisTemplate;
    /**
     * token??????
     */
    private final RemoteTokenService remoteTokenService;
    /**
     * CMDB????????????
     */
    private final RemoteCmdbService remoteCmdbService;
    private final ProjectMapper projectMapper;
    private final UserRoleProjectMapper userRoleProjectMapper;
    private final RetrievePwdMapper retrievePwdMapper;

    private final RemoteWebpushService remoteWebpushService;
    private final RemoteProcessService remoteProcessService;

    private final MinioTemplate minioTemplate;

    private final LockedAccountRecordMapper lockedAccountRecordMapper;
    private final LoginFailRecordMapper loginFailRecordMapper;

    private final AdminUtils adminUtils;

    private static final int NEW_PASSWORD_LEN = 8;

    private final ISessionService sessionService;

    private ThreadPoolTaskExecutor executor;

    private final PasswordRuleServiceImpl passwordRuleServiceImpl;
    private final PasswordTermServiceImpl passwordTermServiceImpl;
    private final PassExpirationMapper passExpirationMapper;

    private static final String HTTP_PORT = "80";

    private static final String HTTPS_PORT = "443";

    /**
     * ??????????????????UserInfo???????????????
     *
     * @param user ??????
     * @return ??????????????????UserInfo???????????????
     */
    @Override
    public UserInfo findUserInfo(User user) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUser(user);
        List<Tenant> tenantList = userMapper.queryTenantByUserId(user.getId());

        userInfo.setTenantList(tenantList);
        return userInfo;
    }

    /**
     * ??????/??????????????????
     *
     * @param userDTO ??????????????????
     * @return true/false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveUser(UserDTO userDTO) {
        List<Integer> tenantIds = new ArrayList<>();
        R<AuthCheck> r = iAuthService.newAuthCheck("user_add", SecurityUtils.getUser().getId(),
                SecurityUtils.getUser().getDefaultTenantId(), null, null, null);
        if (0 == r.getCode() && r.getData().isStatus()) {
            if (OperatingRangeEnum.ALL_CODE.equals(r.getData().getOperatingRange())) {

            } else if (OperatingRangeEnum.TENANT_CODE.equals(r.getData().getOperatingRange())) {
                tenantIds = r.getData().getTenantIds();
            } else if (OperatingRangeEnum.PROJECT_CODE.equals(r.getData().getOperatingRange())) {
                throw new UserCenterException(BusinessEnum.AUTH_CONFIG);
            } else if (OperatingRangeEnum.SELF_CODE.equals(r.getData().getOperatingRange())) {
                throw new UserCenterException(BusinessEnum.AUTH_CONFIG);
            }
        } else {
            throw new UserCenterException(BusinessEnum.AUTH_NOT);
        }
        if (SecurityUtils.getUser().getDefaultTenantId() != -1) {
            if (userDTO.getTenantId() == null) {
                throw new UserCenterException("VDC????????????????????????????????????VDC");
            }
            if (!SecurityUtils.getUser().getDefaultTenantId().equals(userDTO.getTenantId())) {
                if (tenantIds.size() > 0 && !tenantIds.contains(userDTO.getTenantId())) {
                    throw new UserCenterException("VDC??????????????????????????????????????????VDC?????????VDC?????????VDC");
                }
            }

            List<Integer> role = userDTO.getRole();
            if (CollectionUtils.isEmpty(role)) {
                throw new UserCenterException("???????????????????????????????????????????????????");
            }
            List<Role> sDefaultRoles = roleMapper.selectList(new QueryWrapper<Role>().lambda().in(Role::getId, role));
            for (Role srole : sDefaultRoles) {
                if (!(srole.getRoleCode().equals("ordinary_user") || srole.getLevel() == 2)) {
                    throw new UserCenterException("VDC???????????????????????????????????????????????????VDC???????????????");
                }
            }
        }
        if (userDTO.getTenantId() != null) {
            String tenantStatus = tenantMapper.getTenantStatus(userDTO.getTenantId());
            // ??????????????????????????????????????????
            if (CommonConstants.STATUS_DEL.equals(tenantStatus)) {
                throw new UserCenterException(BusinessEnum.USER_TENANT_FORBIDDEN);
            }
        }
        List<Integer> role = userDTO.getRole();
        if (CollectionUtils.isEmpty(role)) {
            throw new UserCenterException("???????????????????????????????????????????????????");
        }
        LambdaQueryWrapper<User> lambdaQueryWrapper = Wrappers.<User>lambdaQuery()
                .eq(StringUtils.isNotBlank(userDTO.getUsername()), User::getUsername, userDTO.getUsername());
        // ???????????????????????????????????????
        Integer count = userMapper.selectCount(lambdaQueryWrapper);
        if (count > 0) {
            throw new UserCenterException(BusinessEnum.USER_REPETITION);
        }
        // ??????????????????????????????.trim() ?????????????????????????????????
        String decodePassword = userDTO.getPassword().trim();
        // ??????????????????
        passwordRuleServiceImpl.checkPassword(decodePassword);

        // ????????????????????????
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        // ?????? 0-?????? 1-?????? 2-????????????(????????????????????????????????????????????????????????????) - 200516
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(user.getCreateTime());
        user.setDelFlag(CommonConstants.STATUS_NORMAL);
        // ????????????????????????????????????
        if (!StrUtil.isNotBlank(userDTO.getUrl())) {
            user.setAvatar(CommonConstants.USER_DEFAULT_SETAVATAR);
        }
        // ????????????
        user.setPassword(ENCODER.encode(decodePassword));
        // ????????????????????????
        user.setPassUpdateTime(LocalDateTime.now());
        // ????????????????????????
        user.setPassExpirationTime(passwordTermServiceImpl.calculateExpirationTime());
        // ??????default_role????????????????????????????????????role
        if (null != userDTO.getRole() && userDTO.getRole().size() >= 1) {
            boolean hasSysRole = userDTO.getRole().stream().anyMatch(roleId -> {
                Role roleTemp = roleMapper.selectById(roleId);
                if (roleTemp.getLevel().equals(RoleLevelEnum.SYSTEM.getCode())) {
                    return true;
                }
                return false;
            });
            if (!hasSysRole) {
                List<Role> sDefaultRoles = roleMapper.selectList(new QueryWrapper<Role>().lambda()
                        .eq(Role::getLevel, RoleLevelEnum.SYSTEM.getCode()).eq(Role::getSystemDefaultRole, true));
                if (CollectionUtils.isEmpty(sDefaultRoles)) {
                    throw new UserCenterException("???????????????????????????????????????");
                }
                userDTO.getRole().add(sDefaultRoles.get(0).getId());
            }
            boolean hasTenRole = false;
            List<Integer> roleList = userDTO.getRole();
            for (Integer roleId : roleList) {
                Role roleTemp = roleMapper.selectById(roleId);
                if (roleTemp.getLevel().equals(RoleLevelEnum.PROJECT.getCode())) {
                    throw new UserCenterException("????????????????????????????????????");
                }
                if (!hasTenRole && userDTO.getTenantId() != null
                        && roleTemp.getLevel().equals(RoleLevelEnum.TENANT.getCode())) {
                    hasTenRole = true;
                }
            }
            if (userDTO.getTenantId() != null) {
                if (!hasTenRole) {
                    List<Role> tDefaultRoles = roleMapper.selectList(new QueryWrapper<Role>().lambda()
                            .eq(Role::getLevel, RoleLevelEnum.TENANT.getCode()).eq(Role::getTenantDefaultRole, true));
                    if (CollectionUtils.isEmpty(tDefaultRoles)) {
                        throw new UserCenterException("????????????VDC?????????????????????");
                    }
                    userDTO.getRole().add(tDefaultRoles.get(0).getId());
                }
            }
        }
        if (null == userDTO.getTenantId() || userDTO.getTenantId().equals(-1)) {
            user.setDefaultTenantId(-1);
        } else {
            user.setDefaultTenantId(userDTO.getTenantId());
        }
        baseMapper.insert(user);
        /*
         * -baseMapper-----------------------------------------tips
         * 2-------------------------------------------
         */
        // ??????????????????redis
        redisTemplate.opsForValue().set(CommonConstants.USER_PREFIX + user.getId(), user.getName());
        // ?????????????????? user??? role???project ???????????????
        List<UserRoleProject> userRoleList = userDTO.getRole().stream().map(roleId -> {
            UserRoleProject userRoleProject = new UserRoleProject();
            Role roleTemp = roleMapper.selectById(roleId);
            if (roleTemp.getLevel().equals(RoleLevelEnum.SYSTEM.getCode())) {
                // ??????????????????
                userRoleProject.setUserId(user.getId());
                userRoleProject.setRoleId(roleId);
                userRoleProject.setProjectId(-1);
                userRoleProject.setTenantId(-1);
            } else {
                // ??????/Project????????????
                userRoleProject.setUserId(user.getId());
                userRoleProject.setRoleId(roleId);
                userRoleProject.setTenantId(userDTO.getTenantId());
                userRoleProject.setProjectId(-1);
            }
            return userRoleProject;
        }).collect(Collectors.toList());
        boolean res = userRoleProjectService.saveBatch(userRoleList);
        if (res) {
            iUserPasswordLogService.saveUserPasswordLog(user.getPassword(), user.getId());
            executor.execute(() -> {
                adminUtils.sendEmail(user.getId(), "????????????", "create-user", "userName:" + user.getUsername(),
                        Lists.newArrayList(user.getEmail()));
            });
        }
        return res;
    }

    /**
     * ????????????
     *
     * @param userDTO
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean resetPassword(UserDTO userDTO) {
        FitmgrUser loginUser = SecurityUtils.getUser();
        if (null == loginUser.getId()) {
            throw new UserCenterException("????????????????????????????????????");
        }
        User originalUser = userMapper.selectById(userDTO.getId());
        checkSystemInternalUser(originalUser);

        Integer defaultTenantId = loginUser.getDefaultTenantId();

        List<Integer> tenantIds = new ArrayList<>();
        R<AuthCheck> r = iAuthService.newAuthCheck("reset_password", loginUser.getId(), defaultTenantId, null, null,
                null);
        if (0 == r.getCode() && r.getData().isStatus()) {
            if (OperatingRangeEnum.ALL_CODE.equals(r.getData().getOperatingRange())) {

            } else if (OperatingRangeEnum.TENANT_CODE.equals(r.getData().getOperatingRange())) {
                tenantIds = r.getData().getTenantIds();
            } else if (OperatingRangeEnum.PROJECT_CODE.equals(r.getData().getOperatingRange())) {
                throw new UserCenterException(BusinessEnum.AUTH_CONFIG);
            } else if (OperatingRangeEnum.SELF_CODE.equals(r.getData().getOperatingRange())) {
                throw new UserCenterException(BusinessEnum.AUTH_CONFIG);
            }
        } else {
            throw new UserCenterException(BusinessEnum.AUTH_NOT);
        }

        if (defaultTenantId != -1) {
            List<Tenant> tenantList = userMapper.queryTenantByUserId(userDTO.getId());
            for (Tenant tenant : tenantList) {
                if (tenantIds.size() > 0 && !tenantIds.contains(tenant.getId())) {
                    throw new UserCenterException("????????????????????????VDC?????????????????????");
                }
            }
        }

        String newPassword = this.genRandomString(NEW_PASSWORD_LEN);
        originalUser.setPassword(ENCODER.encode(newPassword));
        // ????????????????????????
        originalUser.setPassUpdateTime(LocalDateTime.now());
        // ????????????????????????
        originalUser.setPassExpirationTime(passwordTermServiceImpl.calculateExpirationTime());
        originalUser.setUpdateTime(LocalDateTime.now());
        this.updateById(originalUser);
        Integer logNumber = iUserPasswordLogService.passwordLogCount(originalUser.getId());
        if (logNumber > SecurityConstants.HIS_PASS) {
            Boolean aBoolean = iUserPasswordLogService.userPasswordLogLimit(originalUser.getId());
            if (aBoolean) {
                logNumber = SecurityConstants.HIS_PASS;
            }
        }

        Boolean result = false;
        if (logNumber.equals(SecurityConstants.HIS_PASS)) {
            UserPasswordLog userPasswordLog = iUserPasswordLogService.earlyLog(originalUser.getId());
            Boolean aBoolean = iUserPasswordLogService.deleteUserPasswordLog(userPasswordLog.getId());
            if (aBoolean) {
                result = iUserPasswordLogService.saveUserPasswordLog(originalUser.getPassword(), originalUser.getId());
            }
        } else if (0 <= logNumber && logNumber < SecurityConstants.HIS_PASS) {
            result = iUserPasswordLogService.saveUserPasswordLog(originalUser.getPassword(), originalUser.getId());
        }

        redisTemplate.delete(CacheConstants.USER_DETAILS + originalUser.getUsername());

        executor.execute(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
            adminUtils.kickoutByUserId(userDTO.getId(), "??????????????????????????????, ???????????????");

            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("userName", originalUser.getUsername());
            parameters.put("newPassword", newPassword);
            adminUtils.sendEmail(userDTO.getId(), "????????????", "reset-password", parameters,
                    Lists.newArrayList(originalUser.getEmail()));
        });

        return result;
    }

    private String genRandomString(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    /**
     * ??????userId????????????
     *
     * @param userId ??????ID
     * @return true/false
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean removeUserById(Integer userId) {
        User user = baseMapper.selectById(userId);
        if (null == user) {
            throw new UserCenterException("??????????????????");
        }
        checkSystemInternalUser(user);

        Integer loginUserId = SecurityUtils.getUser().getId();
        Integer defaultTenantId = SecurityUtils.getUser().getDefaultTenantId();

        List<Integer> tenantIds = new ArrayList<>();
        R<AuthCheck> r = iAuthService.newAuthCheck("user_delete", loginUserId, defaultTenantId, null, null, null);
        if (0 == r.getCode() && r.getData().isStatus()) {
            if (OperatingRangeEnum.ALL_CODE.equals(r.getData().getOperatingRange())) {

            } else if (OperatingRangeEnum.TENANT_CODE.equals(r.getData().getOperatingRange())) {
                tenantIds = r.getData().getTenantIds();
            } else if (OperatingRangeEnum.PROJECT_CODE.equals(r.getData().getOperatingRange())) {
                throw new UserCenterException(BusinessEnum.AUTH_CONFIG);
            } else if (OperatingRangeEnum.SELF_CODE.equals(r.getData().getOperatingRange())) {
                throw new UserCenterException(BusinessEnum.AUTH_CONFIG);
            }
        } else {
            throw new UserCenterException(BusinessEnum.AUTH_NOT);
        }

        if (defaultTenantId != -1) {
            List<Tenant> tenantList = userMapper.queryTenantByUserId(user.getId());
            for (Tenant tenant : tenantList) {
                if (tenantIds.size() > 0 && !tenantIds.contains(tenant.getId())) {
                    throw new UserCenterException("????????????????????????VDC???????????????");
                }
            }
        }

        // ???????????????????????????????????????????????????????????????????????????????????????
        R<Boolean> customerHasResource = remoteCmdbService.isCustomerHasResource("2", userId);
        if (null != customerHasResource) {
            if (customerHasResource.getData()) {
                throw new UserCenterException("????????????????????????????????????????????????");
            }
        }

        // ?????????????????????????????????
        loginFailRecordMapper
                .delete(new QueryWrapper<LoginFailRecord>().lambda().eq(LoginFailRecord::getUserId, userId));
        lockedAccountRecordMapper
                .delete(new QueryWrapper<LockedAccountRecord>().lambda().eq(LockedAccountRecord::getUserId, userId));

        userRoleProjectMapper
                .delete(new QueryWrapper<UserRoleProject>().lambda().eq(UserRoleProject::getUserId, userId));
        this.removeById(userId);
        // ?????????????????????????????????????????????key?????????Redis?????????????????????
        if (StrUtil.isNotBlank(user.getUsername())) {
            // ???????????????
            Set<String> keys = redisTemplate.keys(CacheConstants.USER_DETAILS_PREFIX + user.getUsername() + "*");
            redisTemplate.delete(keys);
            log.info(user.getUsername() + "???????????????????????????");
        }
        // ????????????id???name??????Redis??????????????????id???name?????????
        redisTemplate.delete(CommonConstants.USER_PREFIX + userId);

        executor.execute(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
            adminUtils.kickoutByUserId(userId, "????????????????????????");

            Integer sendUserId = userId;
            if (userId.equals(loginUserId)) {
                sendUserId = null;
            }
            adminUtils.sendEmail(sendUserId, "????????????", "delete-account", "account:" + user.getUsername(),
                    Lists.newArrayList(user.getEmail()));
        });

        return Boolean.TRUE;
    }

    private void checkSystemInternalUser(User user) {
        if (UserTypeEnum.SYSTEM_INTERNAL.name().equals(user.getUserType())) {
            throw new UserCenterException("??????????????????????????????");
        }
    }

    /**
     * ???????????????/???????????????????????????
     *
     * @param userDTO ????????????
     * @return true/false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateUserInfo(UserDTO userDTO) {
        try {
            User user = new User();
            BeanUtils.copyProperties(userDTO, user);
            user.setUpdateTime(LocalDateTime.now());
            // ?????????????????????????????????????????????????????????????????????????????????
            user.setUsername(null);
            user.setPassword(null);
            user.setDefaultTenantId(null);
            // ?????? defaultProject ??????null???????????????null?????????????????? ?????????
            User userInfo = baseMapper.selectById(userDTO.getId());
            checkSystemInternalUser(userInfo);

            if (null != userDTO.getRole() && userDTO.getRole().size() >= 1) {
                boolean hasSrole = userDTO.getRole().stream().anyMatch(roleId -> {
                    Role roleTemp = roleMapper.selectById(roleId);
                    if (!roleTemp.getLevel().equals(RoleLevelEnum.SYSTEM.getCode())) {
                        return true;
                    }
                    return false;
                });
                if (hasSrole) {
                    throw new UserCenterException("??????????????????????????????");
                }
            }

            /** -------?????????tenant???????????????projectId,?????????????????????project(?????????????????????????????????)---------- */
            if (UserTypeEnum.SYSTEM.toString().equals(userInfo.getUserType())) {
                List<UserRoleProject> list = userRoleProjectService.list(Wrappers.<UserRoleProject>update().lambda()
                        .eq(UserRoleProject::getUserId, userDTO.getId()).eq(UserRoleProject::getTenantId, -1));
                List<Integer> roleIds = userDTO.getRole();
                for (UserRoleProject userRoleProject : list) {
                    if (!roleIds.contains(userRoleProject.getRoleId())) {
                        throw new UserCenterException("??????????????????????????????");
                    }
                }
            }
            this.updateById(user);

            // ??????redis???????????????
            redisTemplate.opsForValue().set(CommonConstants.USER_PREFIX + user.getId(), user.getName());

            userRoleProjectService.remove(Wrappers.<UserRoleProject>update().lambda()
                    .eq(UserRoleProject::getUserId, userDTO.getId()).eq(UserRoleProject::getTenantId, -1));
            userDTO.getRole().forEach(roleId -> {
                UserRoleProject userRoleProject = new UserRoleProject();
                userRoleProject.setUserId(user.getId());
                userRoleProject.setRoleId(roleId);
                userRoleProject.setProjectId(-1);
                userRoleProject.setTenantId(-1);
                userRoleProjectService.save(userRoleProject);
            });

            User user1 = baseMapper.selectById(userDTO.getId());
            List<Role> roleList = userMapper.queryRoleByUserId(userDTO.getId());
            List<String> roleNames = roleList.stream().map(Role::getRoleName).collect(Collectors.toList());
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("account", user1.getName());
            parameters.put("username", user1.getUsername());
            parameters.put("email", user1.getEmail());
            parameters.put("phone", user1.getPhone());
            parameters.put("role", JSON.toJSONString(roleNames));
            adminUtils.sendEmail(userDTO.getId(), "??????????????????", "modify-account-info", parameters,
                    Lists.newArrayList(user1.getEmail()));

            return Boolean.TRUE;
        } catch (UserCenterException th) {
            throw th;
        } catch (Throwable th) {
            log.error("fail ", th);
            throw new RuntimeException(th);
        }
    }

    /**
     * ????????????????????????
     */
    @Override
    public Boolean renameUser(UserDTO userDTO) {
        FitmgrUser fitmgrUser = SecurityUtils.getUser();
        if (fitmgrUser == null || fitmgrUser.getId() == null) {
            throw new UserCenterException("???????????????");
        }

        User user = this.getOne(Wrappers.<User>query().lambda().eq(User::getId, fitmgrUser.getId()));
        if (user == null) {
            throw new UserCenterException("???????????????");
        }
        String username = userDTO.getUsername();
        if (!matches(USERNAME, username)) {
            throw new UserCenterException("????????????????????????????????????-_!@#$%^&*???1-16?????????????????????");
        }
        if (!UserTypeEnum.SYSTEM.toString().equals(user.getUserType())) {
            throw new UserCenterException("????????????????????????????????????");
        }
        User existUser = this.getOne(Wrappers.<User>query().lambda().eq(User::getUsername, userDTO.getUsername()));

        if (existUser != null) {
            throw new UserCenterException("??????????????????");
        }

        user.setUsername(userDTO.getUsername());
        user.setUpdateTime(LocalDateTime.now());
        return baseMapper.updateById(user) > 0;
    }

    /**
     * ??????????????????????????????????????????(??????????????????????????????) ???????????????1??????????????????????????????????????????????????? ??????????????????????????????
     *
     * @param userDTO ????????????
     * @return true/false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateUserPassword(UserDTO userDTO) {
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        FitmgrUser userInfo = SecurityUtils.getUser();
        if (null == userInfo.getId()) {
            throw new UserCenterException("????????????????????????????????????");
        }
        // userInfo???????????????????????????????????????????????????????????????
        User originalUser = userMapper.selectById(userInfo.getId());
        checkSystemInternalUser(originalUser);

        String decodePassword = userDTO.getPassword().trim();
        String decodeNewPassword = userDTO.getNewpassword1().trim();
        // ??????????????????
        if (StrUtil.isNotBlank(userDTO.getPassword()) && StrUtil.isNotBlank(userDTO.getNewpassword1())) {
            if (decodePassword.equals(decodeNewPassword)) {
                throw new UserCenterException(BusinessEnum.USER_PASSWORD_REPETITION);
            }
            // ?????????????????????????????????
            if (ENCODER.matches(decodePassword, originalUser.getPassword())) {
                // ???????????????????????????(??????????????????????????????)
                user.setPassword(ENCODER.encode(decodeNewPassword));
            } else {
                throw new UserCenterException(BusinessEnum.CHECK_PASSWORD);
            }
        }
        // ??????????????????
        passwordRuleServiceImpl.checkPassword(decodeNewPassword);
        // ????????????????????????
        user.setPassUpdateTime(LocalDateTime.now());
        // ????????????????????????
        user.setPassExpirationTime(passwordTermServiceImpl.calculateExpirationTime());

        MaxHisPassCount maxHisPassCount = maxHisPassCountMapper.selectList(new QueryWrapper<MaxHisPassCount>().lambda())
                .get(0);
        Integer count = maxHisPassCount.getCount();
        List<UserPasswordLog> userPasswordLogs = iUserPasswordLogService.userPasswordLogList(userInfo.getId());
        if (null != userPasswordLogs && userPasswordLogs.size() != 0) {
            int a = userPasswordLogs.size() > count ? count : userPasswordLogs.size();
            for (int i = 0; i < a; i++) {
                UserPasswordLog upl = userPasswordLogs.get(i);
                if (ENCODER.matches(decodeNewPassword, upl.getPasswordLog())) {
                    throw new UserCenterException("???????????????????????????" + count + "???????????????");
                }
            }
        }
        user.setId(userInfo.getId());
        user.setUpdateTime(LocalDateTime.now());
        this.updateById(user);
        // ??????????????????????????????
        passExpirationMapper
                .delete(new QueryWrapper<PassExpiration>().lambda().eq(PassExpiration::getUserId, user.getId()));

        Integer logNumber = iUserPasswordLogService.passwordLogCount(userInfo.getId());
        if (logNumber > SecurityConstants.HIS_PASS) {
            Boolean aBoolean = iUserPasswordLogService.userPasswordLogLimit(userInfo.getId());
            if (aBoolean) {
                logNumber = SecurityConstants.HIS_PASS;
            }
        }
        if (logNumber.equals(SecurityConstants.HIS_PASS)) {
            UserPasswordLog userPasswordLog = iUserPasswordLogService.earlyLog(userInfo.getId());
            Boolean aBoolean = iUserPasswordLogService.deleteUserPasswordLog(userPasswordLog.getId());
            if (aBoolean) {
                Boolean aBoolean1 = iUserPasswordLogService.saveUserPasswordLog(user.getPassword(), userInfo.getId());
                return aBoolean1;
            }
        } else if (0 <= logNumber && logNumber < SecurityConstants.HIS_PASS) {
            Boolean aBoolean2 = iUserPasswordLogService.saveUserPasswordLog(user.getPassword(), userInfo.getId());
            return aBoolean2;
        }
        return Boolean.TRUE;
    }

    /**
     * ????????????????????????
     *
     * @param userDTO ????????????
     * @return true/false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateUserStatus(UserDTO userDTO) {
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        user.setUpdateTime(LocalDateTime.now());
        User userTemp = baseMapper.selectById(userDTO.getId());
        checkSystemInternalUser(userTemp);

        List<Integer> tenantIds = new ArrayList<>();
        R<AuthCheck> r = iAuthService.newAuthCheck("enable_disable_user", SecurityUtils.getUser().getId(),
                SecurityUtils.getUser().getDefaultTenantId(), null, null, null);
        if (0 == r.getCode() && r.getData().isStatus()) {
            if (OperatingRangeEnum.ALL_CODE.equals(r.getData().getOperatingRange())) {

            } else if (OperatingRangeEnum.TENANT_CODE.equals(r.getData().getOperatingRange())) {
                tenantIds = r.getData().getTenantIds();
            } else if (OperatingRangeEnum.PROJECT_CODE.equals(r.getData().getOperatingRange())) {
                throw new UserCenterException(BusinessEnum.AUTH_CONFIG);
            } else if (OperatingRangeEnum.SELF_CODE.equals(r.getData().getOperatingRange())) {
                throw new UserCenterException(BusinessEnum.AUTH_CONFIG);
            }
        } else {
            throw new UserCenterException(BusinessEnum.AUTH_NOT);
        }

        // ??????????????????????????????????????????
        if (USER_STATUS_OFF.equals(userDTO.getStatus())) {
            Integer defaultTenantId = SecurityUtils.getUser().getDefaultTenantId();
            if (defaultTenantId != -1) {
                List<Tenant> tenantList = userMapper.queryTenantByUserId(user.getId());
                for (Tenant tenant : tenantList) {
                    if (tenantIds.size() > 0 && !tenantIds.contains(tenant.getId())) {
                        throw new UserCenterException("????????????????????????VDC???????????????");
                    }
                }
            }

            User user1 = baseMapper.selectById(userDTO.getId());
            if (StrUtil.isNotBlank(user1.getUsername())) {
                baseMapper.updateById(user);
                // ???????????????
                Set<String> keys = redisTemplate.keys(CacheConstants.USER_DETAILS_PREFIX + user1.getUsername() + "*");
                redisTemplate.delete(keys);
                log.info(user1.getUsername() + "???????????????????????????");

                executor.execute(() -> {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                    adminUtils.kickoutByUserId(userDTO.getId(), "????????????????????????");
                    adminUtils.sendEmail(userDTO.getId(), "????????????", "enable-disable-account",
                            "account:" + user1.getUsername() + ",action:??????", Lists.newArrayList(user1.getEmail()));
                });
            }
        } else if (USER_STATUS_ON.equals(userDTO.getStatus())) {
            baseMapper.updateById(user);
            executor.execute(() -> {
                User user1 = baseMapper.selectById(userDTO.getId());
                adminUtils.sendEmail(userDTO.getId(), "????????????", "enable-disable-account",
                        "account:" + user1.getUsername() + ",action:??????", Lists.newArrayList(user1.getEmail()));
            });
        }

        return Boolean.TRUE;
    }

    /**
     * ??????????????????
     *
     * @param userDTO ??????????????????DTO
     * @return true/false
     */
    @Override
    public Boolean updateUserAvatar(UserDTO userDTO) {
        User user = new User();
        user.setId(userDTO.getId());
        user.setAvatar(userDTO.getAvatar());
        user.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(user);
        return Boolean.TRUE;
    }

    /**
     * ??????userId??????UserVO????????????
     *
     * @param userId ??????id
     * @return UserVO ??????????????????
     * @throws NullPointerException
     */
    @Override
    public UserVO queryDetailsByUserId(Integer userId) throws NullPointerException {
        UserVO userVO = userMapper.queryDetailsByUserId(userId);
        // ??????user???????????????id?????????????????????????????????userVO??????????????????
        if (null != userVO) {
            // ??????tenantId???TenantName
            // ??????????????????
            List<Role> roleList = userMapper.queryRoleByUserId(userId);
            userVO.setRoleList(roleList);
            List<Role> sysRoleList = userMapper.queryRoleByUserIdAndRoleLevel(userId, RoleLevelEnum.SYSTEM.getCode());
            userVO.setSysRoleList(sysRoleList);
            // ??????project??????
            List<Project> projects = userMapper.queryProjectByUserId(userId);
            userVO.setProjectList(projects);
            List<Tenant> tenants = userMapper.queryTenantByUserId(userId);
            userVO.setTenantList(tenants);
            // ????????????
            userVO.setPassword(null);
            return userVO;
        } else {
            throw new UserCenterException(BusinessEnum.USER_NONENTITY);
        }
    }

    @Override
    public List<TenantRoleVO> queryTenantRoleByUserId(Integer userId) {
        List<TenantRoleVO> tenantRoleVos = new ArrayList<>();
        Map<Integer, TenantRoleVO> tenantRoleVoMap = new LinkedHashMap<>();
        List<TenantRole> tenantRoles = userMapper.queryTenantRoleByUserId(userId);
        if (CollectionUtils.isNotEmpty(tenantRoles)) {
            for (TenantRole tenantRole : tenantRoles) {
                TenantRoleVO tenantRoleVO = tenantRoleVoMap.get(tenantRole.getId());
                if (tenantRoleVO == null) {
                    tenantRoleVO = new TenantRoleVO();
                    BeanUtils.copyProperties(tenantRole, tenantRoleVO);
                    tenantRoleVO.setRoles(new ArrayList<>());
                    tenantRoleVoMap.put(tenantRole.getId(), tenantRoleVO);
                }
                Role role = new Role();
                role.setId(tenantRole.getRoleId());
                role.setRoleName(tenantRole.getRoleName());
                tenantRoleVO.getRoles().add(role);
            }
        }
        tenantRoleVos.addAll(tenantRoleVoMap.values());
        return tenantRoleVos;
    }

    @Override
    public List<ProjectRoleVO> queryProjectRoleByUserId(Integer userId) {
        List<ProjectRoleVO> proejectRoleVos = new ArrayList<>();
        Map<Integer, ProjectRoleVO> projectRoleVoMap = new LinkedHashMap<>();
        List<ProjectRole> projectRoles = userMapper.queryProjectRoleByUserId(userId);
        if (CollectionUtils.isNotEmpty(projectRoles)) {
            for (ProjectRole projectRole : projectRoles) {
                ProjectRoleVO projectRoleVO = projectRoleVoMap.get(projectRole.getId());
                if (projectRoleVO == null) {
                    projectRoleVO = new ProjectRoleVO();
                    BeanUtils.copyProperties(projectRole, projectRoleVO);
                    projectRoleVO.setRoles(new ArrayList<>());
                    projectRoleVoMap.put(projectRole.getId(), projectRoleVO);
                }
                Role role = new Role();
                role.setId(projectRole.getRoleId());
                role.setRoleName(projectRole.getRoleName());
                projectRoleVO.getRoles().add(role);
            }
        }
        proejectRoleVos.addAll(projectRoleVoMap.values());
        if (CollectionUtils.isEmpty(proejectRoleVos)) {
            return proejectRoleVos;
        }
        for (ProjectRoleVO proejectRoleVO : proejectRoleVos) {
            Tenant tenant = tenantMapper.selectById(proejectRoleVO.getTenantId());
            proejectRoleVO.setTenantName(tenant.getName());
        }
        // ??????tenantName
        return proejectRoleVos;
    }

    @Override
    public UserVO queryDetailsByUsername(String username) {
        LambdaQueryWrapper<User> lambdaQueryWrapper = Wrappers.<User>lambdaQuery().eq(StringUtils.isNotBlank(username),
                User::getUsername, username);
        User users = userMapper.selectOne(lambdaQueryWrapper);
        if (null != users) {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(users, userVO);
            List<Role> roleList = userMapper.queryRoleByUserId(users.getId());
            userVO.setRoleList(roleList);
            List<Role> sysRoleList = userMapper.queryRoleByUserIdAndRoleLevel(users.getId(),
                    RoleLevelEnum.SYSTEM.getCode());
            userVO.setSysRoleList(sysRoleList);
            // ??????project??????
            List<Project> projects = userMapper.queryProjectByUserId(users.getId());
            userVO.setProjectList(projects);
            List<Tenant> tenants = userMapper.queryTenantByUserId(users.getId());
            userVO.setTenantList(tenants);
            // ????????????
            userVO.setPassword(null);
            return userVO;
        } else {
            throw new UserCenterException(BusinessEnum.USER_NONENTITY);
        }
    }

    @Override
    public List<User> selectUserListByEmailOrPhone(String emailOrPhone) {
        return baseMapper.selectList(new QueryWrapper<User>().lambda().eq(User::getEmail, emailOrPhone).or()
                .eq(User::getPhone, emailOrPhone));
    }

    /**
     * ??????????????????(??????)
     *
     * @return UserList????????????
     */
    @Override
    public List<User> selectUserList() {
        List<User> users = baseMapper.selectList(
                new QueryWrapper<User>().lambda().ne(User::getUserType, UserTypeEnum.SYSTEM_INTERNAL.name()));
        for (User u : users) {
            u.setPassword(null);
        }
        return users;
    }

    /**
     * ?????????????????????UserVO??????
     *
     * @param page    ????????????
     * @param userDTO ??????????????????DTO
     * @return UserVO??????
     * @description 0-?????? 1-?????? 2-project 3-??????
     */
    @Override
    public IPage<UserVO> selectListByCondition(Page page, UserDTO userDTO, Integer userId) {
        // ?????????????????????controller???????????????userId???
        R<AuthCheck> r = iAuthService.newAuthCheck("user_select_page", userId,
                SecurityUtils.getUser().getDefaultTenantId(), null, null, null);
        if (0 == r.getCode() && r.getData().isStatus()) {
            if (OperatingRangeEnum.ALL_CODE.equals(r.getData().getOperatingRange())) {
                IPage<UserVO> userVoiPage = userListInfo(page, userDTO, r.getData());
                return userVoiPage;
            } else if (OperatingRangeEnum.TENANT_CODE.equals(r.getData().getOperatingRange())) {
                // (Integer) r.getData() ?????????????????????1-?????????
                userDTO.setTenantIds(r.getData().getTenantIds());
                IPage<UserVO> userVoiPage = userListInfo(page, userDTO, r.getData());
                return userVoiPage;
            } else if (OperatingRangeEnum.PROJECT_CODE.equals(r.getData().getOperatingRange())) {
                // (Integer) r.getData() ?????????????????????2-??????-project???
                IPage<UserVO> userVoiPage = userListInfo(page, userDTO, r.getData());
                return userVoiPage;
            } else if (OperatingRangeEnum.SELF_CODE.equals(r.getData().getOperatingRange())) {
                // (Integer) r.getData() ?????????????????????3-???????????????
                userDTO.setId(r.getData().getUserId());
                IPage<UserVO> userVoiPage = userListInfo(page, userDTO, r.getData());
                return userVoiPage;
            }
            throw new UserCenterException(BusinessEnum.AUTH_CONFIG);
        }
        throw new UserCenterException(BusinessEnum.AUTH_NOT);
    }

    @Override
    public IPage<UserVO> selectUsersListByCondition(Page page, UserDTO userDTO) {
        return userMapper.selectListByCondition(page, userDTO);
    }

    /**
     * ?????????????????????UserVO?????? - ??????????????????
     * 
     * @param page      page
     * @param userDTO   userDTO
     * @param authCheck authCheck
     * @return IPage
     */
    private IPage<UserVO> userListInfo(Page page, UserDTO userDTO, AuthCheck authCheck) {
        log.info(">>>>>>>> userDTO values is {}", JSONObject.toJSONString(userDTO));
        IPage<UserVO> userVoLst;
        if (OperatingRangeEnum.PROJECT_CODE.equals(authCheck.getOperatingRange())) {
            userVoLst = userMapper.selectListByProjectCondition(page, userDTO, authCheck);
        } else {
            userVoLst = userMapper.selectListByCondition(page, userDTO);
        }
        List<UserVO> records = userVoLst.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return userVoLst;
        }
        for (UserVO userRec : records) {
            LambdaQueryWrapper<UserRoleProject> wrapperQuery = Wrappers.<UserRoleProject>lambdaQuery().eq(true,
                    UserRoleProject::getUserId, userRec.getId());
            List<UserRoleProject> userRoleProjects = userRoleProjectMapper.selectList(wrapperQuery);
            Set<Integer> projectIds = new HashSet<>();
            Set<Integer> tenantIds = new HashSet<>();
            userRoleProjects.forEach(userRoleProject -> {
                projectIds.add(userRoleProject.getProjectId());
                tenantIds.add(userRoleProject.getTenantId());
            });
            // ??????project??????
            if (CollectionUtils.isNotEmpty(projectIds)) {
                LambdaQueryWrapper<Project> wrapperQueryProject = Wrappers.<Project>lambdaQuery().in(true,
                        Project::getId, new ArrayList<>(projectIds));
                userRec.setProjectList(projectMapper.selectList(wrapperQueryProject));
            }
            // ??????tenant??????
            if (CollectionUtils.isNotEmpty(tenantIds)) {
                LambdaQueryWrapper<Tenant> wrapperQueryTenant = Wrappers.<Tenant>lambdaQuery().in(true, Tenant::getId,
                        new ArrayList<>(tenantIds));
                userRec.setTenantList(tenantMapper.selectList(wrapperQueryTenant));
            }
        }
        log.info(">>>>>>>>>> userVoLst value is {}", JSONObject.toJSONString(userVoLst.getRecords()));
        return userVoLst;
    }

    /**
     * ????????????id???????????????????????????????????????
     *
     * @param userId ??????id
     * @return List<Role> ????????????
     */
    @Override
    public List<Role> queryRoleByUserId(Integer userId) {
        List<Role> roleList = userMapper.queryRoleByUserId(userId);
        return roleList;
    }

    @Override
    public List<Map<String, Integer>> queryRoleByUserIdAndTenantId(Integer userId, Integer tenantId) {
        List<Map<String, Integer>> roleList = userMapper.queryRoleByUserIdAndTenantId(userId, tenantId);
        return roleList;
    }

    /**
     * ????????????id??????????????????????????????????????????
     *
     * @param tenantId ??????id
     * @return ????????????
     */
    @Override
    public List<Role> selectRoleByTenantId(Integer tenantId) {
        List<Role> roleList = userMapper.selectRoleByTenantId(tenantId);
        return roleList;
    }

    /**
     * ????????????id?????????????????????????????????????????????????????????????????? Activiti??????
     *
     * @param tenantId ??????id
     * @return ????????????
     */
    @Override
    public List<Role> selectRoleByTenantIdAndRoleCode(Integer tenantId) {
        // tenantId???null????????????????????????????????????????????????????????????????????????
        // tenantId???????????????????????????????????????????????????
        List<Role> roleList = userMapper.selectRoleByTenantIdAndRoleCode(tenantId);
        return roleList;
    }

    /**
     * ????????????id?????????????????????project????????????
     *
     * @param userId ??????id
     * @return List<Project> ????????????
     */
    @Override
    public List<Project> queryProjectByUserId(Integer userId) {
        List<Project> projects = userMapper.queryProjectByUserId(userId);
        return projects;
    }

    /**
     * ????????????id??????????????????????????????????????????
     *
     * @param userId ??????id
     * @return List<User>
     */
    @Override
    public List<User> queryTenantInfoByUserId(Integer userId) {
        List<UserRoleProject> userRoleProjects = userRoleProjectMapper.selectList(new QueryWrapper<UserRoleProject>()
                .lambda().eq(UserRoleProject::getUserId, userId).gt(UserRoleProject::getTenantId, 0));
        UserDTO userDTO = new UserDTO();
        List<Integer> tenantIds = userRoleProjects.stream().map(UserRoleProject::getTenantId)
                .collect(Collectors.toList());
        userDTO.setTenantIds(tenantIds);
        return userMapper.selectUsersListNoPageByCondition(userDTO);
    }

    @Override
    public IPage<UserVO> queryPageTenantInfoByUserId(Integer userId, Page page, String queryName) {
        List<UserRoleProject> userRoleProjects = userRoleProjectMapper.selectList(new QueryWrapper<UserRoleProject>()
                .lambda().eq(UserRoleProject::getUserId, userId).gt(UserRoleProject::getTenantId, 0));
        UserDTO userDTO = new UserDTO();
        List<Integer> tenantIds = userRoleProjects.stream().map(UserRoleProject::getTenantId)
                .collect(Collectors.toList());
        userDTO.setTenantIds(tenantIds);
        userDTO.setName(queryName);
        return userMapper.selectListByCondition(page, userDTO);
    }

    /**
     * 1?????????tenantId??????????????????tenant?????????????????????????????? 2???????????????user????????????user?????????project??????
     *
     * @param tenantId  ??????id
     * @param queryName ??????????????????
     * @return ??????????????????
     */
    @Override
    public List<UserVO> queryUserByTenantId(Integer tenantId, String queryName, Page page) {
        // ??????project??????
        List<UserVO> userVos = userMapper.queryUserByTenantId(tenantId, queryName);
        return userVos;
    }

    /**
     * 1?????????tenantIdList??????????????????tenant?????????????????????????????? 2???????????????user????????????user?????????project??????
     *
     * @param tenantIdList ??????id??????
     * @return ??????????????????
     */
    @Override
    public List<UserVO> queryUserByTenantIdList(List<Integer> tenantIdList) {
        // ??????project??????
        List<UserVO> userVoList = userMapper.queryUserByTenantIdList(tenantIdList);
        for (UserVO userVO : userVoList) {
            // ????????????user???project??????
            List<Project> projects = userMapper.queryProjectByUserId(userVO.getId());
            userVO.setProjectList(projects);
        }
        return userVoList;
    }

    /**
     * 1?????????tenantId??????????????????tenant??????????????????????????????(??????)
     *
     * @param tenantId ??????id
     * @return ??????????????????
     * @description ????????????????????????????????????user-token
     */
    @Override
    public List<UserVO> queryUserListByTenantId(Integer tenantId) {
        List<UserVO> userVoList = userMapper.queryUserListByTenantId(tenantId);
        return userVoList;
    }

    @Override
    public List<UserCount> queryUserCountByTenantIdList(List<Integer> tenantIdList) {
        List<UserCount> userVoList = userMapper.queryUserCountByTenantIdList(tenantIdList);
        return userVoList;
    }

    @Override
    public List<UserCount> queryUserCountByProjectIdList(List<Integer> projectIdList) {
        List<UserCount> userVoList = userMapper.queryUserCountByProjectIdList(projectIdList);
        return userVoList;
    }

    /**
     * ??????projectId????????????????????????
     *
     * @param projectId ??????id
     * @return List<UserVO> ??????????????????
     */
    @Override
    public List<UserVO> queryUserByProjectId(Integer projectId) {
        List<UserVO> userVos = userMapper.queryUserByProjectId(projectId);
        return userVos;
    }

    /**
     * ??????projectId?????????peiject?????????tenant??????????????????????????????
     *
     * @param projectId ??????id
     * @param queryName ??????????????????
     * @return List<UserVO> ??????????????????
     */
    @Override
    public List<UserVO> queryTenantUserByProjectId(Integer projectId, String queryName) {
        List<UserVO> userVos = userMapper.queryTenantUserByProjectId(projectId, queryName);
        return userVos;
    }

    /**
     * ????????????id????????????????????????
     *
     * @param userId ??????id
     * @return UserVO ??????????????????
     */
    @Override
    public UserVO queryAffiliationType(Integer userId) {
        UserVO userVO = userMapper.queryAffiliationType(userId);
        userVO.setAffiliationType("0");
        return userVO;
    }

    /**
     * ????????????????????????
     *
     * @param userDTO userDTO.tenantId ??????id??????????????????????????????
     * @return PreviewInfoVO ????????????????????????
     */
    @Override
    public PreviewInfoVO previewInformation(UserDTO userDTO) {
        PreviewInfoVO previewInfoVO = new PreviewInfoVO();
        // tenantId????????????tenantNumber = n
        if (null == userDTO.getTenantId() && null == userDTO.getProjectId()) {
            TenantVO tenantVO = userMapper.queryTenantNumber();
            previewInfoVO.setTenantNumber(tenantVO.getTenantNumber());
            // ??????project??????
            ProjectVO projectVO = userMapper.queryProjectNumber(userDTO);
            previewInfoVO.setProjectNumber(projectVO.getProjectNumber());
            // ??????role??????
            RoleVO roleVO = userMapper.queryAllRoleNumber();
            previewInfoVO.setRoleNumber(roleVO.getRoleNumber());
            // ??????user??????
            UserVO userVO = userMapper.queryUserNumber(userDTO);
            previewInfoVO.setUserNumber(userVO.getUserNumber());

            List<UserVO> userVos = userMapper.queryUserRecentlyNumber(userDTO);
            HashMap<String, Object> stringIntegerHashMap = recentlyNumber(userVos);
            previewInfoVO.setRecentlyNumber(stringIntegerHashMap);
        } else if (null != userDTO.getTenantId()) {
            // tenantId??????????????????????????????????????????????????????tenantNumber = 1
            previewInfoVO.setTenantNumber(1);
            // ????????????project??????
            ProjectVO projectVO = userMapper.queryProjectNumber(userDTO);
            previewInfoVO.setProjectNumber(projectVO.getProjectNumber());
            // ???????????????????????????role??????
            RoleVO roleVO = userMapper.queryRoleNumber(userDTO);
            previewInfoVO.setRoleNumber(roleVO.getRoleNumber());
            // ??????????????????????????????
            UserVO userVO = userMapper.queryUserNumber(userDTO);
            previewInfoVO.setUserNumber(userVO.getUserNumber());
            List<UserVO> userVos = userMapper.queryUserRecentlyNumber(userDTO);
            HashMap<String, Object> stringIntegerHashMap = recentlyNumber(userVos);
            previewInfoVO.setRecentlyNumber(stringIntegerHashMap);
        } else if (null != userDTO.getProjectId()) {
            previewInfoVO.setTenantNumber(1);
            previewInfoVO.setProjectNumber(1);
            RoleVO roleVO = userMapper.queryRoleNumber(userDTO);
            previewInfoVO.setRoleNumber(roleVO.getRoleNumber());
            UserVO userVO = userMapper.queryUserNumber(userDTO);
            previewInfoVO.setUserNumber(userVO.getUserNumber());
            List<UserVO> userVos = userMapper.queryProjectUserRecentlyNumber(userDTO);
            HashMap<String, Object> stringIntegerHashMap = recentlyNumber(userVos);
            previewInfoVO.setRecentlyNumber(stringIntegerHashMap);
        }
        return previewInfoVO;
    }

    /**
     * ????????????project????????????
     *
     * @return userVO????????????
     */
    @Override
    public UserVO userTenantProjectRelation() {
        UserVO userVO = new UserVO();
        // ??????token??????????????????,????????????userId = userInfo.getId()
        FitmgrUser userInfo = SecurityUtils.getUser();
        userVO.setId(userInfo.getId());
        return userVO;
    }

    /**
     * ????????????code????????????????????????????????????
     *
     * @param roleCode ????????????Code
     * @return List<UserVO> ????????????????????????
     */
    @Override
    public List<UserVO> queryUserInfoByRoleCodeAndTenantId(String roleCode) {
        log.info("========roleCode is {}======", roleCode);
        // ??????????????????
        LambdaQueryWrapper<Role> roleQueryWrapper = Wrappers.<Role>lambdaQuery().eq(Role::getRoleCode, roleCode)
                .eq(Role::getDelFlag, String.valueOf(DeleteFlagStatusEnum.VIEW.getStatus()));
        Role role = roleMapper.selectOne(roleQueryWrapper);
        if (null == role) {
            return new ArrayList<>();
        }
        // ??????????????????
        Integer code = RoleLevelEnum.SYSTEM.getCode();
        if (code.equals(role.getLevel())) {
            // ?????????????????????????????????
            return userMapper.queryUserInfoByRoleCode(roleCode);
        }
        // ??????tenantId
        Integer tenantId = null;
        if (SecurityUtils.getUser() != null) {
            tenantId = SecurityUtils.getUser().getDefaultTenantId();
        }
        return userMapper.queryUserInfoByRoleCodeAndTenantId(roleCode, tenantId);
    }

    @Override
    public List<UserVO> queryUserInfoByRoleCodeAndTenantId(Integer tenantId, String roleCode) {
        LambdaQueryWrapper<Role> roleQueryWrapper = Wrappers.<Role>lambdaQuery().eq(Role::getRoleCode, roleCode)
                .eq(Role::getDelFlag, String.valueOf(DeleteFlagStatusEnum.VIEW.getStatus()));
        Role role = roleMapper.selectOne(roleQueryWrapper);
        if (null == role) {
            return new ArrayList<>();
        }
        // ??????????????????
        Integer code = RoleLevelEnum.SYSTEM.getCode();
        if (code.equals(role.getLevel())) {
            // ?????????????????????????????????
            return userMapper.queryUserInfoByRoleCode(roleCode);
        }
        return userMapper.queryUserInfoByRoleCodeAndTenantId(roleCode, tenantId);
    }

    @Override
    public List<UserVO> queryUserInfoByRoleCodesAndTenantId(Integer tenantId, List<String> roleCodes) {
        LambdaQueryWrapper<Role> roleQueryWrapper = Wrappers.<Role>lambdaQuery().in(Role::getRoleCode, roleCodes)
                .eq(Role::getDelFlag, String.valueOf(DeleteFlagStatusEnum.VIEW.getStatus()));
        List<Role> roles = roleMapper.selectList(roleQueryWrapper);
        if (null == roles) {
            return new ArrayList<>();
        }
        // ??????????????????
        List<String> sysRoleCodes = new ArrayList<String>();
        List<String> tenRoleCodes = new ArrayList<String>();
        Integer code = RoleLevelEnum.SYSTEM.getCode();
        for (Role role : roles) {
            if (code.equals(role.getLevel())) {
                sysRoleCodes.add(role.getRoleCode());
            } else {
                tenRoleCodes.add(role.getRoleCode());
            }
        }
        return userMapper.queryUserInfoByRoleCodesAndTenantId(sysRoleCodes, tenRoleCodes, tenantId);
    }

    @Override
    public List<UserVO> queryUserInfoByProjectIdAndRoleCode(Integer projectId, String roleCode) {
        return userMapper.queryUserInfoByProjectIdAndRoleCode(roleCode, projectId);
    }

    /**
     * ????????????code????????????????????????????????????
     *
     * @param roleCode ????????????Code
     * @return List<UserVO> ????????????????????????
     */
    @Override
    public List<UserVO> queryUserInfoByRoleCode(String roleCode) {
        List<UserVO> userVos = userMapper.queryUserInfoByRoleCode(roleCode);
        return userVos;
    }

    /**
     * ????????????code????????????????????????????????????????????????
     *
     * @param page
     * @param roleCode ????????????Code
     * @return
     */
    @Override
    public IPage<UserVO> queryUserInfoByRoleCodePage(Page page, String roleCode, String name) {
        boolean flag = false;
        if (StringUtils.isBlank(roleCode) && StringUtils.isBlank(name)) {
            flag = true;
        }
        if (StringUtils.isBlank(roleCode) && StringUtils.isNotBlank(name)) {
            flag = true;
        }
        if (flag) {
            // ?????????????????????
            IPage<UserVO> userVoPage = userMapper.queryUserInfoByName(page, name, null);
            if (null == userVoPage || CollectionUtils.isEmpty(userVoPage.getRecords())) {
                return userVoPage;
            }
            // ????????????????????????
            List<Integer> userIdLst = new ArrayList<>();
            List<UserVO> records = userVoPage.getRecords();
            records.forEach(userVO -> userIdLst.add(userVO.getId()));
            List<UserVO> userVos = userMapper.queryUserAndRoleInfoByUserIdList(userIdLst);
            userVoPage.setRecords(userVos);
            return userVoPage;
        }
        // ???name+code+tenantid????????????
        IPage<UserVO> userVoiPage = userMapper.queryUserInfoByRoleCodePage(page, roleCode, name, null);
        List<UserVO> records = userVoiPage.getRecords();
        for (UserVO userVO : records) {
            userVO.setRoleList(queryRoleByUserId(userVO.getId()));
        }
        return userVoiPage;
    }

    /**
     * ??????????????????????????????????????????
     *
     * @param userIds ??????id??????
     * @return List<UserVO> ????????????????????????
     */
    @Override
    public List<UserVO> queryUserMsg(List<Integer> userIds) {
        return userMapper.queryUserMsg(userIds);
    }

    @Override
    public List<UserVO> queryUserByUsernames(List<String> usernames) {
        return userMapper.queryUserByUsernames(usernames);
    }

    @Override
    public Boolean updateUserLoginTime(Integer id) {
        return userMapper.updateUserLoginTime(id);
    }

    private Long dealTime(String startTime) throws ParseException {
        // ??????Date????????????
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:ss:mm");
        String endTime = df.format(date);
        Date d1 = df.parse(startTime);
        Date d2 = df.parse(endTime);
        long longDealTime = d2.getTime() - d1.getTime();
        long resultTime = (longDealTime / (60 * 60 * 1000)) % 24;
        long resultTime2 = longDealTime / (24 * 60 * 60 * 1000);
        long finalResul = resultTime2 * 24 + resultTime;
        return finalResul;
    }

    public ArrayList<Object> sevenRecentTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd");
        // ????????????Calendar?????????
        Calendar calendar = Calendar.getInstance();
        ArrayList<Object> timeInfo = new ArrayList<>();
        String formatRecentInfo = null;
        Date time = null;
        for (int i = 1; i < 8; i++) {
            calendar.setTime(new Date());
            calendar.add(Calendar.DATE, -(i - 1));
            time = calendar.getTime();
            formatRecentInfo = simpleDateFormat.format(time);
            timeInfo.add(i - 1, formatRecentInfo);
        }
        return timeInfo;
    }

    private HashMap<String, Object> recentlyNumber(List<UserVO> userVos) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:ss:mm");
        Integer oneRecent = 0;
        Integer twoRecent = 0;
        Integer threeRecent = 0;
        Integer fourRecent = 0;
        Integer fiveRecent = 0;
        Integer sixRecent = 0;
        Integer sevenRecent = 0;
        for (UserVO vos : userVos) {
            LocalDateTime lastLoginTime = vos.getLastLoginTime();
            String format = df.format(lastLoginTime);
            try {
                Long aLong = dealTime(format);
                if (24 >= aLong) {
                    oneRecent = oneRecent + 1;
                } else if (24 < aLong && aLong < 48) {
                    twoRecent = twoRecent + 1;
                } else if (48 < aLong && aLong < 72) {
                    threeRecent = threeRecent + 1;
                } else if (72 < aLong && aLong < 96) {
                    fourRecent = fourRecent + 1;
                } else if (96 < aLong && aLong < 120) {
                    fiveRecent = fiveRecent + 1;
                } else if (120 < aLong && aLong < 144) {
                    sixRecent = sixRecent + 1;
                } else if (144 < aLong && aLong <= 168) {
                    sevenRecent = sevenRecent + 1;
                }
            } catch (ParseException e) {
                log.error(e.getMessage(), e);
            }
        }
        ArrayList<Object> numberInfo = new ArrayList<>();
        numberInfo.add(0, oneRecent);
        numberInfo.add(1, twoRecent);
        numberInfo.add(2, threeRecent);
        numberInfo.add(3, fourRecent);
        numberInfo.add(4, fiveRecent);
        numberInfo.add(5, sixRecent);
        numberInfo.add(6, sevenRecent);
        ArrayList<Object> timeInfo = sevenRecentTime();
        HashMap<String, Object> stringIntegerHashMap = new HashMap<>();
        stringIntegerHashMap.put("timeInfo", timeInfo);
        stringIntegerHashMap.put("numberInfo", numberInfo);
        return stringIntegerHashMap;
    }

    /**
     * ???????????????????????????????????????????????????project??????
     *
     * @param tenantId    ??????id
     * @param defaultRole ??????id
     * @param id          ??????id
     * @return
     */
    @Override
    public R<TenantOrProjectVO> getTenOrProList(Integer tenantId, List<Integer> defaultRole, Integer id) {
        String roleCode = "";
        List<Role> roleCodes = roleMapper.selectList(Wrappers.<Role>lambdaQuery().in(Role::getId, defaultRole));
        for (Role role : roleCodes) {
            if (role.getRoleCode().equals(SUPER_ADMIN)) {
                roleCode = SUPER_ADMIN;
            } else if (role.getRoleCode().equals(TENANT_ADMIN)) {
                roleCode = TENANT_ADMIN;
            }
        }
        TenantOrProjectVO tenantOrProjectVO = new TenantOrProjectVO();
        if (roleCode.equals(SUPER_ADMIN)) {
            // ??????????????????
            tenantOrProjectVO
                    .setTenants(tenantMapper.selectList(Wrappers.<Tenant>lambdaQuery().eq(Tenant::getStatus, "0")));
            tenantOrProjectVO.setType("0");
            return R.ok(tenantOrProjectVO);
        } else if (roleCode.equals(TENANT_ADMIN)) {
            tenantOrProjectVO.setProjects(projectMapper.selectList(
                    Wrappers.<Project>lambdaQuery().eq(Project::getTenantId, tenantId).eq(Project::getStatus, "0")));
            tenantOrProjectVO.setType("1");
            return R.ok(tenantOrProjectVO);
        }
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setUserId(id);
        tenantOrProjectVO.setProjects(projectMapper.listNoPageByCondition(projectDTO));
        tenantOrProjectVO.setType("1");
        return R.ok(tenantOrProjectVO);
    }

    @Override
    public void updateTenantId() {
        List<UserRoleProject> userRoleProjects = userRoleProjectMapper.selectList(new QueryWrapper<UserRoleProject>());
        for (UserRoleProject userRoleProject : userRoleProjects) {
            if (userRoleProject.getRoleId() == 2 || userRoleProject.getRoleId() == 3) {
                User user = userMapper.selectById(userRoleProject.getUserId());
                if (user != null) {
                    userRoleProject.setTenantId(user.getDefaultTenantId());
                    userRoleProjectMapper.updateById(userRoleProject);
                }
            }
        }
    }

    @Override
    public void updateOrdinary() {
        // ??????????????????
        LambdaQueryWrapper<User> taskQueryWrapper2 = Wrappers.<User>lambdaQuery().eq(true, User::getDelFlag, "0");
        List<User> users = userMapper.selectList(taskQueryWrapper2);
        for (User user : users) {
            UserRoleProject userRoleProject = new UserRoleProject();
            userRoleProject.setUserId(user.getId());
            userRoleProject.setRoleId(85);
            userRoleProject.setTenantId(user.getDefaultTenantId());
            userRoleProjectMapper.insert(userRoleProject);
        }
    }

    @Override
    public void updateProjectTenant() {
        // ??????project
        LambdaQueryWrapper<Project> projectLambdaQueryWrapper = Wrappers.<Project>lambdaQuery().eq(true,
                Project::getDelFlag, "0");
        List<Project> projects = projectMapper.selectList(projectLambdaQueryWrapper);
        // ?????????map???key-id???value-Project
        Map<Integer, Project> projectMap = new HashMap<>();
        for (Project project : projects) {
            projectMap.put(project.getId(), project);
        }
        // ??????userRoleProject
        List<UserRoleProject> userRoleProjects = userRoleProjectMapper.selectList(new QueryWrapper<UserRoleProject>());
        for (UserRoleProject userRoleProject : userRoleProjects) {
            if (null == userRoleProject.getProjectId() || userRoleProject.getProjectId().equals(0)) {
                continue;
            }
            Project project = projectMap.get(userRoleProject.getProjectId());
            if (null == project) {
                continue;
            }
            userRoleProject.setTenantId(project.getTenantId());
            userRoleProjectMapper.updateById(userRoleProject);
        }
    }

    @Override
    public void clearRedisCache() {
        Set<String> keys = redisTemplate.keys(SecurityConstants.FITMGR_OAUTH_PREFIX + "*");
        if (CollectionUtils.isNotEmpty(keys)) {
            redisTemplate.delete(keys);
        }

        keys = redisTemplate.keys("project*");
        if (CollectionUtils.isNotEmpty(keys)) {
            redisTemplate.delete(keys);
        }

        keys = redisTemplate.keys("user_*");
        if (CollectionUtils.isNotEmpty(keys)) {
            redisTemplate.delete(keys);
        }
    }

    @Override
    public List<User> userLists(UserDTO userDTO) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.<User>lambdaQuery().in(null != userDTO.getUserIds(),
                User::getId, userDTO.getUserIds());
        return userMapper.selectList(queryWrapper);
    }

    @Override
    public void updateDefaultTenant(UserDTO userDTO) {
        userMapper.updateById(userDTO);
    }

    @Override
    public void refreshUrp() {
        List<UserRoleProject> urps = userRoleProjectMapper.selectList(new QueryWrapper<UserRoleProject>());
        for (UserRoleProject userRoleProject : urps) {
            boolean isUpdate = false;
            if (userRoleProject.getTenantId() == null || userRoleProject.getTenantId().equals(0)) {
                userRoleProject.setTenantId(-1);
                isUpdate = true;
            }
            if (userRoleProject.getProjectId() == null || userRoleProject.getProjectId().equals(0)) {
                userRoleProject.setProjectId(-1);
                isUpdate = true;
            }
            if (userRoleProject.getProjectId() > 0) {
                Project project = projectMapper.selectById(userRoleProject.getProjectId());
                if (project != null) {
                    userRoleProject.setTenantId(project.getTenantId());
                    isUpdate = true;
                }
            }
            if (isUpdate) {
                userRoleProjectMapper.updateById(userRoleProject);
            }
        }
        System.out.println("okokokokok");
    }

    @Override
    public boolean checkLoginNeedValidateCode(String userName) {
        List<LoginFailRecord> loginFailRecords = loginFailRecordMapper.selectList(new QueryWrapper<LoginFailRecord>()
                .lambda().eq(LoginFailRecord::getUsername, userName).orderByAsc(LoginFailRecord::getFailTime));
        if (CollectionUtils.isEmpty(loginFailRecords)) {
            return false;
        }
        long timeDiff = System.currentTimeMillis()
                - loginFailRecords.get(0).getFailTime().toInstant(ZoneOffset.of("+8")).toEpochMilli();
        if (timeDiff <= TimeUnit.DAYS.toMillis(1)) {
            return true;
        }
        return false;
    }

    @Override
    public R verifyIdentity(VerifyInfoForFindPwd verifyInfoForFindPwd) {
        User user = userMapper
                .selectOne(new QueryWrapper<User>().lambda().eq(User::getUsername, verifyInfoForFindPwd.getUserName()));
        if (user == null) {
            return R.failed("???????????????");
        }

        if (!StringUtils.equals(user.getEmail(), verifyInfoForFindPwd.getEmail())) {
            return R.failed("???????????????");
        }

        String key = CommonConstants.DEFAULT_CODE_KEY + verifyInfoForFindPwd.getRandomStr();

        if (!redisTemplate.hasKey(key)) {
            return R.failed("????????????????????????????????????????????????");
        }

        Object codeObj = redisTemplate.opsForValue().get(key);

        if (codeObj == null) {
            return R.failed("????????????????????????????????????????????????");
        }

        String saveCode = codeObj.toString();
        if (StrUtil.isBlank(saveCode)) {
            redisTemplate.delete(key);
            return R.failed("????????????????????????????????????????????????");
        }

        if (!StrUtil.equals(saveCode, verifyInfoForFindPwd.getImageCode())) {
            redisTemplate.delete(key);
            return R.failed("?????????????????????");
        }

        redisTemplate.delete(key);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime invalidationTime = now.plusMinutes(30);
        RetrievePwdInfo retrievePwdInfo = new RetrievePwdInfo();
        retrievePwdInfo.setUserId(user.getId());
        retrievePwdInfo.setInvalidationTime(invalidationTime);
        String secretKey = UUID.randomUUID().toString();
        String plainSignature = user.getUsername() + "$" + convertDateStrToTimeStamp(invalidationTime) + "$"
                + secretKey;
        String signature = ENCODER.encode(plainSignature);
        retrievePwdInfo.setHrefSignature(signature);
        retrievePwdMapper.insert(retrievePwdInfo);

        HttpServletRequest request = AuthUtils.getHttpServletRequest();
        String host = request.getHeader("X-Forwarded-Host");
        host = host.split(",")[0];
        String scheme = request.getHeader("X-Forwarded-Scheme");
        String port = request.getHeader("X-Forwarded-Port");
        port = port.split(",")[0];
        if (HTTP_PORT.equals(port) || HTTPS_PORT.equals(port)) {
            port = "";
        } else {
            port = ":" + port;
        }

        String resetHref = scheme + "://" + host + port + "/frame/#/resetpassword?signature=" + signature + "&user-id="
                + user.getId();

        Map<String, String> parameters = new HashMap<>();
        parameters.put("resetHref", resetHref);

        adminUtils.sendEmail(user.getId(), "????????????", "retrieve-password", parameters,
                Lists.newArrayList(user.getEmail()));

        return R.ok();
    }

    @Override
    public R modifyPwdForRetrievePwd(ModifyInfoForRetrievePwd modifyInfoForRetrievePwd) {
        RetrievePwdInfo retrievePwdInfo = retrievePwdMapper.selectOne(new QueryWrapper<RetrievePwdInfo>().lambda()
                .eq(RetrievePwdInfo::getHrefSignature, modifyInfoForRetrievePwd.getSignature()));
        if (retrievePwdInfo == null) {
            return R.failed("??????????????????????????????");
        }

        if (!retrievePwdInfo.getUserId().equals(modifyInfoForRetrievePwd.getUserId())) {
            return R.failed("??????????????????????????????");
        }

        if ((System.currentTimeMillis() - convertDateStrToTimeStamp(retrievePwdInfo.getInvalidationTime())) >= 0) {
            return R.failed("???????????????");
        }

        if (StringUtils.isBlank(modifyInfoForRetrievePwd.getNewPassword())) {
            return R.failed("??????????????????");
        }

        User originalUser = userMapper.selectById(modifyInfoForRetrievePwd.getUserId());
        checkSystemInternalUser(originalUser);

        originalUser.setPassword(ENCODER.encode(modifyInfoForRetrievePwd.getNewPassword()));

        // ??????????????????
        passwordRuleServiceImpl.checkPassword(modifyInfoForRetrievePwd.getNewPassword());
        // ????????????????????????
        originalUser.setPassUpdateTime(LocalDateTime.now());
        // ????????????????????????
        originalUser.setPassExpirationTime(passwordTermServiceImpl.calculateExpirationTime());

        MaxHisPassCount maxHisPassCount = maxHisPassCountMapper.selectList(new QueryWrapper<MaxHisPassCount>().lambda())
                .get(0);
        Integer count = maxHisPassCount.getCount();
        List<UserPasswordLog> userPasswordLogs = iUserPasswordLogService.userPasswordLogList(originalUser.getId());
        if (null != userPasswordLogs && userPasswordLogs.size() != 0) {
            int a = userPasswordLogs.size() > count ? count : userPasswordLogs.size();
            for (int i = 0; i < a; i++) {
                UserPasswordLog upl = userPasswordLogs.get(i);
                if (ENCODER.matches(modifyInfoForRetrievePwd.getNewPassword(), upl.getPasswordLog())) {
                    throw new UserCenterException("???????????????????????????" + count + "???????????????");
                }
            }
        }
        originalUser.setUpdateTime(LocalDateTime.now());
        this.updateById(originalUser);
        // ??????????????????????????????
        passExpirationMapper.delete(
                new QueryWrapper<PassExpiration>().lambda().eq(PassExpiration::getUserId, originalUser.getId()));

        Integer logNumber = iUserPasswordLogService.passwordLogCount(originalUser.getId());
        if (logNumber > SecurityConstants.HIS_PASS) {
            Boolean aBoolean = iUserPasswordLogService.userPasswordLogLimit(originalUser.getId());
            if (aBoolean) {
                logNumber = SecurityConstants.HIS_PASS;
            }
        }
        if (logNumber.equals(SecurityConstants.HIS_PASS)) {
            UserPasswordLog userPasswordLog = iUserPasswordLogService.earlyLog(originalUser.getId());
            Boolean aBoolean = iUserPasswordLogService.deleteUserPasswordLog(userPasswordLog.getId());
            if (aBoolean) {
                iUserPasswordLogService.saveUserPasswordLog(originalUser.getPassword(), originalUser.getId());
            }
        } else if (0 <= logNumber && logNumber < SecurityConstants.HIS_PASS) {
            iUserPasswordLogService.saveUserPasswordLog(originalUser.getPassword(), originalUser.getId());
        }

        retrievePwdMapper.delete(new QueryWrapper<RetrievePwdInfo>().lambda()
                .eq(RetrievePwdInfo::getUserId, modifyInfoForRetrievePwd.getUserId())
                .eq(RetrievePwdInfo::getHrefSignature, modifyInfoForRetrievePwd.getSignature()));

        adminUtils.kickoutByUserId(modifyInfoForRetrievePwd.getUserId(), "??????????????????????????????, ???????????????");

        redisTemplate.delete(CacheConstants.USER_DETAILS + originalUser.getUsername());
        return R.ok();
    }

    @Override
    public R checkHrefForRetrievePwd(CheckHrefInfoForRetrievePwd checkHrefInfoForRetrievePwd) {
        RetrievePwdInfo retrievePwdInfo = retrievePwdMapper.selectOne(new QueryWrapper<RetrievePwdInfo>().lambda()
                .eq(RetrievePwdInfo::getHrefSignature, checkHrefInfoForRetrievePwd.getSignature())
                .eq(RetrievePwdInfo::getUserId, checkHrefInfoForRetrievePwd.getUserId()));
        if (retrievePwdInfo == null) {
            return R.failed("???????????????");
        }

        if ((System.currentTimeMillis() - convertDateStrToTimeStamp(retrievePwdInfo.getInvalidationTime())) >= 0) {
            return R.failed("???????????????");
        }

        return R.ok();
    }

    private long convertDateStrToTimeStamp(LocalDateTime date) {
        return date.toInstant(ZoneOffset.of("+8")).toEpochMilli();
    }

    @Override
    public void mergeUser() {
        List<User> users = userMapper
                .selectList(new QueryWrapper<User>().lambda().eq(User::getNewImport, true).eq(User::getDelFlag, "0"));
        Map<String, List<User>> userMap = new HashMap<>();
        for (User user : users) {
            List<User> usersTemp = userMap.get(user.getEmail());
            if (usersTemp == null) {
                usersTemp = new ArrayList<>();
            }
            usersTemp.add(user);
            userMap.put(user.getEmail(), usersTemp);
        }
        for (Map.Entry<String, List<User>> entry : userMap.entrySet()) {
            List<User> usersTemp = entry.getValue();
            if (usersTemp.size() <= 1) {
                continue;
            }
            User user0 = usersTemp.get(0);
            for (User user : usersTemp) {
                if (user.getId().equals(user0.getId())) {
                    continue;
                }
                List<UserRoleProject> userRoleProjects = userRoleProjectMapper.selectList(
                        new QueryWrapper<UserRoleProject>().lambda().eq(UserRoleProject::getUserId, user.getId()));
                if (userRoleProjects != null) {
                    for (UserRoleProject userRoleProject : userRoleProjects) {
                        userRoleProject.setUserId(user0.getId());
                        userRoleProjectMapper.updateById(userRoleProject);
                    }
                }
                userMapper.deleteById(user.getId());
            }
            int lastIndex = user0.getUsername().lastIndexOf("_");
            if (lastIndex <= 0) {
                System.out.println("userId=" + user0.getId());
                continue;
            }
            String newUserName = user0.getUsername().substring(0, lastIndex);
            user0.setUsername(newUserName);
            userMapper.updateById(user0);
        }
    }

    /**
     * ????????????code??????????????????????????????project?????????????????????????????????
     *
     * @param roleCode ????????????Code
     * @param tpId     ?????????project???Id
     * @return
     */
    @Override
    public R<List<User>> getUserByTenOrPro(String roleCode, Integer tpId) {
        if (roleCode.equals(TENANT_QUOTA_ADMIN)) {
            List<User> users = userMapper.getUserByTenant(roleCode, tpId);
            return R.ok(users);
        } else if (roleCode.equals(PROJECT_QUOTA_ADMIN)) {
            List<User> users = userMapper.getUserByProject(roleCode, tpId);
            return R.ok(users);
        }
        return R.failed(BusinessEnum.USER_ROLE_CODE_FAULT);
    }

    @Override
    public R importUser(ImportUserVo importUserVo) {
        try {
            long startTime = DateUtils.getCurrentTime();
            importUserVo.setStartTime(new Date(startTime));

            String bucket = importUserVo.getBucket();
            String fileName = importUserVo.getFileName();

            List<Map<String, String>> data = getExcel(bucket, fileName);
            if (CollectionUtils.isEmpty(data)) {
                log.error("?????????????????????importUserVo={}", importUserVo);
                return R.failed("??????????????????");
            }
            if (data.size() > IMPORT_MAX_SIZE) {
                log.error("??????????????????????????????10000", importUserVo);
                return R.failed("??????????????????????????????10000");
            }

            executor.execute(() -> {
                int i = 0;
                int total = data.size();
                int success = 0;
                int fail = 0;

                importUserVo.setTotal(total);

                String fileKey = bucket + "_" + fileName;

                String progressKey = CommonConstants.import_user_progress + fileKey;
                String failKey = CommonConstants.import_user_fail + fileKey;

                // ???????????????????????????????????????????????????????????????????????????
                int per = Math.max(total / 100, 1);
                // ?????????????????????????????????
                Map<String, String> failUser = new HashMap<String, String>();

                for (Map<String, String> map : data) {
                    i++;
                    String username = MapUtils.getString(map, "username");
                    try {
                        if (i % 100 == 0) {
                            log.info("---------- ActiveCount:{}, PoolSize:{}, CorePoolSize:{}, MaxPoolSize:{}",
                                    executor.getActiveCount(), executor.getPoolSize(), executor.getCorePoolSize(),
                                    executor.getMaxPoolSize());
                        }

                        // ??????
                        check(map);

                        String tenant = MapUtils.getString(map, "tenant");
                        String role = MapUtils.getString(map, "role");
                        if (StringUtils.isBlank(role)) {
                            log.error("---- ??????????????????");
                            putFailUser(failUser, username, "??????????????????");
                            fail++;
                            continue;
                        }

                        Tenant tenantVo = null;
                        if (StringUtils.isNotBlank(tenant)) {
                            tenantVo = tenantMapper.selectOne(new QueryWrapper<Tenant>().eq("name", tenant));
                            if (tenantVo == null) {
                                log.error("---- ?????????????????????");
                                putFailUser(failUser, username, "?????????VDC??????");
                                fail++;
                                continue;
                            }
                        }

                        List<Integer> roleIds = Arrays.asList(role.split(",")).stream().map(t -> {
                            Role roleVo = roleService.getOne(new QueryWrapper<Role>().eq("role_name", t));
                            if (roleVo != null && StringUtils.isBlank(tenant)
                                    && RoleLevelEnum.TENANT.getCode() == roleVo.getLevel()) {
                                throw new UserCenterException("?????????VDC????????????????????????VDC??????");
                            }

                            return roleVo != null ? roleVo.getId() : null;
                        }).filter(r -> r != null).collect(Collectors.toList());

                        if (CollectionUtils.isEmpty(roleIds)) {
                            log.error("---- ?????????????????????");
                            putFailUser(failUser, username, "?????????????????????");
                            fail++;
                            continue;
                        }

                        UserDTO userDto = new UserDTO();
                        userDto.setName(MapUtils.getString(map, "name"));
                        userDto.setUsername(username);
                        userDto.setPassword(MapUtils.getString(map, "password"));
                        userDto.setPhone(MapUtils.getString(map, "mobile"));
                        userDto.setEmail(MapUtils.getString(map, "email"));
                        userDto.setUserType(UserTypeEnum.IMPORT.toString());
                        userDto.setTenantId(tenantVo != null ? tenantVo.getId() : null);
                        userDto.setRole(roleIds);

                        Boolean result = saveUser(userDto);
                        if (result) {
                            success++;
                        } else {
                            log.error("---- {}??????????????????", username);
                            putFailUser(failUser, username, "????????????");
                            fail++;
                        }
                    } catch (Exception e) {
                        fail++;
                        log.error("---- {}????????????, error={}", username, e);
                        putFailUser(failUser, username, e.getMessage());
                    } finally {
                        if (i % per == 0 || i >= total) {
                            Map<String, String> progressMap = new HashMap<String, String>();
                            progressMap.put("total", String.valueOf(total));
                            progressMap.put("n", String.valueOf(i));
                            progressMap.put("success", String.valueOf(success));
                            progressMap.put("fail", String.valueOf(fail));

                            recordProgress(progressKey, progressMap);
                        }
                    }
                }
                // ???????????????????????????????????????
                if (!failUser.isEmpty()) {
                    recordFailUser(failKey, failUser);
                }
                long endTime = DateUtils.getCurrentTime();
                log.info(">>>>>>>>>>> ?????????????????????bucket:{}, fileName:{}, total={}, success:{}, fail:{}, ??????:{}", bucket,
                        fileName, total, success, fail, DateUtils.formatDuring(startTime - endTime));

                importUserVo.setEndTime(new Date(endTime));
                importUserVo.setProgress((int) (i * 100 / total));
                importUserVo.setStatus(1);
                importUserVo.setSuccess(success);
                importUserVo.setFail(fail);
                importUserVo.setFailLink("/admin/user/downloadFail?bucket=" + bucket + "&fileName=" + fileName);
                recordImportLog(importUserVo);
            });

            return R.ok();
        } catch (Exception e) {
            log.error("??????????????????", e);
            importUserVo.setStatus(2);
            recordImportLog(importUserVo);
            return R.failed("????????????????????????????????????");
        }
    }

    private void check(Map<String, String> dataMap) {
        String username = MapUtils.getString(dataMap, "username");
        String name = MapUtils.getString(dataMap, "name");
        String mobile = MapUtils.getString(dataMap, "mobile");
        String email = MapUtils.getString(dataMap, "email");
        if (!matches(USERNAME, username)) {
            throw new UserCenterException("????????????????????????????????????-_!@#$%^&*???1-16?????????");
        }
        if (!matches(NAME, name)) {
            throw new UserCenterException("????????????????????????:????????????????????????????????????????????????????????????????????????3-64?????????????????????????????????????????????????????????");
        }
        if (!matches(PHONE, mobile)) {
            throw new UserCenterException("????????????????????????????????????1?????????11???????????????");
        }
        if (!matches(EMAIL, email)) {
            throw new UserCenterException("??????????????????");
        }
    }

    private boolean matches(String pattern, String content) {
        try {
            return Pattern.matches(pattern, content);
        } catch (Exception e) {
            return false;
        }
    }

    private void recordImportLog(ImportUserVo importUserVo) {
        redisTemplate.opsForList().leftPush(CommonConstants.import_user_logs, JSONObject.toJSONString(importUserVo));
        redisTemplate.expire(CommonConstants.import_user_logs, 1, TimeUnit.DAYS);

        List<String> list = redisTemplate.opsForList().range(CommonConstants.import_user_logs, 0, -1);
        // ???????????????10?????????
        if (CollectionUtils.isNotEmpty(list) && list.size() > RECORD_IMPORT_LOG_SIZE) {
            redisTemplate.opsForList().trim(CommonConstants.import_user_logs, 0, 9);
        }
    }

    private List<String> queryImportLog() {
        return redisTemplate.opsForList().range(CommonConstants.import_user_logs, 0, -1);
    }

    private void putFailUser(Map<String, String> map, String username, String errorInfo) {
        if (map.containsKey(username)) {
            String value = map.getOrDefault(username, "");
            // ??????1000?????????????????????excel???????????????
            if (value.length() < EXCEL_CELL_LENGTH) {
                map.put(username, value + " || " + errorInfo);
            }
        } else {
            map.put(username, errorInfo);
        }
    }

    private List<Map<String, String>> getExcel(String bucket, String fileName) throws Exception {
        log.info("????????????Excel???bucket={}, fileName={}", bucket, fileName);
        // 1.???????????????Excel??????
        Workbook workbook = WorkbookFactory.create(minioTemplate.getObject(bucket, fileName));
        // 3.??????????????????????????? getSheetAt??????????????????
        Sheet sheet = workbook.getSheetAt(0);
        // 4.??????????????????????????? ?????????????????? ??? ???????????? getLastRowNum() ???????????????????????????
        int lastRowNum = sheet.getLastRowNum();

        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        for (int i = 1; i <= lastRowNum; i++) {
            Map<String, String> map = new HashMap<>();

            // ?????????????????????
            Row row = sheet.getRow(i);
            // ???????????????????????????????????????
            if (row.getCell(0) == null || StringUtils.isBlank(getCellFormatValue(row.getCell(0)))) {
                log.warn("???{}??????????????????????????????????????????", i);
                return list;
            }

            map.put("username", getCellFormatValue(row.getCell(0)));
            map.put("name", getCellFormatValue(row.getCell(1)));
            map.put("password", getCellFormatValue(row.getCell(2)));
            map.put("mobile", getCellFormatValue(row.getCell(3)));
            map.put("email", getCellFormatValue(row.getCell(4)));
            map.put("tenant", getCellFormatValue(row.getCell(5)));
            map.put("role", getCellFormatValue(row.getCell(6)));
            list.add(map);

            // ????????????10000??????????????????
            if (list.size() > 10000) {
                return list;
            }
        }
        log.info("Excel???????????????listSize={}", list.size());
        return list;
    }

    private void recordProgress(String progressKey, Map<String, String> progressMap) {
        try {
            redisTemplate.opsForHash().putAll(progressKey, progressMap);
            redisTemplate.expire(progressKey, 1L, TimeUnit.DAYS);
        } catch (Exception e) {
            log.info("redis error", e);
        }
    }

    private Map<Object, Object> queryProgress(String progressKey) {
        try {
            Map<Object, Object> progressMap = redisTemplate.opsForHash().entries(progressKey);
            return progressMap;
        } catch (Exception e) {
            log.info("redis error", e);
            return new HashMap<>();
        }
    }

    private void recordFailUser(String failKey, Map<String, String> failMap) {
        try {
            redisTemplate.opsForHash().putAll(failKey, failMap);
            redisTemplate.expire(failKey, 1L, TimeUnit.DAYS);
        } catch (Exception e) {
            log.info("redis error", e);
        }
    }

    private Map<Object, Object> queryFailUser(String key) {
        try {
            Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
            return map;
        } catch (Exception e) {
            log.info("redis error", e);
            return new HashMap<>();
        }
    }

    @SuppressWarnings("deprecation")
    private String getCellFormatValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue();
    }

    @Override
    public void downloadFail(HttpServletResponse response, String bucket, String fileName) throws Exception {
        Map<Object, Object> failUserMap = queryFailUser(CommonConstants.import_user_fail + bucket + "_" + fileName);
        List<String> titles = new ArrayList<String>();
        titles.add("??????");
        titles.add("????????????");

        ExcelData data = new ExcelData();
        List<List<Object>> rows = new ArrayList<List<Object>>();

        for (Entry<Object, Object> entry : failUserMap.entrySet()) {
            List<Object> list = new ArrayList<Object>();
            list.add(entry.getKey());
            list.add(entry.getValue());
            rows.add(list);
        }

        data.setTitles(titles);
        data.setName("??????????????????");
        data.setRows(rows);

        ExcelUtil.exportExcel(response, "??????????????????.xls", data);
    }

    @Override
    public ImportUserVo queryProgress(String bucket, String fileName) {
        ImportUserVo importuser = new ImportUserVo();
        importuser.setBucket(bucket);
        importuser.setFileName(fileName);

        String progressKey = CommonConstants.import_user_progress + bucket + "_" + fileName;
        Map<Object, Object> progressMap = queryProgress(progressKey);
        if (progressMap == null) {
            importuser.setProgress(0);
            return importuser;
        }

        Integer total = MapUtils.getInteger(progressMap, "total", 1);
        Integer n = MapUtils.getInteger(progressMap, "n", 0);
        Integer success = MapUtils.getInteger(progressMap, "success", 0);
        Integer fail = MapUtils.getInteger(progressMap, "fail", 0);
        importuser.setTotal(total);
        importuser.setProgress(Math.min(n * 100 / total, 100));
        importuser.setSuccess(success);
        importuser.setFail(fail);

        if (fail > 0) {
            importuser.setFailLink("/admin/user/downloadFail?bucket=" + bucket + "&fileName=" + fileName);
        }

        return importuser;
    }

    @Override
    public List<ImportUserVo> queryLogs() {
        List<String> logs = queryImportLog();
        if (CollectionUtils.isEmpty(logs)) {
            return new ArrayList<>();
        }
        List<ImportUserVo> improtLogs = logs.stream().limit(Math.min(logs.size(), 10)).map(log -> {
            return JSONObject.toJavaObject((JSONObject) JSONObject.parse(log), ImportUserVo.class);
        }).collect(Collectors.toList());

        return improtLogs;
    }
}