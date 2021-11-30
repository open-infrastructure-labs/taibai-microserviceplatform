package com.taibai.admin.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taibai.admin.api.vo.TenantTypeVO;
import com.taibai.admin.service.ITenantTypeService;
import com.taibai.common.core.util.R;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;

/**
 * <p>
 * 租户类型表 前端控制器
 * </p>
 *
 * @author Taibai
 * @since 2019-11-14
 */
@RestController
@AllArgsConstructor
@RequestMapping("/tenant-type")
@Api(value = "tenantType", tags = "租户类型管理模块")
public class TenantTypeController {
    private final ITenantTypeService tenantTypeService;

    /**
     * 查询租户类型列表
     *
     * @return 租户类型列表
     */
    @GetMapping("/list")
    @ApiOperation(value = "获取租户类型列表")
    public R listTenantType() {
        return R.ok(tenantTypeService.list());
    }

    /**
     * 分页查询租户类型列表
     *
     * @return 租户类型列表
     */
    @GetMapping("/list/page")
    @ApiOperation(value = "分页查询租户类型列表")
    @ApiImplicitParams(@ApiImplicitParam(name = "page", value = "分页条件", dataType = "Page", paramType = "query"))
    public R listPageTenantType(Page page) {
        IPage<TenantTypeVO> iPage = tenantTypeService.selectListPage(page);
        return R.ok(iPage);
    }

}
