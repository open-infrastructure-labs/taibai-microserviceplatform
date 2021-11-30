package com.taibai.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taibai.admin.api.entity.UserLoginRecord;

public interface UserLoginRecordService extends IService<UserLoginRecord> {
    /**
     * queryUserCount
     * 
     * @param username username
     * @return UserLoginRecord
     */
    UserLoginRecord queryUserCount(String username);

    /**
     * addUserLoginRecord
     * 
     * @param username username
     */
    void addUserLoginRecord(String username);
}
