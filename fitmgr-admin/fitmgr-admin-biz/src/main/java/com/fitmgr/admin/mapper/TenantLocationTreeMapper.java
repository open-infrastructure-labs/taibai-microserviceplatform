package com.fitmgr.admin.mapper;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitmgr.admin.api.entity.TenantLocationTree;

/**
 * <p>
 * distributableRoles VDC位置树表 Mapper 接口
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
public interface TenantLocationTreeMapper extends BaseMapper<TenantLocationTree> {
    /**
     * updateByTenantId
     * 
     * @param tenantLocationTree tenantLocationTree
     * @return int
     */
    int updateByTenantId(@Param("tenantLocationTree") TenantLocationTree tenantLocationTree);
}
