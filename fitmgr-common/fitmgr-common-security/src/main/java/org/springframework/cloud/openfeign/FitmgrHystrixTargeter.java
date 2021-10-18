
package org.springframework.cloud.openfeign;

import org.springframework.lang.Nullable;

import feign.Feign;
import feign.Target;
import feign.hystrix.HystrixFeign;
import feign.hystrix.SetterFactory;

/**
 * @author Fitmgr
 *         <p>
 *         注入默认的 fallbackFactory
 */
@SuppressWarnings("unchecked")
class FitmgrHystrixTargeter implements Targeter {

    @Override
    public <T> T target(FeignClientFactoryBean factory, Feign.Builder feign, FeignContext context,
            Target.HardCodedTarget<T> target) {
        if (!(feign instanceof HystrixFeign.Builder)) {
            return feign.target(target);
        }
        HystrixFeign.Builder builder = (HystrixFeign.Builder) feign;
        SetterFactory setterFactory = getOptional(factory.getName(), context, SetterFactory.class);
        if (setterFactory != null) {
            builder.setterFactory(setterFactory);
        }
        Class<?> fallback = factory.getFallback();
        if (fallback != void.class) {
            return targetWithFallback(factory.getName(), context, target, builder, fallback);
        }
        Class<?> fallbackFactory = factory.getFallbackFactory();
        if (fallbackFactory != void.class) {
            return targetWithFallbackFactory(factory.getName(), context, target, builder, fallbackFactory);
        }

        FitmgrFallbackFactory fallbackFactoryy = new FitmgrFallbackFactory(target);
        return (T) builder.target(target, fallbackFactoryy);
    }

    private <T> T targetWithFallbackFactory(String feignClientName, FeignContext context,
            Target.HardCodedTarget<T> target, HystrixFeign.Builder builder, Class<?> fallbackFactoryClass) {
        FitmgrFallbackFactory<? extends T> fallbackFactory = (FitmgrFallbackFactory<? extends T>) getFromContext(
                "fallbackFactory", feignClientName, context, fallbackFactoryClass, FitmgrFallbackFactory.class);
        return builder.target(target, fallbackFactory);
    }

    private <T> T targetWithFallback(String feignClientName, FeignContext context, Target.HardCodedTarget<T> target,
            HystrixFeign.Builder builder, Class<?> fallback) {
        T fallbackInstance = getFromContext("fallback", feignClientName, context, fallback, target.type());
        return builder.target(target, fallbackInstance);
    }

    private <T> T getFromContext(String fallbackMechanism, String feignClientName, FeignContext context,
            Class<?> beanType, Class<T> targetType) {
        Object fallbackInstance = context.getInstance(feignClientName, beanType);
        if (fallbackInstance == null) {
            throw new IllegalStateException(
                    String.format("No " + fallbackMechanism + " instance of type %s found for feign client %s",
                            beanType, feignClientName));
        }

        if (!targetType.isAssignableFrom(beanType)) {
            throw new IllegalStateException(String.format(
                    "Incompatible " + fallbackMechanism + " instance. Fallback/fallbackFactory of "
                            + "type %s is not assignable to %s for feign client %s",
                    beanType, targetType, feignClientName));
        }
        return (T) fallbackInstance;
    }

    @Nullable
    private <T> T getOptional(String feignClientName, FeignContext context, Class<T> beanType) {
        return context.getInstance(feignClientName, beanType);
    }
}
