
package com.taibai.common.data.datascope;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.security.core.GrantedAuthority;

import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.handlers.AbstractSqlParserHandler;
import com.taibai.common.core.constant.SecurityConstants;
import com.taibai.common.core.exception.CheckedException;
import com.taibai.common.data.enums.DataScopeTypeEnum;
import com.taibai.common.security.service.FitmgrUser;
import com.taibai.common.security.util.SecurityUtils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Taibai
 * @date 2018/12/26
 *       <p>
 *       mybatis 数据权限拦截器
 */
@Slf4j
@AllArgsConstructor
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class, Integer.class }) })
public class DataScopeInterceptor extends AbstractSqlParserHandler implements Interceptor {
    private final DataSource dataSource;

    @Override
    @SneakyThrows
    public Object intercept(Invocation invocation) {
        StatementHandler statementHandler = PluginUtils.realTarget(invocation.getTarget());
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        this.sqlParser(metaObject);
        // 先判断是不是SELECT操作
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        if (!SqlCommandType.SELECT.equals(mappedStatement.getSqlCommandType())) {
            return invocation.proceed();
        }

        BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
        String originalSql = boundSql.getSql();
        Object parameterObject = boundSql.getParameterObject();

        // 查找参数中包含DataScope类型的参数
        DataScope dataScope = findDataScopeObject(parameterObject);
        if (dataScope == null) {
            return invocation.proceed();
        }

        String scopeName = dataScope.getScopeName();
        List<String> emails = dataScope.getEmails();
        // 优先获取赋值数据
        if (CollUtil.isEmpty(emails)) {
            FitmgrUser user = SecurityUtils.getUser();
            if (user == null) {
                throw new CheckedException("auto datascope, set up security details true");
            }

            List<String> roleIdList = user.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                    .filter(authority -> authority.startsWith(SecurityConstants.ROLE))
                    .map(authority -> authority.split("_")[1]).collect(Collectors.toList());

            Entity query = Db.use(dataSource)
                    .query("SELECT * FROM sys_role where role_id IN (" + CollUtil.join(roleIdList, ",") + ")").stream()
                    .min(Comparator.comparingInt(o -> o.getInt("ds_type"))).get();

            Integer dsType = query.getInt("ds_type");
            // 查询全部
            if (DataScopeTypeEnum.ALL.getType() == dsType) {
                return invocation.proceed();
            }
            // 自定义
            if (DataScopeTypeEnum.CUSTOM.getType() == dsType) {
                String dsScope = query.getStr("ds_scope");
                emails.addAll(Arrays.stream(dsScope.split(",")).collect(Collectors.toList()));
            }
            // 查询本级及其下级
            if (DataScopeTypeEnum.OWN_CHILD_LEVEL.getType() == dsType) {
                List<String> emailList = Db.use(dataSource).findBy("sys_dept_relation", "ancestor", user.getEmail())
                        .stream().map(entity -> entity.getStr("descendant")).collect(Collectors.toList());
                emails.addAll(emailList);
            }
            // 只查询本级
            if (DataScopeTypeEnum.OWN_LEVEL.getType() == dsType) {
                emails.add(user.getEmail());
            }
        }
        String join = CollectionUtil.join(emails, ",");
        originalSql = "select * from (" + originalSql + ") temp_data_scope where temp_data_scope." + scopeName + " in ("
                + join + ")";
        metaObject.setValue("delegate.boundSql.sql", originalSql);
        return invocation.proceed();
    }

    /**
     * 生成拦截对象的代理
     *
     * @param target 目标对象
     * @return 代理对象
     */
    @Override
    public Object plugin(Object target) {
        if (target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    /**
     * mybatis配置的属性
     *
     * @param properties mybatis配置的属性
     */
    @Override
    public void setProperties(Properties properties) {

    }

    /**
     * 查找参数是否包括DataScope对象
     *
     * @param parameterObj 参数列表
     * @return DataScope
     */
    private DataScope findDataScopeObject(Object parameterObj) {
        if (parameterObj instanceof DataScope) {
            return (DataScope) parameterObj;
        } else if (parameterObj instanceof Map) {
            for (Object val : ((Map<?, ?>) parameterObj).values()) {
                if (val instanceof DataScope) {
                    return (DataScope) val;
                }
            }
        }
        return null;
    }

}
