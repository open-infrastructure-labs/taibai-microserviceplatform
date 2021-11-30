package com.taibai.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taibai.admin.api.entity.User;
import com.taibai.admin.api.entity.UserLoginRecord;
import com.taibai.admin.mapper.UserLoginRecordMapper;
import com.taibai.admin.mapper.UserMapper;
import com.taibai.admin.service.UserLoginRecordService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class UserLoginRecordServiceImpl extends ServiceImpl<UserLoginRecordMapper, UserLoginRecord> implements UserLoginRecordService {

    private final UserMapper userMapper;

    private final UserLoginRecordMapper userLoginRecordMapper;

    @Override
    public UserLoginRecord queryUserCount(String username){
        User user = userMapper.selectOne(new QueryWrapper<User>().lambda().eq(User::getUsername, username));
        if(user == null) {
            return null;
        }

        return userLoginRecordMapper.selectOne(new QueryWrapper<UserLoginRecord>().lambda().eq(UserLoginRecord::getUserId, user.getId()));
    }

    @Override
    public void addUserLoginRecord(String username){
        User user = userMapper.selectOne(new QueryWrapper<User>().lambda().eq(User::getUsername, username));
        if(user == null) {
            return;
        }

        UserLoginRecord userLoginRecord = userLoginRecordMapper.selectOne(new QueryWrapper<UserLoginRecord>().lambda().eq(UserLoginRecord::getUserId, user.getId()));
        if(userLoginRecord == null) {
            userLoginRecord = new UserLoginRecord();
            userLoginRecord.setLoginCount(1L);
            userLoginRecord.setUserId(user.getId());
            userLoginRecordMapper.insert(userLoginRecord);
        } else {
            userLoginRecord.setLoginCount(userLoginRecord.getLoginCount() + 1);
            userLoginRecordMapper.updateById(userLoginRecord);
        }
    }
}
