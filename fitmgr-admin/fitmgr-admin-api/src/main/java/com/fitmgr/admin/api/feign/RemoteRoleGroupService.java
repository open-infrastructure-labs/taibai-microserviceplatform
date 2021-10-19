package com.fitmgr.admin.api.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.fitmgr.admin.api.config.AdminFeignConfig;
import com.fitmgr.admin.api.entity.RoleGroup;
import com.fitmgr.admin.api.vo.RoleGroupVO;
import com.fitmgr.common.core.constant.ServiceNameConstants;
import com.fitmgr.common.core.util.R;

/**
 * 创建人 dzl 创建时间 2020/2/29 描述
 **/

@FeignClient(contextId = "remoteRoleGroupService", value = ServiceNameConstants.UMPS_SERVICE, configuration = AdminFeignConfig.class)
public interface RemoteRoleGroupService {

    /**
     * 查询角色分组列表
     *
     * @param id 角色id
     * @return R
     */
    @GetMapping("/roleGroup/list")
    public R<List<RoleGroup>> getRoles();

    /**
     * 通过角色id获取角色分组详情
     * 
     * @param id id
     * @return R
     */
    @GetMapping("/roleGroup/{id}")
    public R<RoleGroupVO> getByRole(@PathVariable(name = "id") Integer id);

    /**
     * 通过角色分组名称获取角色分组详情
     * 
     * @param name name
     * @return R
     */
    @GetMapping("/roleGroup/getByName/{name}")
    public R<RoleGroupVO> getByName(@PathVariable(name = "name") String name);
}
