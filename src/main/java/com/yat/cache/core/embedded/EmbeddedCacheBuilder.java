package com.yat.cache.core.embedded;

import com.yat.cache.core.AbstractCacheBuilder;

/**
 * Created on 16/9/7.
 *
 * @author huangli
 */
public class EmbeddedCacheBuilder<T extends EmbeddedCacheBuilder<T>> extends AbstractCacheBuilder<T> {

    public EmbeddedCacheBuilder() {
    }

    public T limit(int limit) {
        getConfig().setLimit(limit);
        return self();
    }

    @Override
    public EmbeddedCacheConfig getConfig() {
        if (config == null) {
            config = new EmbeddedCacheConfig();
        }
        return (EmbeddedCacheConfig) config;
    }

    public void setLimit(int limit) {
        getConfig().setLimit(limit);
    }

    public static EmbeddedCacheBuilderImpl createEmbeddedCacheBuilder() {
        return new EmbeddedCacheBuilderImpl();
    }

    public static class EmbeddedCacheBuilderImpl extends EmbeddedCacheBuilder<EmbeddedCacheBuilderImpl> {
    }

}
