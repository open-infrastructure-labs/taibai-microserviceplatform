package com.taibai.admin.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taibai.admin.api.dto.FunctionDTO;
import com.taibai.admin.api.entity.Auth;
import com.taibai.admin.api.entity.Function;
import com.taibai.admin.service.IAuthService;
import com.taibai.admin.service.IFunctionService;
import com.taibai.common.core.constant.enums.BusinessEnum;
import com.taibai.common.core.util.R;
import com.taibai.common.log.annotation.SysLog;

import cn.hutool.core.bean.BeanUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 功能表 前端控制器
 * </p>
 *
 * @author Taibai
 * @since 2019-11-16
 */
@RestController
@AllArgsConstructor
@RequestMapping("/function")
@Api(value = "function", tags = "功能操作模块")
@Slf4j
public class FunctionController {

    private final IFunctionService iFunctionService;

    @Autowired
    private IAuthService authService;

    @ApiOperation(value = "分页条件查询功能列表")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "query", name = "page", dataType = "Page", required = true, value = "分页对象"),
            @ApiImplicitParam(paramType = "query", name = "function", dataType = "Function", required = true, value = "条件对象") })
    @GetMapping("/page")
    public R getFunctionPage(Page page, Function function) {
        return R.ok(iFunctionService.page(page, Wrappers.query(function)));
    }

    @ApiOperation(value = "查询所有功能列表")
    @GetMapping("/list")
    public R getList() {
        return R.ok(iFunctionService.list());
    }

    @SysLog(value = "删除功能项", cloudResType = "功能", resIdArgIndex = 0, resIdLocation = "arg")
    @ApiOperation(value = "删除功能项")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "functionId", dataType = "Integer", required = true, value = "功能项id") })
    @DeleteMapping("/{functionId}")
    public R deletFunction(@PathVariable(name = "functionId") Integer functionId) {
        return R.ok(iFunctionService.deletFunction(functionId));
    }

    @SysLog(value = "修改功能项", cloudResType = "功能", resIdArgIndex = 0, resIdLocation = "arg.id")
    @ApiOperation(value = "修改功能项")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "function", dataType = "Function", required = true, value = "功能对象") })
    @PutMapping
    public R updateFunction(@Valid @RequestBody Function function) {
        function.setUpdateTime(LocalDateTime.now());
        return R.ok(iFunctionService.updateById(function));
    }

    @SysLog(value = "添加功能项", cloudResType = "功能", resNameArgIndex = 0, resNameLocation = "arg.name")
    @ApiOperation(value = "添加功能项")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "function", dataType = "Function", required = true, value = "功能对象") })
    @PostMapping
    public R saveFunction(@Valid @RequestBody Function function) {
        int count = iFunctionService
                .count(Wrappers.<Function>lambdaQuery().eq(StringUtils.isNotEmpty(function.getFunctionCode()),
                        Function::getFunctionCode, function.getFunctionCode()));
        if (count > 0) {
            return R.failed(BusinessEnum.FUNCTION_CODE);
        }
        return R.ok(iFunctionService.save(function));
    }

    @ApiOperation(value = "通过菜单id获取功能列表（系统菜单+服务菜单）")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "path", name = "menuId", dataType = "String", required = true, value = "菜单id") })
    @GetMapping("/menu-function/{menuId}")
    public R getFunctionByMenuId(@PathVariable(name = "menuId") String menuId,
            @RequestParam(name = "roleId", required = false) Integer roleId) {
        List<Function> functions = iFunctionService.list(new QueryWrapper<Function>().eq("menu_id", menuId));
        List<FunctionDTO> res = new ArrayList<>();
        for (Function function : functions) {
            FunctionDTO functionDTO = new FunctionDTO();
            BeanUtil.copyProperties(function, functionDTO);
            List<Auth> auths = authService.list(
                    Wrappers.<Auth>lambdaQuery().eq(Auth::getFunctionId, function.getId()).eq(Auth::getRoleId, roleId));
            if (auths.size() == 0 || auths.isEmpty()) {
                res.add(functionDTO);
            } else {
                Auth auth = auths.get(0);
                functionDTO.setStatus(auth.getStatus());
                functionDTO.setOperatingRange(auth.getOperatingRange());
                functionDTO.setTenantRange(auth.getTenantRange());
                res.add(functionDTO);
            }
        }
        return R.ok(res);
    }

    @SysLog(value = "批量修改操作列表", cloudResType = "功能")
    @ApiOperation(value = "批量修改资源操作")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "body", name = "functions", dataType = "Function", required = true, value = "操作List") })
    @PutMapping("/update-function-code")
    public R updateCodefunction(@RequestBody List<Function> functions) {
        return iFunctionService.updateCodefunction(functions);
    }

}
