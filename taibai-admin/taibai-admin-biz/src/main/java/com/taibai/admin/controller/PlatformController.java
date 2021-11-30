package com.fitmgr.admin.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitmgr.admin.api.entity.Platform;
import com.fitmgr.admin.service.IPlatformService;
import com.fitmgr.common.core.util.R;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/platform")
@Api(value = "platform", tags = "平台管理模块")
public class PlatformController {
    
    private IPlatformService platformService;
    
    /**
     * 
     * 根据id查询平台信息
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "根据id查询平台信息")
    @ApiImplicitParams(@ApiImplicitParam(paramType = "query", name = "id", dataType = "Long", required = true, value = "id"))
    @GetMapping("/getById")
    public R info(@RequestParam(name = "id") Long id) {
        try {
            Platform platform = platformService.getById(id);
            if (platform == null) {
                log.error("根据id[{}]查询平台信息为空", id);
                return R.failed(Boolean.FALSE, String.format("平台信息为空 %s", id));
            }
            return R.ok(platform);
        } catch (Exception e) {
            log.error("根据id[{}]查询平台信息异常", id, e);
            return R.failed("id不能为空");
        }
    }
    
    /**
     * 
     * 根据平台标识查询平台信息
     *
     * @param platfomId
     * @return
     */
    @ApiOperation(value = "根据平台标识查询平台信息")
    @ApiImplicitParams(@ApiImplicitParam(paramType = "query", name = "platfomId", dataType = "String", required = true, value = "平台标识"))
    @GetMapping("/info")
    public R info(@RequestParam(name = "platfomId") String platfomId) {
        try {
            Platform platform = platformService.getOne(Wrappers.<Platform>query()
                    .lambda().eq(Platform::getPlatformId, platfomId));
            
            if (platform == null) {
                return R.failed(Boolean.FALSE, String.format("查询平台信息为空 %s", platfomId));
            }
            
            return R.ok(platform);
        } catch (Exception e) {
            log.error("根据平台标识[{}]查询平台信息异常", platfomId, e);
            return R.failed("平台标识不能为空");
        }
    }
    
    
    /**
     * 查询注册平台
     */
    @ApiOperation(value = "查询注册平台")
    @GetMapping("/list")
    public R list() {
        try {
            List<Platform> platform = platformService.list(Wrappers.<Platform>lambdaQuery().orderByDesc(Platform::getCreateTime));
            return R.ok(platform);
        } catch (Exception e) {
            log.error("查询平台列表异常", e);
            return R.failed("查询平台列表失败");
        }
    }
    
    /**
     * 分页查询注册平台列表
     */
    @ApiOperation(value = "分页查询注册平台列表")
    @GetMapping("/page")
    public R page(Page page, Platform platform) {
        try {
            LambdaQueryWrapper<Platform> chargeItemLambdaQueryWrapper = Wrappers.<Platform>lambdaQuery()
                    .like(StringUtils.isNotBlank(platform.getPlatformName()), Platform::getPlatformName, platform.getPlatformName())
                    .orderByDesc(Platform::getCreateTime);
            return R.ok(platformService.page(page, chargeItemLambdaQueryWrapper));
        } catch (Exception e) {
            log.error("查询平台列表失败，platform={}", platform, e);
            return R.failed(new ArrayList<>());
        }
    }  
    
    /**
     * 
     * 新增平台
     *
     * @param platform
     * @return
     */
    @ApiOperation(value = "新增平台")
    @ApiImplicitParams(@ApiImplicitParam(paramType = "body", name = "platform", dataType = "Platform", required = true, value = "新增平台"))
    @PostMapping("/add")
    public R add(@RequestBody Platform platform) {
        try {
            Platform platformDto = platformService.getOne(Wrappers.<Platform>query()
                    .lambda().eq(Platform::getPlatformId, platform.getPlatformId()));
            
            if (platformDto != null) {
                return R.failed("平台标识已存在");
            }
            
            platform.setCreateTime(new Date());
            return R.ok(platformService.save(platform));
        } catch (Exception e) {
            log.error("新增平台异常，platform={}", platform, e);
            return R.failed("新增失败，请稍后重试");
        }
    }
    
    /**
     * 
     * 更新平台信息
     *
     * @param platform
     * @return
     */
    @ApiOperation(value = "更新平台信息")
    @ApiImplicitParams(@ApiImplicitParam(paramType = "body", name = "platform", dataType = "Platform", required = true, value = "平台信息"))
    @PostMapping("/update")
    public R update(@RequestBody Platform platform) {
        try {
            platform.setUpdateTime(new Date());
            return R.ok(platformService.updateById(platform));
        } catch (Exception e) {
            log.error("更新平台异常，platform={}", platform, e);
            return R.failed("更新失败，请稍后重试");
        }
    }
    
    /**
     * 
     * 删除平台
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "删除平台")
    @ApiImplicitParams(@ApiImplicitParam(paramType = "query", name = "id", dataType = "Long", required = true, value = "id"))
    @DeleteMapping("/delete/{id}")
    public R update(@PathVariable(name = "id") Long id) {
        try {
            return R.ok(platformService.removeById(id));
        } catch (Exception e) {
            log.error("根据id[{}]删除平台异常", id, e);
            return R.failed("删除失败，请稍后重试");
        }
    }
}
