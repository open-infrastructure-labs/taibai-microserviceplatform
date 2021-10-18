
package org.springframework.cloud.openfeign;

import org.springframework.cglib.proxy.Enhancer;

import feign.Target;
import feign.hystrix.FallbackFactory;
import lombok.AllArgsConstructor;

/**
 * @author Fitmgr
 *         <p>
 *         默认 Fallback，避免写过多fallback类
 */
@AllArgsConstructor
public class FitmgrFallbackFactory<T> implements FallbackFactory<T> {
    private final Target<T> target;

    @Override
    @SuppressWarnings("unchecked")
    public T create(Throwable cause) {
        final Class<T> targetType = target.type();
        final String targetName = target.name();
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(targetType);
        enhancer.setUseCache(true);
        enhancer.setCallback(new FitmgrFeignFallback<>(targetType, targetName, cause));
        return (T) enhancer.create();
    }
}
