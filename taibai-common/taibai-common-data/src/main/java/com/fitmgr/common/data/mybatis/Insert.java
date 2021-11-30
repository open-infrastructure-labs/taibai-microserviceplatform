package com.fitmgr.common.data.mybatis;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;

public class Insert extends AbstractMethod {

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        KeyGenerator keyGenerator = new NoKeyGenerator();
        SqlMethod sqlMethod = SqlMethod.INSERT_ONE;
        String columnScript = SqlScriptUtils.convertTrim(tableInfo.getAllInsertSqlColumnMaybeIf(), LEFT_BRACKET,
                RIGHT_BRACKET, null, COMMA);
        String valuesScript = SqlScriptUtils.convertTrim(tableInfo.getAllInsertSqlPropertyMaybeIf(null), LEFT_BRACKET,
                RIGHT_BRACKET, null, COMMA);
        String keyProperty = null;
        String keyColumn = null;
        // 表包含主键处理逻辑,如果不包含主键当普通字段处理
        if (StringUtils.isNotBlank(tableInfo.getKeyProperty())) {
            if (Integer.class.getName().equals(tableInfo.getKeyType().getName())
                    || Long.class.getName().equals(tableInfo.getKeyType().getName())) {
                keyGenerator = new Jdbc3KeyGenerator();
            }
        }
        keyProperty = tableInfo.getKeyProperty();
        keyColumn = tableInfo.getKeyColumn();
        String sql = String.format(sqlMethod.getSql(), tableInfo.getTableName(), columnScript, valuesScript);
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
        return this.addInsertMappedStatement(mapperClass, modelClass, getMethod(sqlMethod), sqlSource, keyGenerator,
                keyProperty, keyColumn);
    }
}
