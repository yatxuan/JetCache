package com.yat.cache.core.external;

import com.yat.cache.core.CacheResult;
import com.yat.cache.core.JetCacheManager;
import com.yat.cache.core.support.BroadcastManager;
import com.yat.cache.core.support.CacheMessage;
import lombok.Getter;

/**
 * ClassName MockRemoteCacheBuilder
 * <p>Description 模拟远程缓存构建器</p>
 * <p>
 * 此类用于构建一个模拟远程缓存实例。
 * </p>
 *
 * @author Yat
 * Date 2024/8/22 13:49
 * version 1.0
 */
public class MockRemoteCacheBuilder<T extends ExternalCacheBuilder<T>> extends ExternalCacheBuilder<T> {

    /**
     * 标记是否已开始订阅广播。
     */
    @Getter
    private static volatile boolean subscribeStart;
    /**
     * 上次发布的缓存消息。
     */
    @Getter
    private static volatile CacheMessage lastPublishMessage;

    /**
     * 构造一个新的模拟远程缓存构建器实例。
     */
    public MockRemoteCacheBuilder() {
        // 设置默认键前缀
        this.setKeyPrefix("DEFAULT_PREFIX");
        // 设置构建函数
        buildFunc((c) -> new MockRemoteJetCache((MockRemoteCacheConfig) c));
    }

    /**
     * 设置缓存限制。
     *
     * @param limit 缓存限制
     * @return 当前构建器实例
     */
    @SuppressWarnings("UnusedReturnValue")
    public T limit(int limit) {
        getConfig().setLimit(limit);
        return self();
    }

    /**
     * 设置缓存限制。
     *
     * @param limit 缓存限制
     */
    public void setLimit(int limit) {
        getConfig().setLimit(limit);
    }

    /**
     * 判断是否支持广播功能。
     *
     * @return 是否支持广播功能
     */
    @Override
    public boolean supportBroadcast() {
        return true;
    }

    /**
     * 创建广播管理器。
     *
     * @param jetCacheManager 缓存管理器
     * @return 广播管理器
     */
    @Override
    public BroadcastManager createBroadcastManager(JetCacheManager jetCacheManager) {
        return new BroadcastManager(jetCacheManager) {
            /**
             * 发布缓存消息。
             *
             * @param cacheMessage 要发布的缓存消息
             * @return 发布结果
             */
            @Override
            public CacheResult publish(CacheMessage cacheMessage) {
                lastPublishMessage = cacheMessage;
                return CacheResult.SUCCESS_WITHOUT_MSG;
            }

            /**
             * 开始订阅。
             */
            @Override
            public void startSubscribe() {
                subscribeStart = true;
            }
        };
    }

    /**
     * 获取当前缓存配置。
     *
     * @return 当前缓存配置
     */
    @Override
    public MockRemoteCacheConfig getConfig() {
        if (config == null) {
            config = new MockRemoteCacheConfig();
        }
        return (MockRemoteCacheConfig) config;
    }

    /**
     * 创建一个新的模拟远程缓存构建器实例。
     *
     * @return 新的模拟远程缓存构建器实例
     */
    public static MockRemoteCacheBuilderImpl createMockRemoteCacheBuilder() {
        return new MockRemoteCacheBuilderImpl();
    }

    /**
     * 重置模拟远程缓存构建器的状态。
     */
    public static void reset() {
        subscribeStart = false;
        lastPublishMessage = null;
    }

    public static class MockRemoteCacheBuilderImpl extends MockRemoteCacheBuilder<MockRemoteCacheBuilderImpl> {
    }
}
