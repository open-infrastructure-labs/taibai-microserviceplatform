package com.fitmgr.admin.config;

import javax.servlet.MultipartConfigElement;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import lombok.extern.slf4j.Slf4j;

/**
 * 上传文件大小配置
 *
 * @author Fitmgr
 * @date 2021/3/22 9:11
 */
@Slf4j
@Configuration
public class UploadConfiguration {

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        // 单个文件大小
        factory.setMaxFileSize(DataSize.parse("10MB"));
        // 设置总上传数据大小
        factory.setMaxRequestSize(DataSize.parse("50MB"));
        return factory.createMultipartConfig();
    }
}
