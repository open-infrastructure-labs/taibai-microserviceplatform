package com.fitmgr.meterage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitmgr.meterage.api.entity.MeterageProjectProperty;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zhaock
 * @since 2020-08-12
 */
public interface MeterageProjectPropertyMapper extends BaseMapper<MeterageProjectProperty> {

    List<MeterageProjectProperty> selectHeaderByComponentCode(@Param("componentCode") String componentCode);
}
