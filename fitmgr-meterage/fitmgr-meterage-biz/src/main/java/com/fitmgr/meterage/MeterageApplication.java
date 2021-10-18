package com.fitmgr.meterage;

import com.fitmgr.common.core.ribbon.RibbonDefaultConfig;
import com.fitmgr.common.security.annotation.EnableFitmgrFeignClients;
import com.fitmgr.common.security.annotation.EnableFitmgrResourceServer;
import com.fitmgr.common.swagger.annotation.EnableFitmgrSwagger2;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.ribbon.RibbonClients;

/**
 * @author duan
 * @date 2019年12月23日
 * 计量模块
 */
@EnableFitmgrSwagger2
@SpringCloudApplication
@EnableFitmgrFeignClients
@EnableFitmgrResourceServer
@MapperScan(value = "com.fitmgr.meterage.mapper")
@RibbonClients(defaultConfiguration = RibbonDefaultConfig.class)
public class MeterageApplication {
    public static void main(String[] args) {
        SpringApplication.run(MeterageApplication.class, args);
    }
}
