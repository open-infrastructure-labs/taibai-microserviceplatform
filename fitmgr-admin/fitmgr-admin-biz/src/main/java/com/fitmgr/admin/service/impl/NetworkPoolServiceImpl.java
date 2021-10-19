package com.fitmgr.admin.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitmgr.admin.api.entity.NetworkPool;
import com.fitmgr.admin.mapper.NetworkPoolMapper;
import com.fitmgr.admin.service.INetworkPoolService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 全量网络池信息表 服务实现类
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
@Slf4j
@Service
@AllArgsConstructor
public class NetworkPoolServiceImpl extends ServiceImpl<NetworkPoolMapper, NetworkPool> implements INetworkPoolService {

    private final NetworkPoolMapper networkPoolMapper;

}
