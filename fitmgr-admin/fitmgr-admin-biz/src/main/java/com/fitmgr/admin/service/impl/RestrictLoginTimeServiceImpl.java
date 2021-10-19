package com.fitmgr.admin.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fitmgr.admin.api.entity.RestrictLloginTime;
import com.fitmgr.admin.mapper.RestrictLloginTimeMapper;
import com.fitmgr.admin.service.IRestrictLoginTimeService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 不允许访问时间段配置表 服务实现类
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
@Slf4j
@Service
@AllArgsConstructor
public class RestrictLoginTimeServiceImpl implements IRestrictLoginTimeService {

    private final RestrictLloginTimeMapper restrictLloginTimeMapper;

    @Override
    public List<RestrictLloginTime> list() {
        return restrictLloginTimeMapper.list();
    }

    @Override
    public boolean updateById(RestrictLloginTime restrictLloginTime) {
        int a = restrictLloginTimeMapper.updateById(restrictLloginTime);
        if (a > 0) {
            return true;
        } else {
            return false;
        }
    }

}
