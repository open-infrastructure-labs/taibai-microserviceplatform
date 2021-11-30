
package org.springframework.cloud.openfeign;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.lang.Nullable;

import com.fitmgr.common.core.constant.CommonConstants;
import com.fitmgr.common.core.util.R;

import cn.hutool.core.util.StrUtil;
import feign.FeignException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Fitmgr
 *         <p>
 *         fallback 代理处理
 */
@Slf4j
@AllArgsConstructor
public class FitmgrFeignFallback<T> implements MethodInterceptor {
    private final Class<T> targetType;
    private final String targetName;
    private final Throwable cause;

    @Nullable
    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        Class<?> returnType = method.getReturnType();
        if (R.class != returnType) {
            return null;
        }
        FeignException exception = (FeignException) cause;

        byte[] content = exception.content();

        String str = StrUtil.str(content, StandardCharsets.UTF_8);

        log.error("FeignFallback:[{}.{}] serviceId:[{}] message:[{}]", targetType.getName(), method.getName(),
                targetName, str);
        return R.builder().code(CommonConstants.FAIL).msg(str).build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FitmgrFeignFallback<?> that = (FitmgrFeignFallback<?>) o;
        return targetType.equals(that.targetType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetType);
    }
}
