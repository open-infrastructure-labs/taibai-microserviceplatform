package com.fitmgr.meterage.service;

import com.fitmgr.meterage.api.entity.MeterageItemHeader;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fitmgr.meterage.api.entity.MeterageProjectProperty;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author dzl
 * @since 2020-05-28
 */
public interface IMeterageItemHeaderService extends IService<MeterageItemHeader> {

    /**
     * 添加计量项表头
     * @param meterageItemHeader
     * @return
     */
    int addMeterageItemHeader(MeterageItemHeader meterageItemHeader);

    /**
     * 修改计量项表头
     *
     * @param meterageItemHeader
     * @return
     */
    int updateMeterageItemHeader(MeterageItemHeader meterageItemHeader);

    /**
     * 通过组件code获取表头列表
     *
     * @param code
     * @return
     */
    List<MeterageProjectProperty> getMeterageItemHeaderList(String code);

    /**
     * 通过组件code获取表头列表for计量计算时使用
     *
     * @param code
     * @return
     */
    List<MeterageProjectProperty> getMeterageItemHeaderListForCal(String code);
}
