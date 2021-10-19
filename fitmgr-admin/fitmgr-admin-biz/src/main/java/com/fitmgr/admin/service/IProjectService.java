package com.fitmgr.admin.service;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fitmgr.admin.api.dto.ProjectDTO;
import com.fitmgr.admin.api.entity.Project;
import com.fitmgr.admin.api.entity.UserRoleProject;
import com.fitmgr.admin.api.vo.Member;
import com.fitmgr.admin.api.vo.ProjectVO;
import com.fitmgr.admin.api.vo.UserVO;
import com.fitmgr.common.core.util.R;

/**
 * 创建人 mhp 创建时间 2019/11/15 描述
 **/
public interface IProjectService extends IService<Project> {

    /**
     * 添加project
     * 
     * @param projectDTO projectDTO
     * @return boolean
     */
    boolean saveProject(ProjectDTO projectDTO);

    /**
     * 根据project id删除project
     * 
     * @param projectId projectId
     * @return boolean
     */
    boolean deleteProject(Integer projectId);

    /**
     * 修改project
     * 
     * @param projectDTO projectDTO
     * @return R
     */
    R updateProject(ProjectDTO projectDTO);

    /**
     * 根据project id 查询projectVO
     *
     * @param projectId project id
     * @return ProjectVO
     */
    ProjectVO selectById(Integer projectId);

    /**
     * 根据条件分页查询ProjectVO列表
     *
     * @param page       分页条件对象
     * @param projectDTO DTO对象
     * @param userId     用户id
     * @return IPage
     */
    IPage<ProjectVO> pageList(Page page, ProjectDTO projectDTO, Integer userId);

    /**
     * 批量添加project成员
     *
     * @param list UserRoleProject对象集合
     */
    void addMember(List<UserRoleProject> list);

    /**
     * project删除成员
     *
     * @param userRoleProject UserRoleProject对象
     */
    void removeMember(UserRoleProject userRoleProject);

    /**
     * listCondition
     * 
     * @param projectDTO projectDTO
     * @return List<Project>
     */
    List<Project> listCondition(ProjectDTO projectDTO);

    /**
     * listConditionInnerProject
     * 
     * @param projectDTO projectDTO
     * @param form       form
     * @return List<Project>
     */
    List<Project> listConditionInnerProject(ProjectDTO projectDTO, String form);

    /**
     * 通过TenantId 查询ProjectList列表
     * 
     * @param id id
     * @return List<Project>
     */
    List<Project> projectListByTenantId(Integer id);

    /**
     * 分页查询project成员列表
     * 
     * @param page      page
     * @param projectId projectId
     * @param name      name
     * @return IPage
     */
    IPage listMember(Page page, Integer projectId, String name);

    /**
     * 获取project所有成员
     *
     * @param projectId projectId
     * @return List<Member>
     */
    List<Member> listMember(Integer projectId);

    /**
     * project未绑定成员查询
     * 
     * @param page      page
     * @param tenantId  tenantId
     * @param projectId projectId
     * @param queryName queryName
     * @return IPage<UserVO>
     */
    IPage<UserVO> queryUserForAddMember(Page page, Integer tenantId, Integer projectId, String queryName);

    /**
     * 修改project成员角色
     * 
     * @param userRoleProject userRoleProject
     * @return R
     */
    R updateMemberRole(UserRoleProject userRoleProject);

    /**
     * 修改启用禁用状态
     *
     * @param project project对象
     * @return 修改记录数
     */
    int updateStatus(Project project);

    /**
     * findProject
     * 
     * @param name name
     * @return ProjectDTO
     */
    ProjectDTO findProject(String name);

    /**
     * 更新project所属tenantId
     *
     * @param projectId projectId
     * @param tenantId  tenantId
     * @return int
     */
    int updateProjectTenantId(Integer projectId, Integer tenantId);

    /**
     * 条件查询project列表
     * 
     * @param projectDTO projectDTO
     * @return List<Project>
     */
    List<Project> projectList(ProjectDTO projectDTO);
}
