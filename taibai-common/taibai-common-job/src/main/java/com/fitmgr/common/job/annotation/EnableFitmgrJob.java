
package com.fitmgr.common.job.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.fitmgr.common.job.ElasticJobAutoConfiguration;

/**
 * @author Fitmgr
 * @date 2018/7/24 开启Link job
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({ ElasticJobAutoConfiguration.class })
public @interface EnableFitmgrJob {
}
