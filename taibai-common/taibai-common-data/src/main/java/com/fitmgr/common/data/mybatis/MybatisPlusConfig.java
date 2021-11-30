
package com.fitmgr.common.data.mybatis;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.core.parser.ISqlParser;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.tenant.TenantSqlParser;
import com.fitmgr.common.data.datascope.DataScopeInterceptor;
import com.fitmgr.common.data.tenant.MyTenantHandler;

/**
 * @author Fitmgr
 * @date 2017/10/29
 */
@Configuration
@ConditionalOnBean(DataSource.class)
@MapperScan("com.fitmgr.**.mapper")
public class MybatisPlusConfig {

    /**
     * mybatis/mybatis-plus模糊查询语句特殊字符转义拦截器
     */
    @Bean
    public MybatisLikeSqlInterceptor mybatisSqlInterceptor() {
        return new MybatisLikeSqlInterceptor();
    }

    /**
     * 创建租户维护处理器对象
     *
     * @return 处理后的租户维护处理器
     */
    @Bean
    @ConditionalOnMissingBean
    public MyTenantHandler pigxTenantHandler() {
        return new MyTenantHandler();
    }

    /**
     * 分页插件
     *
     * @param myTenantHandler 租户处理器
     * @return PaginationInterceptor
     */
    @Bean
    @ConditionalOnMissingBean
    public PaginationInterceptor paginationInterceptor(MyTenantHandler myTenantHandler) {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        List<ISqlParser> sqlParserList = new ArrayList<>();
        TenantSqlParser tenantSqlParser = new TenantSqlParser();
        tenantSqlParser.setTenantHandler(myTenantHandler);
        sqlParserList.add(tenantSqlParser);
        paginationInterceptor.setSqlParserList(sqlParserList);
        return paginationInterceptor;
    }

    /**
     * 数据权限插件
     *
     * @param dataSource 数据源
     * @return DataScopeInterceptor
     */
    @Bean
    @ConditionalOnMissingBean
    public DataScopeInterceptor dataScopeInterceptor(DataSource dataSource) {
        return new DataScopeInterceptor(dataSource);
    }

    /**
     * 逻辑删除插件
     *
     * @return LogicSqlInjector
     */
    @Bean
    @ConditionalOnMissingBean
    public ISqlInjector sqlInjector() {
        return new MySqlInjector();
    }
}
