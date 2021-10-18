package com.fitmgr.meterage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitmgr.meterage.api.entity.ChargeItemProperty;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author zhangxiaokang
 * @since 2020-10-21
 */
public interface ChargeItemPropertyMapper extends BaseMapper<ChargeItemProperty> {

    /**
     * 批量新增计费属性
     * @param chargeItemProperties
     * @return
     */
    int saveChargeItemProperties(@Param("list") List<ChargeItemProperty> chargeItemProperties);
}
