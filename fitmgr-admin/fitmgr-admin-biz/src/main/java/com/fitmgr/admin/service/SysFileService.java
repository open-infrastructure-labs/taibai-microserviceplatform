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

package com.fitmgr.admin.service;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fitmgr.admin.api.entity.SysFile;
import com.fitmgr.common.core.util.R;

/**
 * 文件管理
 *
 * @author Fitmgr
 * @date 2019-06-18 17:18:42
 */
public interface SysFileService extends IService<SysFile> {

    /**
     * 上传文件
     *
     * @param file
     * @return
     */
    R uploadFile(MultipartFile file);

    /**
     * 读取文件
     *
     * @param bucket   桶名称
     * @param fileName 文件名称
     * @param response 输出流
     */
    void getFile(String bucket, String fileName, HttpServletResponse response);

    /**
     * 删除文件
     *
     * @param id
     * @return
     */
    Boolean deleteFile(Long id);

    /**
     * 批量删除文件
     * 
     * @param fileList
     * @return
     */
    Boolean batchDeletion(List<SysFile> fileList);
}
