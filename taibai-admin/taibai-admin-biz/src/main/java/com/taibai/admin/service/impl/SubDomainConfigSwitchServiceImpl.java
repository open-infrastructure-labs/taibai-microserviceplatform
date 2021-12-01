package com.taibai.admin.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taibai.admin.api.entity.SubDomainConfigSwitch;
import com.taibai.admin.mapper.SubDomainConfigSwitchMapper;
import com.taibai.admin.service.ISubDomainConfigSwitchService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 分域配置开关表 服务实现类
 * </p>
 *
 * @author Taibai
 * @since 2019-11-16
 */
@Slf4j
@Service
@AllArgsConstructor
public class SubDomainConfigSwitchServiceImpl extends ServiceImpl<SubDomainConfigSwitchMapper, SubDomainConfigSwitch>
        implements ISubDomainConfigSwitchService {

    private final SubDomainConfigSwitchMapper subDomainConfigSwitchMapper;

}
