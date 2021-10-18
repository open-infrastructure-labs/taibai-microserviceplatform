package com.fitmgr.meterage.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitmgr.common.core.util.R;
import com.fitmgr.meterage.api.entity.MeterageProject;
import com.fitmgr.meterage.mapper.MeterageProjectMapper;
import com.fitmgr.meterage.service.IMeterageProjectService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhaock
 * @since 2020-08-12
 */
@Slf4j
@Service
@AllArgsConstructor
public class MeterageProjectServiceImpl extends ServiceImpl<MeterageProjectMapper, MeterageProject> implements IMeterageProjectService {

    private MeterageProjectMapper meterageProjectMapper;

    @Override
    public R add(MeterageProject meterageProject) {
        Integer count = meterageProjectMapper.selectCount(Wrappers.<MeterageProject>lambdaQuery().eq(MeterageProject::getComponentCode, meterageProject.getComponentCode()));
        if(count > 0 ){
           return R.failed("该组件已存在");
        }
        meterageProjectMapper.insert(meterageProject);
        return R.ok();
    }

    @Override
    public List<MeterageProject> selectByCondition(Map<String, Object> map) {
        return meterageProjectMapper.selectByMapSelf(map);
    }
}
