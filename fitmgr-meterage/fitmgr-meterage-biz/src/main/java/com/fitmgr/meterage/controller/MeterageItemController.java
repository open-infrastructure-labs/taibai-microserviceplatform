package com.fitmgr.meterage.controller;


import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitmgr.common.core.constant.enums.BusinessEnum;
import com.fitmgr.common.core.util.R;
import com.fitmgr.meterage.api.entity.MeterageItem;
import com.fitmgr.meterage.api.entity.MeterageProjectProperty;
import com.fitmgr.meterage.service.IMeterageItemService;
import com.fitmgr.meterage.service.IMeterageProjectService;
import com.fitmgr.resource.api.entity.Component;
import com.fitmgr.resource.api.feign.RemoteResourceQuotaService;
import com.fitmgr.resource.api.vo.ComponentAttributeVO;
import com.fitmgr.resource.api.vo.ResourceTypeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 计量项表 前端控制器
 * </p>
 *
 * @author dzl
 * @since 2020-05-28
 */
@RestController
@AllArgsConstructor
@RequestMapping("/meterageItem")
@Api(value = "meterageItem", tags = "计量项模块")
@Slf4j
public class MeterageItemController {

    private final IMeterageItemService meterageItemService;

    private final RemoteResourceQuotaService remoteResourceQuotaService;

    private final IMeterageProjectService meterageProjectService;

    /**
     * 获取云列表
     *
     * @return 云列表
     */
    @GetMapping("/cloud-list")
    @ApiOperation(value = "获取云列表")
    public R cloudList() {
        R<List<ResourceTypeVO>> list = remoteResourceQuotaService.list();
        log.info("访问获取云列表接口");
        if (list.getCode() == 0) {
            return list;
        }
        return R.failed(BusinessEnum.RESOURCE_CLOUD_LIST);
    }

    /**
     * 通过云code-获取组件列表
     *
     * @return 云列表
     */
    @GetMapping("/cloud-component/{cloudId}")
    @ApiOperation(value = "通过云id-获取组件列表")
    @ApiImplicitParams(@ApiImplicitParam(name = "cloudId", value = "云id", dataType = "Integer", paramType = "path", required = true))
    public R cloudComponent(@PathVariable("cloudId") Integer cloudId) {
        R<List<Component>> listR = remoteResourceQuotaService.libraryByResource(cloudId);
        if (listR.getCode() == 0) {
            listR.getData().stream().filter(componentAttributeVO -> "2".equals(componentAttributeVO.getCategoryId())).collect(Collectors.toList());
            return listR;
        }
        return R.failed(BusinessEnum.RESOURCE_COMPONENT_LIST);
    }

    /**
     * 通过组件code-获取组件属性列表
     *
     * @return 云列表
     */
    @GetMapping("/component-attribute/{componentCode}")
    @ApiOperation(value = "通过组件code-获取组件属性列表")
    @ApiImplicitParams(@ApiImplicitParam(name = "componentCode", value = "组件code", dataType = "String", paramType = "path", required = true))
    public R componentAttribute(@PathVariable("componentCode") String componentCode) {
        R<List<ComponentAttributeVO>> listR = remoteResourceQuotaService.selectAttrListByComponentCode(componentCode);
        if (listR.getCode() == 0) {
            return listR;
        }
        return R.failed(BusinessEnum.RESOURCE_ATTRIBUTE_LIST);
    }

    @ApiOperation(value = "获取计量项列表")
    @GetMapping("/list")
    public R getComponentList() {
        return R.ok(meterageProjectService.list());
    }

    @ApiOperation(value = "通过组件code获取计量项")
    @GetMapping("/code-list/{code}")
    @ApiImplicitParams(@ApiImplicitParam(name = "code", value = "组件code", dataType = "String", paramType = "path", required = true))
    public R<MeterageItem> getComponentByMeterageItems(@PathVariable("code") String code) {
        return R.ok(meterageItemService.getOne(Wrappers.<MeterageItem>lambdaQuery().eq(MeterageItem::getComponentCode, code)));
    }

    @ApiOperation(value = "添加计量项")
    @PostMapping("/add")
    @ApiImplicitParams(@ApiImplicitParam(name = "meterageItem", value = "计量对象", dataType = "MeterageItem", paramType = "body", required = true))
    public R addMeterageItem(@RequestBody MeterageItem meterageItem) {
        return R.ok(meterageItemService.save(meterageItem));
    }

    @ApiOperation(value = "修改计量项")
    @PutMapping("/update")
    @ApiImplicitParams(@ApiImplicitParam(name = "meterageItem", value = "计量对象", dataType = "MeterageItem", paramType = "body", required = true))
    public R updateMeterageItem(@RequestBody MeterageItem meterageItem) {
        return R.ok(meterageItemService.updateById(meterageItem));
    }

    @ApiOperation(value = "删除计量项")
    @DeleteMapping("/delete/{id}")
    @ApiImplicitParams(@ApiImplicitParam(name = "id", value = "计量项id", dataType = "Integer", paramType = "path", required = true))
    public R deleteMeterageItem(@PathVariable("id") Integer id) {
        return R.ok(meterageItemService.removeById(id));
    }

}

