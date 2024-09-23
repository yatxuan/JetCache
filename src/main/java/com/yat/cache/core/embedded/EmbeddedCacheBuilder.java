package com.yat.cache.core.embedded;

import com.yat.cache.core.AbstractCacheBuilder;
import lombok.NoArgsConstructor;

/**
 * ClassName EmbeddedCacheBuilder
 * <p>Description 缓存构建器:用于构建缓存配置</p>
 *
 * @author Yat
 * Date 2024/8/22 11:39
 * version 1.0
 */
@NoArgsConstructor
public class EmbeddedCacheBuilder<T extends EmbeddedCacheBuilder<T>> extends AbstractCacheBuilder<T> {

    public T limit(int limit) {
        getConfig().setLimit(limit);
        return self();
    }

    @Override
    public EmbeddedCacheConfig getConfig() {
        if (config == null) {
            config = new EmbeddedCacheConfig<>();
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
