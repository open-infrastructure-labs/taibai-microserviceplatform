package com.fitmgr.admin.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitmgr.admin.api.entity.SubDomainConfigSwitch;
import com.fitmgr.admin.mapper.SubDomainConfigSwitchMapper;
import com.fitmgr.admin.service.ISubDomainConfigSwitchService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 分域配置开关表 服务实现类
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
@Slf4j
@Service
@AllArgsConstructor
public class SubDomainConfigSwitchServiceImpl extends ServiceImpl<SubDomainConfigSwitchMapper, SubDomainConfigSwitch>
        implements ISubDomainConfigSwitchService {

    private final SubDomainConfigSwitchMapper subDomainConfigSwitchMapper;

}