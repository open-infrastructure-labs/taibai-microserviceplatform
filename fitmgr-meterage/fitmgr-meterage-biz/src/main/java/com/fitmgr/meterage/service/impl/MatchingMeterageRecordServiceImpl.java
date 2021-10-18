package com.fitmgr.meterage.service.impl;

import com.fitmgr.common.core.util.R;
import com.fitmgr.meterage.api.entity.MeterageProject;
import com.fitmgr.meterage.api.entity.ResourceMeterageRecord;
import com.fitmgr.meterage.api.vo.ResourceMeterageRecordVO;
import com.fitmgr.meterage.mapper.MeterageProjectMapper;
import com.fitmgr.meterage.service.IMatchingMeterageRecordService;
import com.fitmgr.meterage.service.IMeterageRecordService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 匹配计量记录和计费记录Service
 *
 * @author zhangxiaokang
 * @date 2020/10/23 16:29
 */
@Slf4j
@Service
@AllArgsConstructor
public class MatchingMeterageRecordServiceImpl implements IMatchingMeterageRecordService {

    private final MeterageProjectMapper meterageProjectMapper;

    private final IMeterageRecordService meterageRecordService;

    @Override
    public List<ResourceMeterageRecord> selectMeterageRecords(Integer meterageItemId, int currentPage, int size) {
        MeterageProject meterageProject = meterageProjectMapper.selectById(meterageItemId);
        ResourceMeterageRecordVO resourceMeterageRecordVO = new ResourceMeterageRecordVO();
        resourceMeterageRecordVO.setComponentCode(meterageProject.getComponentCode());
        resourceMeterageRecordVO.setCurrent(currentPage);
        resourceMeterageRecordVO.setSize(size);
        R<List<ResourceMeterageRecord>> listPage = meterageRecordService.getListPage(resourceMeterageRecordVO);
        if (null == listPage || CollectionUtils.isEmpty(listPage.getData())) {
            return new ArrayList<>();
        }
        return listPage.getData().stream().filter(resourceMeterageRecord -> null == resourceMeterageRecord.getEndTime()).collect(Collectors.toList());
    }
}
