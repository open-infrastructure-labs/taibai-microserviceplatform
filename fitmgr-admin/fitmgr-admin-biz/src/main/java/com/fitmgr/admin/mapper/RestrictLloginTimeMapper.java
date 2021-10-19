package com.fitmgr.admin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitmgr.admin.api.entity.RestrictLloginTime;

/**
 * <p>
 * distributableRoles 不允许访问时间段配置表 Mapper 接口
 * </p>
 *
 * @author Fitmgr
 * @since 2019-11-16
 */
public interface RestrictLloginTimeMapper extends BaseMapper<RestrictLloginTime> {
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
     * @return int
     */
    int updateById(@Param("restrictLloginTime") RestrictLloginTime restrictLloginTime);
}
