package com.taibai.admin.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taibai.admin.api.entity.AccountLockStrategy;
import com.taibai.admin.mapper.AccountLockStrategyMapper;
import com.taibai.admin.service.IAccountLockStrategyService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 账号锁定策略配置表 服务实现类
 * </p>
 *
 * @author Taibai
 * @since 2019-11-16
 */
@Slf4j
@Service
@AllArgsConstructor
public class AccountLockStrategyServiceImpl extends ServiceImpl<AccountLockStrategyMapper, AccountLockStrategy>
        implements IAccountLockStrategyService {

    private final AccountLockStrategyMapper accountLockStrategyMapper;

}
