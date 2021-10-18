package com.fitmgr.meterage.service;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fitmgr.meterage.api.dto.MeterageProjectPropertyDTO;
import com.fitmgr.meterage.api.entity.MeterageProjectProperty;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author zhaock
 * @since 2020-08-12
 */
public interface IMeterageProjectPropertyService extends IService<MeterageProjectProperty> {

    /**
     * 条件查询
     * @param map
     * @return
     */
    List<MeterageProjectProperty> selectByCondition(Map<String, Object> map);

    /**
     * 根据组件码查询
     * @param componentCode
     * @return
     */
    List<MeterageProjectProperty> selectByComponentCode(String componentCode);

    List<MeterageProjectProperty> selectFilterForChargeItem(MeterageProjectPropertyDTO meterageProjectPropertyDTO);

    JSONArray getChargeItemProperties(List<MeterageProjectProperty> meterageProjectProperties, Integer meterageProjectId);
}
