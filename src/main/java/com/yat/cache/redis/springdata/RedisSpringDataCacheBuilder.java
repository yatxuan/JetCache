package com.yat.cache.redis.springdata;

import com.yat.cache.core.CacheManager;
import com.yat.cache.core.external.ExternalCacheBuilder;
import com.yat.cache.core.support.BroadcastManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * RedisSpringDataCacheBuilder 类用于构建基于Spring Data Redis的缓存构建器。
 * 它扩展了 ExternalCacheBuilder 类，以支持使用 Spring Data Redis 作为底层存储的缓存配置。
 * <p>
 * 该类提供了设置 Redis 连接工厂和 Redis 消息监听容器的方法，以便在缓存操作中使用。
 * 同时，它也支持创建广播管理器，以实现缓存数据变动时的通知机制。
 * </p>
 *
 * @param <T> 扩展的泛型 ExternalCacheBuilder 类型参数
 * @author Yat
 * Date 2024/8/22 22:12
 * version 1.0
 */
public class RedisSpringDataCacheBuilder<T extends ExternalCacheBuilder<T>> extends ExternalCacheBuilder<T> {

    /**
     * 用于初始化缓存构建器。
     * 通过调用父类的设置创建缓存实例的工厂方法。
     */
    protected RedisSpringDataCacheBuilder() {
        buildFunc(config -> new RedisSpringDataCache((RedisSpringDataCacheConfig) config));
    }

    /**
     * 指示当前缓存配置是否支持广播功能。
     *
     * @return true 表示支持广播功能
     */
    @Override
    public boolean supportBroadcast() {
        return true;
    }

    /**
     * 创建一个 SpringDataBroadcastManager 实例作为广播管理器。
     * 广播管理器用于在缓存数据变动时通知其他监听器。
     *
     * @param cacheManager 缓存管理器实例，用于管理缓存
     * @return 新创建的 SpringDataBroadcastManager 实例
     */
    @Override
    public BroadcastManager createBroadcastManager(CacheManager cacheManager) {
        RedisSpringDataCacheConfig c = (RedisSpringDataCacheConfig) getConfig().clone();
        return new SpringDataBroadcastManager(cacheManager, c);
    }

    /**
     * 获取当前的 RedisSpringDataCacheConfig 配置实例。
     * 如果配置实例未初始化，则新建一个默认的 RedisSpringDataCacheConfig。
     *
     * @return 当前的 RedisSpringDataCacheConfig 实例
     */
    @Override
    public RedisSpringDataCacheConfig getConfig() {
        if (config == null) {
            config = new RedisSpringDataCacheConfig();
        }
        return (RedisSpringDataCacheConfig) config;
    }

    /**
     * 设置 Redis 连接工厂。
     *
     * @param connectionFactory Redis 连接工厂实例
     * @return 当前的 RedisSpringDataCacheBuilder 实例
     */
    public T connectionFactory(RedisConnectionFactory connectionFactory) {
        getConfig().setConnectionFactory(connectionFactory);
        return self();
    }

    /**
     * 设置 Redis 连接工厂。
     *
     * @param connectionFactory Redis 连接工厂实例
     */
    public void setConnectionFactory(RedisConnectionFactory connectionFactory) {
        getConfig().setConnectionFactory(connectionFactory);
    }

    /**
     * 设置 Redis 消息监听容器。
     *
     * @param listenerContainer Redis 消息监听容器实例
     * @return 当前的 RedisSpringDataCacheBuilder 实例
     */
    public T listenerContainer(RedisMessageListenerContainer listenerContainer) {
        getConfig().setListenerContainer(listenerContainer);
        return self();
    }

    /**
     * 设置 Redis 消息监听容器。
     *
     * @param listenerContainer Redis 消息监听容器实例
     */
    public void setListenerContainer(RedisMessageListenerContainer listenerContainer) {
        getConfig().setListenerContainer(listenerContainer);
    }

    public static RedisSpringDataCacheBuilderImpl createBuilder() {
        return new RedisSpringDataCacheBuilderImpl();
    }

    public static class RedisSpringDataCacheBuilderImpl extends RedisSpringDataCacheBuilder<RedisSpringDataCacheBuilderImpl> {
    }
}
