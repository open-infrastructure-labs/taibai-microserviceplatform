package com.taibai.common.data.tenant;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * 多租户配置
 *
 * @author Taibai
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "taibai.tenant")
public class TenantConfigProperties {

    /**
     * 维护租户列名称
     */
    private String column = "tenant_id";

    /**
     * 多租户的数据表集合
     */
    private List<String> tables = new ArrayList<>();
}
