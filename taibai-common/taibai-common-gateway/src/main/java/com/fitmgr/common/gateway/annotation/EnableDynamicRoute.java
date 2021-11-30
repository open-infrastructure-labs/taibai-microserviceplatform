
package com.fitmgr.common.gateway.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.fitmgr.common.gateway.configuration.DynamicRouteAutoConfiguration;

/**
 * @author Fitmgr
 * @date 2018/11/5
 *       <p>
 *       开启fitmgr 动态路由
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(DynamicRouteAutoConfiguration.class)
public @interface EnableDynamicRoute {
}
