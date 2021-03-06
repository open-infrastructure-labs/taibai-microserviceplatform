package com.taibai.admin.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.AntPathMatcher;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.codingapi.tx.annotation.TxTransaction;
import com.taibai.admin.api.dto.TenantTree;
import com.taibai.admin.api.entity.Auth;
import com.taibai.admin.api.entity.AuthCheck;
import com.taibai.admin.api.entity.Function;
import com.taibai.admin.api.entity.ProjectOperatingRange;
import com.taibai.admin.api.entity.Role;
import com.taibai.admin.api.entity.UserRoleProject;
import com.taibai.admin.api.vo.AuthVO;
import com.taibai.admin.api.vo.TenantVO;
import com.taibai.admin.api.vo.UserVO;
import com.taibai.admin.cache.FunctionCache;
import com.taibai.admin.mapper.AuthMapper;
import com.taibai.admin.mapper.FunctionMapper;
import com.taibai.admin.mapper.ProjectMapper;
import com.taibai.admin.mapper.RoleMapper;
import com.taibai.admin.mapper.RoleMenuMapper;
import com.taibai.admin.mapper.TenantMapper;
import com.taibai.admin.mapper.UserMapper;
import com.taibai.admin.mapper.UserRoleProjectMapper;
import com.taibai.admin.service.IAuthService;
import com.taibai.admin.service.ITenantService;
import com.taibai.admin.threadpool.InheritableRequestContextTaskWrapper;
import com.taibai.admin.utils.AdminUtils;
import com.taibai.common.core.constant.enums.OperatingRangeEnum;
import com.taibai.common.core.util.R;
import com.taibai.common.core.util.SpringContextHolder;
import com.taibai.common.security.service.FitmgrUser;
import com.taibai.common.security.util.SecurityUtils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * ??????????????? ???????????????
 * </p>
 *
 * @author Taibai
 * @since 2019-11-14
 */
@Slf4j
@Service
@AllArgsConstructor
public class AuthServiceImpl extends ServiceImpl<AuthMapper, Auth> implements IAuthService {

    private final static String AUTH_STATUS_ON = "0";
    private final static int CHILDEN_TENANT_POSITION = 1;
    private final static int PARENT_TENANT_POSITION = 2;
    private final static char TENANT_RANGE_CHOICE = '1';

    private final AuthMapper authMapper;

    private final UserMapper userMapper;

    private final AdminUtils adminUtils;

    private final RoleMapper roleMapper;

    private final FunctionMapper functionMapper;

    private final FunctionCache functionCache;

    private final RoleMenuMapper roleMenuMapper;

    @Autowired
    private ThreadPoolTaskExecutor threadPoola;

    private final TenantMapper tenantMapper;

    private final ProjectMapper projectMapper;

    private final UserRoleProjectMapper userRoleProjectMapper;

    /**
     * ????????????id?????????id??????????????????
     *
     * @param functionId ??????id
     * @param roleId     ??????id
     * @return
     */
    @Override
    public AuthVO getRoleMenuByAuth(Integer functionId, Integer roleId) {
        return authMapper.getRoleIdByAuths(functionId, roleId);
    }

    @Override
    public R checkByUrlAndMethod(String apiUrl, String httpMethod, Integer userId, Integer defaultTenantId) {
        Map<String, Map<Integer, Function>> functionMap = functionCache.getFunctionCache();
        String[] apiUrlArr = apiUrl.split("/");
        String prefix = apiUrlArr[1];
        Map<Integer, Function> functions = functionMap.get(prefix);
        if (functions == null || functions.isEmpty()) {
            return R.ok(true);
        }
        AntPathMatcher matcher = new AntPathMatcher();
        for (Map.Entry<Integer, Function> functionEntry : functions.entrySet()) {
            if (matcher.match(functionEntry.getValue().getApiUrl(), apiUrl)
                    && StringUtils.equals(httpMethod, functionEntry.getValue().getHttpMethod())) {
                R<AuthCheck> r = this.newAuthCheck(functionEntry.getValue().getFunctionCode(), userId, defaultTenantId,
                        null, null, null);
                if (r.getData().isStatus()) {
                    return R.ok(true);
                } else {
                    return R.ok(false);
                }
            }
        }
        return R.ok(true);
    }

    /**
     * ????????????id?????????code?????????????????????????????????
     *
     * @param code        ??????code
     * @param defaultRole ??????id
     * @return
     */
    @Override
    public AuthVO getUserAuth(String code, Integer defaultRole) {
        return authMapper.getFunctionCodeByAuth(code, defaultRole);
    }

    /**
     * ??????/??????????????????
     *
     * @param auth ????????????dto
     * @return
     */
    @Override
    public R saveAuth(Auth auth) {
        if (auth.getRoleId() == 1) {
            if (!AUTH_STATUS_ON.equals(auth.getStatus())) {
                return R.failed("???????????????????????????????????????");
            }
            if (!OperatingRangeEnum.ALL_CODE.equals(auth.getOperatingRange())) {
                return R.failed("????????????????????????????????????????????????");
            }
        }
        if (AUTH_STATUS_ON.equals(auth.getStatus()) && OperatingRangeEnum.TENANT_CODE.equals(auth.getOperatingRange())
                && auth.getTenantRange() == null) {
            // ?????????????????????vdc???????????????vdc
            auth.setTenantRange("110");
        }
        Integer count = authMapper.selectCount(Wrappers.<Auth>lambdaQuery().eq(Auth::getRoleId, auth.getRoleId())
                .eq(Auth::getFunctionId, auth.getFunctionId()));
        if (count > 0) {
            // ???????????????
            int res = authMapper.update(null,
                    Wrappers.<Auth>lambdaUpdate().eq(Auth::getRoleId, auth.getRoleId())
                            .eq(Auth::getFunctionId, auth.getFunctionId()).set(Auth::getStatus, auth.getStatus())
                            .set(Auth::getOperatingRange, auth.getOperatingRange())
                            .set(Auth::getTenantRange, auth.getTenantRange()));
            if (res > 0) {
                InheritableRequestContextTaskWrapper wrapper = new InheritableRequestContextTaskWrapper();
                threadPoola.submit(() -> {
                    wrapper.lambda2(() -> {
                        try {
                            Role role = roleMapper.selectById(auth.getRoleId());
                            if (role != null) {
                                List<UserVO> userVos = userMapper.queryUserInfoByRoleCode(role.getRoleCode());
                                if (CollectionUtils.isNotEmpty(userVos)) {
                                    List<Integer> userIds = userVos.stream().map(UserVO::getId)
                                            .collect(Collectors.toList());
                                    List<String> emails = userVos.stream().map(UserVO::getEmail)
                                            .collect(Collectors.toList());
                                    adminUtils.batchSendEmail(userIds, "?????????????????????", "modify-role-authority",
                                            "roleName:" + role.getRoleName(), emails);
                                }
                            }
                        } catch (Throwable e) {
                            log.error("Error occurred in async tasks", e);
                        }
                    }).accept();
                });
            }
            return R.ok(res);
        }
        boolean res = this.save(auth);
        if (res) {
            InheritableRequestContextTaskWrapper wrapper = new InheritableRequestContextTaskWrapper();
            threadPoola.submit(() -> {
                wrapper.lambda2(() -> {
                    try {
                        Role role = roleMapper.selectById(auth.getRoleId());
                        if (role != null) {
                            List<UserVO> userVos = userMapper.queryUserInfoByRoleCode(role.getRoleCode());
                            if (CollectionUtils.isNotEmpty(userVos)) {
                                List<Integer> userIds = userVos.stream().map(UserVO::getId)
                                        .collect(Collectors.toList());
                                List<String> emails = userVos.stream().map(UserVO::getEmail)
                                        .collect(Collectors.toList());
                                adminUtils.batchSendEmail(userIds, "?????????????????????", "modify-role-authority",
                                        "roleName:" + role.getRoleName(), emails);
                            }
                        }
                    } catch (Throwable e) {
                        log.error("Error occurred in async tasks", e);
                    }
                }).accept();
            });
        }
        return R.ok(res);
    }

    /**
     * api???????????????????????????
     *
     * @param functionCode ??????????????????
     * @return
     */
    @Override
    public R authCheck(String functionCode, Integer userId) {
        return R.ok(null, OperatingRangeEnum.ALL_CODE);
    }

    @Override
    public R<AuthCheck> newAuthCheck(String functionCode, Integer userId, Integer defaultTenantId, Integer resTenantId,
            Integer resProjectId, Integer resUserId) {
        // TODO ??????????????????????????????????????????????????????
        ITenantService tenantService = SpringContextHolder.getBean(ITenantService.class);

        AuthCheck authCheck = new AuthCheck();
        List<Map<String, Integer>> roleList = userMapper.queryRoleByUserIdAndTenantId(userId, defaultTenantId);
        if (defaultTenantId == -1) {
            // ??????????????????
            List<Integer> list = new ArrayList<Integer>();
            for (Map<String, Integer> role : roleList) {
                list.add(role.get("role_id"));
            }
            List<AuthVO> userAuths = authMapper.getFunctionCodeByAuths(functionCode, list);
            if (userAuths != null && userAuths.size() > 0) {
                Integer operatingRange = 4;
                Integer tenantRange = 0;
                for (AuthVO authVO : userAuths) {
                    if (authVO.getOperatingRange().equals(OperatingRangeEnum.ALL_CODE)) {
                        operatingRange = 0;
                        break;
                    }
                    if (authVO.getOperatingRange().equals(OperatingRangeEnum.TENANT_CODE)) {
                        operatingRange = 1;
                        // ??????????????????????????????
                        tenantRange = tenantRange | Integer.parseInt(authVO.getTenantRange(), 2);
                    }
                    if (operatingRange > 2 && authVO.getOperatingRange().equals(OperatingRangeEnum.PROJECT_CODE)) {
                        operatingRange = 2;
                    }
                    if (operatingRange > 3 && authVO.getOperatingRange().equals(OperatingRangeEnum.SELF_CODE)) {
                        operatingRange = 3;
                    }
                }
                switch (operatingRange) {
                case 0:
                    authCheck.setStatus(true);
                    authCheck.setOperatingRange(OperatingRangeEnum.ALL_CODE);
                    break;
                case 1:
                    authCheck.setStatus(true);
                    authCheck.setOperatingRange(OperatingRangeEnum.TENANT_CODE);
                    List<Integer> tenantIds = new ArrayList<>();
                    String s = Integer.toBinaryString(tenantRange);
                    s = supplementaryLength(s, 3, "before", "0");
                    // ??????vdc
                    if (s.charAt(0) == TENANT_RANGE_CHOICE) {
                        tenantIds.add(defaultTenantId);
                    }
                    // ????????????vdc
                    if (s.charAt(CHILDEN_TENANT_POSITION) == TENANT_RANGE_CHOICE) {
                        List<TenantTree> childTenants = tenantService.queryAllChildTenant(defaultTenantId);
                        if (CollectionUtils.isNotEmpty(childTenants)) {
                            tenantIds.addAll(childTenants.stream().map(TenantTree::getId).collect(Collectors.toList()));
                        }
                    }
                    // ????????????vdc
                    if (s.charAt(PARENT_TENANT_POSITION) == TENANT_RANGE_CHOICE) {
                        queryAllParentTenant(defaultTenantId, tenantService, tenantIds);
                    }
                    if (tenantIds.isEmpty()) {
                        tenantIds.add(0);
                    }
                    authCheck.setTenantIds(tenantIds);
                    break;
                case 2:
                    authCheck.setStatus(false);
                    break;
                case 3:
                    authCheck.setStatus(true);
                    authCheck.setOperatingRange(OperatingRangeEnum.SELF_CODE);
                    List<Integer> tenantIds1 = new ArrayList<>();
                    tenantIds1.add(defaultTenantId);
                    authCheck.setTenantIds(tenantIds1);
                    authCheck.setUserId(userId);
                    break;
                default:
                    log.error("????????????????????????");
                    break;
                }
            }
            return R.ok(checkResource(authCheck, resTenantId, resProjectId, resUserId));
        } else {
            // ??????????????????
            Map<Integer, List<Integer>> map = new HashMap<Integer, List<Integer>>();
            for (Map<String, Integer> role : roleList) {
                if (role.get("project_id") == null) {
                    role.put("project_id", -1);
                }
                if (map.get(role.get("project_id")) == null) {
                    List<Integer> list = new ArrayList<Integer>();
                    list.add(role.get("role_id"));
                    map.put(role.get("project_id"), list);
                } else {
                    List<Integer> list = map.get(role.get("project_id"));
                    list.add(role.get("role_id"));
                    map.put(role.get("project_id"), list);
                }
            }
            List<AuthVO> userAuths = authMapper.getFunctionCodeByAuths(functionCode, map.get(-1));
            map.remove(-1);
            if (userAuths != null && userAuths.size() > 0) {
                Integer operatingRange = 4;
                Integer tenantRange = 0;
                for (AuthVO authVO : userAuths) {
                    if (authVO.getOperatingRange().equals(OperatingRangeEnum.ALL_CODE)) {
                        operatingRange = 0;
                        break;
                    }
                    if (authVO.getOperatingRange().equals(OperatingRangeEnum.TENANT_CODE)) {
                        operatingRange = 1;
                        // ??????????????????????????????
                        tenantRange = tenantRange | Integer.parseInt(authVO.getTenantRange(), 2);
                    }
                    if (operatingRange > 2 && authVO.getOperatingRange().equals(OperatingRangeEnum.PROJECT_CODE)) {
                        operatingRange = 2;
                    }
                    if (operatingRange > 3 && authVO.getOperatingRange().equals(OperatingRangeEnum.SELF_CODE)) {
                        operatingRange = 3;
                    }
                }
                switch (operatingRange) {
                // ?????????????????? ???????????????
                case 0:
                    authCheck.setStatus(true);
                    authCheck.setOperatingRange(OperatingRangeEnum.ALL_CODE);
                    break;
                case 1:
                    // ?????????????????? ????????????
                    Integer index = 1;
                    // project??????????????????
                    for (Entry<Integer, List<Integer>> entry : map.entrySet()) {
                        List<AuthVO> userAuths1 = authMapper.getFunctionCodeByAuths(functionCode, entry.getValue());
                        boolean flag = false;
                        if (userAuths1 != null && userAuths1.size() > 0) {
                            for (AuthVO authVO : userAuths1) {
                                switch (authVO.getOperatingRange()) {
                                case OperatingRangeEnum.ALL_CODE:
                                    index = 0;
                                    break;
                                case OperatingRangeEnum.TENANT_CODE:
                                    // ??????????????????????????????
                                    tenantRange = tenantRange | Integer.parseInt(authVO.getTenantRange(), 2);
                                    break;
                                default:
                                    log.error("VDC?????????VDC???????????????project?????????VDC???????????????????????????");
                                    break;
                                }
                                if (index == 0) {
                                    flag = true;
                                    break;
                                }
                            }
                        }
                        if (flag) {
                            break;
                        }
                    }
                    switch (index) {
                    case 0:
                        // project?????????????????? ??????????????? ?????????????????????
                        authCheck.setStatus(true);
                        authCheck.setOperatingRange(OperatingRangeEnum.ALL_CODE);
                        break;
                    case 1:
                        // project?????????????????? ???????????? ?????????????????? ??????id(??????????????????id)
                        authCheck.setStatus(true);
                        authCheck.setOperatingRange(OperatingRangeEnum.TENANT_CODE);
                        List<Integer> tenantIds = new ArrayList<>();
                        String s = Integer.toBinaryString(tenantRange);
                        s = supplementaryLength(s, 3, "before", "0");
                        // ??????vdc
                        if (s.charAt(0) == TENANT_RANGE_CHOICE) {
                            tenantIds.add(defaultTenantId);
                        }
                        // ????????????vdc
                        if (s.charAt(CHILDEN_TENANT_POSITION) == TENANT_RANGE_CHOICE) {
                            List<TenantTree> childTenants = tenantService.queryAllChildTenant(defaultTenantId);
                            if (CollectionUtils.isNotEmpty(childTenants)) {
                                tenantIds.addAll(
                                        childTenants.stream().map(TenantTree::getId).collect(Collectors.toList()));
                            }
                        }
                        // ????????????vdc
                        if (s.charAt(PARENT_TENANT_POSITION) == TENANT_RANGE_CHOICE) {
                            queryAllParentTenant(defaultTenantId, tenantService, tenantIds);
                        }
                        if (tenantIds.isEmpty()) {
                            tenantIds.add(0);
                        }
                        authCheck.setTenantIds(tenantIds);
                        break;
                    default:
                        log.error("VDC?????????VDC?????????????????????????????????VDC?????????????????????");
                        break;
                    }
                    break;
                case 2:
                    // ?????????????????? project??????
                    Integer index1 = 2;
                    // project??????????????????
                    for (Entry<Integer, List<Integer>> entry : map.entrySet()) {
                        List<AuthVO> userAuths1 = authMapper.getFunctionCodeByAuths(functionCode, entry.getValue());
                        boolean flag = false;
                        if (userAuths1 != null && userAuths1.size() > 0) {
                            for (AuthVO authVO : userAuths1) {
                                switch (authVO.getOperatingRange()) {
                                case OperatingRangeEnum.ALL_CODE:
                                    index1 = 0;
                                    break;
                                case OperatingRangeEnum.TENANT_CODE:
                                    index1 = 1;
                                    // ??????????????????????????????
                                    tenantRange = tenantRange | Integer.parseInt(authVO.getTenantRange(), 2);
                                    break;
                                default:
                                    log.error("VDC?????????project???????????????project?????????VDC???????????????????????????");
                                    break;
                                }
                                if (index1 == 0) {
                                    flag = true;
                                    break;
                                }
                            }
                        }
                        if (flag) {
                            break;
                        }
                    }
                    switch (index1) {
                    case 0:
                        // project?????????????????? ??????????????? ?????????????????????
                        authCheck.setStatus(true);
                        authCheck.setOperatingRange(OperatingRangeEnum.ALL_CODE);
                        break;
                    case 1:
                        // project?????????????????? ???????????? ?????????????????? ??????id(??????????????????id)
                        authCheck.setStatus(true);
                        authCheck.setOperatingRange(OperatingRangeEnum.TENANT_CODE);
                        List<Integer> tenantIds = new ArrayList<>();
                        String s = Integer.toBinaryString(tenantRange);
                        s = supplementaryLength(s, 3, "before", "0");
                        // ??????vdc
                        if (s.charAt(0) == TENANT_RANGE_CHOICE) {
                            tenantIds.add(defaultTenantId);
                        }
                        // ????????????vdc
                        if (s.charAt(CHILDEN_TENANT_POSITION) == TENANT_RANGE_CHOICE) {
                            List<TenantTree> childTenants = tenantService.queryAllChildTenant(defaultTenantId);
                            if (CollectionUtils.isNotEmpty(childTenants)) {
                                tenantIds.addAll(
                                        childTenants.stream().map(TenantTree::getId).collect(Collectors.toList()));
                            }
                        }
                        // ????????????vdc
                        if (s.charAt(PARENT_TENANT_POSITION) == TENANT_RANGE_CHOICE) {
                            queryAllParentTenant(defaultTenantId, tenantService, tenantIds);
                        }
                        if (tenantIds.isEmpty()) {
                            tenantIds.add(0);
                        }
                        authCheck.setTenantIds(tenantIds);
                        break;
                    case 2:
                        // project?????????????????? ???????????? ??????project?????? projectid??????projectids
                        List<Integer> projectIds = new ArrayList<Integer>();
                        LambdaQueryWrapper<UserRoleProject> queryWrapper = Wrappers.<UserRoleProject>lambdaQuery()
                                .eq(UserRoleProject::getUserId, userId)
                                .eq(UserRoleProject::getTenantId, defaultTenantId);
                        List<UserRoleProject> userRoleProjects = userRoleProjectMapper.selectList(queryWrapper);
                        if (CollectionUtils.isNotEmpty(userRoleProjects)) {
                            List<UserRoleProject> collect = userRoleProjects.stream()
                                    .filter(userRoleProject -> userRoleProject.getProjectId() != null)
                                    .collect(Collectors.toList());
                            projectIds.addAll(
                                    collect.stream().map(UserRoleProject::getProjectId).collect(Collectors.toList()));
                            authCheck.setProjectIds(projectIds);
                            authCheck.setStatus(true);
                            authCheck.setOperatingRange(OperatingRangeEnum.PROJECT_CODE);
                        }
                        break;
                    default:
                        log.error("VDC?????????project?????????????????????????????????project?????????????????????");
                        break;
                    }
                    break;
                case 3:
                    // ?????????????????? ????????????
                    Integer index2 = 3;
                    Set<Integer> projectIds1 = new HashSet<Integer>();
                    // project??????????????????
                    for (Entry<Integer, List<Integer>> entry : map.entrySet()) {
                        List<AuthVO> userAuths1 = authMapper.getFunctionCodeByAuths(functionCode, entry.getValue());
                        boolean flag = false;
                        if (userAuths1 != null && userAuths1.size() > 0) {
                            for (AuthVO authVO : userAuths1) {
                                switch (authVO.getOperatingRange()) {
                                case OperatingRangeEnum.ALL_CODE:
                                    index2 = 0;
                                    break;
                                case OperatingRangeEnum.TENANT_CODE:
                                    index2 = 1;
                                    // ??????????????????????????????
                                    tenantRange = tenantRange | Integer.parseInt(authVO.getTenantRange(), 2);
                                    break;
                                case OperatingRangeEnum.PROJECT_CODE:
                                    if (2 < index2) {
                                        index2 = 2;
                                    }
                                    projectIds1.add(entry.getKey());
                                    break;
                                default:
                                    log.error("VDC??????????????????????????????project?????????project???????????????????????????");
                                    break;
                                }
                                if (index2 == 0) {
                                    flag = true;
                                    break;
                                }
                            }
                        }
                        if (flag) {
                            break;
                        }
                    }
                    switch (index2) {
                    case 0:
                        // project?????????????????? ??????????????? ?????????????????????
                        authCheck.setStatus(true);
                        authCheck.setOperatingRange(OperatingRangeEnum.ALL_CODE);
                        break;
                    case 1:
                        // project?????????????????? ???????????? ?????????????????? ??????id(??????????????????id)
                        authCheck.setStatus(true);
                        authCheck.setOperatingRange(OperatingRangeEnum.TENANT_CODE);
                        List<Integer> tenantIds = new ArrayList<>();
                        String s = Integer.toBinaryString(tenantRange);
                        s = supplementaryLength(s, 3, "before", "0");
                        // ??????vdc
                        if (s.charAt(0) == TENANT_RANGE_CHOICE) {
                            tenantIds.add(defaultTenantId);
                        }
                        // ????????????vdc
                        if (s.charAt(CHILDEN_TENANT_POSITION) == TENANT_RANGE_CHOICE) {
                            List<TenantTree> childTenants = tenantService.queryAllChildTenant(defaultTenantId);
                            if (CollectionUtils.isNotEmpty(childTenants)) {
                                tenantIds.addAll(
                                        childTenants.stream().map(TenantTree::getId).collect(Collectors.toList()));
                            }
                        }
                        // ????????????vdc
                        if (s.charAt(PARENT_TENANT_POSITION) == TENANT_RANGE_CHOICE) {
                            queryAllParentTenant(defaultTenantId, tenantService, tenantIds);
                        }
                        if (tenantIds.isEmpty()) {
                            tenantIds.add(0);
                        }
                        authCheck.setTenantIds(tenantIds);
                        break;
                    case 2:
                        // project?????????????????? project?????? ??????project?????? projectid??????projectids ??????????????????uerid???????????????id
                        authCheck.setStatus(true);
                        authCheck.setOperatingRange(OperatingRangeEnum.PROJECT_CODE);
                        authCheck.setProjectIds(new ArrayList<>(projectIds1));
                        List<Integer> tenantIds1 = new ArrayList<>();
                        tenantIds1.add(defaultTenantId);
                        authCheck.setTenantIds(tenantIds1);
                        authCheck.setUserId(userId);
                        break;
                    case 3:
                        // project?????????????????? ???????????? ?????????????????????????????????id???userid
                        authCheck.setStatus(true);
                        authCheck.setOperatingRange(OperatingRangeEnum.SELF_CODE);
                        List<Integer> tenantIds2 = new ArrayList<>();
                        tenantIds2.add(defaultTenantId);
                        authCheck.setTenantIds(tenantIds2);
                        authCheck.setUserId(userId);
                        break;
                    default:
                        log.error("VDC???????????????????????????????????????????????????????????????????????????");
                        break;
                    }
                    break;
                default:
                    log.error("VDC??????????????????");
                    break;
                }
            } else {// ?????????????????? ????????????
                Integer index = 4;
                Integer tenantRange = 0;
                Set<Integer> projectIds = new HashSet<Integer>();
                Set<ProjectOperatingRange> projectOperatingRanges = new HashSet<ProjectOperatingRange>();
                // project??????????????????
                for (Entry<Integer, List<Integer>> entry : map.entrySet()) {
                    List<AuthVO> userAuths1 = authMapper.getFunctionCodeByAuths(functionCode, entry.getValue());
                    boolean flag = false;
                    if (userAuths1 != null && userAuths1.size() > 0) {
                        for (AuthVO authVO : userAuths1) {
                            switch (authVO.getOperatingRange()) {
                            case OperatingRangeEnum.ALL_CODE:
                                index = 0;
                                break;
                            case OperatingRangeEnum.TENANT_CODE:
                                index = 1;
                                // ??????????????????????????????
                                tenantRange = tenantRange | Integer.parseInt(authVO.getTenantRange(), 2);
                                break;
                            case OperatingRangeEnum.PROJECT_CODE:
                            case OperatingRangeEnum.SELF_CODE:
                                if (2 < index) {
                                    index = 2;
                                }
                                if (authVO.getOperatingRange().equals(OperatingRangeEnum.PROJECT_CODE)) {
                                    projectIds.add(entry.getKey());
                                } else {
                                    ProjectOperatingRange projectOperatingRange = new ProjectOperatingRange();
                                    projectOperatingRange.setProjectId(entry.getKey());
                                    projectOperatingRange.setUserId(userId);
                                    projectOperatingRanges.add(projectOperatingRange);
                                }
                                break;
                            default:
                                log.error("VDC?????????????????????project??????????????????????????????????????????");
                                break;
                            }
                            if (index == 0) {
                                flag = true;
                                break;
                            }
                        }
                    }
                    if (flag) {
                        break;
                    }
                }
                switch (index) {
                case 0:
                    // project?????????????????? ??????????????? ?????????????????????
                    authCheck.setStatus(true);
                    authCheck.setOperatingRange(OperatingRangeEnum.ALL_CODE);
                    break;
                case 1:
                    // project?????????????????? ???????????? ?????????????????? ??????id(??????????????????id)
                    authCheck.setStatus(true);
                    authCheck.setOperatingRange(OperatingRangeEnum.TENANT_CODE);
                    List<Integer> tenantIds = new ArrayList<>();
                    String s = Integer.toBinaryString(tenantRange);
                    s = supplementaryLength(s, 3, "before", "0");
                    // ??????vdc
                    if (s.charAt(0) == TENANT_RANGE_CHOICE) {
                        tenantIds.add(defaultTenantId);
                    }
                    // ????????????vdc
                    if (s.charAt(CHILDEN_TENANT_POSITION) == TENANT_RANGE_CHOICE) {
                        List<TenantTree> childTenants = tenantService.queryAllChildTenant(defaultTenantId);
                        if (CollectionUtils.isNotEmpty(childTenants)) {
                            tenantIds.addAll(childTenants.stream().map(TenantTree::getId).collect(Collectors.toList()));
                        }
                    }
                    // ????????????vdc
                    if (s.charAt(PARENT_TENANT_POSITION) == TENANT_RANGE_CHOICE) {
                        queryAllParentTenant(defaultTenantId, tenantService, tenantIds);
                    }
                    if (tenantIds.isEmpty()) {
                        tenantIds.add(0);
                    }
                    authCheck.setTenantIds(tenantIds);
                    break;
                case 2:
                    // project?????????????????? project?????? ??????project?????? projectid??????projectids
                    authCheck.setStatus(true);
                    authCheck.setOperatingRange(OperatingRangeEnum.PROJECT_CODE);
                    authCheck.setProjectIds(new ArrayList<>(projectIds));
                    authCheck.setProjectOperatingRanges(new ArrayList<>(projectOperatingRanges));
                    break;
                default:
                    log.error("VDC???????????????????????????????????????project?????????????????????");
                    break;
                }
            }
            return R.ok(checkResource(authCheck, resTenantId, resProjectId, resUserId));
        }
    }

    public String supplementaryLength(String s, Integer length, String type, String supplyChar) {
        StringBuffer sb = new StringBuffer();
        sb.append(s);
        int count = length - s.length();
        for (int i = 0; i < count; i++) {
            if ("before".equals(type)) {
                sb.insert(0, supplyChar);
            }
            if ("end".equals(type)) {
                sb.append(supplyChar);
            }
        }
        return sb.toString();
    }

    public void queryAllParentTenant(Integer defaultTenantId, ITenantService tenantService, List<Integer> tenantIds) {
        TenantVO tenantVo = tenantService.selectTenantVoById(defaultTenantId);
        Integer level = tenantVo.getLevel();
        Integer parentId = tenantVo.getParentId();
        while (level != 1) {
            tenantVo = tenantService.selectTenantVoById(parentId);
            tenantIds.add(tenantVo.getId());
            level = tenantVo.getLevel();
            parentId = tenantVo.getParentId();
        }
    }

    public AuthCheck checkResource(AuthCheck authCheck, Integer resTenantId, Integer resProjectId, Integer resUserId) {
        if (resTenantId == null && resProjectId == null && resUserId == null) {
            return authCheck;
        }

        if (!authCheck.isStatus() || authCheck.getOperatingRange().equals(OperatingRangeEnum.ALL_CODE)) {
            return authCheck;
        }
        if (authCheck.getOperatingRange().equals(OperatingRangeEnum.TENANT_CODE)) {
            if (resTenantId == null || !authCheck.getTenantIds().contains(resTenantId)) {
                return new AuthCheck();
            }
        } else if (authCheck.getOperatingRange().equals(OperatingRangeEnum.PROJECT_CODE)) {
            if (CollectionUtils.isNotEmpty(authCheck.getProjectIds())) {
                if (resProjectId == null || !authCheck.getProjectIds().contains(resProjectId)) {
                    if (CollectionUtils.isNotEmpty(authCheck.getProjectOperatingRanges())) {
                        if (resProjectId == null || resUserId == null
                                || !isProjectOperationRangesValid(authCheck.getProjectOperatingRanges(), resProjectId,
                                        resUserId)) {
                            if (authCheck.getTenantIds() != null && authCheck.getUserId() != null) {
                                if (resTenantId == null || resUserId == null) {
                                    return new AuthCheck();
                                }
                                if (!(authCheck.getTenantIds().contains(resTenantId)
                                        && resUserId.equals(authCheck.getUserId()))) {
                                    return new AuthCheck();
                                }
                            }
                        }
                    }
                }
            }
        } else if (authCheck.getOperatingRange().equals(OperatingRangeEnum.SELF_CODE)) {
            if (resTenantId == null || resUserId == null) {
                return new AuthCheck();
            } else {
                if (authCheck.getTenantIds().contains(-1)) {
                    if (!resUserId.equals(authCheck.getUserId())) {
                        return new AuthCheck();
                    }
                } else if (!(authCheck.getTenantIds().contains(resTenantId)
                        && resUserId.equals(authCheck.getUserId()))) {
                    return new AuthCheck();
                }
            }
        }
        return authCheck;
    }

    private boolean isProjectOperationRangesValid(List<ProjectOperatingRange> projectOperatingRanges,
            Integer resProjectId, Integer resUserId) {
        for (ProjectOperatingRange var : projectOperatingRanges) {
            if (resProjectId.equals(var.getProjectId()) && resUserId.equals(var.getUserId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * ??????????????????????????????????????????????????????????????????
     *
     * @param roleId        ?????????id
     * @param inheritRoleId ????????????id
     * @return
     */
    @TxTransaction
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer inheritAuth(Integer roleId, Integer inheritRoleId) {
        List<Auth> list = this
                .list(Wrappers.<Auth>lambdaQuery().eq(null != inheritRoleId, Auth::getRoleId, inheritRoleId));
        if (list.size() > 0) {
            return authMapper.saveAuth(roleId, list);
        }
        return null;
    }

    /**
     * @param menuId      ??????id
     * @param defaultRole ????????????
     * @return
     */
    @Override
    public List<AuthVO> getMenuByAuth(String menuId) {
        FitmgrUser user = SecurityUtils.getUser();
        List<Map<String, Integer>> roleList = userMapper.queryRoleByUserIdAndTenantId(user.getId(),
                user.getDefaultTenantId());
        List<Integer> roleIds = new ArrayList<>();
        for (Map<String, Integer> role : roleList) {
            roleIds.add(role.get("role_id"));
        }
        List<AuthVO> list = authMapper.getMenuByAuth(menuId, roleIds);
        return list;
    }

    @Override
    public void processAuth(Integer roleId) {

        List<Auth> authList = authMapper
                .selectList(new QueryWrapper<Auth>().lambda().eq(Auth::getRoleId, 4).eq(Auth::getDelFlag, 0));
        for (Auth auth : authList) {
            List<Auth> authTemp = authMapper.selectList(new QueryWrapper<Auth>().lambda()
                    .eq(Auth::getFunctionId, auth.getFunctionId()).eq(Auth::getRoleId, roleId).eq(Auth::getDelFlag, 0));
            if (CollectionUtils.isNotEmpty(authTemp)) {
                Auth auth1 = authTemp.get(0);
                auth1.setStatus(auth.getStatus());
                auth1.setOperatingRange(auth.getOperatingRange());
                authMapper.updateById(auth1);
            } else {
                Auth auth1 = new Auth();
                auth1.setOperatingRange(auth.getOperatingRange());
                auth1.setStatus(auth.getStatus());
                auth1.setDelFlag(auth.getDelFlag());
                auth1.setFunctionId(auth.getFunctionId());
                auth1.setRoleId(roleId);
                authMapper.insert(auth1);
            }
        }
    }

    @Override
    public AuthVO getFunctionIdByAuths(Integer functionId, List<Integer> roleIds) {
        return authMapper.getFunctionIdByAuths(functionId, roleIds);
    }
}
