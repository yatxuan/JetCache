package com.yat.cache.core.embedded;

/**
 * ClassName LinkedHashMapCacheBuilder
 * <p>Description LinkedHashMap缓存构建器</p>
 *
 * @author Yat
 * Date 2024/8/22 13:22
 * version 1.0
 */
public class LinkedHashMapCacheBuilder<T extends EmbeddedCacheBuilder<T>> extends EmbeddedCacheBuilder<T> {

    protected LinkedHashMapCacheBuilder() {
        buildFunc((c) -> new LinkedHashMapCache<>((EmbeddedCacheConfig) c));
    }

    public static LinkedHashMapCacheBuilderImpl createLinkedHashMapCacheBuilder() {
        return new LinkedHashMapCacheBuilderImpl();
    }

    public static class LinkedHashMapCacheBuilderImpl extends LinkedHashMapCacheBuilder<LinkedHashMapCacheBuilderImpl> {
    }
}
