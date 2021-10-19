/*
 *    Copyright (c) 2018-2025, lengleng All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * Neither the name of the pig4cloud.com developer nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * Author: lengleng (wangiegie@gmail.com)
 */

package com.fitmgr.admin.controller;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitmgr.admin.api.entity.SysFile;
import com.fitmgr.admin.service.SysFileService;
import com.fitmgr.common.core.util.R;
import com.fitmgr.common.log.annotation.SysLog;
import com.fitmgr.common.security.annotation.Inner;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;

/**
 * 文件管理
 *
 * @author Fitmgr
 * @date 2019-06-18 17:18:42
 */
@RestController
@AllArgsConstructor
@RequestMapping("/sys-file")
@Api(value = "sys-file", tags = "文件管理")
public class SysFileController {
    private final SysFileService sysFileService;

    /**
     * 分页查询
     *
     * @param page    分页对象
     * @param sysFile 文件管理
     * @return
     */
    @ApiOperation(value = "分页查询", notes = "分页查询")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "query", name = "page", dataType = "Page", required = true, value = "分页"),
            @ApiImplicitParam(paramType = "query", name = "sysFile", dataType = "SysFile", required = true, value = "文件") })
    @GetMapping("/page")
    public R getSysFilePage(Page page, SysFile sysFile) {
        return R.ok(sysFileService.page(page, Wrappers.query(sysFile)));
    }

    /**
     * 通过id删除文件管理
     *
     * @param id id
     * @return R
     */
    @ApiOperation(value = "通过id删除文件管理", notes = "通过id删除文件管理")
    @ApiImplicitParams(value = @ApiImplicitParam(paramType = "path", name = "id", dataType = "Long", required = true, value = "分页"))
    @SysLog("删除文件管理")
    @DeleteMapping("/{id}")
    public R removeById(@PathVariable Long id) {
        return R.ok(sysFileService.deleteFile(id));
    }

    /**
     * 上传文件 文件名采用uuid,避免原始文件名中带"-"符号导致下载的时候解析出现异常
     *
     * @param file 资源
     * @return R(/ admin / bucketName / filename)
     */
    @PostMapping("/upload")
    @ApiOperation(value = "上传文件", notes = "上传文件")
    @ApiImplicitParams(value = @ApiImplicitParam(paramType = "query", name = "file", dataType = "MultipartFile", required = true, value = "文件"))
    public R upload(@RequestParam("file") MultipartFile file) {
        return sysFileService.uploadFile(file);
    }

    /**
     * 获取文件
     *
     * @param bucket   桶名称
     * @param fileName 文件空间/名称
     * @param response
     * @return
     */
    @Inner(false)
    @GetMapping("/{bucket}/{fileName}")
    @ApiOperation(value = "上传文件", notes = "上传文件")
    @ApiImplicitParams(value = @ApiImplicitParam(paramType = "query", name = "file", dataType = "MultipartFile", required = true, value = "文件"))
    public void file(@PathVariable String bucket, @PathVariable String fileName, HttpServletResponse response) {
        sysFileService.getFile(bucket, fileName, response);
    }

    /**
     * 批量删除文件
     * 
     * @param fileList
     * @return
     */
    @DeleteMapping("/batch-deletion")
    public R batchDeletion(@RequestBody List<SysFile> fileList) {
        return R.ok(sysFileService.batchDeletion(fileList));
    }
}
