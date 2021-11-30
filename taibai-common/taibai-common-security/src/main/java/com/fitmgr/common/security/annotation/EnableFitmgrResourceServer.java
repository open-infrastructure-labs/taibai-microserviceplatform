
package com.fitmgr.common.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

import com.fitmgr.common.security.component.ResourceServerAutoConfiguration;
import com.fitmgr.common.security.component.SecurityBeanDefinitionRegistrar;

/**
 * @author Fitmgr
 * @date 2018/11/10
 *       <p>
 *       资源服务注解
 */
@Documented
@Inherited
@EnableResourceServer
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Import({ ResourceServerAutoConfiguration.class, SecurityBeanDefinitionRegistrar.class })
public @interface EnableFitmgrResourceServer {

}
