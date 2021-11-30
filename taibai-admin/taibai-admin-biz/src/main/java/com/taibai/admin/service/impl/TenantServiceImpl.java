package com.taibai.admin.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taibai.admin.api.constants.NetworkPoolEnum;
import com.taibai.admin.api.constants.NetworkPoolTypeEnum;
import com.taibai.admin.api.constants.RoleLevelEnum;
import com.taibai.admin.api.dto.NetworkPoolDTO;
import com.taibai.admin.api.dto.TenantDTO;
import com.taibai.admin.api.dto.TenantTree;
import com.taibai.admin.api.dto.TokenDTO;
import com.taibai.admin.api.dto.UserDTO;
import com.taibai.admin.api.entity.AllLocationTree;
import com.taibai.admin.api.entity.AuthCheck;
import com.taibai.admin.api.entity.ExcelData;
import com.taibai.admin.api.entity.LocationTreeNode;
import com.taibai.admin.api.entity.NetworkPool;
import com.taibai.admin.api.entity.NetworkPoolInfo;
import com.taibai.admin.api.entity.Project;
import com.taibai.admin.api.entity.Role;
import com.taibai.admin.api.entity.SubDomainConfigSwitch;
import com.taibai.admin.api.entity.Tenant;
import com.taibai.admin.api.entity.TenantAdmin;
import com.taibai.admin.api.entity.TenantLocationTree;
import com.taibai.admin.api.entity.TenantNetworkPool;
import com.taibai.admin.api.entity.TenantResourcePool;
import com.taibai.admin.api.entity.TenantService;
import com.taibai.admin.api.entity.User;
import com.taibai.admin.api.entity.UserRoleProject;
import com.taibai.admin.api.feign.RemoteTokenService;
import com.taibai.admin.api.vo.ImportTenantVo;
import com.taibai.admin.api.vo.ImportUserVo;
import com.taibai.admin.api.vo.Member;
import com.taibai.admin.api.vo.MemberVO;
import com.taibai.admin.api.vo.TenantProjectUserVO;
import com.taibai.admin.api.vo.TenantResourcePoolVO;
import com.taibai.admin.api.vo.TenantRoleVO;
import com.taibai.admin.api.vo.TenantVO;
import com.taibai.admin.api.vo.UserVO;
import com.taibai.admin.exceptions.UserCenterException;
import com.taibai.admin.mapper.AllLocationTreeMapper;
import com.taibai.admin.mapper.NetworkPoolMapper;
import com.taibai.admin.mapper.ProjectMapper;
import com.taibai.admin.mapper.RoleMapper;
import com.taibai.admin.mapper.SubDomainConfigSwitchMapper;
import com.taibai.admin.mapper.TenantLocationTreeMapper;
import com.taibai.admin.mapper.TenantMapper;
import com.taibai.admin.mapper.TenantNetworkPoolMapper;
import com.taibai.admin.mapper.TenantServiceMapper;
import com.taibai.admin.mapper.UserMapper;
import com.taibai.admin.mapper.UserRoleProjectMapper;
import com.taibai.admin.service.IAuthService;
import com.taibai.admin.service.ITenantService;
import com.taibai.admin.service.ITenantTypeService;
import com.taibai.admin.service.IUserService;
import com.taibai.admin.threadpool.InheritableRequestContextTaskWrapper;
import com.taibai.admin.utils.AdminUtils;
import com.taibai.admin.utils.DateUtils;
import com.taibai.admin.utils.ExcelUtil;
import com.taibai.admin.utils.PageUtil;
import com.taibai.common.core.constant.CommonConstants;
import com.taibai.common.core.constant.SecurityConstants;
import com.taibai.common.core.constant.enums.ApiEnum;
import com.taibai.common.core.constant.enums.BusinessEnum;
import com.taibai.common.core.constant.enums.OperatingRangeEnum;
import com.taibai.common.core.constant.enums.ResponseCodeEnum;
import com.taibai.common.core.exception.BusinessException;
import com.taibai.common.core.util.R;
import com.taibai.common.core.util.TreeNode;
import com.taibai.common.minio.service.MinioTemplate;
import com.taibai.common.security.service.FitmgrUser;
import com.taibai.common.security.util.SecurityUtils;
import com.taibai.quota.api.entity.QuotaRelationTenant;
import com.taibai.quota.api.feign.RemoteQuotaService;
import com.taibai.resource.api.dto.ResourceOperateDTO;
import com.taibai.resource.api.feign.RemoteCmdbReportService;
import com.taibai.resource.api.feign.RemoteCmdbService;
import com.google.common.collect.Lists;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 创建人   mhp
 * 创建时间 2019/11/12
 * 描述
 **/

@Slf4j
@Service
@AllArgsConstructor
public class TenantServiceImpl extends ServiceImpl<TenantMapper, Tenant> implements ITenantService {
    
    private final String TENANT_STATUS_OFF = "1";
    private final String USER_STATUS_OFF = "1";
    private final String POOL_RESOURCE_RZ = "RZ";

    private final RemoteTokenService remoteTokenService;
    private final TenantMapper tenantMapper;
    private final RoleMapper roleMapper;
    private UserRoleProjectMapper userRoleProjectMapper;
    private final IAuthService iAuthService;
    private final ITenantTypeService tenantTypeService;
    private final IUserService userService;
    private final RedisTemplate<String, String> redisTemplate;
    private final ProjectMapper projectMapper;
    private final RemoteCmdbService remoteCmdbService;
    private final RemoteCmdbReportService remoteCmdbReportService;
    private final UserMapper userMapper;
    private final AdminUtils adminUtils;
    private final ThreadPoolTaskExecutor threadPoola;
    private final RemoteQuotaService remoteQuotaService;
    private final TenantServiceMapper tenantServiceMapper;
    private final AllLocationTreeMapper allLocationTreeMapper;
    private final TenantLocationTreeMapper tenantLocationTreeMapper;
    private final NetworkPoolMapper networkPoolMapper;
    private final TenantNetworkPoolMapper tenantNetworkPoolMapper;
    private final SubDomainConfigSwitchMapper subDomainConfigSwitchMapper;
    private ThreadPoolTaskExecutor executor;
    
    private final MinioTemplate minioTemplate;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public R saveTenant(TenantDTO tenantDTO) {
        Tenant tenant = new Tenant();
        BeanUtils.copyProperties(tenantDTO, tenant);
        tenant.setStatus(CommonConstants.STATUS_NORMAL);
        tenant.setDelFlag(CommonConstants.STATUS_NORMAL);
        tenant.setCreateTime(LocalDateTime.now());
        tenant.setUpdateTime(tenant.getCreateTime());
        if (tenantDTO.getParentId() == null) {
            tenant.setParentId(-1);
            tenant.setLevel(1);
        } else {
            Tenant parentTenant = tenantMapper.selectOne(new QueryWrapper<Tenant>().lambda().eq(Tenant::getId, tenant.getParentId()));
            if (parentTenant != null) {
                if (parentTenant.getLevel() != null) {
                    tenant.setLevel(parentTenant.getLevel() + 1);
                } else {
                    return R.failed("参数错误");
                }
            } else {
                return R.failed("参数错误");
            }
        }

        if (tenant.getLevel() > CommonConstants.MAX_TENANT_LEVEL) {
            return R.failed("最多只能创建6级VDC");
        }

        int tenantCount = tenantMapper.selectCount(new QueryWrapper<Tenant>().lambda().eq(Tenant::getLevel, tenant.getLevel()));
        if (tenantCount > CommonConstants.MAX_SAME_LEVEL_TENANT_COUNT) {
            return R.failed("同一级别VDC最多创建1000个");
        }

        int i = tenantMapper.insert(tenant);
        saveTenantInfoToQuota(tenantDTO);
        redisTemplate.opsForValue().set(CommonConstants.TENANT_PREFIX + tenant.getId(), tenant.getName());
        //添加位置树
        List<AllLocationTree> allLocationTrees = allLocationTreeMapper.selectList(Wrappers.query(new AllLocationTree()).orderByDesc("id"));
        AllLocationTree allLocationTree = allLocationTrees.get(0);
        List<LocationTreeNode> locationTreeJson = JSONObject.parseArray(allLocationTree.getLocationTree(), LocationTreeNode.class);
        JSONArray locationTreeArray = new JSONArray();
        for (LocationTreeNode locationTreeNode : locationTreeJson) {
            JSONObject json = new JSONObject();
            json.put("uuid", locationTreeNode.getUuid());
            json.put("half", false);
            locationTreeArray.add(json);
        }
        TenantLocationTree tenantLocationTree = new TenantLocationTree();
        tenantLocationTree.setLocationTree(locationTreeArray.toJSONString());
        tenantLocationTree.setTreeVersion(allLocationTree.getTreeVersion());
        tenantLocationTree.setVdcId(tenant.getId());
        tenantLocationTree.setParentVdcId(tenant.getParentId());
        tenantLocationTreeMapper.insert(tenantLocationTree);
        //添加网络池
        addNetworkPool(allLocationTree, tenant.getId(), NetworkPoolTypeEnum.SP_IP.getCode().toString());
        addNetworkPool(allLocationTree, tenant.getId(), NetworkPoolTypeEnum.IP_SUBNET.getCode().toString());
        addNetworkPool(allLocationTree, tenant.getId(), NetworkPoolTypeEnum.VLAN.getCode().toString());
        
        executor.execute(() -> {
            FitmgrUser loginUser = SecurityUtils.getUser();
            User user = userMapper.selectById(loginUser.getId());
            adminUtils.sendEmail(loginUser.getId(), "新建VDC", "create-tenant", "tenantName:" + tenant.getName(), Lists.newArrayList(user.getEmail()));
        });
        return i > 0 ? R.ok() : R.failed();
    }
    
    public void addNetworkPool(AllLocationTree allLocationTree, Integer tenantId, String networkPoolTypeEnum) {
        // 根据RZ全选网络池
        List<NetworkPoolInfo> tenantNetworkPoolInfos = new ArrayList<NetworkPoolInfo>();
        List<NetworkPool> networkPools = networkPoolMapper
                .selectList(Wrappers.query(new NetworkPool()).eq("network_pool_type", networkPoolTypeEnum).orderByDesc("id"));
        List<NetworkPoolInfo> oldnetworkPoolInfos = JSONObject.parseArray(networkPools.get(0).getNetworkPoolInfo(), NetworkPoolInfo.class);
        for (int i = 0; i < oldnetworkPoolInfos.size(); i++) {
            NetworkPoolInfo map = oldnetworkPoolInfos.get(i);
            NetworkPoolInfo networkPoolInfo = new NetworkPoolInfo(map.getUuid().toString(),
                    "1", map.getResourceZoneId().toString());
            tenantNetworkPoolInfos.add(networkPoolInfo);
        }
        TenantNetworkPool tenantNetworkPool = new TenantNetworkPool();
        tenantNetworkPool.setNetworkPoolType(networkPoolTypeEnum);
        tenantNetworkPool.setVdcId(tenantId);
        tenantNetworkPool.setVersion(allLocationTree.getTreeVersion());
        tenantNetworkPool.setNetworkPoolInfo(JSON.toJSONString(tenantNetworkPoolInfos));
        tenantNetworkPoolMapper.insert(tenantNetworkPool);
    }

    public Integer saveTenantInfoToQuota(TenantDTO tenantDTO) {
        TenantDTO condition = new TenantDTO();
        condition.setName(tenantDTO.getName());
        List<Tenant> result = tenantList(condition);
        TenantVO tenantVO = selectTenantVoById(result.get(0).getId());
        QuotaRelationTenant quotaRelationTenant = new QuotaRelationTenant();
        quotaRelationTenant.setCreateTime(tenantVO.getCreateTime());
        quotaRelationTenant.setName(tenantVO.getName());
        quotaRelationTenant.setLevel(tenantVO.getLevel());
        quotaRelationTenant.setParentTenantId(tenantVO.getParentId());
        quotaRelationTenant.setTenantId(tenantVO.getId());
        quotaRelationTenant.setTypeId(tenantVO.getTypeId());
        quotaRelationTenant.setTypeName(tenantVO.getTypeName());
        R r = remoteQuotaService.addTenantRelation(quotaRelationTenant);
        return r.getCode();
    }

    public R deleteTenantInfoToQuota(Integer tenantId) {
        R r = null;
        try {
            r = remoteQuotaService.deleteTenantRelation(tenantId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return r;
    }


    public Integer updateTenantInfoToQuota(TenantDTO tenantDTO) {
        QuotaRelationTenant quotaRelationTenant = new QuotaRelationTenant();
        TenantVO tenantVO = selectTenantVoById(tenantDTO.getId());
        quotaRelationTenant.setName(tenantDTO.getName());
        quotaRelationTenant.setTypeName(tenantVO.getTypeName());
        quotaRelationTenant.setTenantId(tenantDTO.getId());
        R r = remoteQuotaService.updateTenantRelation(quotaRelationTenant);
        return r.getCode();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTenant(Integer tenantId) {
        Integer selectCount = tenantMapper.selectCount(new QueryWrapper<Tenant>().lambda().eq(Tenant::getId, tenantId));
        if (selectCount < 1) {
            throw new UserCenterException("VDC不存在");
        }
        //若该租户下仍然存在用户，不允许删除
        int count = userRoleProjectMapper.selectCount(new QueryWrapper<UserRoleProject>().lambda().eq(UserRoleProject::getTenantId, tenantId));
        if (count > 0) {
            throw new UserCenterException("该VDC仍然存在成员，不能删除该VDC");
        }
        Integer count1 = projectMapper.selectCount(new QueryWrapper<Project>().lambda().eq(Project::getTenantId, tenantId));
        if (count1 > 0) {
            throw new UserCenterException("该VDC仍然存在项目，不能删除该VDC");
        }

        Integer count2 = tenantMapper.selectCount(new QueryWrapper<Tenant>().lambda().eq(Tenant::getParentId, tenantId));
        if (count2 > 0) {
            throw new UserCenterException("该VDC仍然存在下级VDC，不能删除该VDC");
        }

        R<Boolean> customerHasResource = remoteCmdbService.isCustomerHasResource("0", tenantId);
        if (ResponseCodeEnum.SUCCESS.getCode() != customerHasResource.getCode()) {
            throw new BusinessException("检查VDC资源时发生异常");
        }
        if (customerHasResource.getData()) {
            throw new BusinessException("该VDC下存在资源未释放，不能删除该VDC");
        }

        // TODO 删除配额报错了
        R r = deleteTenantInfoToQuota(tenantId);
        if (r == null || r.getCode() == 1) {
            log.error("quota delete tenant operate is error!");
            throw new BusinessException(r == null ? "删除VDC配额失败" : r.getMsg());
        }
        
        //删除位置树
        tenantLocationTreeMapper.delete(new QueryWrapper<TenantLocationTree>().lambda().eq(TenantLocationTree::getVdcId, tenantId));
        tenantNetworkPoolMapper.delete(new QueryWrapper<TenantNetworkPool>().lambda().eq(TenantNetworkPool::getVdcId, tenantId));
        tenantServiceMapper.delete(new QueryWrapper<TenantService>().lambda().eq(TenantService::getTenantId, tenantId));

        boolean b = this.removeById(tenantId);
        redisTemplate.delete(CommonConstants.TENANT_PREFIX + tenantId);
        return b;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTenant(TenantDTO tenantDTO) {
        Tenant oldTenant = tenantMapper.selectById(tenantDTO.getId());
        if (oldTenant == null) {
            return false;
        }
        Tenant tenant = new Tenant();
        BeanUtils.copyProperties(tenantDTO, tenant);
        tenant.setUpdateTime(LocalDateTime.now());
        int i = tenantMapper.updateById(tenant);
        Integer code = updateTenantInfoToQuota(tenantDTO);
        if (code.equals(1)) {
            log.info("quota update tenant operate is error!");
        }
        redisTemplate.opsForValue().set(CommonConstants.TENANT_PREFIX + tenant.getId(), tenant.getName());

        InheritableRequestContextTaskWrapper wrapper = new InheritableRequestContextTaskWrapper();
        threadPoola.submit(() -> {
            wrapper.lambda2(() -> {
                try {
                    List<Integer> userIds = new ArrayList<Integer>();
                    List<String> emails = new ArrayList<String>();
                    Role role = roleMapper.selectOne(new QueryWrapper<Role>().lambda().eq(Role::getRoleCode, "tenant_admin"));
                    List<UserVO> userVos = userMapper.queryUserInfoByRoleIdAndTenantId(role.getId(), tenant.getId());
                    if (CollectionUtils.isNotEmpty(userVos)) {
                        userIds = userVos.stream().map(UserVO::getId).collect(Collectors.toList());
                        emails = userVos.stream().map(User::getEmail).collect(Collectors.toList());
                    }
                    String parameters = "tenantName:" + oldTenant.getName() + ","
                            + "newTenantName:" + tenant.getName() + ","
                            + "newEnTenantName:" + tenant.getEnglishName();
                    adminUtils.batchSendEmail(userIds, "修改VDC信息", "modify-tenant", parameters, emails);
                } catch (Throwable e) {
                    log.error("Error occurred in async tasks", e);
                }
            }).accept();
        });
        return i > 0;
    }

    /**
     * 根据条件 分页查询租户列表
     *
     * @param page      分页条件
     * @param tenantDTO 条件对象
     * @param userId    用户id
     * @return
     */
    @Override
    public IPage<TenantVO> tenantList(Page page, TenantDTO tenantDTO, Integer userId) {
      //获取数据权限
        R<AuthCheck> r = iAuthService.newAuthCheck(ApiEnum.SELECT_TENANT_LIST.getCode(), userId, SecurityUtils.getUser().getDefaultTenantId(),null,null,null);
        if (r.getCode() == 0 && r.getData().isStatus()) {
            if (tenantDTO.getParentId() == null) {
                if (OperatingRangeEnum.ALL_CODE.equals(r.getData().getOperatingRange())) {
                    return tenantMapper.selectListByCondition(page, tenantDTO);
                } else if (OperatingRangeEnum.TENANT_CODE.equals(r.getData().getOperatingRange())) {
                    tenantDTO.setTenantIds(r.getData().getTenantIds());
                    return tenantMapper.selectListByCondition(page, tenantDTO);
                } else if (OperatingRangeEnum.PROJECT_CODE.equals(r.getData().getOperatingRange())) {
                    throw new UserCenterException(BusinessEnum.AUTH_NOT);
                } else if (OperatingRangeEnum.SELF_CODE.equals(r.getData().getOperatingRange())) {
                    throw new UserCenterException(BusinessEnum.AUTH_NOT);
                }
                throw new UserCenterException(BusinessEnum.AUTH_CONFIG);
            } else {
                if (r.getData().getOperatingRange().equals(OperatingRangeEnum.ALL_CODE)) {
                    String name = tenantDTO.getName();
                    tenantDTO.setName(null);
                    List<TenantVO> tenantVos = tenantMapper.selectListByConditionNoPage(tenantDTO);
                    List<TenantVO> resultList = new ArrayList<>();
                    Map<Integer, TenantVO> tenantVoMap = new HashMap<>();
                    for (TenantVO tenantVO : tenantVos) {
                        if(tenantDTO.getOnlyChild()) {
                            if (tenantVO.getParentId().equals(tenantDTO.getParentId())) {
                                resultList.add(tenantVO);
                                tenantVoMap.put(tenantVO.getId(), tenantVO);
                            } else {
                                if (tenantVoMap.containsKey(tenantVO.getParentId())) {
                                    resultList.add(tenantVO);
                                    tenantVoMap.put(tenantVO.getId(), tenantVO);
                                }
                            }
                        }else {
                            if (tenantVO.getId().equals(tenantDTO.getParentId())) {
                                resultList.add(tenantVO);
                                tenantVoMap.put(tenantVO.getId(), tenantVO);
                            } else {
                                if (tenantVoMap.containsKey(tenantVO.getParentId())) {
                                    resultList.add(tenantVO);
                                    tenantVoMap.put(tenantVO.getId(), tenantVO);
                                }
                            }
                        }
                    }
                    List<TenantVO> resultTenants = new ArrayList<>();
                    for (TenantVO tenantVO : resultList) {
                        if(tenantVO.getName().contains(name)) {
                            resultTenants.add(tenantVO);
                        }
                    }
                    IPage<TenantVO> iPage = new Page<TenantVO>();
                    if (CollectionUtils.isNotEmpty(resultTenants)) {
                        PageUtil<TenantVO> pageUtil = new PageUtil<TenantVO>(resultTenants, (int) page.getSize());
                        iPage.setPages(pageUtil.getPageCount());
                        iPage.setSize(page.getSize());
                        iPage.setTotal(resultTenants.size());
                        iPage.setCurrent(page.getCurrent());
                        iPage.setRecords(pageUtil.page((int) page.getCurrent()));
                    }
                    return iPage;
                } else if (r.getData().getOperatingRange().equals(OperatingRangeEnum.TENANT_CODE)) {
                    List<Integer> tenantIds = r.getData().getTenantIds();
                    List<TenantVO> tenantVos;
                    if(tenantIds.size()==0) {
                        tenantVos = new ArrayList<TenantVO>();
                    }else {
                        tenantDTO.setTenantIds(tenantIds);
                        tenantVos = tenantMapper.selectListByConditionNoPage(tenantDTO);
                    }
                    List<TenantVO> resultList = new ArrayList<>();
                    Map<Integer, TenantVO> tenantVoMap = new HashMap<>();
                    if(tenantDTO.getOnlyChild()) {
                        for (TenantVO tenantVO : tenantVos) {
                            if (tenantVO.getParentId().equals(tenantDTO.getParentId())) {
                                resultList.add(tenantVO);
                                tenantVoMap.put(tenantVO.getId(), tenantVO);
                            } else {
                                if (tenantVoMap.containsKey(tenantVO.getParentId())) {
                                    resultList.add(tenantVO);
                                    tenantVoMap.put(tenantVO.getId(), tenantVO);
                                }
                            }
                        }
                    }else {
                        resultList = tenantVos;
                    }
                    IPage<TenantVO> iPage = new Page<TenantVO>();
                    if (CollectionUtils.isNotEmpty(resultList)) {
                        PageUtil<TenantVO> pageUtil = new PageUtil<TenantVO>(resultList, (int) page.getSize());
                        iPage.setPages(pageUtil.getPageCount());
                        iPage.setSize(page.getSize());
                        iPage.setTotal(resultList.size());
                        iPage.setCurrent(page.getCurrent());
                        iPage.setRecords(pageUtil.page((int) page.getCurrent()));
                    }
                    return iPage;
                } else if (r.getData().getOperatingRange().equals(OperatingRangeEnum.PROJECT_CODE)) {
                    throw new UserCenterException(BusinessEnum.AUTH_NOT);
                } else if (r.getData().getOperatingRange().equals(OperatingRangeEnum.SELF_CODE)) {
                    throw new UserCenterException(BusinessEnum.AUTH_NOT);
                }
                throw new UserCenterException(BusinessEnum.AUTH_CONFIG);
            }
        }
        throw new UserCenterException(BusinessEnum.AUTH_NOT);
    }

    @Override
    public TenantVO selectTenantVoById(Integer tenantId) {
        TenantVO tenantVO = tenantMapper.selectTenantVoById(tenantId);
        return tenantVO;
    }

    @Override
    public TenantVO selectByUserId(Integer userId) {
        return tenantMapper.selectByUserId(userId);
    }

    @Override
    public List<Tenant> tenantList(TenantDTO tenantDTO) {
        FitmgrUser user = SecurityUtils.getUser();
        if (null == user) {
            throw new BusinessException("用户未登录");
        }
        R<AuthCheck> r = iAuthService.newAuthCheck(ApiEnum.SELECT_TENANT_LIST.getCode(), user.getId(), SecurityUtils.getUser().getDefaultTenantId(),null,null,null);
        if (ResponseCodeEnum.SUCCESS.getCode() != r.getCode() || !r.getData().isStatus()) {
            throw new BusinessException("获取用户权限时发生异常");
        }
        switch (r.getData().getOperatingRange()) {
            case OperatingRangeEnum.ALL_CODE:
                break;
            case OperatingRangeEnum.TENANT_CODE:
                tenantDTO.setTenantIds(r.getData().getTenantIds());
                break;
            case OperatingRangeEnum.PROJECT_CODE:
            case OperatingRangeEnum.SELF_CODE:
                throw new BusinessException("获取用户权限时发生异常");
            default:
                throw new BusinessException("获取用户权限时发生异常");
            
        }
        LambdaQueryWrapper<Tenant> queryWrapper =
                Wrappers.<Tenant>lambdaQuery()
                        .in(null != tenantDTO.getTenantIds(), Tenant::getId, tenantDTO.getTenantIds())
                        .eq(StringUtils.isNoneBlank(tenantDTO.getStatus()), Tenant::getStatus, tenantDTO.getStatus())
                        .eq(StringUtils.isNoneBlank(tenantDTO.getCreateProject()), Tenant::getCreateProject, tenantDTO.getCreateProject())
                        .eq(StringUtils.isNoneBlank(tenantDTO.getName()), Tenant::getName, tenantDTO.getName());

        return tenantMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateStatus(Tenant tenant) {
        Tenant tenant1 = tenantMapper.selectById(tenant.getId());
        if (tenant1 == null) {
            return 0;
        }
        updateTenantStatus(tenant.getId(), tenant.getStatus());

        InheritableRequestContextTaskWrapper wrapper = new InheritableRequestContextTaskWrapper();
        threadPoola.submit(() -> {
            wrapper.lambda2(() -> {
                sendEmailForTenantStatus(tenant.getId(), tenant.getStatus());
            }).accept();
        });
        return 1;
    }

    private void updateTenantStatus(Integer tenantId, String status) {
        Tenant tenantTemp = new Tenant();
        tenantTemp.setId(tenantId);
        tenantTemp.setStatus(status);
        tenantMapper.update(tenantTemp, new UpdateWrapper<Tenant>().lambda().eq(Tenant::getId, tenantId));
        // 租户禁用状态，需要清除该租户下所有的用户Redis缓存信息，预防出现可利用缓存登录的异常-20.05.03
        if (TENANT_STATUS_OFF.equals(status)) {
            List<UserVO> userVos = userService.queryUserListByTenantId(tenantId);
            for (UserVO u : userVos) {
                if (StrUtil.isNotBlank(u.getUsername())) {
                    redisTemplate.delete(CommonConstants.USER_LOGIN_PREFIX + u.getUsername());
                }
            }
        }

        List<Tenant> tenants = tenantMapper.selectList(new QueryWrapper<Tenant>().lambda().eq(Tenant::getParentId, tenantId));
        if (CollectionUtils.isEmpty(tenants)) {
            return;
        }
        for (Tenant tenant : tenants) {
            updateTenantStatus(tenant.getId(), status);
        }
    }

    private void sendEmailForTenantStatus(Integer tenantId, String status) {
        try {
            Tenant tenant1 = tenantMapper.selectById(tenantId);
            List<UserVO> users = userMapper.queryUserListByTenantId(tenantId);
            if (CollectionUtils.isNotEmpty(users)) {
                List<Integer> userIds = users.stream().map(UserVO::getId).collect(Collectors.toList());
                List<String> emails = users.stream().map(UserVO::getEmail).collect(Collectors.toList());
                if (TENANT_STATUS_OFF.equals(status)) {
                    adminUtils.batchSendEmail(userIds, "禁用VDC", "enable-disable-tenant", "tenantName:" + tenant1.getName() + ",action:禁用", emails);
                } else {
                    adminUtils.batchSendEmail(userIds, "启用VDC", "enable-disable-tenant", "tenantName:" + tenant1.getName() + ",action:启用", emails);
                }
            }
        } catch (Throwable e) {
            log.error("Error occurred in async tasks", e);
        }

        List<Tenant> tenants = tenantMapper.selectList(new QueryWrapper<Tenant>().lambda().eq(Tenant::getParentId, tenantId));
        if (CollectionUtils.isEmpty(tenants)) {
            return;
        }
        for (Tenant tenant : tenants) {
            this.sendEmailForTenantStatus(tenant.getId(), status);
        }
    }

    @Override
    public R saveAdmin(TenantAdmin tenantAdmin) {
        Role role = roleMapper.selectOne(new QueryWrapper<Role>().lambda().eq(Role::getRoleCode, ApiEnum.TENANT_ADMIN.getCode()));
        List<UserVO> users = userMapper.queryUserListByTenantId(tenantAdmin.getTenantId());
        if (CollectionUtils.isNotEmpty(users)) {
            List<Integer> userIds = users.stream().map(User::getId).collect(Collectors.toList());
            userRoleProjectMapper.delete(new QueryWrapper<UserRoleProject>().lambda().in(UserRoleProject::getUserId, userIds).eq(UserRoleProject::getRoleId, role.getId()));
        }

        for (Integer userId : tenantAdmin.getUserIds()) {
            User user = userService.getById(userId);
            if (null == user) {
                return R.failed("用户不存在");
            }
            UserRoleProject userRoleProject = new UserRoleProject();
            userRoleProject.setUserId(userId);
            userRoleProject.setRoleId(role.getId());
            userRoleProject.setProjectId(0);
            userRoleProjectMapper.insert(userRoleProject);
        }
        if (CollectionUtils.isNotEmpty(tenantAdmin.getUserIds())) {
            InheritableRequestContextTaskWrapper wrapper = new InheritableRequestContextTaskWrapper();
            threadPoola.submit(() -> {
                wrapper.lambda2(() -> {
                    try {
                        List<User> user1s = userMapper.selectList(new QueryWrapper<User>().lambda().in(User::getId, tenantAdmin.getUserIds()));
                        List<String> emails = user1s.stream().map(User::getEmail).collect(Collectors.toList());
                        adminUtils.batchSendEmail(tenantAdmin.getUserIds(), "VDC成员角色变更", "tenant-member-role-change", "roleName:VDC管理员", emails);
                    } catch (Throwable e) {
                        log.error("Error occurred in async tasks", e);
                    }
                }).accept();
            });
        }
        return R.ok();
    }

    @Override
    public R saveQuotaAdmin(TenantAdmin tenantAdmin) {
        Role role = roleMapper.selectOne(new QueryWrapper<Role>().lambda().eq(Role::getRoleCode, ApiEnum.TENANT_QUOTA_ADMIN.getCode()));
        List<UserVO> users = userMapper.queryUserListByTenantId(tenantAdmin.getTenantId());
        if (CollectionUtils.isNotEmpty(users)) {
            List<Integer> userIds = users.stream().map(User::getId).collect(Collectors.toList());
            userRoleProjectMapper.delete(new QueryWrapper<UserRoleProject>().lambda().in(UserRoleProject::getUserId, userIds).eq(UserRoleProject::getRoleId, role.getId()));
        }

        for (Integer userId : tenantAdmin.getUserIds()) {
            User user = userService.getById(userId);
            if (null == user) {
                return R.failed("用户不存在");
            }
            UserRoleProject userRoleProject = new UserRoleProject();
            userRoleProject.setUserId(userId);
            userRoleProject.setRoleId(role.getId());
            userRoleProject.setProjectId(0);
            userRoleProjectMapper.insert(userRoleProject);
        }

        if (CollectionUtils.isNotEmpty(tenantAdmin.getUserIds())) {
            InheritableRequestContextTaskWrapper wrapper = new InheritableRequestContextTaskWrapper();
            threadPoola.submit(() -> {
                wrapper.lambda2(() -> {
                    try {
                        List<User> user1s = userMapper.selectList(new QueryWrapper<User>().lambda().in(User::getId, tenantAdmin.getUserIds()));
                        List<String> emails = user1s.stream().map(User::getEmail).collect(Collectors.toList());
                        adminUtils.batchSendEmail(tenantAdmin.getUserIds(), "VDC成员角色变更", "tenant-member-role-change", "roleName:VDC配额管理员", emails);
                    } catch (Throwable e) {
                        log.error("Error occurred in async tasks", e);
                    }
                }).accept();
            });
        }
        return R.ok();
    }

    /**
     * 根据租户ID或projectID获取中文名称
     *
     * @param
     * @return
     */
    @Override
    public String translateIdToName(String prefix, Integer id) {
        String name = null;
        name = redisTemplate.opsForValue().get(prefix + id);
        if (name == null) {
            switch (prefix) {
                case CommonConstants.TENANT_PREFIX:
                    Tenant tenant = tenantMapper.selectById(id);
                    name = tenant == null ? "" : tenant.getName();
                    break;
                case CommonConstants.PROJECT_PREFIX:
                    Project project = projectMapper.selectById(id);
                    name = project == null ? "" : project.getName();
                    break;
                case CommonConstants.USER_PREFIX:
                    User user = userService.getById(id);
                    name = user == null ? "" : user.getName();
                    break;
                default:
                    throw new UserCenterException(BusinessEnum.PARAMETER_FAULT);
            }
            redisTemplate.opsForValue().set(prefix + id, name);
        }
        return name;
    }

    /**
     * 根据租户ID和projectID获取中文名称
     *
     * @return
     */
    @Override
    public R<TenantProjectUserVO> translation(TenantProjectUserVO tenantProjectUserVO) {
        Integer projectId = tenantProjectUserVO.getProjectId();
        if (projectId == null || projectId == 0) {
            tenantProjectUserVO = tenantMapper.translationNoProject(tenantProjectUserVO.getTenantId(), tenantProjectUserVO.getUserId());
        } else {
            tenantProjectUserVO = tenantMapper.translation(tenantProjectUserVO.getTenantId(), tenantProjectUserVO.getProjectId(), tenantProjectUserVO.getUserId());
        }
        return R.ok(tenantProjectUserVO);
    }

    @Override
    public R bind(TenantResourcePool tenantResourcePool) {
        Tenant tenant = tenantMapper.selectById(tenantResourcePool.getTenantId());
        if (tenant.getLevel() != 1) {
            return R.failed("只有一级VDC可以绑定资源池");
        }
        if (tenantMapper.bind(tenantResourcePool) > 0) {
            return R.ok();
        } else {
            return R.failed("绑定失败");
        }
    }

    @Override
    public boolean quitBind(TenantResourcePool tenantResourcePool) {
        return tenantMapper.quitBind(tenantResourcePool) > 0;
    }


    @Override
    public IPage<TenantResourcePoolVO> selectBindResourcePools(Page page, Integer tenantId, String resourcePoolCode) {
        //查到的资源池只有id
        return tenantMapper.selectBindList(page, tenantId, resourcePoolCode);
    }

    @Override
    public R addMember(List<UserRoleProject> list) {
        if (list.isEmpty()) {
            return R.failed("参数不能为空");
        }
        Set<Integer> tenantIdSet = new HashSet<>();
        Set<Integer> userIdSet = new HashSet<>();
        Integer tenantId = null;
        for (UserRoleProject userRoleProject : list) {
            tenantId = userRoleProject.getTenantId();
            if (tenantId == null) {
                return R.failed("tenant_id参数不能为空");
            }
            tenantIdSet.add(tenantId);
            Integer userId = userRoleProject.getUserId();
            if (userId == null) {
                return R.failed("user_id参数不能为空");
            }
            userIdSet.add(userId);
        }
        if (tenantIdSet.size() != 1) {
            return R.failed("tenant_id参数错误，应指定同一个tenant");
        }
        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) {
            return R.failed("VDC不存在");
        }
        if (TENANT_STATUS_OFF.equals(tenant.getStatus())) {
            return R.failed("VDC已被禁用");
        }
        if (userIdSet.size() < list.size()) {
            return R.failed("user_id参数有重复");
        }

        List<Member> members = tenantMapper.listMember(tenantId, null);
        if (members.size() > 0) {
            //project中已有成员，如果参数中成员已经在project中

            for (UserRoleProject userRoleProject : list) {
                for (Member member : members) {
                    if (member.getUserId().equals(userRoleProject.getUserId())) {

                        return R.failed("添加失败，用户 id = [" + userRoleProject.getUserId() + "]已经在该tenant中，不能重复添加");
                    }
                }
            }

        }
        Role role = roleMapper.selectOne(new QueryWrapper<Role>().lambda().eq(Role::getLevel, RoleLevelEnum.TENANT.getCode()).eq(Role::getTenantDefaultRole, true));
        if (role == null) {
            return R.failed("没有设置VDC级别的默认角色");
        }

        //新加入的成员默认是普通成员
        for (UserRoleProject userRoleProject : list) {
            userRoleProject.setRoleId(role.getId());
        }
        tenantMapper.addMember(list);
        return R.ok();
    }

    @Override
    public R removeMember(UserRoleProject userRoleProject, String authHeader) {
        if (userRoleProject.getTenantId() == null) {
            return R.failed("tenant id参数不能为空");
        }
        Tenant tenant = tenantMapper.selectById(userRoleProject.getTenantId());
        if (tenant == null) {
            return R.failed("VDC不存在");
        }
        if (userRoleProject.getUserId() == null) {
            return R.failed("用户 id参数不能为空");
        }
        if (TENANT_STATUS_OFF.equals(tenant.getStatus())) {
            return R.failed("VDC已被禁用");
        }
        User user = userService.getById(userRoleProject.getUserId());
        if (user == null) {
            return R.failed("用户不存在");
        }
        if (USER_STATUS_OFF.equals(user.getStatus())) {
            return R.failed("用户已被禁用");
        }
        int count = userRoleProjectMapper.selectCount(new QueryWrapper<UserRoleProject>().lambda().eq(UserRoleProject::getTenantId, userRoleProject.getTenantId()).eq(UserRoleProject::getUserId, userRoleProject.getUserId()));
        if (count < 1) {
            return R.failed("用户不在该VDC中");
        }

        tenantMapper.removeMember(userRoleProject);
        
        FitmgrUser fituser = SecurityUtils.getUser();
        if (StrUtil.isBlank(authHeader) && null != fituser.getId()) {
            return R.failed(BusinessEnum.NOT_LOGIN);
        }
        if(fituser.getDefaultTenantId().equals(userRoleProject.getTenantId())) {
            User user1 = new User();
            user1.setId(fituser.getId());
            user1.setUsername(fituser.getUsername());
            user1.setDefaultTenantId(-1);
            String token = authHeader.replace(OAuth2AccessToken.BEARER_TYPE, StrUtil.EMPTY).trim();
            
            TokenDTO tokenDTO = new TokenDTO();
            tokenDTO.setToken(token);
            tokenDTO.setUser(user1);
            remoteTokenService.updateRdisToken(tokenDTO, SecurityConstants.FROM_IN);
        }
        return R.ok();
    }
    
    @Override
    public IPage allListMember(Page page, List<Integer> tenantIds, String name) {
        IPage<Member> iPage = tenantMapper.allListMemberByName(page, tenantIds, name);
        if (CollectionUtils.isNotEmpty(iPage.getRecords())) {
            List<Tenant> tenants = tenantMapper.selectList(new QueryWrapper<Tenant>().lambda());
            Map<Integer, Tenant> tenantMap = tenants.stream().collect(Collectors.toMap(Tenant::getId, tenant -> tenant));
            List<Role> roleList = roleMapper.selectList(new QueryWrapper<Role>().lambda().eq(Role::getDelFlag, 0).eq(Role::getLevel, 2));
            Map<Integer, Role> roleMap = roleList.stream().collect(Collectors.toMap(Role::getId, role -> role));
            List<Integer> userIds = iPage.getRecords().stream().map(Member::getUserId).collect(Collectors.toList());
            List<UserRoleProject> userRoleProjects = userRoleProjectMapper.selectList(new QueryWrapper<UserRoleProject>().lambda().in(UserRoleProject::getTenantId, tenantIds).in(UserRoleProject::getUserId, userIds));
            Map<Integer, Map<Integer,TenantRoleVO>> userRoleProjectMap = new HashMap<>();
            for (UserRoleProject userRoleProject : userRoleProjects) {
                Map<Integer, TenantRoleVO> map = userRoleProjectMap.get(userRoleProject.getUserId());
                if (map == null) {
                    map = new HashMap<>();
                }
                TenantRoleVO tenantRoleVO = map.get(userRoleProject.getTenantId());
                if(tenantRoleVO==null) {
                    tenantRoleVO = new TenantRoleVO(tenantMap.get(userRoleProject.getTenantId()));
                }
                List<Role> roles = tenantRoleVO.getRoles();
                if(roles==null) {
                    roles = new ArrayList<Role>();
                }
                if (roleMap.get(userRoleProject.getRoleId()) != null) {
                    roles.add(roleMap.get(userRoleProject.getRoleId()));
                }
                tenantRoleVO.setRoles(roles);
                map.put(userRoleProject.getTenantId(), tenantRoleVO);
                userRoleProjectMap.put(userRoleProject.getUserId(), map);
            }
            List<MemberVO> list = new ArrayList<MemberVO>();
            for (Member member : iPage.getRecords()) {
                MemberVO memberVO = new MemberVO(member);
                List<TenantRoleVO> tenantRoles = new ArrayList<TenantRoleVO>();
                Map<Integer,TenantRoleVO> map = userRoleProjectMap.get(member.getUserId());
                for (Entry<Integer, TenantRoleVO> entry : map.entrySet()) {
                    tenantRoles.add(entry.getValue());
                }
                memberVO.setTenantRoles(tenantRoles);
                list.add(memberVO);
            }
            IPage<MemberVO> iPageVO = new Page<>();
            iPageVO.setRecords(list);
            iPageVO.setCurrent(iPage.getCurrent());
            iPageVO.setSize(iPage.getSize());
            iPageVO.setTotal(iPage.getTotal());
            return iPageVO;
        }
        return iPage;
    }

    @Override
    public IPage listMember(Page page, Integer tenantId, String name) {
        IPage<Member> iPage = null;
        if (StringUtils.isEmpty(name)) {
            iPage = tenantMapper.listMemberPage(page, tenantId);
        } else {
            iPage = tenantMapper.listMemberByName(page, tenantId, name);
        }
        if (CollectionUtils.isNotEmpty(iPage.getRecords())) {
            List<Role> roleList = roleMapper.selectList(new QueryWrapper<Role>().lambda().eq(Role::getDelFlag, 0).eq(Role::getLevel, 2));
            Map<Integer, Role> roleMap = roleList.stream().collect(Collectors.toMap(Role::getId, role -> role));
            List<Integer> userIds = iPage.getRecords().stream().map(Member::getUserId).collect(Collectors.toList());
            List<UserRoleProject> userRoleProjects = userRoleProjectMapper.selectList(new QueryWrapper<UserRoleProject>().lambda().eq(UserRoleProject::getTenantId, tenantId).in(UserRoleProject::getUserId, userIds));
            Map<Integer, List<Role>> userRoleProjectMap = new HashMap<>();
            for (UserRoleProject userRoleProject : userRoleProjects) {
                List<Role> roles = userRoleProjectMap.get(userRoleProject.getUserId());
                if (roles == null) {
                    roles = new ArrayList<>();
                    userRoleProjectMap.put(userRoleProject.getUserId(), roles);
                }
                if (roleMap.get(userRoleProject.getRoleId()) != null)
                    roles.add(roleMap.get(userRoleProject.getRoleId()));
            }

            for (Member member : iPage.getRecords()) {
                member.setRoles(userRoleProjectMap.get(member.getUserId()));
            }
        }
        return iPage;
    }

    private Map<Integer, Member> findMemRoleMap(Integer tenantId, String status) {
        List<Member> initMembers = tenantMapper.listMember(tenantId, status);
        Map<Integer, Member> memberRoleMap = new HashMap<>();
        if (initMembers != null) {
            for (Member member : initMembers) {
                Member newMember = memberRoleMap.get(member.getUserId());
                if (newMember == null) {
                    newMember = new Member();
                    newMember.setName(member.getName());
                    newMember.setPhone(member.getPhone());
                    newMember.setUserId(member.getUserId());
                    newMember.setRoleId(member.getRoleId());
                    newMember.setRoleName(member.getRoleName());
                    List<Role> roles = new ArrayList<>();
                    newMember.setRoles(roles);
                    memberRoleMap.put(member.getUserId(), newMember);
                }
                Role role = new Role();
                role.setId(member.getRoleId());
                role.setRoleName(member.getRoleName());
                newMember.getRoles().add(role);
            }
        }
        return memberRoleMap;
    }

    public List<Member> listMember(Integer tenantId, String status) {
        Map<Integer, Member> memberRoleMap = findMemRoleMap(tenantId, status);
        List<Member> members = new ArrayList<>();
        members.addAll(memberRoleMap.values());
        return members;
    }

    public R updateMemberRole(UserRoleProject userRoleProject) {
        if (userRoleProject.getTenantId() == null) {
            return R.failed("tenant_id参数不能为空");
        }
        Tenant tenant = tenantMapper.selectById(userRoleProject.getTenantId());
        if (tenant == null) {
            return R.failed("指定的VDC不存在");
        }
        if (TENANT_STATUS_OFF.equals(tenant.getStatus())) {
            return R.failed("VDC已被禁用");
        }
        if (userRoleProject.getUserId() == null) {
            return R.failed("user_id参数不能为空");
        }
        User user = userMapper.selectById(userRoleProject.getUserId());
        if (user == null) {
            return R.failed("指定的用户不存在");
        }

        if (USER_STATUS_OFF.equals(user.getStatus())) {
            return R.failed("用户已被禁用");
        }

        List<UserRoleProject> one = userRoleProjectMapper.selectList(new QueryWrapper<UserRoleProject>().lambda()
                .eq(UserRoleProject::getTenantId, userRoleProject.getTenantId())
                .eq(UserRoleProject::getUserId, userRoleProject.getUserId()).eq(UserRoleProject::getProjectId, -1));

        if (CollectionUtils.isEmpty(one)) {
            return R.failed("用户未加入该租户");
        }

        userRoleProjectMapper.delete(new QueryWrapper<UserRoleProject>().lambda().eq(UserRoleProject::getTenantId, userRoleProject.getTenantId())
                .eq(UserRoleProject::getUserId, userRoleProject.getUserId()).eq(UserRoleProject::getProjectId, -1));

        for (Integer roleId : userRoleProject.getRoleIds()) {
            UserRoleProject userRoleProjectTemp = new UserRoleProject();
            userRoleProjectTemp.setUserId(userRoleProject.getUserId());
            userRoleProjectTemp.setRoleId(roleId);
            userRoleProjectTemp.setTenantId(userRoleProject.getTenantId());
            userRoleProjectTemp.setProjectId(-1);
            userRoleProjectMapper.insert(userRoleProjectTemp);
        }
        
        InheritableRequestContextTaskWrapper wrapper = new InheritableRequestContextTaskWrapper();
        threadPoola.submit(() -> {
            wrapper.lambda2(() -> {
                try {
                    List<Integer> userIds = new ArrayList<Integer>();
                    List<String> emails = new ArrayList<String>();
                    Role role = roleMapper.selectOne(new QueryWrapper<Role>().lambda().eq(Role::getRoleCode, "tenant_admin"));
                    List<UserVO> userVos = userMapper.queryUserInfoByRoleIdAndTenantId(role.getId(), tenant.getId());
                    if (CollectionUtils.isNotEmpty(userVos)) {
                        userIds.addAll(userVos.stream().map(UserVO::getId).collect(Collectors.toList()));
                        emails.addAll(userVos.stream().map(User::getEmail).collect(Collectors.toList()));
                    }
                    userIds.add(user.getId());
                    emails.add(user.getEmail());
                    List<Role> roles = roleMapper.selectList(new QueryWrapper<Role>().lambda().in(Role::getId, userRoleProject.getRoleIds()));
                    List<String> roleNames = roles.stream().map(Role::getRoleName).collect(Collectors.toList());
                    Map<String, String> parameters = new HashMap<String, String>();
                    parameters.put("userName", user.getName());
                    parameters.put("tenantName", tenant.getName());
                    parameters.put("roleName", JSON.toJSONString(roleNames));
                    adminUtils.batchSendEmail(userIds, "VDC成员角色变更", "tenant-member-role-change", parameters, emails);
                } catch (Throwable e) {
                    log.error("Error occurred in async tasks", e);
                }
            }).accept();
        });
        return R.ok();
    }

    @Override
    public List<TenantTree> queryTenantTree(String createProject, Integer vdcId) {
        FitmgrUser user = SecurityUtils.getUser();
        R<AuthCheck> r = iAuthService.newAuthCheck(ApiEnum.SELECT_TENANT_LIST.getCode(), user.getId(), user.getDefaultTenantId(),null,null,null);
        if (r.getCode() == 0 && r.getData().isStatus()) {
            if (r.getData().getOperatingRange().equals(OperatingRangeEnum.ALL_CODE)) {
                List<Tenant> tenants = null;
                if(StringUtils.isNotEmpty(createProject)) {
                    tenants = this.list(new QueryWrapper<Tenant>().lambda()
                            .eq(Tenant::getDelFlag, "0")
                            .eq(Tenant::getStatus, "0")
                            .eq(Tenant::getCreateProject, createProject));
                } else {
                    tenants = this.list(new QueryWrapper<Tenant>().lambda()
                            .eq(Tenant::getDelFlag, "0")
                            .eq(Tenant::getStatus, "0"));
                }
                List<TenantTree> resultList = new ArrayList<>();
                Map<Integer, TenantTree> tenantVoMap = new HashMap<>();
                for (Tenant tenant : tenants) {
                    TenantTree tenantTree = new TenantTree(tenant);
                    tenantVoMap.put(tenant.getId(), tenantTree);
                    TenantTree pTenantTree = tenantVoMap.get(tenant.getParentId());
                    if (pTenantTree != null) {
                        if (pTenantTree.getChildren() == null) {
                            pTenantTree.setChildren(new ArrayList<>());
                        }
                        pTenantTree.getChildren().add(tenantTree);
                    }
                    if (tenantTree.getParentId() == -1) {

                        resultList.add(tenantTree);
                    }
                }
                if(vdcId!=null && vdcId!=-1) {
                    TenantTree defaultTenantTree = tenantVoMap.get(vdcId);
                    List<TenantTree> finalResultList = new ArrayList<>();
                    finalResultList.add(defaultTenantTree);
                    return finalResultList;
                }
                return resultList;
            } else if (r.getData().getOperatingRange().equals(OperatingRangeEnum.TENANT_CODE)) {
                List<Tenant> tenants = null;
                if(StringUtils.isNotEmpty(createProject)) {
                    tenants = this.list(new QueryWrapper<Tenant>().lambda()
                            .eq(Tenant::getDelFlag, "0")
                            .eq(Tenant::getStatus, "0")
                            .eq(Tenant::getCreateProject, createProject));
                } else {
                    tenants = this.list(new QueryWrapper<Tenant>().lambda()
                            .eq(Tenant::getDelFlag, "0")
                            .eq(Tenant::getStatus, "0"));
                }
                List<TenantTree> resultList = new ArrayList<>();
                Map<Integer, TenantTree> tenantTreeMap = new HashMap<>();
                Integer defaultTenantId = user.getDefaultTenantId();
                for (Tenant tenant : tenants) {
                    if(!r.getData().getTenantIds().contains(tenant.getId())) {
                        continue;
                    }
                    TenantTree tenantTree = new TenantTree(tenant);
                    tenantTreeMap.put(tenant.getId(), tenantTree);
                    if (tenant.getId().equals(defaultTenantId)) {
                        resultList.add(tenantTree);
                    } else if (tenant.getParentId().equals(defaultTenantId)) {
                        TenantTree defaultTenantTree = tenantTreeMap.get(defaultTenantId);
                        if (null == defaultTenantTree) {
                            continue;
                        }
                        if (defaultTenantTree.getChildren() == null) {
                            defaultTenantTree.setChildren(new ArrayList<>());
                        }
                        defaultTenantTree.getChildren().add(tenantTree);
                    } else if (tenantTreeMap.containsKey(tenant.getParentId())) {
                        TenantTree pTenantTree = tenantTreeMap.get(tenant.getParentId());
                        if (pTenantTree.getChildren() == null) {
                            pTenantTree.setChildren(new ArrayList<>());
                        }
                        pTenantTree.getChildren().add(tenantTree);
                    }
                }
                TenantTree defaultTenantTree = tenantTreeMap.get(defaultTenantId);
                if (null == defaultTenantTree || defaultTenantTree.getParentId() == -1) {
                    return resultList;
                }
                List<TenantTree> finalResultList = new ArrayList<>();
                finalResultList.add(defaultTenantTree);
                return finalResultList;
            } else if (r.getData().getOperatingRange().equals(OperatingRangeEnum.PROJECT_CODE)) {
                throw new UserCenterException(BusinessEnum.AUTH_NOT);
            } else if (r.getData().getOperatingRange().equals(OperatingRangeEnum.SELF_CODE)) {
                throw new UserCenterException(BusinessEnum.AUTH_NOT);
            }
            throw new UserCenterException(BusinessEnum.AUTH_CONFIG);
        }
        throw new UserCenterException(BusinessEnum.AUTH_NOT);
    }

    private void getParentTenantTree(TenantTree tenantTree, Map<Integer, TenantTree> tenantTreeMap, List<TenantTree> result) {
        if (tenantTree.getParentId() == -1) {
            result.add(tenantTree);
            return;
        }
        TenantTree pTenantTree = tenantTreeMap.get(tenantTree.getParentId());
        if (pTenantTree != null) {
            if (pTenantTree.getChildren() == null) {
                pTenantTree.setChildren(new ArrayList<>());
            }
            pTenantTree.getChildren().add(tenantTree);
            getParentTenantTree(pTenantTree, tenantTreeMap, result);
        }
    }

    @Override
    public List<TenantTree> queryAllChildTenant(Integer tenantId) {
        List<TenantTree> result = new ArrayList<>();
        List<Tenant> tenants = this.list(new QueryWrapper<Tenant>().lambda().eq(Tenant::getDelFlag, "0"));
        List<TenantTree> resultList = new ArrayList<>();
        Map<Integer, TenantTree> tenantVoMap = new HashMap<>();
        for (Tenant tenant : tenants) {
            TenantTree tenantTree = new TenantTree(tenant);
            tenantVoMap.put(tenant.getId(), tenantTree);
            TenantTree pTenantTree = tenantVoMap.get(tenant.getParentId());
            if (pTenantTree != null) {
                if (pTenantTree.getChildren() == null) {
                    pTenantTree.setChildren(new ArrayList<>());
                }
                pTenantTree.getChildren().add(tenantTree);
            }
            if (tenantTree.getParentId() == -1) {
                resultList.add(tenantTree);
            }
        }
        for (TenantTree tenantTree : resultList) {
            if (tenantId.equals(tenantTree.getId())) {
                recGetAllChild(tenantTree, result);
            } else {
                recFindChild(tenantTree, tenantId, result);
            }
        }
        return result;
    }

    @Override
    public IPage queryUserForAddMember(Page page, Integer tenantId, String queryName) {
        // 将该租户已经配置了的用户过滤，只返回未配置的用户
        LambdaQueryWrapper<UserRoleProject> wrapperQuery = Wrappers.<UserRoleProject>lambdaQuery()
                .eq(true, UserRoleProject::getTenantId, tenantId);
        List<UserRoleProject> userRoleProjects = userRoleProjectMapper.selectList(wrapperQuery);
        Set<Integer> userIds = new HashSet<>();
        if (CollectionUtils.isNotEmpty(userRoleProjects)) {
            userRoleProjects.forEach(userRoleProject -> userIds.add(userRoleProject.getUserId()));
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setUserIds(new ArrayList<>(userIds));
        userDTO.setName(queryName);
        userDTO.setStatus("0");
        return userMapper.selectListByCondition(page, userDTO);
    }

    @Override
    public List<Tenant> tenantLists(TenantDTO tenantDTO) {
        LambdaQueryWrapper<Tenant> queryWrapper =
                Wrappers.<Tenant>lambdaQuery()
                        .in(null != tenantDTO.getTenantIds(), Tenant::getId, tenantDTO.getTenantIds())
                        .eq(StringUtils.isNoneBlank(tenantDTO.getStatus()), Tenant::getStatus, tenantDTO.getStatus())
                        .eq(StringUtils.isNoneBlank(tenantDTO.getCreateProject()), Tenant::getCreateProject, tenantDTO.getCreateProject())
                        .eq(StringUtils.isNoneBlank(tenantDTO.getName()), Tenant::getName, tenantDTO.getName());

        return tenantMapper.selectList(queryWrapper);
    }

    private void recFindChild(TenantTree tenantTree, Integer tenantId, List<TenantTree> result) {
        if (CollectionUtils.isEmpty(tenantTree.getChildren())) {
            return;
        }
        for (TreeNode treeNode : tenantTree.getChildren()) {
            TenantTree tenantTree1 = (TenantTree) treeNode;
            if (tenantId.equals(tenantTree1.getId())) {
                recGetAllChild(tenantTree1, result);
            }else {
                recFindChild(tenantTree1, tenantId, result);
            }
        }
    }

    private void recGetAllChild(TenantTree tenantTree, List<TenantTree> result) {
        if (CollectionUtils.isEmpty(tenantTree.getChildren())) {
            return;
        }
        for (TreeNode treeNode : tenantTree.getChildren()) {
            result.add((TenantTree) treeNode);
            recGetAllChild((TenantTree) treeNode, result);
        }
    }
    
    @Override
    public R<List<LocationTreeNode>> queryPoolResources(Integer id,String type,String parentId,String cloudPlatformType,String networkProvider) {
        List<LocationTreeNode> result = new ArrayList<LocationTreeNode>();

        List<String> list = null;
        if (id != -1) {
            list = new ArrayList<String>();
            TenantLocationTree tenantLocationTree = tenantLocationTreeMapper
                    .selectOne(new QueryWrapper<TenantLocationTree>().lambda().eq(TenantLocationTree::getVdcId, id));

            JSONArray tenantLocationTreeArray = JSONArray.parseArray(tenantLocationTree.getLocationTree());
            if (tenantLocationTreeArray != null) {
                for (Object object : tenantLocationTreeArray) {
                    JSONObject json = (JSONObject) object;
                    list.add(json.getString("uuid"));
                }
            }
        }
        AllLocationTree allLocationTree = allLocationTreeMapper
                .selectList(Wrappers.query(new AllLocationTree()).orderByDesc("id")).get(0);
        List<LocationTreeNode> locationTreeJson = JSONObject.parseArray(allLocationTree.getLocationTree(),
                LocationTreeNode.class);
        List<String> parentIds = new ArrayList<String>();
        if (parentId != null) {
            parentIds.add(parentId);
            if (POOL_RESOURCE_RZ.equals(type)) {
                for (LocationTreeNode locationTreeNode : locationTreeJson) {
                    if (locationTreeNode.getType().equals("AZ") && parentId.equals(locationTreeNode.getParentId())) {
                        parentIds.add(locationTreeNode.getUuid());
                    }
                }
            }
        }
        for (LocationTreeNode locationTreeNode : locationTreeJson) {
            if (locationTreeNode.getState().equals("1") && locationTreeNode.getType().equals(type)) {
                if(list == null || list.contains(locationTreeNode.getUuid())) {
                    if (parentIds.size() > 0 && !parentIds.contains(locationTreeNode.getParentId())) {
                        continue;
                    }
                    if (cloudPlatformType != null && !cloudPlatformType.equals(locationTreeNode.getCloudPlatformType())) {
                        continue;
                    }
                    if (networkProvider != null && !networkProvider.equals(locationTreeNode.getNetworkProvider())) {
                        continue;
                    }
                    result.add(locationTreeNode);
                }
            }
        }
        return R.ok(result);
    }
    
    @Override
    public R<SubDomainConfigSwitch> querySubDomainConfigSwitch(String networkPoolTypeEnum) {
        SubDomainConfigSwitch subDomainConfigSwitch = subDomainConfigSwitchMapper
                .selectList(Wrappers.query(new SubDomainConfigSwitch()).eq("network_pool_type", networkPoolTypeEnum)).get(0);
        return R.ok(subDomainConfigSwitch);
    }
    
    @Override
    public R updateSubDomainConfigSwitch(SubDomainConfigSwitch configSwitch) {
        SubDomainConfigSwitch subDomainConfigSwitch = subDomainConfigSwitchMapper.selectList(Wrappers
                .query(new SubDomainConfigSwitch()).eq("network_pool_type", configSwitch.getNetworkPoolType()))
                .get(0);
        subDomainConfigSwitch.setConfigSwitch(configSwitch.getConfigSwitch());
        subDomainConfigSwitchMapper.updateById(subDomainConfigSwitch);
        return R.ok();
    }
    
    @Override
    public R<NetworkPoolDTO> queryNetworkPool(Integer id,String networkPoolTypeEnum,String resourceZoneId,String networkType,String action) {
        NetworkPoolDTO networkPoolDTO = new NetworkPoolDTO();
        networkPoolDTO.setVdcId(id);
        networkPoolDTO.setNetworkPoolType(networkPoolTypeEnum);
        SubDomainConfigSwitch subDomainConfigSwitch = subDomainConfigSwitchMapper
                .selectList(Wrappers.query(new SubDomainConfigSwitch()).eq("network_pool_type", networkPoolTypeEnum)).get(0);
        networkPoolDTO.setConfigSwitch(subDomainConfigSwitch.getConfigSwitch());

        // 调用resource服务查询全量网络池信息，浮动IP地址池、云主机IP地址池、VLAN池
        Map<String, JSONObject> hxcloudPoolsSpipMap = null;
        Map<String, Object> mapp = new HashMap<String, Object>();
        if (networkPoolTypeEnum.equals(NetworkPoolTypeEnum.SP_IP.getCode().toString())) {
            mapp.put("componentCode", "resourcecenter_networking_sp_ip_pools");
            ResourceOperateDTO resourceOperateDTO = new ResourceOperateDTO();
            resourceOperateDTO.setOperateCode("select_resourcecenter_networking_sp_ip_pools_v1_list");
            resourceOperateDTO.setComponentCode("resourcecenter_networking_sp_ip_pools_v1");
            resourceOperateDTO.setResourceTypeCode("hxcloud");
            if(resourceZoneId!=null) {
                JSONArray operateParams = new JSONArray();
                JSONObject operateParam = new JSONObject();
                operateParam.put("resource_zone_id", resourceZoneId);
                operateParam.put("size", 500);
                operateParams.add(operateParam);
                resourceOperateDTO.setOperateParams(operateParams);
            }
            Object hxcloudPools = remoteCmdbReportService.resourceOperate(resourceOperateDTO).getData();
            if(hxcloudPools!=null) {
                try {
                    JSONArray hxcloudPoolsSpip = JSONObject.parseObject(hxcloudPools.toString()).getJSONArray("records");
                    hxcloudPoolsSpipMap = hxcloudPoolsSpip.stream().collect(Collectors.toMap(item -> ((JSONObject) item).getString("uuid"), item -> (JSONObject)item));
                } catch (Exception e) {
                    Map map = (Map) hxcloudPools;
                    JSONObject json = ((JSONObject)JSONObject.toJSON(map));
                    JSONArray hxcloudPoolsSpip = json.getJSONArray("records");
                    hxcloudPoolsSpipMap = hxcloudPoolsSpip.stream().collect(Collectors.toMap(item -> ((JSONObject) item).getString("uuid"), item -> (JSONObject)item));
                }
            }
        }
        if (networkPoolTypeEnum.equals(NetworkPoolTypeEnum.IP_SUBNET.getCode().toString())) {
            mapp.put("componentCode", "resourcecenter_networking_ip_subnetpools");
        }
        if (networkPoolTypeEnum.equals(NetworkPoolTypeEnum.VLAN.getCode().toString())) {
            mapp.put("componentCode", "resourcecenter_networking_vlan");
        }
        List<Map<String, Object>> spIpList = remoteCmdbService.selectByCondition(mapp, "Y").getData();
        if (id != -1) {
         // 查询VDC网络池
            TenantNetworkPool tenantNetworkPool = tenantNetworkPoolMapper
                    .selectOne(new QueryWrapper<TenantNetworkPool>().lambda().eq(TenantNetworkPool::getVdcId, id)
                            .eq(TenantNetworkPool::getNetworkPoolType, networkPoolTypeEnum));
            if (subDomainConfigSwitch.getConfigSwitch().equals(NetworkPoolEnum.CONFIG_SWITCH_ON.getCode())) {
                // 分域配置打开
                String version = null;
             // 查询全量网络池
                List<NetworkPool> networkPools = networkPoolMapper.selectList(Wrappers.query(new NetworkPool())
                        .eq("network_pool_type", networkPoolTypeEnum).orderByDesc("id"));
                if (networkPools != null && networkPools.size() > 0) {
                    version = networkPools.get(0).getVersion();
                }
                if (version != null) {
                    if (version.equals(tenantNetworkPool.getVersion())) {
                        // 版本号一致，返回VDC网络池
                        List<NetworkPoolInfo> networkPoolInfos = JSONObject
                                .parseArray(tenantNetworkPool.getNetworkPoolInfo(), NetworkPoolInfo.class);
                        networkPoolDTO.setNetworkPoolInfos(JSON.toJSONString(networkPoolInfos));
                    } else {
                        // 版本号不一致，重构VDC网络池
                        List<String> uuids = new ArrayList<String>();
                     // 查询可用RZ
                        List<LocationTreeNode> locationTreeNodes = queryPoolResources(id, "RZ", null, null, null)
                                .getData();
                        if (locationTreeNodes != null && locationTreeNodes.size() > 0) {
                            for (LocationTreeNode locationTreeNode : locationTreeNodes) {
                                uuids.add(locationTreeNode.getUuid());
                            }
                        }
                        // 根据RZ和历史选择网络池
                        List<String> oldSpIpIds = new ArrayList<String>();
                        List<NetworkPoolInfo> oldTenantNetworkPoolInfos = JSONObject
                                .parseArray(tenantNetworkPool.getNetworkPoolInfo(), NetworkPoolInfo.class);
                        for (NetworkPoolInfo tenantNetworkPoolInfo : oldTenantNetworkPoolInfos) {
                            if (tenantNetworkPoolInfo.getChoice().equals(NetworkPoolEnum.CHOICE_OFF.getCode())) {
                                oldSpIpIds.add(tenantNetworkPoolInfo.getUuid());
                            }
                        }
                        List<NetworkPoolInfo> tenantNetworkPoolInfos = new ArrayList<NetworkPoolInfo>();
                        for (int i = 0; i < spIpList.size(); i++) {
                            Map<String, Object> map = spIpList.get(i);
                            if (uuids.contains(map.get("resource_zone_id").toString())) {
                                NetworkPoolInfo networkPoolInfo = new NetworkPoolInfo(map.get("uuid").toString(),
                                        oldSpIpIds.contains(map.get("uuid").toString())
                                                ? NetworkPoolEnum.CHOICE_OFF.getCode()
                                                : NetworkPoolEnum.CHOICE_ON.getCode(),
                                        map.get("resource_zone_id").toString());
                                tenantNetworkPoolInfos.add(networkPoolInfo);
                            }
                        }
                        networkPoolDTO.setNetworkPoolInfos(JSON.toJSONString(tenantNetworkPoolInfos));
                    }
                }
            } else {
                // 分域配置关闭
                if (action != null && action.equals(NetworkPoolEnum.ACTION_CONFIG.getCode())) {
                    // 查看分域配置
                    List<NetworkPoolInfo> networkPoolInfos = JSONObject
                            .parseArray(tenantNetworkPool.getNetworkPoolInfo(), NetworkPoolInfo.class);
                    networkPoolDTO.setNetworkPoolInfos(JSON.toJSONString(networkPoolInfos));
                } else {
                    // 使用分域
                    List<String> uuids = new ArrayList<String>();
                 // 查询可用RZ
                    List<LocationTreeNode> locationTreeNodes = queryPoolResources(id, "RZ", null, null, null).getData();
                    if (locationTreeNodes != null && locationTreeNodes.size() > 0) {
                        for (LocationTreeNode locationTreeNode : locationTreeNodes) {
                            uuids.add(locationTreeNode.getUuid());
                        }
                    }
                 // 查询全量网络池
                    List<NetworkPool> networkPools = networkPoolMapper.selectList(Wrappers.query(new NetworkPool())
                            .eq("network_pool_type", networkPoolTypeEnum).orderByDesc("id"));
                    if (networkPools != null && networkPools.size() > 0) {
                        List<NetworkPoolInfo> networkPoolInfos1 = new ArrayList<NetworkPoolInfo>();
                        List<NetworkPoolInfo> networkPoolInfos = JSONObject
                                .parseArray(networkPools.get(0).getNetworkPoolInfo(), NetworkPoolInfo.class);
                        for (NetworkPoolInfo networkPoolInfo : networkPoolInfos) {
                            // 根据可用RZ过滤可用网络池
                            if (uuids.contains(networkPoolInfo.getResourceZoneId())) {
                                networkPoolInfos1.add(networkPoolInfo);
                            }
                        }
                        networkPoolDTO.setNetworkPoolInfos(JSON.toJSONString(networkPoolInfos1));
                    }
                }
            }
         // 可用网络池
            List<NetworkPoolInfo> networkPoolInfos = JSONObject.parseArray(networkPoolDTO.getNetworkPoolInfos(),
                    NetworkPoolInfo.class);
            Map<String,NetworkPoolInfo> networkPoolInfoMap = networkPoolInfos.stream().collect(Collectors.toMap(NetworkPoolInfo::getUuid,networkPoolInfo->networkPoolInfo));
            List<Map<String, Object>> spIpList1 = new ArrayList<Map<String, Object>>();
            for (Map<String, Object> spIp : spIpList) {
                if (networkType != null && !networkType.equals(spIp.get("network_type"))) {
                    continue;
                }
                if (resourceZoneId != null && !resourceZoneId.equals(spIp.get("resource_zone_id"))) {
                    continue;
                }
                if(networkPoolInfoMap.containsKey(spIp.get("uuid").toString())) {
                    // 根据可用网络池返回底层全量参数网络池
                    if(hxcloudPoolsSpipMap!=null) {
                        spIp.put("ip_pools", hxcloudPoolsSpipMap.get(spIp.get("uuid")).getJSONArray("ip_pools"));
                    }
                    spIp.put("choice", networkPoolInfoMap.get(spIp.get("uuid").toString()).getChoice());
                    spIpList1.add(spIp);
                }
            }
            networkPoolDTO.setNetworkPoolDetailInfos(spIpList1);
            return R.ok(networkPoolDTO);
        } else {
            // 查询全量网络池
            List<Map<String, Object>> spIpList1 = new ArrayList<Map<String, Object>>();
            for (Map<String, Object> spIp : spIpList) {
                if (networkType != null && !networkType.equals(spIp.get("network_type"))) {
                    continue;
                }
                if (resourceZoneId != null && !resourceZoneId.equals(spIp.get("resource_zone_id"))) {
                    continue;
                }
                if(hxcloudPoolsSpipMap!=null) {
                    spIp.put("ip_pools", hxcloudPoolsSpipMap.get(spIp.get("uuid")).getJSONArray("ip_pools"));
                }
                spIp.put("choice", NetworkPoolEnum.CHOICE_ON.getCode());
                spIpList1.add(spIp);
            }
            networkPoolDTO.setNetworkPoolDetailInfos(spIpList1);
            return R.ok(networkPoolDTO);
        }
    }
    
    @Override
    public R updateNetworkPool(NetworkPoolDTO networkPoolDTO) {
        if(networkPoolDTO.getConfigSwitch().equals(NetworkPoolEnum.CONFIG_SWITCH_ON.getCode())) {
            //分域开关打开时才更新分域
            List<Integer> tenantIds = new ArrayList<>();
            tenantIds.add(networkPoolDTO.getVdcId());
            if (networkPoolDTO.getSyncSubset()) {
                List<TenantTree> childTenants = queryAllChildTenant(networkPoolDTO.getVdcId());
                if (CollectionUtils.isNotEmpty(childTenants)) {
                    tenantIds.addAll(childTenants.stream().map(TenantTree::getId).collect(Collectors.toList()));
                }
            }
            List<TenantNetworkPool> tenantNetworkPools = tenantNetworkPoolMapper
                    .selectList(new QueryWrapper<TenantNetworkPool>().lambda().in(TenantNetworkPool::getVdcId, tenantIds)
                            .eq(TenantNetworkPool::getNetworkPoolType, networkPoolDTO.getNetworkPoolType()));
            if (!CollectionUtils.isEmpty(tenantNetworkPools)) {
                for (TenantNetworkPool tenantNetworkPool : tenantNetworkPools) {
                    tenantNetworkPool.setNetworkPoolInfo(networkPoolDTO.getNetworkPoolInfos());
                    tenantNetworkPoolMapper.updateById(tenantNetworkPool);
                }
            }
        }
        return R.ok();
    }
    
    @Override
    public R importTenant(ImportTenantVo importTenantVo) {
        try {
            long startTime = DateUtils.getCurrentTime();
            importTenantVo.setStartTime(new Date(startTime));
            
            String bucket = importTenantVo.getBucket();
            String fileName = importTenantVo.getFileName();
            
            List<Map<String, String>> data = getExcel(bucket, fileName);
            if (CollectionUtils.isEmpty(data)) {
                log.error("录入数据为空，importTenantVo={}", importTenantVo);
                return R.failed("录入VDC为空");
            }
            if(data.size() > 10000) {
                log.error("单次录入数量不能超过10000", importTenantVo);
                return R.failed("单次录入数量不能超过10000");
            }
            
            executor.execute(() -> {
                int i = 0;
                int total = data.size();
                int success = 0;
                int fail = 0;
                
                importTenantVo.setTotal(total);
                
                
                String fileKey = bucket + "_" + fileName;
                
                String progressKey = CommonConstants.import_vdc_progress + fileKey;
                String failKey = CommonConstants.import_vdc_fail + fileKey;
                
                // 以百分之一为一个进度跳动，计算每个进度最小处理数量
                int per = Math.max(total / 100, 1);
                // 失败VDC集合及失败原因
                Map<String, String> failUser = new HashMap<String, String>();
                
                for (Map<String, String> map : data) {
                    i++;
                    String name = MapUtils.getString(map, "name");
                    try {
                        if (i % 100 == 0) {
                            log.info("---------- ActiveCount:{}, PoolSize:{}, CorePoolSize:{}, MaxPoolSize:{}", 
                                    executor.getActiveCount(), executor.getPoolSize(), executor.getCorePoolSize(), executor.getMaxPoolSize());
                        }
                        
                        // 检验
                        check(map);
                        
                        int count = tenantMapper.selectCount(new QueryWrapper<Tenant>().eq("name", name));
                        if (count > 0) {
                            log.error("---- VDC名称已存在");
                            putFailUser(failUser, name, "VDC名称已存在");
                            fail++; 
                            continue;
                        }
                        TenantDTO tenantDTO = new TenantDTO();
                        String parent = MapUtils.getString(map, "parent");
                        if (StringUtils.isNotBlank(parent)) {
                            Tenant parentTenant = tenantMapper.selectOne(new QueryWrapper<Tenant>().eq("name", parent));
                            if(parentTenant!=null) {
                                tenantDTO.setParentId(parentTenant.getId());
                            }else {
                                log.error("---- 父级VDC不存在");
                                putFailUser(failUser, name, "父级VDC不存在");
                                fail++; 
                                continue;
                            }
                        }
                        tenantDTO.setName(MapUtils.getString(map, "name"));
                        tenantDTO.setEnglishName(MapUtils.getString(map, "englishName"));
                        tenantDTO.setCreateProject(MapUtils.getString(map, "createProject"));
                        tenantDTO.setDescription(MapUtils.getString(map, "description"));
                        
                        R r = saveTenant(tenantDTO);
                        if (r.getCode()==CommonConstants.SUCCESS) {
                           success++; 
                        } else {
                            log.error("---- {}录入失败", name);
                            putFailUser(failUser, name, r.getMsg()==null?"录入失败":r.getMsg());
                            fail++;
                        }
                    } catch (Exception e) {
                        fail++; 
                        log.error("---- {}录入异常, error={}", name, e);
                        putFailUser(failUser, name, e.getMessage());
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
                // 录入失败的VDC记录到缓存中
                if (!failUser.isEmpty()) {
                    recordFailUser(failKey, failUser);
                }
                long endTime = DateUtils.getCurrentTime();
                log.info(">>>>>>>>>>> 本次录入完成：bucket:{}, fileName:{}, total={}, success:{}, fail:{}, 耗時:{}", bucket, fileName, total, success, fail, DateUtils.formatDuring(startTime - endTime));
                
                importTenantVo.setEndTime(new Date(endTime));
                importTenantVo.setProgress((int)(i * 100 / total));
                importTenantVo.setStatus(1);
                importTenantVo.setSuccess(success);
                importTenantVo.setFail(fail);
                importTenantVo.setFailLink("/admin/tenant/downloadFail?bucket=" + bucket + "&fileName=" + fileName);
                recordImportLog(importTenantVo);
            });
            
            return R.ok();
        } catch (Exception e) {
            log.error("VDC录入失败", e);
            importTenantVo.setStatus(2);
            recordImportLog(importTenantVo);
            return R.failed("VDC录入异常，请稍后重试");
        }
    }
    
    private List<Map<String, String>> getExcel(String bucket, String fileName) throws Exception {
        log.info("开始解析Excel，bucket={}, fileName={}", bucket, fileName);
        //  1.通过流读取Excel文件
        Workbook workbook = WorkbookFactory.create(minioTemplate.getObject(bucket, fileName));
        //  3.从文件中获取表对象  getSheetAt通过下标获取
        Sheet sheet = workbook.getSheetAt(0);
        //  4.从表中获取到行数据  从第二行开始 到 最后一行  getLastRowNum() 获取最后一行的下标
        int lastRowNum = sheet.getLastRowNum();
        
        
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        for (int i = 1; i <= lastRowNum; i++) {
            Map<String, String> map = new HashMap<>();
            
            // 通过下标获取行
            Row row = sheet.getRow(i);
            // 如果读取不到VDC名，则跳过
            if (row.getCell(0) == null || StringUtils.isBlank(getCellFormatValue(row.getCell(0)))) {
                log.warn("第{}行检测到VDC名为空，读取结束", i);
                return list;
            }
            
            map.put("name", getCellFormatValue(row.getCell(0)));
            map.put("englishName", getCellFormatValue(row.getCell(1)));
            map.put("parent", getCellFormatValue(row.getCell(2)));
            map.put("createProject", getCellFormatValue(row.getCell(3)));
            map.put("description", getCellFormatValue(row.getCell(4)));
            list.add(map);
            
            // 限制超过10000条不允许录入
            if (list.size() > 10000) {
                return list;
            }
        }
        log.info("Excel解析结束，listSize={}", list.size());
        return list;
    }
    
    @SuppressWarnings("deprecation")
    private String getCellFormatValue(Cell cell){
        if (cell == null) {
            return null;
        }
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue();
    }
    
    private void check(Map<String, String> dataMap) {
        String name = MapUtils.getString(dataMap, "name");
        String englishName = MapUtils.getString(dataMap, "englishName");
        String createProject = MapUtils.getString(dataMap, "createProject");
        String description = MapUtils.getString(dataMap, "description");
        if (!matches("^[a-zA-Z0-9\\u4e00-\\u9fa5][-_a-zA-Z0-9\\u4e00-\\u9fa5]{2,63}$", name)) {
            throw new UserCenterException("名称格式错误:允许中文、大小写英文字母、数字、下划线、中划线（3-64位），且首位必须为英文字母、数字或中文");
        }
        if (!matches("^[a-zA-Z0-9][a-zA-Z0-9_-]{2,63}$", englishName)) {
            throw new UserCenterException("英文名称格式错误:允许大小写英文字母、数字、下划线、中划线（3-64位），且首位必须为英文字母或数字");
        }
        if (!matches("[01]", createProject)) {
            throw new UserCenterException("参数[createProject]错误,取值字符0或字符1");
        }
        if (description!=null && !matches("^.{0,255}$", description)) {
            throw new UserCenterException("描述信息最长为255个字符");
        }
    }
    
    private boolean matches(String pattern,  String content) {
        try {
            return Pattern.matches(pattern, content);
        } catch (Exception e) {
            return false;
        }
    }
    
    private void putFailUser(Map<String, String> map, String name, String errorInfo) {
        if (map.containsKey(name)) {
            String value = map.getOrDefault(name, "");
            // 增加1000字符限制，防止excel单元格超长
            if (value.length() < 1000) {
                map.put(name, value + " || " + errorInfo);
            }
        } else {
            map.put(name, errorInfo);
        }
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
    
    private void recordImportLog(ImportTenantVo importTenantVo) {
        redisTemplate.opsForList().leftPush(CommonConstants.import_vdc_logs, JSONObject.toJSONString(importTenantVo));
        redisTemplate.expire(CommonConstants.import_vdc_logs, 1, TimeUnit.DAYS);
        
        List<String> list = redisTemplate.opsForList().range(CommonConstants.import_vdc_logs, 0, -1);
        // 只保留最近10条记录
        if (CollectionUtils.isNotEmpty(list) && list.size() > 10) {
            redisTemplate.opsForList().trim(CommonConstants.import_vdc_logs, 0, 9);
        }
    }
    
    private List<String> queryImportLog() {
        return redisTemplate.opsForList().range(CommonConstants.import_vdc_logs, 0, -1);
    }

    @Override
    public void downloadFail(HttpServletResponse response, String bucket, String fileName) throws Exception {
        Map<Object, Object> failUserMap = queryFailUser(CommonConstants.import_vdc_fail + bucket + "_" + fileName);
        List<String> titles = new ArrayList<String>();
        titles.add("VDC名称");
        titles.add("失败原因");

        ExcelData data = new ExcelData();
        List<List<Object>> rows = new ArrayList<List<Object>>();

        for (Entry<Object, Object> entry : failUserMap.entrySet()) {
            List<Object> list = new ArrayList<Object>();
            list.add(entry.getKey());
            list.add(entry.getValue());
            rows.add(list);
        }

        data.setTitles(titles);
        data.setName("录入失败VDC");
        data.setRows(rows);
        
        ExcelUtil.exportExcel(response, "录入失败VDC.xls", data);
    }

    @Override
    public ImportUserVo queryProgress(String bucket, String fileName) {
        ImportUserVo importuser = new ImportUserVo();
        importuser.setBucket(bucket);
        importuser.setFileName(fileName);
        
        String progressKey = CommonConstants.import_vdc_progress + bucket + "_" + fileName;
        Map<Object, Object>  progressMap = queryProgress(progressKey);
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
            importuser.setFailLink("/admin/tenant/downloadFail?bucket=" + bucket + "&fileName=" + fileName);
        }
        
        return importuser;
    }

    @Override
    public List<ImportTenantVo> queryLogs() {
        List<String> logs = queryImportLog();
        if (CollectionUtils.isEmpty(logs)) {
            return new ArrayList<>();
        }
        List<ImportTenantVo> improtLogs = logs.stream().limit(Math.min(logs.size(), 10)).map(log -> {
            return JSONObject.toJavaObject((JSONObject)JSONObject.parse(log), ImportTenantVo.class);
        }).collect(Collectors.toList());
        
        return improtLogs;
    }
}
