package com.fitmgr.admin.threadpool;

import java.util.Map;
import java.util.function.Function;

import org.slf4j.MDC;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

public class InheritableRequestContextTaskWrapper {
    private Map parentMdc = MDC.getCopyOfContextMap();
    private RequestAttributes parentAttrs = RequestContextHolder.currentRequestAttributes();

    public <T, R> Function<T, R> lambda1(Function<T, R> runnable) {
        return t -> {
            Map orinMdc = MDC.getCopyOfContextMap();
            if (parentMdc == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(parentMdc);
            }

            RequestAttributes orinAttrs = null;
            try {
                orinAttrs = RequestContextHolder.currentRequestAttributes();
            } catch (IllegalStateException e) {
            }
            RequestContextHolder.setRequestAttributes(parentAttrs, true);
            try {
                return runnable.apply(t);
            } finally {
                if (orinMdc == null) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(orinMdc);
                }
                if (orinAttrs == null) {
                    RequestContextHolder.resetRequestAttributes();
                } else {
                    RequestContextHolder.setRequestAttributes(orinAttrs, true);
                }
            }
        };
    }

    public ConsumerInterface lambda2(ConsumerInterface runnable) {
        return () -> {
            Map orinMdc = MDC.getCopyOfContextMap();
            if (parentMdc == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(parentMdc);
            }

            RequestAttributes orinAttrs = null;
            try {
                orinAttrs = RequestContextHolder.currentRequestAttributes();
            } catch (IllegalStateException e) {
            }
            RequestContextHolder.setRequestAttributes(parentAttrs, true);
            try {
                runnable.accept();
            } finally {
                if (orinMdc == null) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(orinMdc);
                }
                if (orinAttrs == null) {
                    RequestContextHolder.resetRequestAttributes();
                } else {
                    RequestContextHolder.setRequestAttributes(orinAttrs, true);
                }
            }
        };
    }
}
