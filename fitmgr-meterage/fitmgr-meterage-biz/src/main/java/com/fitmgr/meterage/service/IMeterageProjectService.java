package com.fitmgr.meterage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fitmgr.common.core.util.R;
import com.fitmgr.meterage.api.entity.MeterageProject;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhaock
 * @since 2020-08-12
 */
public interface IMeterageProjectService extends IService<MeterageProject> {
    /**
     * 新增一条记录
     * @param meterageProject
     * @return
     */
    R add(MeterageProject meterageProject);

    /**
     * 条件查询
     * @param map
     * @return
     */
    List<MeterageProject> selectByCondition(Map<String, Object> map);
}
