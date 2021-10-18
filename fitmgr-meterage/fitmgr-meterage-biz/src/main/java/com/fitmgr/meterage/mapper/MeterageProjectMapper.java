package com.fitmgr.meterage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitmgr.meterage.api.entity.MeterageProject;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zhaock
 * @since 2020-08-12
 */
public interface MeterageProjectMapper extends BaseMapper<MeterageProject> {

    List<MeterageProject> selectByMapSelf(Map<String, Object> map);
}
