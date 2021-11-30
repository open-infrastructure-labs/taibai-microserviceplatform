package com.taibai.admin.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taibai.admin.api.entity.LockedAccountRecord;
import com.taibai.admin.mapper.LockedAccountRecordMapper;
import com.taibai.admin.service.ILockedAccountRecordService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 锁定用户记录表 服务实现类
 * </p>
 *
 * @author Taibai
 * @since 2019-11-16
 */
@Slf4j
@Service
@AllArgsConstructor
public class LockedAccountRecordServiceImpl extends ServiceImpl<LockedAccountRecordMapper, LockedAccountRecord>
        implements ILockedAccountRecordService {

    private final LockedAccountRecordMapper lockedAccountRecordMapper;

}
