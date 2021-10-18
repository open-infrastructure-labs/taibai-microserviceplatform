package com.fitmgr.meterage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fitmgr.common.core.util.R;
import com.fitmgr.meterage.api.dto.StatisticsDTO;

import java.util.List;
import java.util.Map;


public interface IMeterageStatisticsService{

   IPage<Map<String,String>> getList(StatisticsDTO statisticsDTO);

   /**
    * 获取表头
    * @return list
    */
   R<List<Map<String,String>>> getTableHead(Integer meterageItem);
}
