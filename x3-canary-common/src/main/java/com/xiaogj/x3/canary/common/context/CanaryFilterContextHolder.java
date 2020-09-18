package com.xiaogj.x3.canary.common.context;

/**
 * ribbon拦截 上下文从操作类
 *
 * @author xiaolinlin
 */
public class CanaryFilterContextHolder {

    /**
     * Stores the {@link CanaryContext} for current thread.
     */
    private static final ThreadLocal<CanaryContext> contextHolder = new InheritableThreadLocal<CanaryContext>() {
        @Override
        protected CanaryContext initialValue() {
            return new DefaultCanaryContext();
        }
    };

    /**
     * Retrieves the current thread bound instance of {@link CanaryContext}.
     *
     * @return the current context
     */
    public static CanaryContext getCurrentContext() {
        return contextHolder.get();
    }

    /**
     * Clears the current context.
     */
    public static void clearCurrentContext() {
        contextHolder.remove();
    }
}