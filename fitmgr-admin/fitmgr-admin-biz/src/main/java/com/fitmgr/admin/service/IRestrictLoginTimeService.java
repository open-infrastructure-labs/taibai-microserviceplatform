package com.fitmgr.admin.service;

import java.util.List;

import com.fitmgr.admin.api.entity.RestrictLloginTime;

/**
 * <p>
 * 不允许访问时间段配置表 服务类
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
public interface IRestrictLoginTimeService {
    /**
     * list
     * 
     * @return List<RestrictLloginTime>
     */
    List<RestrictLloginTime> list();

    /**
     * updateById
     * 
     * @param restrictLloginTime restrictLloginTime
     * @return boolean
     */
    boolean updateById(RestrictLloginTime restrictLloginTime);

}
