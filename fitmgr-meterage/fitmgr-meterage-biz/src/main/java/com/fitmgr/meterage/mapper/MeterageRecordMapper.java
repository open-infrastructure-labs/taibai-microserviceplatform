package com.fitmgr.meterage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitmgr.meterage.api.entity.ResourceMeterageRecord;
import com.fitmgr.meterage.api.vo.ResourceMeterageRecordListVo;
import com.fitmgr.meterage.api.vo.ResourceMeterageRecordVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author dzl
 * @since 2020-02-14
 */
public interface MeterageRecordMapper extends BaseMapper<ResourceMeterageRecord> {

    Page<ResourceMeterageRecordListVo> getMeterageList(Page page, @Param(value = "resourceMeterageRecordVO") ResourceMeterageRecordVO resourceMeterageRecordVO);

    Page<ResourceMeterageRecord> getListPage(Page page, @Param(value = "resourceMeterageRecordVO") ResourceMeterageRecordVO resourceMeterageRecordVO);

    List<ResourceMeterageRecord> getList(@Param(value = "resourceMeterageRecordVO") ResourceMeterageRecordVO resourceMeterageRecordVO);

    List<ResourceMeterageRecordListVo> getListVo(@Param(value = "resourceMeterageRecordVO") ResourceMeterageRecordVO resourceMeterageRecordVO);

    List<ResourceMeterageRecord> getUnfinishedRecordsByCmpInstanceName(@Param("cmpInstanceName") String cmpInstanceName);

    List<ResourceMeterageRecord> getViewList(@Param(value = "tenants") List<Integer> tenants);

}
