package com.fitmgr.meterage.service.impl;

import com.fitmgr.meterage.api.entity.MeterageItemHeader;
import com.fitmgr.meterage.api.entity.MeterageProjectProperty;
import com.fitmgr.meterage.mapper.MeterageItemHeaderMapper;
import com.fitmgr.meterage.service.IMeterageItemHeaderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitmgr.meterage.service.IMeterageProjectPropertyService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author dzl
 * @since 2020-05-28
 */
@Slf4j
@Service
@AllArgsConstructor
public class MeterageItemHeaderServiceImpl extends ServiceImpl<MeterageItemHeaderMapper, MeterageItemHeader> implements IMeterageItemHeaderService {

    private final MeterageItemHeaderMapper meterageItemHeaderMapper;

    private final IMeterageProjectPropertyService meterageProjectPropertyMapper;

    /**
     * 添加计量项表头
     *
     * @param meterageItemHeader
     */
    @Override
    public int addMeterageItemHeader(MeterageItemHeader meterageItemHeader) {
        return meterageItemHeaderMapper.insert(meterageItemHeader);
    }

    /**
     * 修改计量项表头
     *
     * @param meterageItemHeader
     * @return
     */
    @Override
    public int updateMeterageItemHeader(MeterageItemHeader meterageItemHeader) {
        return meterageItemHeaderMapper.updateById(meterageItemHeader);
    }

    /**
     * 通过组件code获取表头列表
     *
     * @param code
     * @return
     */
    @Override
    public List<MeterageProjectProperty> getMeterageItemHeaderList(String code) {
        List<MeterageProjectProperty> list =  meterageProjectPropertyMapper.selectByComponentCode(code);
        log.info("通过组件code ：{} 获取表头列表开始！", code);
        for (MeterageProjectProperty meterageProjectProperty:list) {
            if(null != meterageProjectProperty.getForeignComponentId() && !"".equals(meterageProjectProperty.getForeignComponentId())){
                meterageProjectProperty.setSourceKey(meterageProjectProperty.getForeignKey());
            }
        }
        return list;
    }

    @Override
    public List<MeterageProjectProperty> getMeterageItemHeaderListForCal(String code) {
        List<MeterageProjectProperty> list =  meterageProjectPropertyMapper.selectByComponentCode(code);
        return list;
    }

}
