package com.yat.cache.core;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * MultiLevelCacheBuilder 类用于构建多级缓存
 * 它继承自 AbstractCacheBuilder<T> 并具体实现多级缓存的构建逻辑
 *
 * @author Yat
 * Date 2024/8/22 20:38
 * version 1.0
 */
public class MultiLevelCacheBuilder<T extends MultiLevelCacheBuilder<T>> extends AbstractCacheBuilder<T> {

    /**
     * 初始化时设置构建函数
     */
    protected MultiLevelCacheBuilder() {
        // 设置 MultiLevelCache 作为缓存构建的实现
        buildFunc(config -> new MultiLevelJetCache((MultiLevelCacheConfig) config));
    }

    /**
     * 添加一个或多个缓存到多级缓存配置中
     *
     * @param caches 要添加的缓存数组
     * @return 返回自身实例，支持链式调用
     */
    public T addCache(JetCache... caches) {
        for (JetCache c : caches) {
            getConfig().getCaches().add(c);
        }
        return self();
    }

    /**
     * 获取多级缓存配置
     *
     * @return MultiLevelCacheConfig 实例
     */
    @Override
    public MultiLevelCacheConfig getConfig() {
        if (config == null) {
            config = new MultiLevelCacheConfig();
        }
        return (MultiLevelCacheConfig) config;
    }

    /**
     * 设置键转换器，对于多级缓存不支持此功能
     *
     * @param keyConvertor 键转换器函数
     * @throws UnsupportedOperationException 固定抛出不支持操作异常
     */
    @Override
    public T keyConvertor(Function<Object, Object> keyConvertor) {
        throw new UnsupportedOperationException("MultiLevelCache do not need a key convertor");
    }

    /**
     * 设置键转换器，对于多级缓存不支持此功能
     *
     * @param keyConvertor 键转换器函数
     * @throws UnsupportedOperationException 固定抛出不支持操作异常
     */
    @Override
    public void setKeyConvertor(Function<Object, Object> keyConvertor) {
        throw new UnsupportedOperationException("MultiLevelCache do not need a key convertor");
    }

    @Override
    public T expireAfterAccess(long defaultExpire, TimeUnit timeUnit) {
        throw new UnsupportedOperationException("MultiLevelCache do not support expireAfterAccess");
    }

    /**
     * 设置访问后的过期时间，对于多级缓存不支持此功能
     *
     * @param expireAfterAccessInMillis 过期时间（毫秒）
     * @throws UnsupportedOperationException 固定抛出不支持操作异常
     */
    @Override
    public void setExpireAfterAccessInMillis(long expireAfterAccessInMillis) {
        throw new UnsupportedOperationException("MultiLevelCache do not support expireAfterAccess");
    }

    /**
     * 设置多级缓存中的缓存列表
     *
     * @param caches 缓存列表
     */
    public void setCaches(List<JetCache> caches) {
        getConfig().setCaches(caches);
    }

    public T useExpireOfSubCache(boolean useExpireOfSubCache) {
        getConfig().setUseExpireOfSubCache(useExpireOfSubCache);
        return self();
    }

    /**
     * 设置是否使用子缓存的过期时间
     *
     * @param useExpireOfSubCache 是否使用子缓存的过期时间
     */
    public void setUseExpireOfSubCache(boolean useExpireOfSubCache) {
        getConfig().setUseExpireOfSubCache(useExpireOfSubCache);
    }

    /**
     * 创建多级缓存构建器实例
     *
     * @return MultiLevelCacheBuilderImpl 实例
     */
    public static MultiLevelCacheBuilderImpl createMultiLevelCacheBuilder() {
        return new MultiLevelCacheBuilderImpl();
    }

    /**
     * MultiLevelCacheBuilderImpl 类，具体实现 MultiLevelCacheBuilder<T>
     */
    public static class MultiLevelCacheBuilderImpl extends MultiLevelCacheBuilder<MultiLevelCacheBuilderImpl> {
    }

}
