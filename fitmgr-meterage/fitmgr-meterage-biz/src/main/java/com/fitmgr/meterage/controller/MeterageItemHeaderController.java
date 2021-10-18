package com.fitmgr.meterage.controller;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitmgr.common.core.constant.ServiceNameConstants;
import com.fitmgr.common.core.constant.enums.BusinessEnum;
import com.fitmgr.common.core.util.R;
import com.fitmgr.meterage.api.entity.MeterageItemHeader;
import com.fitmgr.meterage.api.entity.MeterageProjectProperty;
import com.fitmgr.meterage.service.IMeterageItemHeaderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * <p>
 * 计量表头模块
 * </p>
 *
 * @author dzl
 * @since 2020-05-28
 */
@RestController
@AllArgsConstructor
@RequestMapping("/meterageItemHeader")
@Api(value = "meterageItemHeader", tags = "计量表头模块")
public class MeterageItemHeaderController {

    private final IMeterageItemHeaderService meterageItemHeaderService;


    @ApiOperation(value = "通过计量项id获取表头列表")
    @GetMapping("/list/{id}")
    @ApiImplicitParams(@ApiImplicitParam(name = "id", value = "计量项id", dataType = "Integer", paramType = "path", required = true))
    public R getList(@PathVariable("id") Integer id) {
        return R.ok(meterageItemHeaderService.list(Wrappers.<MeterageItemHeader>lambdaQuery().eq(MeterageItemHeader::getMeterageItemId, id)));
    }

    @ApiOperation(value = "通过组件code获取表头列表")
    @GetMapping("/headerList/{code}")
    @ApiImplicitParams(@ApiImplicitParam(name = "code", value = "组件code", dataType = "String", paramType = "path", required = true))
    public R<List<MeterageProjectProperty>> getMeterageItemHeaderList(@PathVariable("code") String code) {
        return R.ok(meterageItemHeaderService.getMeterageItemHeaderList(code));
    }

    @ApiOperation(value = "通过组件code获取表头列表for计量计算时使用")
    @GetMapping("/headerList/forCal/{code}")
    @ApiImplicitParams(@ApiImplicitParam(name = "code", value = "组件code", dataType = "String", paramType = "path", required = true))
    public R<List<MeterageProjectProperty>> getMeterageItemHeaderListForCal(@PathVariable("code") String code) {
        return R.ok(meterageItemHeaderService.getMeterageItemHeaderListForCal(code));
    }


    @ApiOperation(value = "添加计量项表头")
    @PostMapping("/add")
    @ApiImplicitParams(@ApiImplicitParam(name = "meterageItemHeader", value = "计量项表头对象", dataType = "MeterageItemHeader", paramType = "body", required = true))
    public R addMeterageItemHeader(@RequestBody MeterageItemHeader meterageItemHeader) {
        return R.ok(meterageItemHeaderService.addMeterageItemHeader(meterageItemHeader));
    }

    @ApiOperation(value = "修改计量项表头")
    @PutMapping("/update")
    @ApiImplicitParams(@ApiImplicitParam(name = "meterageItemHeader", value = "计量项表头对象", dataType = "MeterageItemHeader", paramType = "body", required = true))
    public R updateMeterageItemHeader(@RequestBody MeterageItemHeader meterageItemHeader) {
        return R.ok(meterageItemHeaderService.updateMeterageItemHeader(meterageItemHeader));
    }

    @ApiOperation(value = "删除计量项表头")
    @DeleteMapping("/delete/{id}")
    @ApiImplicitParams(@ApiImplicitParam(name = "id", value = "计量项Id", dataType = "Integer", paramType = "path", required = true))
    public R deleteMeterageItemHeader(@PathVariable("id") Integer id) {
        return R.ok(meterageItemHeaderService.removeById(id));
    }

}

