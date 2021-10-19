package com.fitmgr.admin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitmgr.admin.api.dto.ProjectDTO;
import com.fitmgr.admin.api.entity.Project;
import com.fitmgr.admin.api.entity.UserRoleProject;
import com.fitmgr.admin.api.vo.Member;
import com.fitmgr.admin.api.vo.ProjectVO;

/**
 * <p>
 * project表 Mapper 接口
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-15
 */
public interface ProjectMapper extends BaseMapper<Project> {

    /**
     * 根据项目id查询ProjectVO信息
     * 
     * @param projectId projectId
     * @return ProjectVO
     */
    ProjectVO selectProjectVoById(Integer projectId);

    /**
     * 根据条件分页查询ProjectVO列表
     *
     * @param page       分页参数
     * @param projectDTO DTO对象
     * @return ProjectVO列表
     */
    IPage<ProjectVO> listByCondition(@Param("page") Page page, @Param("projectDTO") ProjectDTO projectDTO);

    /**
     * 查询ProjectVO列表
     * 
     * @return List<ProjectVO>
     */
    List<ProjectVO> getProjectListInfo();

    /**
     * project批量加入成员
     *
     * @param list userProject对象集合
     */
    void addMember(@Param("list") List<UserRoleProject> list);

    /**
     * project移除成员
     *
     * @param userRoleProject userProject对象
     */
    void removeMember(@Param("userRoleProject") UserRoleProject userRoleProject);

    /**
     * 分页查询project成员列表
     *
     * @param page      分页条件
     * @param projectId projectId
     * @return IPage
     */
    IPage listMemberPage(@Param("page") Page page, @Param("projectId") Integer projectId);

    /**
     * listMemberByName
     * 
     * @param page      projectId
     * @param projectId projectId
     * @param name      name
     * @return IPage
     */
    IPage listMemberByName(@Param("page") Page page, @Param("projectId") Integer projectId, @Param("name") String name);

    /**
     * 获取project所有成员
     *
     * @param projectId projectId
     * @return List<Member>
     */
    List<Member> listMember(@Param("projectId") Integer projectId);

    /**
     * 修改project成员角色
     *
     * @param userRoleProject UserRoleProject对象
     */
    void updateMemberRole(UserRoleProject userRoleProject);

    /**
     * 通过角色id和用户id获取project列表
     *
     * @param userId userId
     * @param roleId roleId
     * @return List<Project>
     */
    List<Project> getProjectList(@Param("userId") Integer userId, @Param("roleId") Integer roleId);

    /**
     * 获取用户所在的project列表
     *
     * @param id 用户id
     * @return project列表
     */
    List<Project> getProjectsByCurrentUser(@Param("id") Integer id);

    /**
     * listNoPageByCondition
     * 
     * @param projectDTO projectDTO
     * @return List<Project>
     */
    List<Project> listNoPageByCondition(@Param("projectDTO") ProjectDTO projectDTO);
}
