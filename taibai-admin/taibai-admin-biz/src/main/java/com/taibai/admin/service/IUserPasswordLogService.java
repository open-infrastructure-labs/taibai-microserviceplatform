package com.taibai.admin.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taibai.admin.api.entity.UserPasswordLog;

/**
 * <p>
 * 用户密码记录表 服务类
 * </p>
 *
 * @author Taibai
 * @since 2020-05-20
 */
public interface IUserPasswordLogService extends IService<UserPasswordLog> {

    /**
     * 新增密码日志记录信息
     * 
     * @param password password
     * @param id       id
     * @return Boolean
     */
    Boolean saveUserPasswordLog(String password, Integer id);

    /**
     * 通过密码日志记录id删除记录信息
     * 
     * @param id 密码日志记录id
     * @return true/false
     */
    Boolean deleteUserPasswordLog(Integer id);

    /**
     * 只保留三条密码日志记录
     * 
     * @param id 密码日志记录id
     * @return true/false
     */
    Boolean userPasswordLogLimit(Integer id);

    /**
     * 通过用户id统计密码数量
     * 
     * @param userId 用户id
     * @return 密码纪录数量
     */
    Integer passwordLogCount(Integer userId);

    /**
     * 通过用户id查询该用户中最早的密码记录
     * 
     * @param userId 用户id
     * @return UserPasswordLog密码日志记录对象
     */
    UserPasswordLog earlyLog(Integer userId);

    /**
     * 通过用户id查询该用户所有的密码
     * 
     * @param userId 用户id
     * @return UserPasswordLog密码日志记录对象
     */
    List<UserPasswordLog> userPasswordLogList(Integer userId);
}
