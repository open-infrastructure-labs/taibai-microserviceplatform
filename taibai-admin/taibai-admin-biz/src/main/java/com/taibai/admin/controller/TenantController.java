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
 * ????????? mhp ???????????? 2019/11/12 ??????
 **/

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/tenant")
@Api(value = "tenant", tags = "VDC????????????")
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
     * ????????????
     *
     * @param tenantDTO ????????????
     * @return R
     */
    @SysLog(value = "??????VDC", cloudResType = "VDC", resNameArgIndex = 0, resNameLocation = "arg.name")
    @PostMapping
    @ApiOperation(value = "??????VDC")
    @ApiImplicitParams(@ApiImplicitParam(name = "tenantDTO", value = "??????VDC??????", dataType = "TenantDTO", paramType = "body", required = true))
    public R createTenant(@Validated(Save.class) @RequestBody TenantDTO tenantDTO) {
        boolean isLock = LockManager.tryLock(LOCK_KEY, 1, TimeUnit.MINUTES.toSeconds(10));
        if (!isLock) {
            return R.failed("????????????VDC?????????????????????");
        }
        try {
            if (tenantDTO.getParentId() != null && !tenantDTO.getParentId().equals(-1)) {
                FitmgrUser user = SecurityUtils.getUser();
                R<AuthCheck> r = iAuthService.newAuthCheck("create_tenant", user.getId(), user.getDefaultTenantId(),
                        null, null, null);
                if (r.getCode() == 0 && r.getData().isStatus()) {
                    if (r.getData().getOperatingRange().equals("0")) { // ??????
                        return saveTenant(tenantDTO);
                    } else if (r.getData().getOperatingRange().equals("1")) {// ????????????
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
            return R.failed("VDC???????????????");
        }
        R r = tenantService.saveTenant(tenantDTO);
        return r;
    }

    /**
     * ????????????id????????????
     *
     * @param tenantId ??????id
     * @return R
     */
    @SysLog(value = "??????VDC", cloudResType = "VDC", resIdArgIndex = 0, resIdLocation = "arg")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "????????????")
    @ApiImplicitParams(@ApiImplicitParam(name = "id", value = "??????id", dataType = "Integer", paramType = "path", required = true))
    public R deleteTenant(@PathVariable("id") Integer tenantId) {
        boolean b = tenantService.deleteTenant(tenantId);
        return b ? R.ok() : R.failed();
    }

    /**
     * ??????????????????
     *
     * @param tenantDTO ????????????
     * @return R
     */
    @SysLog(value = "??????VDC??????", cloudResType = "VDC", resNameArgIndex = 0, resNameLocation = "arg.name", resIdArgIndex = 0, resIdLocation = "arg.id")
    @PutMapping
    @ApiOperation(value = "??????????????????")
    @ApiImplicitParams(@ApiImplicitParam(name = "tenantDTO", value = "????????????", dataType = "TenantDTO", paramType = "body", required = true))
    public R updateTenant(@Validated(Update.class) @RequestBody TenantDTO tenantDTO) {
        tenantDTO.setName(tenantDTO.getName().trim());
        Tenant tenant = tenantService.getOne(new QueryWrapper<Tenant>().eq("name", tenantDTO.getName()));
        if (tenant != null && !tenant.getId().equals(tenantDTO.getId())) {
            return R.failed("VDC???????????????");
        }

        boolean b = tenantService.updateTenant(tenantDTO);
        return b ? R.ok() : R.failed();
    }

    /**
     * ????????????id??????????????????
     *
     * @param id ??????id
     * @return R
     */
    @GetMapping("/detail/{id}")
    @ApiOperation(value = "????????????id??????????????????")
    @ApiImplicitParams(@ApiImplicitParam(name = "id", value = "??????id", dataType = "Integer", paramType = "path", required = true))
    public R tenantInfo(@PathVariable(value = "id") Integer id) {
        int count = tenantService.count(new QueryWrapper<Tenant>().lambda().eq(Tenant::getId, id));
        if (count < 1) {
            return R.failed("VDC[id = " + id + " ]?????????");
        }
        TenantVO tenantVo = tenantService.selectTenantVoById(id);
        return R.ok(tenantVo);
    }

    /**
     * ????????????????????????????????????
     *
     * @param page      ????????????
     * @param tenantDTO ????????????
     * @return ????????????
     */
    @GetMapping("/list/page")
    @ApiOperation(value = "??????????????????????????????")
    @ApiImplicitParams({ @ApiImplicitParam(name = "page", value = "??????????????????", dataType = "Page", paramType = "query"),
            @ApiImplicitParam(name = "tenantDTO", value = "??????????????????", dataType = "TenantDTO", paramType = "query") })
    public R selectByCondition(Page page, TenantDTO tenantDTO) {
        // ?????????????????????????????????id
        FitmgrUser user = SecurityUtils.getUser();
        if (null != user) {
            IPage<TenantVO> iPage = tenantService.tenantList(page, tenantDTO, user.getId());
            return R.ok(iPage);
        }
        return R.failed(BusinessEnum.NOT_LOGIN);

    }

    /**
     * ????????????????????????
     *
     * @return R
     */
    @GetMapping("/list/tree")
    @ApiOperation(value = "??????????????????????????????")
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
     * ??????????????????????????????
     *
     * @return R
     */
    @GetMapping("/list/tree/defaultTenant")
    @ApiOperation(value = "??????????????????????????????")
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
     * ????????????????????????????????????
     *
     * @return ????????????
     */
    @GetMapping("/list/no-param")
    @ApiOperation(value = "????????????????????????")
    public R getTenantList() {
        List<Tenant> list = tenantMapper.selectList(Wrappers.emptyWrapper());
        return R.ok(list);
    }

    /**
     * ????????????????????????
     *
     * @return ????????????
     */
    @GetMapping("/list")
    @ApiOperation(value = "????????????????????????")
    @ApiImplicitParams(@ApiImplicitParam(name = "tenantDTO", value = "??????????????????", dataType = "TenantDTO", paramType = "query"))
    public R getList(TenantDTO tenantDTO) {
        List<Tenant> list = tenantService.tenantList(tenantDTO);
        return R.ok(list);
    }

    /**
     * ????????????????????????2
     *
     * @return ????????????2
     */
    @PostMapping("/list/info")
    @ApiOperation(value = "????????????????????????2", notes = "????????????????????????2")
    @ApiImplicitParams(value = @ApiImplicitParam(paramType = "body", name = "tenantDTO", dataType = "TenantDTO", required = true, value = "tenantDTO"))
    public R getListInfo(@RequestBody TenantDTO tenantDTO) {
        List<Tenant> list = tenantService.tenantList(tenantDTO);
        return R.ok(list);
    }

    /**
     * ????????????????????????????????????????????????????????????????????????????????????
     *
     * @return ????????????????????????????????????
     */
    @GetMapping("/conrole/list/tree")
    @ApiOperation(value = "????????????????????????????????????????????????????????????????????????????????????", notes = "????????????????????????????????????????????????????????????????????????????????????")
    public R getTreeListByConRole(@RequestParam(value = "createProject", required = false) String createProject,
            @RequestParam(value = "vdcId", required = false) Integer vdcId) {
        List<TenantTree> tenantTrees = tenantService.queryTenantTree(createProject, vdcId);
        return R.ok(tenantTrees);
    }

    /**
     * ????????????id????????????
     *
     * @param userId ??????id
     * @return ????????????
     */
    @GetMapping("/detail/user/{id}")
    @ApiOperation(value = "????????????id??????????????????")
    @ApiImplicitParams(@ApiImplicitParam(name = "id", value = "??????id", dataType = "Integer", paramType = "path", required = true))
    public R selectByUserId(@PathVariable("id") Integer userId) {
        TenantVO tenantVO = tenantService.selectByUserId(userId);
        return R.ok(tenantVO);
    }

    /**
     * ??????????????????????????????
     *
     * @param tenant ????????????
     * @return R
     */
    @SysLog(value = "??????VDC??????????????????", cloudResType = "VDC", resIdArgIndex = 0, resIdLocation = "arg.id")
    @ApiOperation(value = "??????VDC??????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tenant", value = "????????????", dataType = "Tenant", paramType = "body", required = true), })
    @PutMapping("/status")
    public R updateStatus(@RequestBody Tenant tenant) {
        if (tenant.getId() == null) {
            return R.failed("?????????VDC");
        }
        int count = tenantService.count(new QueryWrapper<Tenant>().lambda().eq(Tenant::getId, tenant.getId()));
        if (count < 1) {
            return R.failed("VDC?????????");
        }
        if (null == tenant.getStatus()) {
            return R.failed("????????????????????????????????????");
        }
        boolean b = tenant.getStatus().matches("[01]");
        if (!b) {
            return R.failed("??????[status]????????????:??????0?????????1");
        }
        int i = tenantService.updateStatus(tenant);
        return i == 1 ? R.ok() : R.failed("??????????????????");
    }

    /**
     * ?????????????????????
     *
     * @param tenantAdmin ??????id
     * @return
     */
    @SysLog(value = "??????VDC?????????", cloudResType = "VDC", resIdArgIndex = 0, resIdLocation = "arg.tenantId")
    @ApiOperation(value = "??????VDC?????????")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tenantAdmin", value = "tenantAdmin", dataType = "TenantAdmin", paramType = "body", required = true), })
    @PostMapping("/save_admin")
    public R saveAdmin(@RequestBody TenantAdmin tenantAdmin) {
        if (CollectionUtils.isEmpty(tenantAdmin.getUserIds())) {
            return R.failed("????????????id????????????");
        }
        return tenantService.saveAdmin(tenantAdmin);
    }

    /**
     * ???????????????????????????
     *
     * @param tenantAdmin ??????id
     * @return
     */
    @SysLog(value = "??????VDC???????????????", cloudResType = "VDC", resIdArgIndex = 0, resIdLocation = "arg.tenantId")
    @ApiOperation(value = "??????VDC???????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tenantAdmin", value = "tenantAdmin", dataType = "TenantAdmin", paramType = "body", required = true), })
    @PostMapping("/save_quota_admin")
    public R saveQuotaAdmin(@RequestBody TenantAdmin tenantAdmin) {
        if (CollectionUtils.isEmpty(tenantAdmin.getUserIds())) {
            return R.failed("????????????id????????????");
        }
        return tenantService.saveQuotaAdmin(tenantAdmin);
    }

    /**
     * ????????????ID???projectID???userID??????????????????
     *
     * @param prefix ??????
     * @param id     ?????????project????????????id
     * @return
     */
    @ApiOperation(value = "????????????ID???projectID??????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "prefix", value = "?????????project???????????????", dataType = "String", paramType = "path", required = true),
            @ApiImplicitParam(name = "id", value = "?????????project????????????id", dataType = "Integer", paramType = "path", required = true) })
    @GetMapping("/id-name/{prefix}/{id}")
    public R<String> translateIdToName(@PathVariable("prefix") String prefix, @PathVariable("id") Integer id) {
        return R.ok(tenantService.translateIdToName(prefix, id));
    }

    /**
     * ????????????ID???projectID???userID??????????????????
     *
     * @param tenantProjectUserVO ?????????project?????????VO
     * @return
     */
    @ApiOperation(value = "????????????ID???projectID??????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tenantProjectUserVO", value = "?????????project?????????VO??????", dataType = "TenantProjectUserVO", paramType = "body", required = true), })
    @PostMapping("/translation")
    public R<TenantProjectUserVO> translation(@RequestBody TenantProjectUserVO tenantProjectUserVO) {
        return tenantService.translation(tenantProjectUserVO);
    }

    /**
     * ?????????????????????
     *
     * @param tenantResourcePool tenantResource??????
     * @return R
     */
    @SysLog(value = "VDC???????????????", cloudResType = "VDC", resIdArgIndex = 0, resIdLocation = "arg.tenantId")
    @PostMapping("/bind-resource-pool")
    @ApiOperation(value = "?????????????????????")
    @ApiImplicitParams(@ApiImplicitParam(name = "list", value = "TenantResourcePool??????", dataType = "TenantResourcePool", paramType = "body", required = true))
    public R bindResourcePool(@Valid @RequestBody TenantResourcePool tenantResourcePool) {
        return tenantService.bind(tenantResourcePool);
    }

    /**
     * ???????????????????????????
     *
     * @param tenantResourcePool tenantResource??????
     */
    @SysLog(value = "VDC?????????????????????", cloudResType = "VDC", resIdArgIndex = 0, resIdLocation = "arg.tenantId")
    @DeleteMapping("/de-bind-resource-pool")
    @ApiOperation(value = "???????????????????????????")
    @ApiImplicitParams(@ApiImplicitParam(name = "tenantResourcePool", value = "TenantTypeResourcePool??????", dataType = "TenantTypeResourcePool", paramType = "body", required = true))
    public R deBindResourcePool(@Valid @RequestBody TenantResourcePool tenantResourcePool) {
        boolean b = tenantService.quitBind(tenantResourcePool);
        return b ? R.ok() : R.failed("??????????????????");
    }

    /**
     * ???????????????????????????????????? ??????
     *
     * @param tenantId ??????id
     * @return ?????????????????????????????? ??????
     */
    @GetMapping("/resource-pool-list/{tenantId}/{resourcePoolCode}")
    @ApiOperation(value = "???????????????????????????????????? ??????", notes = "???????????????????????????????????? ??????")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "query", name = "page", dataType = "Page", required = true, value = "page"),
            @ApiImplicitParam(paramType = "path", name = "tenantId", dataType = "Integer", required = true, value = "tenantId"),
            @ApiImplicitParam(paramType = "path", name = "resourcePoolCode", dataType = "String", required = true, value = "resourcePoolCode") })
    public R selectBindResourcePool(Page page, @PathVariable("tenantId") Integer tenantId,
            @PathVariable("resourcePoolCode") String resourcePoolCode) {
        IPage<TenantResourcePoolVO> ipage = tenantService.selectBindResourcePools(page, tenantId, resourcePoolCode);
        return R.ok(ipage);
    }

    @SysLog(value = "VDC????????????", cloudResType = "VDC")
    @PostMapping("/member-add")
    @ApiOperation(value = "??????????????????")
    @ApiImplicitParams(@ApiImplicitParam(name = "list", value = "UserRoleProject????????????", allowMultiple = true, dataType = "UserRoleProject", paramType = "body", required = true))
    public R addMember(@Valid @RequestBody List<UserRoleProject> list) {
        return tenantService.addMember(list);
    }

    @SysLog(value = "VDC????????????????????????", cloudResType = "VDC")
    @GetMapping("/member-add/users/{tenantId}")
    @ApiOperation(value = "??????????????????????????????")
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
     * tenant????????????
     *
     * @param userRoleProject userRoleProject
     * @return R
     */
    @SysLog(value = "VDC????????????", cloudResType = "VDC", resIdArgIndex = 0, resIdLocation = "arg.tenantId")
    @DeleteMapping("/member-remove")
    @ApiOperation(value = "tenant????????????")
    @ApiImplicitParams(@ApiImplicitParam(name = "userRoleProject", value = "userRoleProject", dataType = "UserRoleProject", paramType = "body", required = true))
    public R removeMember(@RequestBody UserRoleProject userRoleProject,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        return tenantService.removeMember(userRoleProject, authHeader);
    }

    /**
     * ????????????????????????????????????????????????????????????????????????
     *
     * @param page ????????????
     * @param id   ??????id
     * @return R
     */
    @GetMapping("/all-member-list/page/{id}")
    @ApiOperation(value = "????????????????????????????????????????????????????????????????????????")
    @ApiImplicitParams({ @ApiImplicitParam(name = "page", value = "????????????", dataType = "Page", paramType = "query"),
            @ApiImplicitParam(name = "id", value = "??????id", dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "name", value = "name", dataType = "String", paramType = "path") })
    public R allListMember(Page page, @PathVariable("id") Integer id,
            @RequestParam(value = "name", required = false) String name) {
        if (id == null) {
            return R.failed("tenant id??????????????????");
        }
        Tenant tenant = tenantService.getById(id);
        if (tenant == null) {
            return R.failed("tenant?????????");
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
     * ????????????tenant????????????
     *
     * @param page ????????????
     * @param id   ??????id
     * @return R
     */
    @GetMapping("/member-list/page/{id}")
    @ApiOperation(value = "????????????tenant????????????")
    @ApiImplicitParams({ @ApiImplicitParam(name = "page", value = "????????????", dataType = "Page", paramType = "query"),
            @ApiImplicitParam(name = "id", value = "??????id", dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "name", value = "name", dataType = "String", paramType = "path") })
    public R listMember(Page page, @PathVariable("id") Integer id,
            @RequestParam(value = "name", required = false) String name) {
        if (id == null) {
            return R.failed("tenant id??????????????????");
        }
        Tenant tenant = tenantService.getById(id);
        if (tenant == null) {
            return R.failed("tenant?????????");
        }
        IPage iPage = tenantService.listMember(page, id, name);
        return R.ok(iPage);
    }

    /**
     * ??????tenant????????????
     *
     * @param id ??????id
     * @return R
     */
    @GetMapping("/member-list/{id}")
    @ApiOperation(value = "??????tenant????????????")
    @ApiImplicitParams({ @ApiImplicitParam(name = "id", value = "??????id", dataType = "Integer", paramType = "path") })
    public R<List<Member>> listMember(@PathVariable("id") Integer id,
            @RequestParam(value = "status", required = false) String status) {
        if (id == null) {
            return R.failed("tenant id??????????????????");
        }
        Tenant tenant = tenantService.getById(id);
        if (tenant == null) {
            return R.failed("tenant?????????");
        }
        return R.ok(tenantService.listMember(id, status));
    }

    /**
     * ??????tenant????????????
     *
     * @param userRoleProject UserRoleProject??????
     * @return R
     */
    @SysLog(value = "??????VDC????????????", cloudResType = "VDC", resIdArgIndex = 0, resIdLocation = "arg.tenantId")
    @PutMapping("/member-role")
    @ApiOperation(value = "??????VDC????????????")
    @ApiImplicitParams(@ApiImplicitParam(name = "userRoleProject", value = "UserRoleProject??????", dataType = "UserRoleProject", paramType = "body"))
    public R updateMemberRole(@RequestBody UserRoleProject userRoleProject) {
        return tenantService.updateMemberRole(userRoleProject);
    }

    /**
     * ??????name???tenant
     *
     * @param name
     * @return
     */
    @GetMapping("/name")
    @ApiOperation(value = "??????name???tenant", notes = "??????name???tenant")
    @ApiImplicitParams(value = @ApiImplicitParam(paramType = "query", name = "name", dataType = "String", required = true, value = "name"))
    public R findByName(@RequestParam(value = "name", required = false) String name) {
        return R.ok(tenantMapper
                .selectOne(Wrappers.<Tenant>lambdaQuery().eq(Tenant::getName, name).eq(Tenant::getDelFlag, 0)));
    }

    @GetMapping("/keyword")
    @ApiOperation(value = "????????????", notes = "????????????")
    @ApiImplicitParams(value = @ApiImplicitParam(paramType = "query", name = "name", dataType = "String", required = true, value = "name"))
    public R<List<Tenant>> findTenantListBykeyword(@RequestParam("name") String name) {
        log.info("name={}", name);
        final LambdaQueryWrapper<Tenant> wrapper = Wrappers.<Tenant>lambdaQuery()
                .like(StringUtils.isNotBlank(name), Tenant::getName, name)
                .eq(Tenant::getDelFlag, DeleteFlagStatusEnum.VIEW.getStatus());
        return R.ok(tenantMapper.selectList(wrapper));
    }

    /**
     * ????????????????????????(quota??????)
     *
     * @return ????????????
     */
    @PostMapping("/list/quota")
    @ApiOperation(value = "????????????????????????(quota??????)", notes = "????????????????????????(quota??????)")
    @ApiImplicitParams(value = @ApiImplicitParam(paramType = "body", name = "tenantDTO", dataType = "TenantDTO", required = true, value = "tenantDTO"))
    public R getLists(@RequestBody TenantDTO tenantDTO) {
        List<Tenant> list = tenantService.tenantLists(tenantDTO);
        return R.ok(list);
    }

    /**
     * ????????????????????????(quota??????)
     *
     * @return ????????????
     */
    @GetMapping("/list/quotaForAudit")
    @ApiOperation(value = "????????????????????????(quota??????)", notes = "????????????????????????(quota??????)")
    public R getTenantInfo() {
        TenantDTO tenantDTO = new TenantDTO();
        List<TenantVO> list = tenantMapper.selectListByConditionNoPage(tenantDTO);
        return R.ok(list);
    }

    /**
     * ??????ProjectId????????????VDC
     *
     * @return ??????
     */
    @GetMapping("/getOneLevelByProjectId/{projectId}")
    @ApiOperation(value = "??????ProjectId????????????VDC")
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
     * ??????TenantId????????????VDC
     *
     * @return ??????
     */
    @GetMapping("/getOneLevelByTenantId/{tenantId}")
    @ApiOperation(value = "??????TenantId????????????VDC")
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
     * ??????TenantId????????????vdc????????????
     *
     * @return ??????
     */
    @GetMapping("/getParentTreeByTenantId/{tenantId}")
    @ApiOperation(value = "??????TenantId????????????vdc????????????")
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
     * ??????TenantId????????????vdc????????????
     *
     * @return ??????
     */
    @PostMapping("/getParentTreeByTenantIds")
    @ApiOperation(value = "??????TenantId????????????vdc????????????")
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
     * ??????TenantId????????????vdc??????
     *
     * @return ??????
     */
    @GetMapping("/getChildrenByTenantId/{tenantId}")
    @ApiOperation(value = "??????TenantId????????????vdc??????")
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
     * ??????TenantId????????????vdc??????
     *
     * @return ??????
     */
    @PostMapping("/getChildrenByTenantIds")
    @ApiOperation(value = "??????TenantId????????????vdc??????")
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
     * ????????????id???????????????
     *
     * @param id ??????id
     * @return R
     */
    @GetMapping("/locationTree/{id}")
    @ApiOperation(value = "????????????id???????????????")
    @ApiImplicitParams(@ApiImplicitParam(name = "id", value = "??????id", dataType = "Integer", paramType = "path", required = true))
    public R<TenantLocationTree> locationTreeInfo(@PathVariable(value = "id") Integer id) {
        List<AllLocationTree> allLocationTrees = allLocationTreeService
                .list(Wrappers.query(new AllLocationTree()).orderByDesc("id"));
        AllLocationTree allLocationTree = allLocationTrees.get(0);
        TenantLocationTree tenantLocationTree = tenantLocationTreeService
                .getOne(new QueryWrapper<TenantLocationTree>().lambda().eq(TenantLocationTree::getVdcId, id));
        // ????????????????????????????????????vdc????????????
        if (!allLocationTree.getTreeVersion().equals(tenantLocationTree.getTreeVersion())) {
            // ?????????????????????
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
            // ?????????????????????
            for (LocationTreeNode locationTreeNode : locationTreeJson1) {
                list.add(locationTreeNode.getUuid());
            }
            // ????????????vdc????????????
            JSONArray tenantLocationTreeArray = JSONArray.parseArray(tenantLocationTree.getLocationTree());
            if (tenantLocationTreeArray != null) {
                // ????????????????????????
                for (Object object : tenantLocationTreeArray) {
                    JSONObject json = (JSONObject) object;
                    list.add(json.getString("uuid"));
                }
            }
            // ????????????????????????????????????vdc????????????
            JSONArray array = new JSONArray();
            SyncAllLocationTreeTask.structure(locationTreeJson, array, list, "-1");
            tenantLocationTree.setLocationTree(array.toJSONString());
            tenantLocationTree.setTreeVersion(allLocationTree.getTreeVersion());
            updateLocationTree(tenantLocationTree);
        }
        return R.ok(tenantLocationTree);
    }

    /**
     * ?????????????????????
     *
     * @param tenantDTO ????????????
     * @return R
     */
    @SysLog(value = "?????????????????????", cloudResType = "VDC", resNameArgIndex = 0, resIdArgIndex = 0, resIdLocation = "arg.vdcId")
    @PutMapping("/locationTree")
    @ApiOperation(value = "?????????????????????")
    @ApiImplicitParams(@ApiImplicitParam(name = "tenantLocationTree", value = "VDC???????????????", dataType = "TenantLocationTree", paramType = "body", required = true))
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
     * ????????????id??????Region,AZ,RZ
     *
     * @param id ??????id
     * @return R
     */
    @GetMapping("/queryPoolResources/{id}/{type}")
    @ApiOperation(value = "????????????id??????Region,AZ,RZ")
    @ApiImplicitParams(@ApiImplicitParam(name = "id", value = "??????id", dataType = "Integer", paramType = "path", required = true))
    public R<List<LocationTreeNode>> queryPoolResources(@PathVariable(value = "id") Integer id,
            @PathVariable(value = "type") String type,
            @RequestParam(name = "parentId", required = false) String parentId,
            @RequestParam(name = "cloudPlatformType", required = false) String cloudPlatformType,
            @RequestParam(name = "networkProvider", required = false) String networkProvider) {
        return tenantService.queryPoolResources(id, type, parentId, cloudPlatformType, networkProvider);
    }

    /**
     * ???????????????????????????
     *
     * @param id ??????id
     * @return R
     */
    @GetMapping("/querySubDomainConfigSwitch/{type}")
    @ApiOperation(value = "???????????????????????????")
    @ApiImplicitParams(@ApiImplicitParam(name = "type", value = "???????????????", dataType = "String", paramType = "path", required = true))
    public R<SubDomainConfigSwitch> querySubDomainConfigSwitch(
            @PathVariable(value = "type") String networkPoolTypeEnum) {
        return tenantService.querySubDomainConfigSwitch(networkPoolTypeEnum);
    }

    /**
     * ???????????????????????????
     *
     * @param tenantDTO ????????????
     * @return R
     */
    @SysLog(value = "???????????????????????????", cloudResType = "VDC", resNameArgIndex = 0, resIdArgIndex = 0, resIdLocation = "arg.networkPoolType")
    @PutMapping("/updateSubDomainConfigSwitch")
    @ApiOperation(value = "???????????????????????????")
    @ApiImplicitParams(@ApiImplicitParam(name = "subDomainConfigSwitch", value = "?????????????????????", dataType = "SubDomainConfigSwitch", paramType = "body", required = true))
    public R updateSubDomainConfigSwitch(
            @Validated(Update.class) @RequestBody SubDomainConfigSwitch subDomainConfigSwitch) {
        return tenantService.updateSubDomainConfigSwitch(subDomainConfigSwitch);
    }

    /**
     * ????????????id??????????????????????????????IP?????????????????????IP????????????VLAN???
     *
     * @param id ??????id
     * @return R
     */
    @GetMapping("/queryNetworkPool/{id}/{type}")
    @ApiOperation(value = "????????????id??????????????????????????????IP?????????????????????IP????????????VLAN???")
    @ApiImplicitParams(@ApiImplicitParam(name = "id", value = "??????id", dataType = "Integer", paramType = "path", required = true))
    public R<NetworkPoolDTO> queryNetworkPool(@PathVariable(value = "id") Integer id,
            @PathVariable(value = "type") String networkPoolTypeEnum,
            @RequestParam(name = "resourceZoneId", required = false) String resourceZoneId,
            @RequestParam(name = "networkType", required = false) String networkType,
            @RequestParam(name = "action", required = false) String action) {
        return tenantService.queryNetworkPool(id, networkPoolTypeEnum, resourceZoneId, networkType, action);
    }

    /**
     * ?????????????????????
     *
     * @param tenantDTO ????????????
     * @return R
     */
    @SysLog(value = "?????????????????????", cloudResType = "VDC", resNameArgIndex = 0, resIdArgIndex = 0, resIdLocation = "arg.vdcId")
    @PutMapping("/updateNetworkPool")
    @ApiOperation(value = "?????????????????????")
    @ApiImplicitParams(@ApiImplicitParam(name = "networkPoolDTO", value = "?????????????????????", dataType = "NetworkPoolDTO", paramType = "body", required = true))
    public R updateNetworkPool(@Validated(Update.class) @RequestBody NetworkPoolDTO networkPoolDTO) {
        return tenantService.updateNetworkPool(networkPoolDTO);
    }

    @ApiOperation(value = "??????VDC")
    @PostMapping(value = { "/import" })
    public R improt(@RequestBody ImportTenantVo importTenantVo) {
        return tenantService.importTenant(importTenantVo);
    }

    @ApiOperation(value = "?????????????????????VDC")
    @GetMapping(value = { "/downloadFail" })
    public void downloadFail(HttpServletResponse response,
            @RequestParam(value = "bucket", required = true) String bucket,
            @RequestParam(value = "fileName", required = true) String fileName) throws Exception {
        tenantService.downloadFail(response, bucket, fileName);
    }

    @ApiOperation(value = "??????VDC????????????")
    @GetMapping(value = { "/queryProgress" })
    public R<ImportUserVo> queryProgress(@RequestParam(value = "bucket", required = true) String bucket,
            @RequestParam(value = "fileName", required = true) String fileName) throws Exception {
        ImportUserVo importUserVo = tenantService.queryProgress(bucket, fileName);
        return R.ok(importUserVo);
    }

    @ApiOperation(value = "????????????????????????")
    @GetMapping(value = { "/queryLogs" })
    public R<List<ImportTenantVo>> queryLogs() throws Exception {
        List<ImportTenantVo> importTenantVo = tenantService.queryLogs();
        return R.ok(importTenantVo);
    }
}
