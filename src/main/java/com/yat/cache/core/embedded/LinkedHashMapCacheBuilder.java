package com.yat.cache.core.embedded;

/**
 * Created on 2016/11/29.
 *
 * @author huangli
 */
public class LinkedHashMapCacheBuilder<T extends EmbeddedCacheBuilder<T>> extends EmbeddedCacheBuilder<T> {
    protected LinkedHashMapCacheBuilder() {
        buildFunc((c) -> new LinkedHashMapCache((EmbeddedCacheConfig) c));
    }

    public static LinkedHashMapCacheBuilderImpl createLinkedHashMapCacheBuilder() {
        return new LinkedHashMapCacheBuilderImpl();
    }

    public static class LinkedHashMapCacheBuilderImpl extends LinkedHashMapCacheBuilder<LinkedHashMapCacheBuilderImpl> {
    }
}
