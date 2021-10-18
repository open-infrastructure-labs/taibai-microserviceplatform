package com.fitmgr.meterage.controller;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitmgr.common.core.constant.enums.BusinessEnum;
import com.fitmgr.common.core.util.R;
import com.fitmgr.meterage.api.dto.MeterageProjectDTO;
import com.fitmgr.meterage.api.entity.MeterageProject;
import com.fitmgr.meterage.service.IMeterageProjectService;
import com.fitmgr.resource.api.feign.RemoteResourceQuotaService;
import com.fitmgr.resource.api.vo.ComponentAttributeVO;
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
@RequestMapping("/meterage-project")
@AllArgsConstructor
public class MeterageProjectController {

    private final IMeterageProjectService iMeterageProjectService;

    private final RemoteResourceQuotaService remoteResourceQuotaService;


    @ApiOperation(value = "添加计量项")
    @PostMapping("/add")
    @ApiImplicitParams(@ApiImplicitParam(name = "meterageProjectDTO", value = "计量项对象", dataType = "MeterageProjectDTO", paramType = "body", required = true))
    public R add(@Valid @RequestBody MeterageProjectDTO meterageProjectDTO)
    {
        MeterageProject meterageProject = new MeterageProject();
        BeanUtil.copyProperties(meterageProjectDTO, meterageProject);
        R r = iMeterageProjectService.add(meterageProject);
        return r;
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

    @ApiOperation(value = "查询计量项详情")
    @GetMapping("/detail/{id}")
    @ApiImplicitParams(@ApiImplicitParam(name = "id", value = "计量项id", dataType = "Integer", paramType = "path", required = true))
    public R detail(@PathVariable Integer id)
    {
        MeterageProject meterageProject = iMeterageProjectService.getById(id);
        return R.ok(meterageProject);
    }

    @ApiOperation(value = "查询计量项列表")
    @GetMapping("/list")
    public R list()
    {
        List<MeterageProject> list = iMeterageProjectService.list();
        return R.ok(list);
    }

    @ApiOperation(value = "分页查询计量项")
    @PostMapping("/page")
    @ApiImplicitParams({@ApiImplicitParam(name = "current", value = "页码", dataType = "Integer", paramType = "query", required = true),
            @ApiImplicitParam(name = "size", value = "一页的数量", dataType = "Integer", paramType = "query", required = true)})
    public R page(Integer current, Integer size) {
        Page<MeterageProject> page = new Page<>(current, size);
        IPage<MeterageProject> projects = iMeterageProjectService.page(page);
        return R.ok(projects);
    }

    @ApiOperation(value = "修改计量项")
    @PutMapping("/update")
    @ApiImplicitParams(@ApiImplicitParam(name = "meterageProjectDTO", value = "计量项对象", dataType = "MeterageProjectDTO", paramType = "body", required = true))
    public R updateMeterageItem(@RequestBody MeterageProjectDTO meterageProjectDTO) {
        MeterageProject meterageProject = new MeterageProject();
        BeanUtil.copyProperties(meterageProjectDTO, meterageProject);
        return R.ok(iMeterageProjectService.updateById(meterageProject));
    }

    @ApiOperation(value = "删除计量项")
    @DeleteMapping("/delete/{id}")
    @ApiImplicitParams(@ApiImplicitParam(name = "id", value = "计量项id", dataType = "Integer", paramType = "path", required = true))
    public R delete(@PathVariable("id") Integer id) {
        return R.ok(iMeterageProjectService.removeById(id));
    }

    @ApiOperation(value = "查询计量项详情")
    @PostMapping("/list/condition")
    public R listByCondition(@RequestBody Map<String, Object> map)
    {
        List<MeterageProject> meterageProjects = iMeterageProjectService.selectByCondition(map);
        return R.ok(meterageProjects);
    }
}
