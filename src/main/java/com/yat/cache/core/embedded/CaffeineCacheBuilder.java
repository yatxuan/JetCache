package com.yat.cache.core.embedded;

/**
 * Created on 2016/11/29.
 *
 * @author huangli
 */
public class CaffeineCacheBuilder<T extends EmbeddedCacheBuilder<T>> extends EmbeddedCacheBuilder<T> {
    protected CaffeineCacheBuilder() {
        buildFunc((c) -> new CaffeineCache((EmbeddedCacheConfig) c));
    }

    public static CaffeineCacheBuilderImpl createCaffeineCacheBuilder() {
        return new CaffeineCacheBuilderImpl();
    }

    public static class CaffeineCacheBuilderImpl extends CaffeineCacheBuilder<CaffeineCacheBuilderImpl> {
    }
}
