package com.fitmgr.meterage.service;

import com.fitmgr.meterage.api.entity.ResourceMeterageRecord;

import java.util.List;

/**
 * @author zhangxiaokang
 * @date 2020/10/23 16:29
 */
public interface IMatchingMeterageRecordService {

    /**
     * 查询计量记录数据
     * @param meterageItemId
     * @param currentPage
     * @param size
     * @return
     */
    List<ResourceMeterageRecord> selectMeterageRecords(Integer meterageItemId, int currentPage, int size);

}
