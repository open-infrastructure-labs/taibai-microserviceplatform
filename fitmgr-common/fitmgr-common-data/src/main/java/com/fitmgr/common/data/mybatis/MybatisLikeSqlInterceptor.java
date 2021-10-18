package com.fitmgr.common.data.mybatis;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import lombok.extern.slf4j.Slf4j;

/**
 * mybatis/mybatis-plus模糊查询语句特殊字符转义拦截器
 *
 * @author Fitmgr
 * @date 2021/6/29 9:51
 */

@Slf4j
@Intercepts({ @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
        RowBounds.class, ResultHandler.class }) })
public class MybatisLikeSqlInterceptor implements Interceptor {

    /**
     * SQL语句like
     */
    private final static String SQL_LIKE = " like ";

    /**
     * SQL语句占位符
     */
    private final static String SQL_PLACEHOLDER = "?";

    /**
     * SQL语句占位符分隔
     */
    private final static String SQL_PLACEHOLDER_REGEX = "\\?";

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement statement = (MappedStatement) args[0];
        Object parameterObject = args[1];
        BoundSql boundSql = statement.getBoundSql(parameterObject);
        String sql = boundSql.getSql();
        transferLikeSql(sql, parameterObject, boundSql);
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties arg0) {

    }

    /**
     * 修改包含like的SQL语句
     */
    private void transferLikeSql(String sql, Object parameterObject, BoundSql boundSql) {
        if (!isEscape(sql)) {
            return;
        }
        sql = sql.replaceAll(" {2}", " ");
        // 获取关键字的个数（去重）
        Set<String> fields = getKeyFields(sql, boundSql);
//        if (fields == null) {
//            return;
//        }
        SqlConverterUtils.convert(sql, fields, parameterObject);
    }

    /**
     * 是否需要转义
     */
    private boolean isEscape(String sql) {
        return hasLike(sql) && hasPlaceholder(sql);
    }

    /**
     * 判断SQL语句中是否含有like关键字
     */
    private boolean hasLike(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        return str.toLowerCase().contains(SQL_LIKE);
    }

    /**
     * 判断SQL语句中是否包含SQL占位符
     */
    private boolean hasPlaceholder(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        return str.toLowerCase().contains(SQL_PLACEHOLDER);
    }

    /**
     * 获取需要替换的所有字段集合
     */
    private Set<String> getKeyFields(String sql, BoundSql boundSql) {
        String[] params = sql.split(SQL_PLACEHOLDER_REGEX);
        Set<String> fields = new HashSet<>();
        for (int i = 0; i < params.length; i++) {
            if (hasLike(params[i])) {
                String field = boundSql.getParameterMappings().get(i).getProperty();
                fields.add(field);
            }
        }
        return fields;
    }

}
