
package com.fitmgr.common.log.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.fitmgr.common.core.constant.enums.OperateTypeEnum;

/**
 * @author Fitmgr
 * @date 2019/11/27 操作日志注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SysLog {

    /**
     * 描述
     *
     * @return {String}
     */
    String value() default "";

    /**
     * 是否是资源操作
     * 
     * @return true/false
     */
    boolean resource() default false;

    /**
     * 云资源类型
     * 
     * @return
     */
    String cloudResType() default "";

    /**
     * 是否为批量操作
     * 
     * @return
     */
    boolean isBatch() default false;

    /**
     * 云资源名称的参数位置 arg ：表示接口的入参就是名称 arg.xxx ： 表示接口的入参是个对象，对象里面的xxx字段表示名称
     * 
     * @return
     */
    String resNameLocation() default "";

    /**
     * 云资源名称在第几个接口入参中，默认是第0个
     * 
     * @return
     */
    int resNameArgIndex() default 0;

    /**
     * 云资源id的参数位置 arg : 表示接口的入参就是id arg.xxx : 表示接口的入参是个对象，对象里面的xxx字段表示id
     * 
     * @return
     */
    String resIdLocation() default "";

    /**
     * 云资源id在第几个接口入参中，默认是第0个
     * 
     * @return
     */
    int resIdArgIndex() default 0;

    /**
     * 操作类型
     * 
     * @return
     */
    OperateTypeEnum operateType() default OperateTypeEnum.OTHER;

    /**
     * 操作位置
     * 
     * @return
     */
    String operateLocation() default "";
}
