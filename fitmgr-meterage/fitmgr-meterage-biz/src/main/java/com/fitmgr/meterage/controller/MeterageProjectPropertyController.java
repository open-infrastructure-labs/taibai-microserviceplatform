package com.fitmgr.meterage.controller;


import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitmgr.common.core.util.R;
import com.fitmgr.meterage.api.dto.MeterageProjectPropertyDTO;
import com.fitmgr.meterage.api.entity.MeterageProjectProperty;
import com.fitmgr.meterage.service.IMeterageProjectPropertyService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zhaock
 * @since 2020-08-12
 */
@RestController
@RequestMapping("/meterage-project-property")
@AllArgsConstructor
public class MeterageProjectPropertyController {

    private IMeterageProjectPropertyService iMeterageProjectPropertyService;

    @ApiOperation(value = "添加计量项属性")
    @PostMapping("/add")
    @ApiImplicitParams(@ApiImplicitParam(name = "meterageProjectPropertyDTO", value = "计量项属性对象", dataType = "MeterageProjectPropertyDTO", paramType = "body", required = true))
    public R add(@Valid @RequestBody MeterageProjectPropertyDTO meterageProjectPropertyDTO)
    {
        MeterageProjectProperty meterageProjectProperty = new MeterageProjectProperty();
        BeanUtil.copyProperties(meterageProjectPropertyDTO, meterageProjectProperty);
        int count = iMeterageProjectPropertyService.count(Wrappers.<MeterageProjectProperty>lambdaQuery().eq(MeterageProjectProperty::getForeignKey, meterageProjectProperty.getForeignKey())
                .eq(MeterageProjectProperty::getSourceKey, meterageProjectProperty.getSourceKey()));
        if (count > 0) {
            return R.failed("源组件属性关联的外部组件属性已经存在！");
        }
        iMeterageProjectPropertyService.save(meterageProjectProperty);
        return R.ok();
    }

    @ApiOperation(value = "查询计量项属性详情")
    @GetMapping("/detail/{id}")
    @ApiImplicitParams(@ApiImplicitParam(name = "id", value = "计量项属性id", dataType = "Integer", paramType = "path", required = true))
    public R detail(@PathVariable Integer id)
    {
        MeterageProjectProperty meterageProjectProperty = iMeterageProjectPropertyService.getById(id);
        return R.ok(meterageProjectProperty);
    }

    @ApiOperation(value = "查询计量项属性列表")
    @GetMapping("/list")
    public R list()
    {
        List<MeterageProjectProperty> list = iMeterageProjectPropertyService.list();
        return R.ok(list);
    }

    @ApiOperation(value = "分页查询计量项属性")
    @PostMapping("/page")
    @ApiImplicitParams({@ApiImplicitParam(name = "current", value = "页码", dataType = "Integer", paramType = "query", required = true),
            @ApiImplicitParam(name = "size", value = "数量", dataType = "Integer", paramType = "query", required = true),
            @ApiImplicitParam(name = "meterageProjectPropertyDTO", value = "计量项属性对象", dataType = "MeterageProjectPropertyDTO", paramType = "body", required = true)})
    public R page(Integer current, Integer size, @RequestBody MeterageProjectPropertyDTO meterageProjectPropertyDTO) {
        QueryWrapper<MeterageProjectProperty> filter = new QueryWrapper<>();
        filter.eq(null != meterageProjectPropertyDTO.getMeterageProjectId(), "meterage_project_id", meterageProjectPropertyDTO.getMeterageProjectId());
        Page<MeterageProjectProperty> page = new Page<>(current, size);
        IPage<MeterageProjectProperty> projects = iMeterageProjectPropertyService.page(page, filter);
        return R.ok(projects);
    }

    @ApiOperation(value = "条件查询计量项属性")
    @PostMapping("/filter")
    @ApiImplicitParams(@ApiImplicitParam(name = "meterageProjectPropertyDTO", value = "计量项属性对象", dataType = "MeterageProjectPropertyDTO", paramType = "body", required = true))
    public R selectFilter(@RequestBody MeterageProjectPropertyDTO meterageProjectPropertyDTO) {
        List<MeterageProjectProperty> projects = iMeterageProjectPropertyService.list(Wrappers.<MeterageProjectProperty>lambdaQuery()
                .eq(MeterageProjectProperty::getMeterageProjectId, meterageProjectPropertyDTO.getMeterageProjectId()));
        return R.ok(projects);
    }

    @ApiOperation(value = "条件查询计量项属性适配计费项")
    @PostMapping("/charge/filter")
    @ApiImplicitParams(@ApiImplicitParam(name = "meterageProjectPropertyDTO", value = "计量项属性对象", dataType = "MeterageProjectPropertyDTO", paramType = "body", required = true))
    public R selectFilterForChargeItem(@RequestBody MeterageProjectPropertyDTO meterageProjectPropertyDTO) {
        List<MeterageProjectProperty> meterageProjectProperties = iMeterageProjectPropertyService.selectFilterForChargeItem(meterageProjectPropertyDTO);
        JSONArray chargeItemProperties = iMeterageProjectPropertyService.getChargeItemProperties(meterageProjectProperties, meterageProjectPropertyDTO.getMeterageProjectId());
        return R.ok(chargeItemProperties);
    }

    @ApiOperation(value = "修改计量项属性")
    @PutMapping("/update")
    @ApiImplicitParams(@ApiImplicitParam(name = "meterageProjectPropertyDTO", value = "计量项id", dataType = "MeterageProjectPropertyDTO", paramType = "body", required = true))
    public R updateMeterageItem(@RequestBody MeterageProjectPropertyDTO meterageProjectPropertyDTO) {
        MeterageProjectProperty meterageProjectProperty = new MeterageProjectProperty();
        BeanUtil.copyProperties(meterageProjectPropertyDTO, meterageProjectProperty);
        return R.ok(iMeterageProjectPropertyService.updateById(meterageProjectProperty));
    }

    @ApiOperation(value = "删除计量项属性")
    @ApiImplicitParams(@ApiImplicitParam(name = "id", value = "计量项属性", dataType = "Integer", paramType = "path", required = true))
    @DeleteMapping("/delete/{id}")
    public R delete(@PathVariable("id") Integer id) {
        return R.ok(iMeterageProjectPropertyService.removeById(id));
    }

    @ApiOperation(value = "查询计量项属性详情")
    @GetMapping("/list/condition")
    public R listByCondition(Map<String, Object> map)
    {
        List<MeterageProjectProperty> meterageProjectProperties = iMeterageProjectPropertyService.selectByCondition(map);
        return R.ok(meterageProjectProperties);
    }

    @ApiOperation(value = "通过组件code查询计量项属性列表")
    @GetMapping("/listByCode/{componentCode}")
    @ApiImplicitParams(@ApiImplicitParam(name = "componentCode", value = "组件code", dataType = "String", paramType = "path", required = true))
    public R listByComponentCode(String componentCode)
    {
        List<MeterageProjectProperty> meterageProjectProperties = iMeterageProjectPropertyService.selectByComponentCode(componentCode);
        return R.ok(meterageProjectProperties);
    }
}
