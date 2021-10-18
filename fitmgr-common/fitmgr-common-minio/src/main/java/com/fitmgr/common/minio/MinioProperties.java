
package com.fitmgr.common.minio;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * minio 配置信息
 *
 * @author Fitmgr
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
    /**
     * minio 服务地址 http://ip:port
     */
    private String url;

    /**
     * 用户名
     */
    private String accessKey;

    /**
     * 密码
     */
    private String secretKey;

}
