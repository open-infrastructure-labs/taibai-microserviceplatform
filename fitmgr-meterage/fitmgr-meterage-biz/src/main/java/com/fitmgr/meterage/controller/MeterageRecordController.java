package com.fitmgr.meterage.controller;

import com.fitmgr.common.core.constant.enums.BusinessEnum;
import com.fitmgr.common.core.util.R;
import com.fitmgr.common.security.service.FitmgrUser;
import com.fitmgr.common.security.util.SecurityUtils;
import com.fitmgr.meterage.api.vo.MeterageViewDataVO;
import com.fitmgr.meterage.api.vo.ResourceMeterageRecordVO;
import com.fitmgr.meterage.service.IMeterageRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * <p>
 * 计量记录
 * </p>
 *
 * @author dzl
 * @since 2020-02-11
 */
@RestController
@AllArgsConstructor
@RequestMapping("/meterageRecord")
@Api(value = "meterageRecord", tags = "计量记录模块")
public class MeterageRecordController {

    private final IMeterageRecordService meterageRecordService;

    @ApiOperation(value = "分页条件获取计量记录列表")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "body", name = "resourceMeterageRecordVO", dataType = "ResourceMeterageRecordVO", required = true, value = "条件对象"),
            @ApiImplicitParam(paramType = "query", name = "current", dataType = "Integer", required = true, value = "页码"),
            @ApiImplicitParam(paramType = "query", name = "size", dataType = "Integer", required = true, value = "数量")})
    @PostMapping("/page")
    public R<Object> getMeterageRecordList(@RequestBody ResourceMeterageRecordVO resourceMeterageRecordVO) {

        //获取当前用户的默认角色id
        FitmgrUser user = SecurityUtils.getUser();
        if (null != user) {
            return meterageRecordService.getMeterageRecordList(resourceMeterageRecordVO);
        }
        return R.failed(BusinessEnum.NOT_LOGIN);
    }

    @ApiOperation(value = "导出当前规则下的计量记录")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query", name = "tenantId", dataType = "Integer", required = true, value = "租户id"),
            @ApiImplicitParam(paramType = "query", name = "startTime", dataType = "String", required = true, value = "开始时间"),
            @ApiImplicitParam(paramType = "query", name = "endTime", dataType = "String", required = true, value = "结束时间"),
            @ApiImplicitParam(paramType = "query", name = "size", dataType = "Integer", required = true, value = "数量"),
            @ApiImplicitParam(paramType = "query", name = "current", dataType = "Integer", required = true, value = "页码"),
            @ApiImplicitParam(paramType = "query", name = "componentCode", dataType = "String", required = true, value = "组件code")})
    @GetMapping("/export")
    public void exportExcel( HttpServletResponse response, @RequestParam(value = "tenantId", required = false)Integer tenantId,
                             @RequestParam(value = "startTime", required = false) String startTime,
                             @RequestParam(value = "endTime", required = false) String endTime,
                             @RequestParam(value = "size", defaultValue = "10")Integer size,
                             @RequestParam(value = "current", defaultValue = "1")Integer current,
                             @RequestParam(value = "componentCode", required = false)String componentCode) {
        ResourceMeterageRecordVO resourceMeterageRecordVO = new ResourceMeterageRecordVO();
        resourceMeterageRecordVO.setTenantId(tenantId);
        if(null != startTime && !startTime.isEmpty()){
            resourceMeterageRecordVO.setStartTime(startTime);
            resourceMeterageRecordVO.setEndTime(endTime);
        }
        resourceMeterageRecordVO.setComponentCode(componentCode);
        meterageRecordService.exportExcel(response, resourceMeterageRecordVO);
    }

    @ApiOperation(value = "获取概览需求数据")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "body", name = "meterageViewDataVO", dataType = "MeterageViewDataVO", required = true, value = "概览过滤数据对象")
          })
    @PostMapping("/view/List")
    public R getViewDataList(@RequestBody MeterageViewDataVO meterageViewDataVO) {
        return meterageRecordService.getViewDate(meterageViewDataVO);
    }

}
