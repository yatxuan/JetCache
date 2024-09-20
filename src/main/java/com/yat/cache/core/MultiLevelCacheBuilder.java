package com.yat.cache.core;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created on 2017/5/24.
 *
 * @author huangli
 */
public class MultiLevelCacheBuilder<T extends MultiLevelCacheBuilder<T>> extends AbstractCacheBuilder<T> {
    protected MultiLevelCacheBuilder() {
        buildFunc(config -> new MultiLevelCache((MultiLevelCacheConfig) config));
    }

    public T addCache(Cache... caches) {
        for (Cache c : caches) {
            getConfig().getCaches().add(c);
        }
        return self();
    }

    @Override
    public MultiLevelCacheConfig getConfig() {
        if (config == null) {
            config = new MultiLevelCacheConfig();
        }
        return (MultiLevelCacheConfig) config;
    }

    @Override
    public T keyConvertor(Function<Object, Object> keyConvertor) {
        throw new UnsupportedOperationException("MultiLevelCache do not need a key convertor");
    }

    @Override
    public void setKeyConvertor(Function<Object, Object> keyConvertor) {
        throw new UnsupportedOperationException("MultiLevelCache do not need a key convertor");
    }

    @Override
    public T expireAfterAccess(long defaultExpire, TimeUnit timeUnit) {
        throw new UnsupportedOperationException("MultiLevelCache do not support expireAfterAccess");
    }

    @Override
    public void setExpireAfterAccessInMillis(long expireAfterAccessInMillis) {
        throw new UnsupportedOperationException("MultiLevelCache do not support expireAfterAccess");
    }

    public void setCaches(List<Cache> caches) {
        getConfig().setCaches(caches);
    }

    public T useExpireOfSubCache(boolean useExpireOfSubCache) {
        getConfig().setUseExpireOfSubCache(useExpireOfSubCache);
        return self();
    }

    public void setUseExpireOfSubCache(boolean useExpireOfSubCache) {
        getConfig().setUseExpireOfSubCache(useExpireOfSubCache);
    }

    public static MultiLevelCacheBuilderImpl createMultiLevelCacheBuilder() {
        return new MultiLevelCacheBuilderImpl();
    }

    public static class MultiLevelCacheBuilderImpl extends MultiLevelCacheBuilder<MultiLevelCacheBuilderImpl> {
    }

}
