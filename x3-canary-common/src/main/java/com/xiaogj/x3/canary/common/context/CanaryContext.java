package com.xiaogj.x3.canary.common.context;

import java.util.Map;

/**
 * ribbon拦截上下文接口
 *
 * @author xiaolinlin
 */
public interface CanaryContext {

    /**
     * Adds the context attribute.
     *
     * @param key the attribute key
     * @param value the attribute value
     * @return the context instance
     */
    CanaryContext add(String key, String value);

    /**
     * Retrieves the context attribute.
     *
     * @param key the attribute key
     * @return the attribute value
     */
    String get(String key);

    /**
     * Removes the context attribute.
     *
     * @param key the context attribute
     * @return the context instance
     */
    CanaryContext remove(String key);

    /**
     * Retrieves the attributes.
     *
     * @return the attributes
     */
    Map<String, String> getAttributes();
}