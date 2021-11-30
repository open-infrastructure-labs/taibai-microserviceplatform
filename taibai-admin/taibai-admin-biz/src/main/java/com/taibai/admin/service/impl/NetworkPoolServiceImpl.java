package com.taibai.admin.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taibai.admin.api.entity.NetworkPool;
import com.taibai.admin.mapper.NetworkPoolMapper;
import com.taibai.admin.service.INetworkPoolService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 全量网络池信息表 服务实现类
 * </p>
 *
 * @author Taibai
 * @since 2019-11-16
 */
@Slf4j
@Service
@AllArgsConstructor
public class NetworkPoolServiceImpl extends ServiceImpl<NetworkPoolMapper, NetworkPool> implements INetworkPoolService {

    private final NetworkPoolMapper networkPoolMapper;

}
