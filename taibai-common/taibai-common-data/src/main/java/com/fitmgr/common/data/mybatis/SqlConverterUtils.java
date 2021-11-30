package com.taibai.common.data.mybatis;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Taibai
 * @date 2021/6/29 11:05
 */

@Slf4j
public class SqlConverterUtils {
    /**
     * SQL语句like使用关键字%
     */
    private final static String LIKE_SQL_KEY = "%";

    /**
     * SQL语句需要转义的关键字
     */
    private final static String[] ESCAPE_CHAR = new String[] { LIKE_SQL_KEY, "_", "\\" };

    /**
     * mybatis-plus中like的SQL语句样式
     */
    private final static String MYBATIS_PLUS_LIKE_SQL = " like ?";

    /**
     * mybatis-plus中参数前缀
     */
    private final static String MYBATIS_PLUS_WRAPPER_PREFIX = "ew.paramNameValuePairs.";

    /**
     * mybatis-plus中参数键
     */
    final static String MYBATIS_PLUS_WRAPPER_KEY = "ew";

    /**
     * mybatis-plus中参数分隔符
     */
    final static String MYBATIS_PLUS_WRAPPER_SEPARATOR = ".";

    /**
     * mybatis-plus中参数分隔符替换器
     */
    final static String MYBATIS_PLUS_WRAPPER_SEPARATOR_REGEX = "\\.";

    /**
     * 已经替换过的标记
     */
    final static String REPLACED_LIKE_KEYWORD_MARK = "replaced.keyword";

    /**
     * 转义特殊字符
     */
    public static void convert(String sql, Set<String> fields, Object parameter) {
        for (String field : fields) {
            if (hasMybatisPlusLikeSql(sql)) {
                if (hasWrapper(field)) {
                    // 第一种情况：在业务层进行条件构造产生的模糊查询关键字,使用QueryWrapper,LambdaQueryWrapper
                    transferWrapper(field, parameter);
                } else {
                    // 第二种情况：未使用条件构造器，但是在service层进行了查询关键字与模糊查询符`%`手动拼接
                    transferSelf(field, parameter);
                }
            } else {
                // 第三种情况：在Mapper类的注解SQL中进行了模糊查询的拼接
                transferSplice(field, parameter);
            }
        }
    }

    /**
     * 转义条件构造的特殊字符 在业务层进行条件构造产生的模糊查询关键字,使用QueryWrapper,LambdaQueryWrapper
     */
    public static void transferWrapper(String field, Object parameterObject) {
        if (!(parameterObject instanceof HashMap)) {
            return;
        }
        Map parameter = (HashMap) parameterObject;
        AbstractWrapper wrapper = (AbstractWrapper) parameter.get(MYBATIS_PLUS_WRAPPER_KEY);
        parameter = wrapper.getParamNameValuePairs();
        String[] keys = field.split(MYBATIS_PLUS_WRAPPER_SEPARATOR_REGEX);
        // ew.paramNameValuePairs.param1，截取字符串之后，获取第三个，即为参数名
        String paramName = keys[2];
        String mapKey = String.format("%s.%s", REPLACED_LIKE_KEYWORD_MARK, paramName);
        if (parameter.containsKey(mapKey) && Objects.equals(parameter.get(mapKey), true)) {
            return;
        }
        if (cascade(field)) {
            resolveCascadeObj(field, parameter);
        } else {
            Object param = parameter.get(paramName);
            if (hasEscapeChar(param)) {
                String paramStr = param.toString();
                parameter.put(keys[2],
                        String.format("%%%s%%", escapeChar(paramStr.substring(1, paramStr.length() - 1))));
            }
        }
        parameter.put(mapKey, true);
    }

    /**
     * 转义自定义条件拼接的特殊字符 未使用条件构造器，但是在service层进行了查询关键字与模糊查询符`%`手动拼接
     */
    public static void transferSelf(String field, Object parameterObject) {
        if (!(parameterObject instanceof HashMap)) {
            return;
        }
        Map parameter = (HashMap) parameterObject;
        if (cascade(field)) {
            resolveCascadeObj(field, parameter);
            return;
        }
        Object param = parameter.get(field);
        if (hasEscapeChar(param)) {
            String paramStr = param.toString();
            parameter.put(field, String.format("%%%s%%", escapeChar(paramStr.substring(1, paramStr.length() - 1))));
        }
    }

    /**
     * 转义自定义条件拼接的特殊字符 在Mapper类的注解SQL中进行了模糊查询的拼接
     */
    public static void transferSplice(String field, Object parameterObject) {
        if (!(parameterObject instanceof HashMap)) {
            resolveObj(field, parameterObject);
            return;
        }
        Map parameter = (HashMap) parameterObject;
        if (cascade(field)) {
            resolveCascadeObj(field, parameter);
            return;
        }
        Object param = parameter.get(field);
        if (hasEscapeChar(param)) {
            parameter.put(field, escapeChar(param.toString()));
        }
    }

    /**
     * 处理级联属性
     */
    private static void resolveCascadeObj(String field, Map parameter) {
        int index = field.indexOf(MYBATIS_PLUS_WRAPPER_SEPARATOR);
        Object param = parameter.get(field.substring(0, index));
        if (param == null) {
            return;
        }
        resolveObj(field.substring(index + 1), param);
    }

    /**
     * 转义通配符
     */
    public static String escapeChar(String before) {
        if (StringUtils.isNotBlank(before)) {
            before = before.replaceAll("\\\\", "\\\\\\\\");
            before = before.replaceAll("_", "\\\\_");
            before = before.replaceAll("%", "\\\\%");
        }
        return before;
    }

    /**
     * 处理对象like问题
     */
    static void resolveObj(String field, Object parameter) {
        if (parameter == null || StringUtils.isBlank(field)) {
            return;
        }
        try {
            PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(parameter.getClass(), field);
            Method readMethod = descriptor.getReadMethod();
            Object param = readMethod.invoke(parameter);
            if (hasEscapeChar(param)) {
                Method setMethod = descriptor.getWriteMethod();
                setMethod.invoke(parameter, escapeChar(param.toString()));
            } else if (cascade(field)) {
                int index = field.indexOf(MYBATIS_PLUS_WRAPPER_SEPARATOR) + 1;
                resolveObj(field.substring(index), param);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("反射 {} 的 {} get/set方法出现异常", parameter, field, e);
        }
    }

    /**
     * 判断是否是级联属性
     */
    static boolean cascade(String field) {
        if (StringUtils.isBlank(field)) {
            return false;
        }
        return field.contains(MYBATIS_PLUS_WRAPPER_SEPARATOR) && !hasWrapper(field);
    }

    /**
     * 是否包含mybatis-plus的包含like的SQL语句格式
     */
    private static boolean hasMybatisPlusLikeSql(String sql) {
        if (StringUtils.isBlank(sql)) {
            return false;
        }
        return sql.toLowerCase().contains(MYBATIS_PLUS_LIKE_SQL);
    }

    /**
     * 判断是否使用mybatis-plus条件构造器
     */
    private static boolean hasWrapper(String field) {
        if (StringUtils.isBlank(field)) {
            return false;
        }
        return field.contains(MYBATIS_PLUS_WRAPPER_PREFIX);
    }

    /**
     * 是否包含需要转义的字符
     */
    static boolean hasEscapeChar(Object obj) {
        if (!(obj instanceof String)) {
            return false;
        }
        String str = ((String) obj);
        if (StringUtils.isBlank(str)) {
            return false;
        }
        for (String s : ESCAPE_CHAR) {
            if (str.contains(s)) {
                return true;
            }
        }
        return false;
    }
}