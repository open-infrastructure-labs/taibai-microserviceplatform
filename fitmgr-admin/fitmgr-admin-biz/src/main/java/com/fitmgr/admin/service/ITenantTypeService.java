package com.fitmgr.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fitmgr.admin.api.dto.TenantTypeDTO;
import com.fitmgr.admin.api.entity.TenantTypeResourcePool;
import com.fitmgr.admin.api.entity.TenantType;
import com.fitmgr.admin.api.vo.TenantTypeVO;
import com.fitmgr.common.core.util.R;

import java.util.List;

/**
 * 创建人   mhp
 * 创建时间 2019/11/14
 * 描述
 **/
public interface ITenantTypeService extends IService<TenantType> {

    /** 添加租户类型
     * @param tenantTypeDTO DTO
     */
    void saveTenantType(TenantTypeDTO tenantTypeDTO);

    /** 修改租户类型
     * @param tenantTypeDTO DTO
     */
    void updateTenantType(TenantTypeDTO tenantTypeDTO);

    /** 根据租户类型id删除租户类型
     * @param tenantTypeId 租户类型id
     */
    void deleteTenantType(Integer tenantTypeId);

    /** 分页查询租户类型列表
     * @param page 分页条件
     * @return 租户类型列表
     */
    IPage<TenantTypeVO> selectListPage(Page page);

}
