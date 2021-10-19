package com.fitmgr.admin.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitmgr.admin.api.entity.MaxHisPassCount;
import com.fitmgr.admin.mapper.MaxHisPassCountMapper;
import com.fitmgr.admin.service.IMaxHisPassCountService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 最大历史密码次数表 服务实现类
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
@Slf4j
@Service
@AllArgsConstructor
public class MaxHisPassCountServiceImpl extends ServiceImpl<MaxHisPassCountMapper, MaxHisPassCount>
        implements IMaxHisPassCountService {

    private final MaxHisPassCountMapper maxHisPassCountMapper;

}
