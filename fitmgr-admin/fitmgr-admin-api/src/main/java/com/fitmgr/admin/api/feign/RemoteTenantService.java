package com.fitmgr.admin.api.feign;

import java.util.List;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.fitmgr.admin.api.config.AdminFeignConfig;
import com.fitmgr.admin.api.dto.TenantDTO;
import com.fitmgr.admin.api.dto.TenantTree;
import com.fitmgr.admin.api.entity.Tenant;
import com.fitmgr.admin.api.entity.TenantLocationTree;
import com.fitmgr.admin.api.vo.TenantProjectUserVO;
import com.fitmgr.admin.api.vo.TenantVO;
import com.fitmgr.common.core.constant.SecurityConstants;
import com.fitmgr.common.core.constant.ServiceNameConstants;
import com.fitmgr.common.core.util.R;

/**
 * 创建人 mhp 创建时间 2019/11/29 描述
 **/

@FeignClient(contextId = "remoteTenantService", value = ServiceNameConstants.UMPS_SERVICE, configuration = AdminFeignConfig.class)
public interface RemoteTenantService {

    /**
     * 根据租户id获取租户信息
     *
     * @param id 租户id
     * @return R<TenantVO>
     */
    @GetMapping("/tenant/detail/{id}")
    R<TenantVO> tenantInfo(@PathVariable("id") Integer id);

    /**
     * 获取租户列表（无参查询）
     *
     * @return 租户列表
     */
    @GetMapping("/tenant/list/no-param")
    R<List<Tenant>> getTenantList();

    /**
     * 获取租户列表
     *
     * @param tenantDTO tenantDTO
     * @return R<List<Tenant>>
     */
    @GetMapping("/tenant/list")
    R<List<Tenant>> tenantList(TenantDTO tenantDTO);

    /**
     * 获取租户列表(quota使用)
     *
     * @param tenantDTO tenantDTO
     * @return R<List<Tenant>>
     */
    @PostMapping("/tenant/list/quota")
    R<List<Tenant>> tenantLists(@RequestBody TenantDTO tenantDTO);

    /**
     * 获取租户列表(无权限限制)
     * 
     * @param from from
     * @return R<List<TenantVO>>
     */
    @GetMapping("/tenant/list/quotaForAudit")
    R<List<TenantVO>> getTenantInfo(@RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 获取租户列表2
     *
     * @param tenantDTO tenantDTO
     * @return R<List<Tenant>>
     */
    @PostMapping("/tenant/list/info")
    R<List<Tenant>> getListInfo(@RequestBody TenantDTO tenantDTO);

    /**
     * 根据租户ID获取是否限制配额
     *
     * @param userId 用户id
     * @return R<String>
     */
    @GetMapping("/tenant/is_limit/{userId}")
    R<String> isLimit(@PathVariable("userId") Integer userId);

    /**
     * 根据租户ID、projectID、userID获取中文名称
     * 
     * @param prefix prefix
     * @param id     id
     * @return R<String>
     */
    @GetMapping("/tenant/id-name/{prefix}/{id}")
    R<String> translateIdToName(@PathVariable("prefix") String prefix, @PathVariable("id") Integer id);

    /**
     * 根据租户ID、projectID、userID获取中文名称
     *
     * @param tenantProjectUserVOs 租户、project、用户VO
     * @return R<TenantProjectUserVO>
     */
    @PostMapping("/tenant/translation")
    R<TenantProjectUserVO> translation(@RequestBody TenantProjectUserVO tenantProjectUserVO);

    /**
     * getDashboard
     * 
     * @param inheritId inheritId
     * @param id        id
     * @return R<List<Map<String, Object>>>
     */
    @GetMapping("/tenant/dashboard/{inheritId}/{id}")
    R<List<Map<String, Object>>> getDashboard(@PathVariable(value = "inheritId") Integer inheritId,
            @PathVariable(value = "id", required = false) Integer id);

    /**
     * 根据name查tenant
     * 
     * @param name name
     * @param from from
     * @return R<Tenant>
     */
    @GetMapping("/tenant/name")
    R<Tenant> findByName(@RequestParam("name") String name, @RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 根据租户名称模糊匹配符合条件的租户列表
     * 
     * @param name name
     * @return R<List<Tenant>>
     */
    @GetMapping("/tenant/keyword")
    R<List<Tenant>> findTenantListBykeyword(@RequestParam("name") String name);

    /**
     * 根据ProjectId查询一级VDC
     * 
     * @param projectId projectId
     * @return R<TenantVO>
     */
    @GetMapping("/tenant/getOneLevelByProjectId/{projectId}")
    R<TenantVO> getOneLevelByProjectId(@PathVariable(name = "projectId") Integer projectId);

    /**
     * 根据TenantId查询一级VDC
     * 
     * @param tenantId tenantId
     * @return R<TenantVO>
     */
    @GetMapping("/tenant/getOneLevelByTenantId/{tenantId}")
    R<TenantVO> getOneLevelByTenantId(@PathVariable(name = "tenantId") Integer tenantId);

    /**
     * 根据TenantId查询父级vdc树形结构
     * 
     * @param tenantId tenantId
     * @return R<List<TenantVO>>
     */
    @GetMapping("/tenant/getParentTreeByTenantId/{tenantId}")
    public R<List<TenantVO>> getParentTreeByTenantId(@PathVariable(name = "tenantId") Integer tenantId);

    /**
     * 根据TenantId查询子级vdc列表
     * 
     * @param tenantId tenantId
     * @return R<List<Integer>>
     */
    @GetMapping("/tenant/getChildrenByTenantId/{tenantId}")
    public R<List<Integer>> getChildrenByTenantId(@PathVariable(name = "tenantId") Integer tenantId);

    /**
     * 根据TenantId查询子级vdc列表
     * 
     * @param tenantIds tenantIds
     * @return R<Map<Integer, List<Tenant>>>
     */
    @PostMapping("/tenant/getChildrenByTenantIds")
    public R<Map<Integer, List<Tenant>>> getChildrenByTenantIds(@RequestBody List<Integer> tenantIds);

    /**
     * 返回树形租户列表
     * 
     * @return R<List<TenantTree>>
     */
    @GetMapping("/tenant/list/tree")
    public R<List<TenantTree>> listTree();

    /**
     * 根据租户id查询位置树
     *
     * @param id 租户id
     * @return R<TenantLocationTree>
     */
    @GetMapping("/tenant/locationTree/{id}")
    public R<TenantLocationTree> locationTreeInfo(@PathVariable(value = "id") Integer id);
}
