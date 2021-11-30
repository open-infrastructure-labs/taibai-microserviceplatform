
package com.taibai.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.context.SecurityContextHolder;

import com.taibai.common.core.ribbon.RibbonDefaultConfig;
import com.taibai.common.security.annotation.EnableFitmgrFeignClients;
import com.taibai.common.security.annotation.EnableFitmgrResourceServer;
import com.taibai.common.swagger.annotation.EnableFitmgrSwagger2;

/**
 * @author Taibai
 * @date 2018年06月21日s 用户统一管理系统
 */
@EnableFitmgrSwagger2
@SpringCloudApplication
@EnableFitmgrFeignClients
@EnableFitmgrResourceServer
@EnableAsync
@MapperScan(value = "com.taibai.admin.mapper")
@RibbonClients(defaultConfiguration = RibbonDefaultConfig.class)
public class AdminApplication {

    public static void main(String[] args) {
        SecurityContextHolder.setStrategyName("MODE_INHERITABLETHREADLOCAL");
        SpringApplication.run(AdminApplication.class, args);
    }

}
