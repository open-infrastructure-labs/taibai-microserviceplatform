package com.fitmgr.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fitmgr.admin.api.entity.TenantLocationTree;

/**
 * <p>
 * VDC位置树表 服务类
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
public interface ITenantLocationTreeService extends IService<TenantLocationTree> {
    /**
     * updateByTenantId
     * 
     * @param tenantLocationTree tenantLocationTree
     * @return int
     */
    int updateByTenantId(TenantLocationTree tenantLocationTree);
}
