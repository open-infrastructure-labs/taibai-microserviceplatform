package com.fitmgr.admin.api.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.fitmgr.admin.api.config.AdminFeignConfig;
import com.fitmgr.admin.api.dto.SwitchUserVdcDTO;
import com.fitmgr.admin.api.entity.Role;
import com.fitmgr.common.core.constant.SecurityConstants;
import com.fitmgr.common.core.constant.ServiceNameConstants;
import com.fitmgr.common.core.util.R;

/**
 * 创建人 dzl 创建时间 2020/2/29 描述
 **/

@FeignClient(contextId = "remoteRoleService", value = ServiceNameConstants.UMPS_SERVICE, configuration = AdminFeignConfig.class)
public interface RemoteRoleService {

    /**
     * 通过角色id查询角色详情
     *
     * @param id 角色id
     * @return R
     */
    @GetMapping("/role/{id}")
    R<Role> getByRole(@PathVariable("id") Integer id);

    /**
     * 通过角色id查询角色详情，内部调用
     * 
     * @param id   id
     * @param from from
     * @return R
     */
    @GetMapping("/role/inner/{id}")
    R<Role> getInnerByRole(@PathVariable("id") Integer id, @RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 获取所有角色列表
     *
     * @return R
     */
    @GetMapping("/role/role-list")
    R<List<Role>> getRoles();

    /**
     * getRoleListByTwoId
     * 
     * @param userId    userId
     * @param projectId projectId
     * @return R
     */
    @GetMapping("/role/role-list/byTwoId")
    R<List<Role>> getRoleListByTwoId(@RequestParam("userId") Integer userId,
            @RequestParam("projectId") Integer projectId);

    /**
     * getRoleList
     * 
     * @param tenantId tenantId
     * @return R
     */
    @GetMapping("/role/config-list")
    R<List<Role>> getRoleList(@RequestParam(value = "tenantId", required = false) Integer tenantId);

    /**
     * switchUserVDC
     * 
     * @param switchUserVdcDTO switchUserVdcDTO
     * @return R
     */
    @PostMapping("/role/switch-user-vdc")
    R switchUserVdc(@RequestBody SwitchUserVdcDTO switchUserVdcDTO);
}
