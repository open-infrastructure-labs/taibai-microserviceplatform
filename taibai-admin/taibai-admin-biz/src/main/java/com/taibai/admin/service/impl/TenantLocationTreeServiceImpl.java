package com.fitmgr.admin.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitmgr.admin.api.entity.TenantLocationTree;
import com.fitmgr.admin.mapper.TenantLocationTreeMapper;
import com.fitmgr.admin.service.ITenantLocationTreeService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * VDC位置树表 服务实现类
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
@Slf4j
@Service
@AllArgsConstructor
public class TenantLocationTreeServiceImpl extends ServiceImpl<TenantLocationTreeMapper, TenantLocationTree>
        implements ITenantLocationTreeService {

    private final TenantLocationTreeMapper tenantLocationTreeMapper;

    @Override
    public int updateByTenantId(TenantLocationTree tenantLocationTree) {
        return tenantLocationTreeMapper.updateByTenantId(tenantLocationTree);
    }

}
