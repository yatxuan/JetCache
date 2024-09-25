package com.yat.cache.core.embedded;

/**
 * ClassName CaffeineCacheBuilder
 * <p>Description 基于 Caffeine 的缓存的构建器</p>
 *
 * @author Yat
 * Date 2024/8/22 11:37
 * version 1.0
 */
public class CaffeineCacheBuilder<T extends EmbeddedCacheBuilder<T>> extends EmbeddedCacheBuilder<T> {

    protected CaffeineCacheBuilder() {
        buildFunc((c) -> new CaffeineCache((EmbeddedCacheConfig) c));
    }

    /**
     * 创建 Caffeine 缓存构建器的静态方法。
     *
     * @return Caffeine 缓存构建器的实现。
     */
    public static CaffeineCacheBuilderImpl createCaffeineCacheBuilder() {
        return new CaffeineCacheBuilderImpl();
    }

    /**
     * Caffeine 缓存构建器的具体实现类。
     */
    public static class CaffeineCacheBuilderImpl extends CaffeineCacheBuilder<CaffeineCacheBuilderImpl> {
    }
}
