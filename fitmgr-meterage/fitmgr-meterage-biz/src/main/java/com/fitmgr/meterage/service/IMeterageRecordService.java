package com.fitmgr.meterage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fitmgr.common.core.util.R;
import com.fitmgr.meterage.api.dto.MeterageViewDataDTO;
import com.fitmgr.meterage.api.entity.ResourceMeterageRecord;
import com.fitmgr.meterage.api.vo.MeterageViewDataVO;
import com.fitmgr.meterage.api.vo.ResourceMeterageRecordVO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author dzl
 * @since 2020-02-14
 */
public interface IMeterageRecordService extends IService<ResourceMeterageRecord> {

    /**
     * 分页条件获取计量记录列表
     *
     * @param resourceMeterageRecordVO
     * @return
     */
    R<Object> getMeterageRecordList(ResourceMeterageRecordVO resourceMeterageRecordVO);

    /**
     * 计费记录导出
     * @param response
     * @param resourceMeterageRecordVO
     */
    void exportExcel(HttpServletResponse response, ResourceMeterageRecordVO resourceMeterageRecordVO);

    /**
     * 分页条件查询计量记录列表
     *
     * @param resourceMeterageRecordVO 筛选条件对象
     * @return
     */
    R<Object> getMeterageRecordPage(ResourceMeterageRecordVO resourceMeterageRecordVO);

    /**
     * 分页条件查询计量记录列表
     *
     * @param resourceMeterageRecordVO 筛选条件对象
     * @return
     */
    R<List<ResourceMeterageRecord>> getListPage(ResourceMeterageRecordVO resourceMeterageRecordVO);

    /**
     * 条件查询计量记录列表
     *
     * @param resourceMeterageRecordVO 筛选条件对象
     * @return
     */
    R<List<ResourceMeterageRecord>> getList(ResourceMeterageRecordVO resourceMeterageRecordVO);

    /**
     * 导出查询计量记录列表
     *
     * @param resourceMeterageRecordVO 筛选条件对象
     * @return
     */
    R<List<Map<String, Object>>> getMeterageRecordExport(ResourceMeterageRecordVO resourceMeterageRecordVO);

    R<Map<Integer, Map<String, List<MeterageViewDataDTO>>>> getViewDate(MeterageViewDataVO meterageViewDataVO);

}
