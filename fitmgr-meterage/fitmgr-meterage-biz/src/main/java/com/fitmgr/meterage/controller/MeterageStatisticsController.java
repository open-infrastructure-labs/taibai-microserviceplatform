package com.fitmgr.meterage.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitmgr.common.core.util.R;
import com.fitmgr.meterage.api.dto.StatisticsDTO;
import com.fitmgr.meterage.service.IMeterageStatisticsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/meterageStatistics")
@Api(value = "meterageStatistics", tags = "计量统计模块")
public class MeterageStatisticsController {

    private IMeterageStatisticsService meterageStatisticsService;


    @ApiOperation(value = "获取计量统计表头")
    @GetMapping("/table-head/{meterageItem}")
    public R getTableHead(@PathVariable("meterageItem") Integer meterageItem) {
        return R.ok(meterageStatisticsService.getTableHead(meterageItem));
    }


    @PostMapping("/list")
    public R getList(@RequestBody StatisticsDTO statisticsDTO){
        return R.ok(meterageStatisticsService.getList(statisticsDTO));
    }

}
