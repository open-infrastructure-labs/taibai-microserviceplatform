package com.taibai.admin.service;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.taibai.admin.api.dto.NetworkPoolDTO;
import com.taibai.admin.api.dto.TenantDTO;
import com.taibai.admin.api.dto.TenantTree;
import com.taibai.admin.api.entity.LocationTreeNode;
import com.taibai.admin.api.entity.SubDomainConfigSwitch;
import com.taibai.admin.api.entity.Tenant;
import com.taibai.admin.api.entity.TenantAdmin;
import com.taibai.admin.api.entity.TenantResourcePool;
import com.taibai.admin.api.entity.UserRoleProject;
import com.taibai.admin.api.vo.ImportTenantVo;
import com.taibai.admin.api.vo.ImportUserVo;
import com.taibai.admin.api.vo.Member;
import com.taibai.admin.api.vo.TenantProjectUserVO;
import com.taibai.admin.api.vo.TenantResourcePoolVO;
import com.taibai.admin.api.vo.TenantVO;
import com.taibai.common.core.util.R;

/**
 * 创建人 mhp 创建时间 2019/11/12 描述
 **/
public interface ITenantService extends IService<Tenant> {

    /**
     * 保存租户信息
     * 
     * @param tenantDTO tenantDTO
     * @return R
     */
    R saveTenant(TenantDTO tenantDTO);

    /**
     * 删除租户
     * 
     * @param tenantId tenantId
     * @return boolean
     */
    boolean deleteTenant(Integer tenantId);

    /**
     * 更新租户信息
     * 
     * @param tenantDTO tenantDTO
     * @return boolean
     */
    boolean updateTenant(TenantDTO tenantDTO);

    /**
     * 根据条件 分页查询租户列表
     *
     * @param page      分页条件
     * @param tenantDTO 条件对象
     * @param userId    用户id
     * @return IPage
     */
    IPage<TenantVO> tenantList(Page page, TenantDTO tenantDTO, Integer userId);

    /**
     * 根据租户id查询租户信息
     *
     * @param tenantId 租户id
     * @return 租户
     */
    TenantVO selectTenantVoById(Integer tenantId);

    /**
     * 根据用户id查询租户信息
     *
     * @param userId 用户id
     * @return 租户
     */
    TenantVO selectByUserId(Integer userId);

    /**
     * 条件查询租户列表
     *
     * @param tenantDTO 条件对象
     * @return 租户列表
     */
    List<Tenant> tenantList(TenantDTO tenantDTO);

    /**
     * 修改租户启用禁用状态
     *
     * @param tenant 租户对象
     * @return 修改记录数
     */
    int updateStatus(Tenant tenant);

    /**
     * 指定租户管理员
     *
     * @param tenantAdmin tenantAdmin
     * @return R
     */
    R saveAdmin(TenantAdmin tenantAdmin);

    /**
     * 指定租户配额管理员
     *
     * @param tenantAdmin tenantAdmin
     * @return R
     */
    R saveQuotaAdmin(TenantAdmin tenantAdmin);

    /**
     * 根据租户ID或projectID获取中文名称
     * 
     * @param prefix prefix
     * @param id     id
     * @return String
     */
    String translateIdToName(String prefix, Integer id);

    /**
     * 根据租户ID和projectID获取中文名称
     *
     * @param tenantProjectUserVO 租户、project、用户VO
     * @return R<TenantProjectUserVO>
     */
    R<TenantProjectUserVO> translation(TenantProjectUserVO tenantProjectUserVO);

    /**
     * 租户绑定资源池
     * 
     * @param tenantResourcePool tenantResourcePool
     * @return R
     */
    R bind(TenantResourcePool tenantResourcePool);

    /**
     * 租户取消绑定资源池
     * 
     * @param tenantResourcePool tenantResourcePool
     * @return boolean
     */
    boolean quitBind(TenantResourcePool tenantResourcePool);

    /**
     * 查询租户绑定的资源池列表
     *
     * @param page             page
     * @param tenantId         租户id
     * @param resourcePoolCode 资源池组件code
     * @return 列表
     */
    IPage<TenantResourcePoolVO> selectBindResourcePools(Page page, Integer tenantId, String resourcePoolCode);

    /**
     * addMember
     * 
     * @param list list
     * @return R
     */
    R addMember(List<UserRoleProject> list);

    /**
     * removeMember
     * 
     * @param userRoleProject userRoleProject
     * @param authHeader      authHeader
     * @return R
     */
    R removeMember(UserRoleProject userRoleProject, String authHeader);

    /**
     * allListMember
     * 
     * @param page      page
     * @param tenantIds tenantIds
     * @param name      name
     * @return IPage
     */
    IPage allListMember(Page page, List<Integer> tenantIds, String name);

    /**
     * listMember
     * 
     * @param page     page
     * @param tenantId tenantId
     * @param name     name
     * @return IPage
     */
    IPage listMember(Page page, Integer tenantId, String name);

    /**
     * listMember
     * 
     * @param tenantId tenantId
     * @param status   status
     * @return List<Member>
     */
    List<Member> listMember(Integer tenantId, String status);

    /**
     * updateMemberRole
     * 
     * @param userRoleProject userRoleProject
     * @return R
     */
    R updateMemberRole(UserRoleProject userRoleProject);

    /**
     * queryTenantTree
     * 
     * @param createProject createProject
     * @param vdcId         vdcId
     * @return List<TenantTree>
     */
    List<TenantTree> queryTenantTree(String createProject, Integer vdcId);

    /**
     * queryAllChildTenant
     * 
     * @param tenantId tenantId
     * @return List<TenantTree>
     */
    List<TenantTree> queryAllChildTenant(Integer tenantId);

    /**
     * queryUserForAddMember
     * 
     * @param page      page
     * @param tenantId  tenantId
     * @param queryName queryName
     * @return IPage
     */
    IPage queryUserForAddMember(Page page, Integer tenantId, String queryName);

  /**
   * 条件查询租户列表
   * @param tenantDTO
   * @return
   */
  List<Tenant> tenantLists(TenantDTO tenantDTO);
  
  public R<SubDomainConfigSwitch> querySubDomainConfigSwitch(String networkPoolTypeEnum);
  
  public R updateSubDomainConfigSwitch(SubDomainConfigSwitch subDomainConfigSwitch);
  
  
  R importTenant(ImportTenantVo importTenantVo);
  
  void downloadFail(HttpServletResponse response, String bucket, String fileName) throws Exception;
  
  ImportUserVo queryProgress(String bucket, String fileName);
  
  List<ImportTenantVo> queryLogs();

    /**
     * queryPoolResources
     * 
     * @param id                id
     * @param type              type
     * @param parentId          parentId
     * @param cloudPlatformType cloudPlatformType
     * @param networkProvider   networkProvider
     * @return R<List<LocationTreeNode>>
     */
    public R<List<LocationTreeNode>> queryPoolResources(Integer id, String type, String parentId,
            String cloudPlatformType, String networkProvider);

    /**
     * queryNetworkPool
     * 
     * @param id                  id
     * @param networkPoolTypeEnum networkPoolTypeEnum
     * @param resourceZoneId      resourceZoneId
     * @param networkType         networkType
     * @param action              action
     * @return R<NetworkPoolDTO>
     */
    public R<NetworkPoolDTO> queryNetworkPool(Integer id, String networkPoolTypeEnum, String resourceZoneId,
            String networkType, String action);

    /**
     * updateNetworkPool
     * 
     * @param networkPoolDTO networkPoolDTO
     * @return R
     */
    public R updateNetworkPool(NetworkPoolDTO networkPoolDTO);
}
