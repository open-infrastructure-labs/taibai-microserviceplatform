package com.fitmgr.admin.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitmgr.admin.api.dto.NetworkPoolDTO;
import com.fitmgr.admin.api.dto.TenantDTO;
import com.fitmgr.admin.api.dto.TenantTree;
import com.fitmgr.admin.api.entity.AllLocationTree;
import com.fitmgr.admin.api.entity.AuthCheck;
import com.fitmgr.admin.api.entity.LocationTreeNode;
import com.fitmgr.admin.api.entity.SubDomainConfigSwitch;
import com.fitmgr.admin.api.entity.Tenant;
import com.fitmgr.admin.api.entity.TenantAdmin;
import com.fitmgr.admin.api.entity.TenantLocationTree;
import com.fitmgr.admin.api.entity.TenantResourcePool;
import com.fitmgr.admin.api.entity.UserRoleProject;
import com.fitmgr.admin.api.validation.Save;
import com.fitmgr.admin.api.validation.Update;
import com.fitmgr.admin.api.vo.ImportTenantVo;
import com.fitmgr.admin.api.vo.ImportUserVo;
import com.fitmgr.admin.api.vo.Member;
import com.fitmgr.admin.api.vo.ProjectVO;
import com.fitmgr.admin.api.vo.TenantProjectUserVO;
import com.fitmgr.admin.api.vo.TenantResourcePoolVO;
import com.fitmgr.admin.api.vo.TenantVO;
import com.fitmgr.admin.api.vo.UserVO;
import com.fitmgr.admin.mapper.TenantMapper;
import com.fitmgr.admin.service.IAllLocationTreeService;
import com.fitmgr.admin.service.IAuthService;
import com.fitmgr.admin.service.IProjectService;
import com.fitmgr.admin.service.ITenantLocationTreeService;
import com.fitmgr.admin.service.ITenantService;
import com.fitmgr.admin.service.ITenantTypeService;
import com.fitmgr.admin.syncproject.LockManager;
import com.fitmgr.admin.task.SyncAllLocationTreeTask;
import com.fitmgr.common.core.constant.enums.BusinessEnum;
import com.fitmgr.common.core.constant.enums.DeleteFlagStatusEnum;
import com.fitmgr.common.core.util.R;
import com.fitmgr.common.core.util.TreeUtil;
import com.fitmgr.common.log.annotation.SysLog;
import com.fitmgr.common.security.service.FitmgrUser;
import com.fitmgr.common.security.util.SecurityUtils;
import com.fitmgr.job.api.entity.Task;
import com.fitmgr.job.api.sdk.FhJobApiController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 创建人 mhp 创建时间 2019/11/12 描述
 **/

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/tenant")
@Api(value = "tenant", tags = "VDC管理模块")
public class TenantController {

    private final ITenantService tenantService;
    private final TenantMapper tenantMapper;
    private final ITenantTypeService tenantTypeService;
    private final IProjectService projectService;
    private final IAllLocationTreeService allLocationTreeService;
    private final ITenantLocationTreeService tenantLocationTreeService;
    private final IAuthService iAuthService;

    private static final String LOCK_KEY = "create_tenant";

    /**
     * 添加租户
     *
     * @param tenantDTO 租户信息
     * @return R
     */
    @SysLog(value = "添加VDC", cloudResType = "VDC", resNameArgIndex = 0, resNameLocation = "arg.name")
    @PostMapping
    @ApiOperation(value = "添加VDC")
    @ApiImplicitParams(@ApiImplicitParam(name = "tenantDTO", value = "新增VDC对象", dataType = "TenantDTO", paramType = "body", required = true))
    public R createTenant(@Validated(Save.class) @RequestBody TenantDTO tenantDTO) {
        boolean isLock = LockManager.tryLock(LOCK_KEY, 1, TimeUnit.MINUTES.toSeconds(10));
        if (!isLock) {
            return R.failed("正在新建VDC中，请稍后再试");
        }
        try {
            if (tenantDTO.getParentId() != null && !tenantDTO.getParentId().equals(-1)) {
                FitmgrUser user = SecurityUtils.getUser();
                R<AuthCheck> r = iAuthService.newAuthCheck("create_tenant", user.getId(), user.getDefaultTenantId(),
                        null, null, null);
                if (r.getCode() == 0 && r.getData().isStatus()) {
                    if (r.getData().getOperatingRange().equals("0")) { // 全局
                        return saveTenant(tenantDTO);
                    } else if (r.getData().getOperatingRange().equals("1")) {// 租户级别
                        if (!r.getData().getTenantIds().contains(tenantDTO.getParentId())) {
                            return R.failed(BusinessEnum.AUTH_NOT);
                        }
                        return saveTenant(tenantDTO);
                    }
                    return R.failed(BusinessEnum.AUTH_CONFIG);
                }
                return R.failed(BusinessEnum.AUTH_NOT);
            }
            return saveTenant(tenantDTO);
        } finally {
            LockManager.unlock(LOCK_KEY);
        }
    }

    public R saveTenant(TenantDTO tenantDTO) {
        tenantDTO.setName(tenantDTO.getName().trim());
        int count = tenantService.count(new QueryWrapper<Tenant>().eq("name", tenantDTO.getName()));
        if (count > 0) {
            return R.failed("VDC名称已存在");
        }
        R r = tenantService.saveTenant(tenantDTO);
        return r;
    }

    /**
     * 根据租户id删除租户
     *
     * @param tenantId 租户id
     * @return R
     */
    @SysLog(value = "删除VDC", cloudResType = "VDC", resIdArgIndex = 0, resIdLocation = "arg")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除租户")
    @ApiImplicitParams(@ApiImplicitParam(name = "id", value = "租户id", dataType = "Integer", paramType = "path", required = true))
    public R deleteTenant(@PathVariable("id") Integer tenantId) {
        boolean b = tenantService.deleteTenant(tenantId);
        return b ? R.ok() : R.failed();
    }

    /**
     * 更新租户信息
     *
     * @param tenantDTO 租户信息
     * @return R
     */
    @SysLog(value = "更新VDC信息", cloudResType = "VDC", resNameArgIndex = 0, resNameLocation = "arg.name", resIdArgIndex = 0, resIdLocation = "arg.id")
    @PutMapping
    @ApiOperation(value = "更新租户信息")
    @ApiImplicitParams(@ApiImplicitParam(name = "tenantDTO", value = "租户对象", dataType = "TenantDTO", paramType = "body", required = true))
    public R updateTenant(@Validated(Update.class) @RequestBody TenantDTO tenantDTO) {
        tenantDTO.setName(tenantDTO.getName().trim());
        Tenant tenant = tenantService.getOne(new QueryWrapper<Tenant>().eq("name", tenantDTO.getName()));
        if (tenant != null && !tenant.getId().equals(tenantDTO.getId())) {
            return R.failed("VDC名称已存在");
        }

        boolean b = tenantService.updateTenant(tenantDTO);
        return b ? R.ok() : R.failed();
    }

    /**
     * 根据租户id查询租户详情
     *
     * @param id 租户id
     * @return R
     */
    @GetMapping("/detail/{id}")
    @ApiOperation(value = "根据租户id查询租户详情")
    @ApiImplicitParams(@ApiImplicitParam(name = "id", value = "租户id", dataType = "Integer", paramType = "path", required = true))
    public R tenantInfo(@PathVariable(value = "id") Integer id) {
        int count = tenantService.count(new QueryWrapper<Tenant>().lambda().eq(Tenant::getId, id));
        if (count < 1) {
            return R.failed("VDC[id = " + id + " ]不存在");
        }
        TenantVO tenantVo = tenantService.selectTenantVoById(id);
        return R.ok(tenantVo);
    }

    /**
     * 根据条件分页查询租户列表
     *
     * @param page      分页条件
     * @param tenantDTO 筛选条件
     * @return 租户列表
     */
    @GetMapping("/list/page")
    @ApiOperation(value = "条件分页查询租户列表")
    @ApiImplicitParams({ @ApiImplicitParam(name = "page", value = "分页条件对象", dataType = "Page", paramType = "query"),
            @ApiImplicitParam(name = "tenantDTO", value = "筛选条件对象", dataType = "TenantDTO", paramType = "query") })
    public R selectByCondition(Page page, TenantDTO tenantDTO) {
        // 获取当前用户的默认角色id
        FitmgrUser user = SecurityUtils.getUser();
        if (null != user) {
            IPage<TenantVO> iPage = tenantService.tenantList(page, tenantDTO, user.getId());
            return R.ok(iPage);
        }
        return R.failed(BusinessEnum.NOT_LOGIN);

    }

    /**
     * 返回树形租户列表
     *
     * @return R
     */
    @GetMapping("/list/tree")
    @ApiOperation(value = "获取所有租户树形列表")
    public R<List<TenantTree>> listTree() {
        List<Tenant> tenants = tenantService.list(new QueryWrapper<Tenant>().lambda().eq(Tenant::getDelFlag, "0"));
        List<TenantTree> tenantTreeList = new ArrayList<>();
        for (Tenant tenant : tenants) {
            TenantTree tenantTree = new TenantTree(tenant);
            tenantTreeList.add(tenantTree);
        }
        return R.ok(TreeUtil.buildByRecursive(tenantTreeList, -1));
    }

    /**
     * 返回树形当前租户列表
     *
     * @return R
     */
    @GetMapping("/list/tree/defaultTenant")
    @ApiOperation(value = "获取当前租户树形列表")
    public R listTreeByDefaultTenant() {
        FitmgrUser user = SecurityUtils.getUser();
        TenantVO tenantVo = tenantService.selectTenantVoById(user.getDefaultTenantId());
        List<Tenant> tenants = tenantService.list(new QueryWrapper<Tenant>().lambda().eq(Tenant::getDelFlag, "0"));
        List<TenantTree> tenantTreeList = new ArrayList<>();
        for (Tenant tenant : tenants) {
            if (tenant.getLevel() > tenantVo.getLevel() || tenant.getId().equals(tenantVo.getId())) {
                TenantTree tenantTree = new TenantTree(tenant);
                tenantTreeList.add(tenantTree);
            }
        }
        return R.ok(TreeUtil.buildByRecursive(tenantTreeList, -1));
    }

    /**
     * 获取租户列表（无参查询）
     *
     * @return 租户列表
     */
    @GetMapping("/list/no-param")
    @ApiOperation(value = "条件获取租户列表")
    public R getTenantList() {
        List<Tenant> list = tenantMapper.selectList(Wrappers.emptyWrapper());
        return R.ok(list);
    }

    /**
     * 条件查询租户列表
     *
     * @return 租户列表
     */
    @GetMapping("/list")
    @ApiOperation(value = "条件获取租户列表")
    @ApiImplicitParams(@ApiImplicitParam(name = "tenantDTO", value = "筛选条件对象", dataType = "TenantDTO", paramType = "query"))
    public R getList(TenantDTO tenantDTO) {
        List<Tenant> list = tenantService.tenantList(tenantDTO);
        return R.ok(list);
    }

    /**
     * 条件查询租户列表2
     *
     * @return 租户列表2
     */
    @PostMapping("/list/info")
    @ApiOperation(value = "条件查询租户列表2", notes = "条件查询租户列表2")
    @ApiImplicitParams(value = @ApiImplicitParam(paramType = "body", name = "tenantDTO", dataType = "TenantDTO", required = true, value = "tenantDTO"))
    public R getListInfo(@RequestBody TenantDTO tenantDTO) {
        List<Tenant> list = tenantService.tenantList(tenantDTO);
        return R.ok(list);
    }

    /**
     * 根据当前用户角色返回当前用户有权限能看到的所有的租户列表
     *
     * @return 当前用户能看到的租户列表
     */
    @GetMapping("/conrole/list/tree")
    @ApiOperation(value = "根据当前用户角色返回当前用户有权限能看到的所有的租户列表", notes = "根据当前用户角色返回当前用户有权限能看到的所有的租户列表")
    public R getTreeListByConRole(@RequestParam(value = "createProject", required = false) String createProject,
            @RequestParam(value = "vdcId", required = false) Integer vdcId) {
        List<TenantTree> tenantTrees = tenantService.queryTenantTree(createProject, vdcId);
        return R.ok(tenantTrees);
    }

    /**
     * 根据用户id查询租户
     *
     * @param userId 用户id
     * @return 租户信息
     */
    @GetMapping("/detail/user/{id}")
    @ApiOperation(value = "根据用户id查询租户详情")
    @ApiImplicitParams(@ApiImplicitParam(name = "id", value = "用户id", dataType = "Integer", paramType = "path", required = true))
    public R selectByUserId(@PathVariable("id") Integer userId) {
        TenantVO tenantVO = tenantService.selectByUserId(userId);
        return R.ok(tenantVO);
    }

    /**
     * 修改租户启用禁用状态
     *
     * @param tenant 租户对象
     * @return R
     */
    @SysLog(value = "修改VDC启用禁用状态", cloudResType = "VDC", resIdArgIndex = 0, resIdLocation = "arg.id")
    @ApiOperation(value = "修改VDC启用禁用状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tenant", value = "租户对象", dataType = "Tenant", paramType = "body", required = true), })
    @PutMapping("/status")
    public R updateStatus(@RequestBody Tenant tenant) {
        if (tenant.getId() == null) {
            return R.failed("未指定VDC");
        }
        int count = tenantService.count(new QueryWrapper<Tenant>().lambda().eq(Tenant::getId, tenant.getId()));
        if (count < 1) {
            return R.failed("VDC不存在");
        }
        if (null == tenant.getStatus()) {
            return R.failed("未指定将要修改的状态参数");
        }
        boolean b = tenant.getStatus().matches("[01]");
        if (!b) {
            return R.failed("参数[status]取值范围:字符0或字符1");
        }
        int i = tenantService.updateStatus(tenant);
        return i == 1 ? R.ok() : R.failed("修改状态失败");
    }

    /**
     * 指定租户管理员
     *
     * @param tenantAdmin 用户id
     * @return
     */
    @SysLog(value = "指定VDC管理员", cloudResType = "VDC", resIdArgIndex = 0, resIdLocation = "arg.tenantId")
    @ApiOperation(value = "指定VDC管理员")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tenantAdmin", value = "tenantAdmin", dataType = "TenantAdmin", paramType = "body", required = true), })
    @PostMapping("/save_admin")
    public R saveAdmin(@RequestBody TenantAdmin tenantAdmin) {
        if (CollectionUtils.isEmpty(tenantAdmin.getUserIds())) {
            return R.failed("参数用户id不能为空");
        }
        return tenantService.saveAdmin(tenantAdmin);
    }

    /**
     * 指定租户配额管理员
     *
     * @param tenantAdmin 用户id
     * @return
     */
    @SysLog(value = "指定VDC配额管理员", cloudResType = "VDC", resIdArgIndex = 0, resIdLocation = "arg.tenantId")
    @ApiOperation(value = "指定VDC配额管理员")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tenantAdmin", value = "tenantAdmin", dataType = "TenantAdmin", paramType = "body", required = true), })
    @PostMapping("/save_quota_admin")
    public R saveQuotaAdmin(@RequestBody TenantAdmin tenantAdmin) {
        if (CollectionUtils.isEmpty(tenantAdmin.getUserIds())) {
            return R.failed("参数用户id不能为空");
        }
        return tenantService.saveQuotaAdmin(tenantAdmin);
    }

    /**
     * 根据租户ID或projectID或userID获取中文名称
     *
     * @param prefix 前缀
     * @param id     租户、project、用户的id
     * @return
     */
    @ApiOperation(value = "根据租户ID或projectID获取中文名称")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "prefix", value = "租户、project、用户标识", dataType = "String", paramType = "path", required = true),
            @ApiImplicitParam(name = "id", value = "租户、project、用户的id", dataType = "Integer", paramType = "path", required = true) })
    @GetMapping("/id-name/{prefix}/{id}")
    public R<String> translateIdToName(@PathVariable("prefix") String prefix, @PathVariable("id") Integer id) {
        return R.ok(tenantService.translateIdToName(prefix, id));
    }

    /**
     * 根据租户ID、projectID、userID获取中文名称
     *
     * @param tenantProjectUserVO 租户、project、用户VO
     * @return
     */
    @ApiOperation(value = "根据租户ID和projectID获取中文名称")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tenantProjectUserVO", value = "租户、project、用户VO集合", dataType = "TenantProjectUserVO", paramType = "body", required = true), })
    @PostMapping("/translation")
    public R<TenantProjectUserVO> translation(@RequestBody TenantProjectUserVO tenantProjectUserVO) {
        return tenantService.translation(tenantProjectUserVO);
    }

    /**
     * 租户绑定资源池
     *
     * @param tenantResourcePool tenantResource对象
     * @return R
     */
    @SysLog(value = "VDC绑定资源池", cloudResType = "VDC", resIdArgIndex = 0, resIdLocation = "arg.tenantId")
    @PostMapping("/bind-resource-pool")
    @ApiOperation(value = "租户绑定资源池")
    @ApiImplicitParams(@ApiImplicitParam(name = "list", value = "TenantResourcePool对象", dataType = "TenantResourcePool", paramType = "body", required = true))
    public R bindResourcePool(@Valid @RequestBody TenantResourcePool tenantResourcePool) {
        return tenantService.bind(tenantResourcePool);
    }

    /**
     * 租户取消绑定资源池
     *
     * @param tenantResourcePool tenantResource对象
     */
    @SysLog(value = "VDC解除绑定资源池", cloudResType = "VDC", resIdArgIndex = 0, resIdLocation = "arg.tenantId")
    @DeleteMapping("/de-bind-resource-pool")
    @ApiOperation(value = "租户解除绑定资源池")
    @ApiImplicitParams(@ApiImplicitParam(name = "tenantResourcePool", value = "TenantTypeResourcePool对象", dataType = "TenantTypeResourcePool", paramType = "body", required = true))
    public R deBindResourcePool(@Valid @RequestBody TenantResourcePool tenantResourcePool) {
        boolean b = tenantService.quitBind(tenantResourcePool);
        return b ? R.ok() : R.failed("解除绑定失败");
    }

    /**
     * 查询租户与资源池绑定关系 列表
     *
     * @param tenantId 租户id
     * @return 租户与资源池绑定关系 列表
     */
    @GetMapping("/resource-pool-list/{tenantId}/{resourcePoolCode}")
    @ApiOperation(value = "查询租户与资源池绑定关系 列表", notes = "查询租户与资源池绑定关系 列表")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "query", name = "page", dataType = "Page", required = true, value = "page"),
            @ApiImplicitParam(paramType = "path", name = "tenantId", dataType = "Integer", required = true, value = "tenantId"),
            @ApiImplicitParam(paramType = "path", name = "resourcePoolCode", dataType = "String", required = true, value = "resourcePoolCode") })
    public R selectBindResourcePool(Page page, @PathVariable("tenantId") Integer tenantId,
            @PathVariable("resourcePoolCode") String resourcePoolCode) {
        IPage<TenantResourcePoolVO> ipage = tenantService.selectBindResourcePools(page, tenantId, resourcePoolCode);
        return R.ok(ipage);
    }

    @SysLog(value = "VDC添加成员", cloudResType = "VDC")
    @PostMapping("/member-add")
    @ApiOperation(value = "租户添加成员")
    @ApiImplicitParams(@ApiImplicitParam(name = "list", value = "UserRoleProject对象集合", allowMultiple = true, dataType = "UserRoleProject", paramType = "body", required = true))
    public R addMember(@Valid @RequestBody List<UserRoleProject> list) {
        return tenantService.addMember(list);
    }

    @SysLog(value = "VDC添加成员查询用户", cloudResType = "VDC")
    @GetMapping("/member-add/users/{tenantId}")
    @ApiOperation(value = "租户添加成员查询用户")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "query", name = "page", dataType = "Page", required = true, value = "page"),
            @ApiImplicitParam(paramType = "path", name = "tenantId", dataType = "Integer", required = true, value = "tenantId"),
            @ApiImplicitParam(paramType = "query", name = "queryName", dataType = "String", required = true, value = "queryName") })
    public R addMemberQueryUser(Page page, @PathVariable("tenantId") Integer tenantId,
            @RequestParam(value = "queryName", required = false) String queryName) {
        IPage<UserVO> iPage = tenantService.queryUserForAddMember(page, tenantId, queryName);
        return R.ok(iPage);
    }

    /**
     * tenant删除成员
     *
     * @param userRoleProject userRoleProject
     * @return R
     */
    @SysLog(value = "VDC移除成员", cloudResType = "VDC", resIdArgIndex = 0, resIdLocation = "arg.tenantId")
    @DeleteMapping("/member-remove")
    @ApiOperation(value = "tenant删除成员")
    @ApiImplicitParams(@ApiImplicitParam(name = "userRoleProject", value = "userRoleProject", dataType = "UserRoleProject", paramType = "body", required = true))
    public R removeMember(@RequestBody UserRoleProject userRoleProject,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        return tenantService.removeMember(userRoleProject, authHeader);
    }

    /**
     * 查询一级租户下面的所有成员（包括子级租户的成员）
     *
     * @param page 分页条件
     * @param id   项目id
     * @return R
     */
    @GetMapping("/all-member-list/page/{id}")
    @ApiOperation(value = "查询一级租户下面的所有成员（包括子级租户的成员）")
    @ApiImplicitParams({ @ApiImplicitParam(name = "page", value = "分页条件", dataType = "Page", paramType = "query"),
            @ApiImplicitParam(name = "id", value = "项目id", dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "name", value = "name", dataType = "String", paramType = "path") })
    public R allListMember(Page page, @PathVariable("id") Integer id,
            @RequestParam(value = "name", required = false) String name) {
        if (id == null) {
            return R.failed("tenant id参数不能为空");
        }
        Tenant tenant = tenantService.getById(id);
        if (tenant == null) {
            return R.failed("tenant不存在");
        }
        List<Integer> tenantIds = new ArrayList<>();
        List<TenantTree> childTenants = tenantService.queryAllChildTenant(id);
        if (CollectionUtils.isNotEmpty(childTenants)) {
            tenantIds.addAll(childTenants.stream().map(TenantTree::getId).collect(Collectors.toList()));
        }
        tenantIds.add(id);
        IPage iPage = tenantService.allListMember(page, tenantIds, name);
        return R.ok(iPage);
    }

    /**
     * 分页查询tenant成员列表
     *
     * @param page 分页条件
     * @param id   项目id
     * @return R
     */
    @GetMapping("/member-list/page/{id}")
    @ApiOperation(value = "分页查询tenant成员列表")
    @ApiImplicitParams({ @ApiImplicitParam(name = "page", value = "分页条件", dataType = "Page", paramType = "query"),
            @ApiImplicitParam(name = "id", value = "项目id", dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "name", value = "name", dataType = "String", paramType = "path") })
    public R listMember(Page page, @PathVariable("id") Integer id,
            @RequestParam(value = "name", required = false) String name) {
        if (id == null) {
            return R.failed("tenant id参数不能为空");
        }
        Tenant tenant = tenantService.getById(id);
        if (tenant == null) {
            return R.failed("tenant不存在");
        }
        IPage iPage = tenantService.listMember(page, id, name);
        return R.ok(iPage);
    }

    /**
     * 查询tenant成员列表
     *
     * @param id 项目id
     * @return R
     */
    @GetMapping("/member-list/{id}")
    @ApiOperation(value = "查询tenant成员列表")
    @ApiImplicitParams({ @ApiImplicitParam(name = "id", value = "项目id", dataType = "Integer", paramType = "path") })
    public R<List<Member>> listMember(@PathVariable("id") Integer id,
            @RequestParam(value = "status", required = false) String status) {
        if (id == null) {
            return R.failed("tenant id参数不能为空");
        }
        Tenant tenant = tenantService.getById(id);
        if (tenant == null) {
            return R.failed("tenant不存在");
        }
        return R.ok(tenantService.listMember(id, status));
    }

    /**
     * 修改tenant成员角色
     *
     * @param userRoleProject UserRoleProject对象
     * @return R
     */
    @SysLog(value = "修改VDC成员角色", cloudResType = "VDC", resIdArgIndex = 0, resIdLocation = "arg.tenantId")
    @PutMapping("/member-role")
    @ApiOperation(value = "修改VDC成员角色")
    @ApiImplicitParams(@ApiImplicitParam(name = "userRoleProject", value = "UserRoleProject对象", dataType = "UserRoleProject", paramType = "body"))
    public R updateMemberRole(@RequestBody UserRoleProject userRoleProject) {
        return tenantService.updateMemberRole(userRoleProject);
    }

    /**
     * 根据name查tenant
     *
     * @param name
     * @return
     */
    @GetMapping("/name")
    @ApiOperation(value = "根据name查tenant", notes = "根据name查tenant")
    @ApiImplicitParams(value = @ApiImplicitParam(paramType = "query", name = "name", dataType = "String", required = true, value = "name"))
    public R findByName(@RequestParam(value = "name", required = false) String name) {
        return R.ok(tenantMapper
                .selectOne(Wrappers.<Tenant>lambdaQuery().eq(Tenant::getName, name).eq(Tenant::getDelFlag, 0)));
    }

    @GetMapping("/keyword")
    @ApiOperation(value = "查询租户", notes = "查询租户")
    @ApiImplicitParams(value = @ApiImplicitParam(paramType = "query", name = "name", dataType = "String", required = true, value = "name"))
    public R<List<Tenant>> findTenantListBykeyword(@RequestParam("name") String name) {
        log.info("name={}", name);
        final LambdaQueryWrapper<Tenant> wrapper = Wrappers.<Tenant>lambdaQuery()
                .like(StringUtils.isNotBlank(name), Tenant::getName, name)
                .eq(Tenant::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus());
        return R.ok(tenantMapper.selectList(wrapper));
    }

    /**
     * 条件查询租户列表(quota使用)
     *
     * @return 租户列表
     */
    @PostMapping("/list/quota")
    @ApiOperation(value = "条件查询租户列表(quota使用)", notes = "条件查询租户列表(quota使用)")
    @ApiImplicitParams(value = @ApiImplicitParam(paramType = "body", name = "tenantDTO", dataType = "TenantDTO", required = true, value = "tenantDTO"))
    public R getLists(@RequestBody TenantDTO tenantDTO) {
        List<Tenant> list = tenantService.tenantLists(tenantDTO);
        return R.ok(list);
    }

    /**
     * 条件查询租户列表(quota使用)
     *
     * @return 租户列表
     */
    @GetMapping("/list/quotaForAudit")
    @ApiOperation(value = "条件查询租户列表(quota使用)", notes = "条件查询租户列表(quota使用)")
    public R getTenantInfo() {
        TenantDTO tenantDTO = new TenantDTO();
        List<TenantVO> list = tenantMapper.selectListByConditionNoPage(tenantDTO);
        return R.ok(list);
    }

    /**
     * 根据ProjectId查询一级VDC
     *
     * @return 租户
     */
    @GetMapping("/getOneLevelByProjectId/{projectId}")
    @ApiOperation(value = "根据ProjectId查询一级VDC")
    public R<TenantVO> getOneLevelByProjectId(@PathVariable(name = "projectId") Integer projectId) {
        Integer level = 0;
        ProjectVO projectVO = projectService.selectById(projectId);
        if (projectVO == null) {
            log.info("getOneLevelByProjectId projectId={}", projectId);
            return R.failed();
        }
        TenantVO tenantVo = tenantService.selectTenantVoById(projectVO.getTenantId());
        level = tenantVo.getLevel();
        while (level != 1) {
            tenantVo = tenantService.selectTenantVoById(tenantVo.getParentId());
            level = tenantVo.getLevel();
        }
        return R.ok(tenantVo);
    }

    /**
     * 根据TenantId查询一级VDC
     *
     * @return 租户
     */
    @GetMapping("/getOneLevelByTenantId/{tenantId}")
    @ApiOperation(value = "根据TenantId查询一级VDC")
    public R<TenantVO> getOneLevelByTenantId(@PathVariable(name = "tenantId") Integer tenantId) {
        Integer level = 0;
        TenantVO tenantVo = tenantService.selectTenantVoById(tenantId);
        if (tenantVo == null) {
            log.info("getOneLevelByTenantId tenantId={}", tenantId);
            return R.failed();
        }
        level = tenantVo.getLevel();
        while (level != 1) {
            tenantVo = tenantService.selectTenantVoById(tenantVo.getParentId());
            level = tenantVo.getLevel();
        }
        return R.ok(tenantVo);
    }

    /**
     * 根据TenantId查询父级vdc树形结构
     *
     * @return 租户
     */
    @GetMapping("/getParentTreeByTenantId/{tenantId}")
    @ApiOperation(value = "根据TenantId查询父级vdc树形结构")
    public R<List<TenantVO>> getParentTreeByTenantId(@PathVariable(name = "tenantId") Integer tenantId) {
        List<TenantVO> list = new LinkedList<TenantVO>();
        TenantVO tenantVo = tenantService.selectTenantVoById(tenantId);
        list.add(tenantVo);
        Integer level = tenantVo.getLevel();
        while (level != 1) {
            tenantVo = tenantService.selectTenantVoById(tenantVo.getParentId());
            list.add(tenantVo);
            level = tenantVo.getLevel();
        }
        return R.ok(list);
    }

    /**
     * 根据TenantId查询父级vdc树形结构
     *
     * @return 租户
     */
    @PostMapping("/getParentTreeByTenantIds")
    @ApiOperation(value = "根据TenantId查询父级vdc树形结构")
    public R<List<List<Integer>>> getParentTreeByTenantIds(@RequestBody List<Integer> tenantIds) {
        List<List<Integer>> result = new LinkedList<List<Integer>>();
        for (Integer tenantId : tenantIds) {
            List<Integer> list = new LinkedList<Integer>();
            TenantVO tenantVo = tenantService.selectTenantVoById(tenantId);
            list.add(tenantVo.getId());
            Integer level = tenantVo.getLevel();
            while (level != 1) {
                tenantVo = tenantService.selectTenantVoById(tenantVo.getParentId());
                list.add(tenantVo.getId());
                level = tenantVo.getLevel();
            }
            List<Integer> list1 = new LinkedList<Integer>();
            for (int i = list.size() - 1; i > -1; i--) {
                list1.add(list.get(i));
            }
            result.add(list1);
        }
        return R.ok(result);
    }

    /**
     * 根据TenantId查询子级vdc列表
     *
     * @return 租户
     */
    @GetMapping("/getChildrenByTenantId/{tenantId}")
    @ApiOperation(value = "根据TenantId查询子级vdc列表")
    public R<List<Integer>> getChildrenByTenantId(@PathVariable(name = "tenantId") Integer tenantId) {
        List<Tenant> tenantVos = tenantService.list();
        List<Integer> resultList = new ArrayList<>();
        Map<Integer, Tenant> tenantVoMap = new HashMap<>();
        for (Tenant tenantVO : tenantVos) {
            if (tenantVO.getParentId().equals(tenantId)) {
                resultList.add(tenantVO.getId());
                tenantVoMap.put(tenantVO.getId(), tenantVO);
            } else {
                if (tenantVoMap.containsKey(tenantVO.getParentId())) {
                    resultList.add(tenantVO.getId());
                    tenantVoMap.put(tenantVO.getId(), tenantVO);
                }
            }
        }
        return R.ok(resultList);
    }

    /**
     * 根据TenantId查询子级vdc列表
     *
     * @return 租户
     */
    @PostMapping("/getChildrenByTenantIds")
    @ApiOperation(value = "根据TenantId查询子级vdc列表")
    public R<Map<Integer, List<Tenant>>> getChildrenByTenantIds(@RequestBody List<Integer> tenantIds) {
        List<Tenant> tenantVos = tenantService.list();
        Map<Integer, List<Tenant>> map = new HashMap<Integer, List<Tenant>>();
        for (Integer tenantId : tenantIds) {
            List<Tenant> resultList = new ArrayList<>();
            Map<Integer, Tenant> tenantVoMap = new HashMap<>();
            for (Tenant tenantVO : tenantVos) {
                if (tenantVO.getParentId().equals(tenantId)) {
                    resultList.add(tenantVO);
                    tenantVoMap.put(tenantVO.getId(), tenantVO);
                } else {
                    if (tenantVoMap.containsKey(tenantVO.getParentId())) {
                        resultList.add(tenantVO);
                        tenantVoMap.put(tenantVO.getId(), tenantVO);
                    }
                }
            }
            map.put(tenantId, resultList);
        }
        return R.ok(map);
    }

    /**
     * 根据租户id查询位置树
     *
     * @param id 租户id
     * @return R
     */
    @GetMapping("/locationTree/{id}")
    @ApiOperation(value = "根据租户id查询位置树")
    @ApiImplicitParams(@ApiImplicitParam(name = "id", value = "租户id", dataType = "Integer", paramType = "path", required = true))
    public R<TenantLocationTree> locationTreeInfo(@PathVariable(value = "id") Integer id) {
        List<AllLocationTree> allLocationTrees = allLocationTreeService
                .list(Wrappers.query(new AllLocationTree()).orderByDesc("id"));
        AllLocationTree allLocationTree = allLocationTrees.get(0);
        TenantLocationTree tenantLocationTree = tenantLocationTreeService
                .getOne(new QueryWrapper<TenantLocationTree>().lambda().eq(TenantLocationTree::getVdcId, id));
        // 根据全量位置树重新构建该vdc的位置树
        if (!allLocationTree.getTreeVersion().equals(tenantLocationTree.getTreeVersion())) {
            // 获取新增位置树
            String oldLocationTree = allLocationTrees.get(1).getLocationTree();
            List<LocationTreeNode> oldLocationTreeJson = JSONObject.parseArray(oldLocationTree, LocationTreeNode.class);
            List<LocationTreeNode> locationTreeJson = JSONObject.parseArray(allLocationTree.getLocationTree(),
                    LocationTreeNode.class);

            List<String> list = new ArrayList<String>();
            List<LocationTreeNode> locationTreeJson1 = new ArrayList<LocationTreeNode>();
            locationTreeJson1.addAll(locationTreeJson);
            if (oldLocationTreeJson != null) {
                for (LocationTreeNode locationTreeNode1 : locationTreeJson) {
                    for (LocationTreeNode locationTreeNode2 : oldLocationTreeJson) {
                        if (locationTreeNode1.getUuid().equals(locationTreeNode2.getUuid())) {
                            locationTreeJson1.remove(locationTreeNode1);
                        }
                    }
                }
            }
            // 添加新增的节点
            for (LocationTreeNode locationTreeNode : locationTreeJson1) {
                list.add(locationTreeNode.getUuid());
            }
            // 更新当前vdc的位置树
            JSONArray tenantLocationTreeArray = JSONArray.parseArray(tenantLocationTree.getLocationTree());
            if (tenantLocationTreeArray != null) {
                // 添加原有的位置树
                for (Object object : tenantLocationTreeArray) {
                    JSONObject json = (JSONObject) object;
                    list.add(json.getString("uuid"));
                }
            }
            // 根据全量位置树重新构建该vdc的位置树
            JSONArray array = new JSONArray();
            SyncAllLocationTreeTask.structure(locationTreeJson, array, list, "-1");
            tenantLocationTree.setLocationTree(array.toJSONString());
            tenantLocationTree.setTreeVersion(allLocationTree.getTreeVersion());
            updateLocationTree(tenantLocationTree);
        }
        return R.ok(tenantLocationTree);
    }

    /**
     * 更新位置树信息
     *
     * @param tenantDTO 租户信息
     * @return R
     */
    @SysLog(value = "更新位置树信息", cloudResType = "VDC", resNameArgIndex = 0, resIdArgIndex = 0, resIdLocation = "arg.vdcId")
    @PutMapping("/locationTree")
    @ApiOperation(value = "更新位置树信息")
    @ApiImplicitParams(@ApiImplicitParam(name = "tenantLocationTree", value = "VDC位置树对象", dataType = "TenantLocationTree", paramType = "body", required = true))
    public R updateLocationTree(@Validated(Update.class) @RequestBody TenantLocationTree tenantLocationTree) {
        String treeVersion = LocalDateTime.now() + UUID.randomUUID().toString();
        AllLocationTree allLocationTree = allLocationTreeService
                .list(Wrappers.query(new AllLocationTree()).orderByDesc("id")).get(0);
        allLocationTree.setTreeVersion(treeVersion);
        allLocationTreeService.updateById(allLocationTree);

        tenantLocationTree.setTreeVersion(treeVersion);
        boolean b = tenantLocationTreeService.updateById(tenantLocationTree);
        if (b) {
            List<Integer> tenantIds = new ArrayList<>();
            List<TenantTree> childTenants = tenantService.queryAllChildTenant(tenantLocationTree.getVdcId());
            if (CollectionUtils.isNotEmpty(childTenants)) {
                tenantIds.addAll(childTenants.stream().map(TenantTree::getId).collect(Collectors.toList()));
            }
            for (Integer tenantId : tenantIds) {
                tenantLocationTree.setVdcId(tenantId);
                tenantLocationTree.setTreeVersion(treeVersion);
                tenantLocationTreeService.updateByTenantId(tenantLocationTree);
            }
            List<TenantLocationTree> list = tenantLocationTreeService.list(new QueryWrapper<TenantLocationTree>()
                    .lambda().ne(TenantLocationTree::getTreeVersion, treeVersion));
            if (list != null && list.size() > 0) {
                for (TenantLocationTree tenantLocationTre : list) {
                    tenantLocationTre.setTreeVersion(treeVersion);
                    tenantLocationTreeService.updateById(tenantLocationTre);
                }
            }
            Task task = FhJobApiController.queryByTaskId("SyncNetworkPoolTask");
            FhJobApiController.trigger(task);
        }
        return b ? R.ok() : R.failed();
    }

    /**
     * 根据租户id查询Region,AZ,RZ
     *
     * @param id 租户id
     * @return R
     */
    @GetMapping("/queryPoolResources/{id}/{type}")
    @ApiOperation(value = "根据租户id查询Region,AZ,RZ")
    @ApiImplicitParams(@ApiImplicitParam(name = "id", value = "租户id", dataType = "Integer", paramType = "path", required = true))
    public R<List<LocationTreeNode>> queryPoolResources(@PathVariable(value = "id") Integer id,
            @PathVariable(value = "type") String type,
            @RequestParam(name = "parentId", required = false) String parentId,
            @RequestParam(name = "cloudPlatformType", required = false) String cloudPlatformType,
            @RequestParam(name = "networkProvider", required = false) String networkProvider) {
        return tenantService.queryPoolResources(id, type, parentId, cloudPlatformType, networkProvider);
    }

    /**
     * 查询网络池分域开关
     *
     * @param id 租户id
     * @return R
     */
    @GetMapping("/querySubDomainConfigSwitch/{type}")
    @ApiOperation(value = "查询网络池分域开关")
    @ApiImplicitParams(@ApiImplicitParam(name = "type", value = "网络池类型", dataType = "String", paramType = "path", required = true))
    public R<SubDomainConfigSwitch> querySubDomainConfigSwitch(
            @PathVariable(value = "type") String networkPoolTypeEnum) {
        return tenantService.querySubDomainConfigSwitch(networkPoolTypeEnum);
    }

    /**
     * 更新网络池分域开关
     *
     * @param tenantDTO 租户信息
     * @return R
     */
    @SysLog(value = "更新网络池分域开关", cloudResType = "VDC", resNameArgIndex = 0, resIdArgIndex = 0, resIdLocation = "arg.networkPoolType")
    @PutMapping("/updateSubDomainConfigSwitch")
    @ApiOperation(value = "更新网络池分域开关")
    @ApiImplicitParams(@ApiImplicitParam(name = "subDomainConfigSwitch", value = "网络池分域开关", dataType = "SubDomainConfigSwitch", paramType = "body", required = true))
    public R updateSubDomainConfigSwitch(
            @Validated(Update.class) @RequestBody SubDomainConfigSwitch subDomainConfigSwitch) {
        return tenantService.updateSubDomainConfigSwitch(subDomainConfigSwitch);
    }

    /**
     * 根据租户id查询网络池信息，浮动IP地址池、云主机IP地址池、VLAN池
     *
     * @param id 租户id
     * @return R
     */
    @GetMapping("/queryNetworkPool/{id}/{type}")
    @ApiOperation(value = "根据租户id查询网络池信息，浮动IP地址池、云主机IP地址池、VLAN池")
    @ApiImplicitParams(@ApiImplicitParam(name = "id", value = "租户id", dataType = "Integer", paramType = "path", required = true))
    public R<NetworkPoolDTO> queryNetworkPool(@PathVariable(value = "id") Integer id,
            @PathVariable(value = "type") String networkPoolTypeEnum,
            @RequestParam(name = "resourceZoneId", required = false) String resourceZoneId,
            @RequestParam(name = "networkType", required = false) String networkType,
            @RequestParam(name = "action", required = false) String action) {
        return tenantService.queryNetworkPool(id, networkPoolTypeEnum, resourceZoneId, networkType, action);
    }

    /**
     * 更新网络池信息
     *
     * @param tenantDTO 租户信息
     * @return R
     */
    @SysLog(value = "更新网络池信息", cloudResType = "VDC", resNameArgIndex = 0, resIdArgIndex = 0, resIdLocation = "arg.vdcId")
    @PutMapping("/updateNetworkPool")
    @ApiOperation(value = "更新网络池信息")
    @ApiImplicitParams(@ApiImplicitParam(name = "networkPoolDTO", value = "网络池信息对象", dataType = "NetworkPoolDTO", paramType = "body", required = true))
    public R updateNetworkPool(@Validated(Update.class) @RequestBody NetworkPoolDTO networkPoolDTO) {
        return tenantService.updateNetworkPool(networkPoolDTO);
    }

    @ApiOperation(value = "录入VDC")
    @PostMapping(value = { "/import" })
    public R improt(@RequestBody ImportTenantVo importTenantVo) {
        return tenantService.importTenant(importTenantVo);
    }

    @ApiOperation(value = "下载录入失败的VDC")
    @GetMapping(value = { "/downloadFail" })
    public void downloadFail(HttpServletResponse response,
            @RequestParam(value = "bucket", required = true) String bucket,
            @RequestParam(value = "fileName", required = true) String fileName) throws Exception {
        tenantService.downloadFail(response, bucket, fileName);
    }

    @ApiOperation(value = "获取VDC录入进度")
    @GetMapping(value = { "/queryProgress" })
    public R<ImportUserVo> queryProgress(@RequestParam(value = "bucket", required = true) String bucket,
            @RequestParam(value = "fileName", required = true) String fileName) throws Exception {
        ImportUserVo importUserVo = tenantService.queryProgress(bucket, fileName);
        return R.ok(importUserVo);
    }

    @ApiOperation(value = "查看历史录入记录")
    @GetMapping(value = { "/queryLogs" })
    public R<List<ImportTenantVo>> queryLogs() throws Exception {
        List<ImportTenantVo> importTenantVo = tenantService.queryLogs();
        return R.ok(importTenantVo);
    }
}
