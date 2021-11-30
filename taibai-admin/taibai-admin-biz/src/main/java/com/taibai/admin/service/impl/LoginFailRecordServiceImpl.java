package com.fitmgr.admin.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitmgr.admin.api.entity.LoginFailRecord;
import com.fitmgr.admin.mapper.LoginFailRecordMapper;
import com.fitmgr.admin.service.ILoginFailRecordService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 登录失败记录表 服务实现类
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
@Slf4j
@Service
@AllArgsConstructor
public class LoginFailRecordServiceImpl extends ServiceImpl<LoginFailRecordMapper, LoginFailRecord>
        implements ILoginFailRecordService {

    private final LoginFailRecordMapper loginFailRecordMapper;

}
