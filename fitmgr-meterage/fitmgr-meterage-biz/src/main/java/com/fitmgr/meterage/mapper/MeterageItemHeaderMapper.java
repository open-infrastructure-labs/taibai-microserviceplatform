package com.fitmgr.meterage.mapper;

import com.fitmgr.meterage.api.entity.MeterageItemHeader;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author dzl
 * @since 2020-05-28
 */
public interface MeterageItemHeaderMapper extends BaseMapper<MeterageItemHeader> {

    /**
     * 通过组件code获取表头列表
     *
     * @param code
     * @return
     */
    List<MeterageItemHeader> getMeterageItemHeaderList(@Param("code") String code);
}
