package com.taibai.admin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taibai.admin.api.dto.TenantDTO;
import com.taibai.admin.api.entity.Tenant;
import com.taibai.admin.api.entity.TenantResourcePool;
import com.taibai.admin.api.entity.UserRoleProject;
import com.taibai.admin.api.vo.Member;
import com.taibai.admin.api.vo.TenantProjectUserVO;
import com.taibai.admin.api.vo.TenantResourcePoolVO;
import com.taibai.admin.api.vo.TenantVO;

/**
 * <p>
 * 租户表 Mapper 接口
 * </p>
 *
 * @author Taibai
 * @since 2019-11-12
 */
public interface TenantMapper extends BaseMapper<Tenant> {

    /**
     * selectListByCondition
     * 
     * @param page      page
     * @param tenantDTO tenantDTO
     * @return IPage<TenantVO>
     */
    IPage<TenantVO> selectListByCondition(@Param("page") Page page, @Param("tenantDTO") TenantDTO tenantDTO);

    /**
     * selectListByConditionNoPage
     * 
     * @param tenantDTO tenantDTO
     * @return List<TenantVO>
     */
    List<TenantVO> selectListByConditionNoPage(@Param("tenantDTO") TenantDTO tenantDTO);

    /**
     * 根据租户id查询租户信息
     *
     * @param tenantId 租户id
     * @return 租户信息
     */
    TenantVO selectTenantVoById(Integer tenantId);

    /**
     * 根据用户id查询租户
     *
     * @param userId 用户id
     * @return 租户信息
     */
    TenantVO selectByUserId(Integer userId);

    /**
     * 查询租户启用禁用状态
     *
     * @param tenantId 租户id
     * @return status启用禁用状态
     */
    String getTenantStatus(Integer tenantId);

    /**
     * 根据租户ID和projectID获取中文名称
     *
     * @param tenantId  tenantId
     * @param projectId projectId
     * @param userId    userId
     * @return TenantProjectUserVO
     */
    TenantProjectUserVO translation(@Param("tenantId") Integer tenantId, @Param("projectId") Integer projectId,
            @Param("userId") Integer userId);

    /**
     * 根据租户ID和projectID获取中文名称
     *
     * @param tenantId tenantId
     * @param userId   userId
     * @return TenantProjectUserVO
     */
    TenantProjectUserVO translationNoProject(@Param("tenantId") Integer tenantId, @Param("userId") Integer userId);

    /**
     * 租户绑定资源池
     * 
     * @param tenantResourcePool tenantResourcePool
     * @return int
     */
    int bind(TenantResourcePool tenantResourcePool);

    /**
     * 租户取消绑定资源池
     * 
     * @param tenantResourcePool tenantResourcePool
     * @return int
     */
    int quitBind(TenantResourcePool tenantResourcePool);

    /**
     * 查询租户与资源池绑定列表
     *
     * @param page             page
     * @param tenantId         租户id
     * @param resourcePoolCode 资源池code
     * @return 绑定关系列表
     */
    IPage<TenantResourcePoolVO> selectBindList(Page page, @Param("tenantId") Integer tenantId,
            @Param("resourcePoolCode") String resourcePoolCode);

    /**
     * listMember
     * 
     * @param tenantId tenantId
     * @param status   status
     * @return List<Member>
     */
    List<Member> listMember(@Param("tenantId") Integer tenantId, @Param("status") String status);

    /**
     * addMember
     * 
     * @param list list
     */
    void addMember(@Param("list") List<UserRoleProject> list);

    /**
     * removeMember
     * 
     * @param userRoleProject userRoleProject
     */
    void removeMember(@Param("userRoleProject") UserRoleProject userRoleProject);

    /**
     * listMemberPage
     * 
     * @param page     page
     * @param tenantId tenantId
     * @return IPage
     */
    IPage listMemberPage(@Param("page") Page page, @Param("tenantId") Integer tenantId);

    /**
     * listMemberByName
     * 
     * @param page     page
     * @param tenantId tenantId
     * @param name     name
     * @return IPage
     */
    IPage listMemberByName(@Param("page") Page page, @Param("tenantId") Integer tenantId, @Param("name") String name);

    /**
     * allListMemberByName
     * 
     * @param page      page
     * @param tenantIds tenantIds
     * @param name      name
     * @return IPage
     */
    IPage allListMemberByName(@Param("page") Page page, @Param("tenantIds") List<Integer> tenantIds,
            @Param("name") String name);
}
