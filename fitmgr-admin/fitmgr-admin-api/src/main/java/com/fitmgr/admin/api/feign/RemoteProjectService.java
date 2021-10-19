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
import com.fitmgr.admin.api.dto.ProjectDTO;
import com.fitmgr.admin.api.entity.Project;
import com.fitmgr.admin.api.entity.UserRoleProject;
import com.fitmgr.admin.api.vo.Member;
import com.fitmgr.admin.api.vo.ProjectVO;
import com.fitmgr.common.core.constant.SecurityConstants;
import com.fitmgr.common.core.constant.ServiceNameConstants;
import com.fitmgr.common.core.util.R;

/**
 * 创建人   mhp
 * 创建时间 2019/11/29
 * 描述
 **/

@FeignClient(contextId = "remoteProjectService", value = ServiceNameConstants.UMPS_SERVICE, configuration = AdminFeignConfig.class)
public interface RemoteProjectService {

    /** 根据project id获取租户信息
     * 
     * @param id project id
     * @return R
     */
    @GetMapping("/project/detail/{id}")
    R<ProjectVO> selectProject(@PathVariable("id") Integer id);

    /**
     * selectProject
     * 
     * @param id id
     * @param from from
     * @return R
     */
    @GetMapping("/project/detail/{id}")
    R<ProjectVO> selectProject(@PathVariable("id") Integer id, @RequestHeader(SecurityConstants.FROM) String from);

    /** 获取project列表
     * 
     * @param
     * @return R
     */
    @GetMapping("/project/list")
    R<List<Project>> projectList();

    /**
     * getInnerProjectList
     * 
     * @param projectDTO projectDTO
     * @param from from
     * @return R
     */
    @PostMapping("/project/list_inner")
    R<List<Project>> getInnerProjectList(@RequestBody ProjectDTO projectDTO,@RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 获取project列表(quota使用)
     *
     * @param projectDTO projectDTO
     * @return R
     */
    @PostMapping("/project/list/quota")
    R<List<Project>> projectLists(@RequestBody ProjectDTO projectDTO);

    /**
     * 获取project列表(无权限限制)
     * 
     * @param from from
     * @return R
     */
    @GetMapping("/project/list/quotaForAudit")
    R<List<ProjectVO>> getProjectInfo(@RequestHeader(SecurityConstants.FROM) String from);

    /** 获取project所有成员列表
     * 
     * @param projectId projectId
     * @return R
     */
    @GetMapping("/project/member-list/{id}")
    R<List<Member>> projectMemberList(@PathVariable("id")Integer projectId);

    /**
     * projectMemberList
     * 
     * @param projectId projectId
     * @param from from
     * @return R
     */
    @GetMapping("/project/member-list/{id}")
    R<List<Member>> projectMemberList(@PathVariable("id")Integer projectId, @RequestHeader(SecurityConstants.FROM) String from);

    /**
     * getProjectBy
     * 
     * @param userId userId
     * @param roleId roleId
     * @return R
     */
    @GetMapping("/project/by/{userId}/{roleId}")
    R<List<UserRoleProject>> getProjectBy(@PathVariable("userId") Integer userId, @PathVariable("roleId")Integer roleId);

    /**
     * findProjectByName
     * 
     * @param name name
     * @param from from
     * @return R
     */
    @GetMapping("/project/inclusion/order/project")
    R findProjectByName(@RequestParam("name")String name, @RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 根据Project名称模糊匹配符合条件的Project列表
     * 
     * @param name name
     * @return R
     */
    @GetMapping("/project/keyword")
    R<List<Project>> findProjectListBykeyword(@RequestParam("name") String name);
}
