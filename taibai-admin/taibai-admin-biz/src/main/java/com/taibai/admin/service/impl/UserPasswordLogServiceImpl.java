package com.taibai.admin.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taibai.admin.api.entity.UserPasswordLog;
import com.taibai.admin.mapper.MaxHisPassCountMapper;
import com.taibai.admin.mapper.UserPasswordLogMapper;
import com.taibai.admin.service.IUserPasswordLogService;
import com.taibai.common.core.constant.SecurityConstants;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 用户密码记录表 服务实现类
 * </p>
 *
 * @author Taibai
 * @since 2020-05-20
 */
@Slf4j
@Service
@AllArgsConstructor
public class UserPasswordLogServiceImpl extends ServiceImpl<UserPasswordLogMapper, UserPasswordLog>
        implements IUserPasswordLogService {

    private final MaxHisPassCountMapper maxHisPassCountMapper;

    @Override
    public Boolean saveUserPasswordLog(String password, Integer id) {
        UserPasswordLog userPasswordLog = new UserPasswordLog();
        userPasswordLog.setPasswordLog(password);
        userPasswordLog.setUserId(id);
        userPasswordLog.setCreateTime(LocalDateTime.now());
        int insert = baseMapper.insert(userPasswordLog);
        if (insert > 0) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean deleteUserPasswordLog(Integer id) {
        this.removeById(id);
        return Boolean.TRUE;
    }

    @Override
    public Boolean userPasswordLogLimit(Integer id) {
        // 通过id先统计出日志记录个数 > 6,则进行处理
        Integer logNumber = passwordLogCount(id);
        for (Integer i = logNumber; i > SecurityConstants.HIS_PASS; i--) {
            // 获取最早的密码日志纪录，并获取其id
            UserPasswordLog userPasswordLog = earlyLog(id);
            deleteUserPasswordLog(userPasswordLog.getId());
        }
        return Boolean.TRUE;
    }

    @Override
    public Integer passwordLogCount(Integer userId) {
        LambdaQueryWrapper<UserPasswordLog> queryWrapper = Wrappers.<UserPasswordLog>lambdaQuery()
                .eq(UserPasswordLog::getUserId, userId);
        Integer integer = baseMapper.selectCount(queryWrapper);
        return integer;
    }

    @Override
    public UserPasswordLog earlyLog(Integer userId) {
        LambdaQueryWrapper<UserPasswordLog> queryWrapper = Wrappers.<UserPasswordLog>lambdaQuery()
                .eq(UserPasswordLog::getUserId, userId).orderByAsc(UserPasswordLog::getCreateTime);
        List<UserPasswordLog> userPasswordLogs = baseMapper.selectList(queryWrapper);
        UserPasswordLog userPasswordLog = userPasswordLogs.get(0);
        return userPasswordLog;
    }

    @Override
    public List<UserPasswordLog> userPasswordLogList(Integer userId) {
        LambdaQueryWrapper<UserPasswordLog> queryWrapper = Wrappers.<UserPasswordLog>lambdaQuery()
                .eq(UserPasswordLog::getUserId, userId).orderByDesc(UserPasswordLog::getCreateTime);
        List<UserPasswordLog> userPasswordLogs = baseMapper.selectList(queryWrapper);
        return userPasswordLogs;
    }

}
