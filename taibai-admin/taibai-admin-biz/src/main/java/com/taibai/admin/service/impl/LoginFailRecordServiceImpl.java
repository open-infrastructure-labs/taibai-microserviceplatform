package com.taibai.admin.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taibai.admin.api.entity.LoginFailRecord;
import com.taibai.admin.mapper.LoginFailRecordMapper;
import com.taibai.admin.service.ILoginFailRecordService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 登录失败记录表 服务实现类
 * </p>
 *
 * @author Taibai
 * @since 2019-11-16
 */
@Slf4j
@Service
@AllArgsConstructor
public class LoginFailRecordServiceImpl extends ServiceImpl<LoginFailRecordMapper, LoginFailRecord>
        implements ILoginFailRecordService {

    private final LoginFailRecordMapper loginFailRecordMapper;

}
