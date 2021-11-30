package com.taibai.admin.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taibai.admin.api.entity.TenantNetworkPool;
import com.taibai.admin.mapper.TenantNetworkPoolMapper;
import com.taibai.admin.service.ITenantNetworkPoolService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * VDC网络池信息表 服务实现类
 * </p>
 *
 * @author Taibai
 * @since 2019-11-16
 */
@Slf4j
@Service
@AllArgsConstructor
public class TenantNetworkPoolServiceImpl extends ServiceImpl<TenantNetworkPoolMapper, TenantNetworkPool>
        implements ITenantNetworkPoolService {

    private final TenantNetworkPoolMapper tenantNetworkPoolMapper;

}
