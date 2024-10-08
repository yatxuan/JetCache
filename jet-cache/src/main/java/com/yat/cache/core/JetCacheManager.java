package com.yat.cache.core;

import com.yat.cache.anno.api.DefaultCacheConstant;
import com.yat.cache.core.support.BroadcastManager;
import com.yat.cache.core.template.QuickConfig;

/**
 * ClassName JetCacheManager
 * <p>Description 定义了缓存管理和广播管理的基本操作</p>
 *
 * @author Yat
 * Date 2024/8/22 11:50
 * <p>
 * version 1.0
 */
public interface JetCacheManager {

    /**
     * 根据指定的区域获取广播管理器。
     *
     * @param area 广播管理器所在的区域
     * @return 广播管理器实例
     */
    BroadcastManager getBroadcastManager(String area);

    /**
     * 根据缓存名称获取缓存实例，默认区域为 DEFAULT_AREA。
     *
     * @param <K>       缓存键的类型
     * @param <V>       缓存值的类型
     * @param cacheName 缓存的名称
     * @return 缓存实例
     */
    default <K, V> JetCache<K, V> getCache(String cacheName) {
        return getCache(DefaultCacheConstant.DEFAULT_AREA, cacheName);
    }

    /**
     * 根据指定的区域和缓存名称获取缓存实例。
     *
     * @param <K>       缓存键的类型
     * @param <V>       缓存值的类型
     * @param area      缓存所在的区域
     * @param cacheName 缓存的名称
     * @return 缓存实例
     */
    <K, V> JetCache<K, V> getCache(String area, String cacheName);

    /**
     * 放置一个缓存实例，默认区域为 DEFAULT_AREA。
     *
     * @param cacheName 缓存的名称
     * @param jetCache  缓存实例
     */
    default void putCache(String cacheName, JetCache jetCache) {
        putCache(DefaultCacheConstant.DEFAULT_AREA, cacheName, jetCache);
    }

    /**
     * 在指定的区域内放置一个缓存实例。
     *
     * @param area      缓存所在的区域
     * @param cacheName 缓存的名称
     * @param jetCache  缓存实例
     */
    void putCache(String area, String cacheName, JetCache jetCache);

    /**
     * 获取或创建缓存实例。
     *
     * @param <K>    缓存键的类型
     * @param <V>    缓存值的类型
     * @param config 快速配置对象
     * @return 缓存实例
     * @see QuickConfig#newBuilder(String)
     */
    <K, V> JetCache<K, V> getOrCreateCache(QuickConfig config);

    /**
     * 放置一个广播管理器，默认区域为 DEFAULT_AREA。
     *
     * @param broadcastManager 广播管理器实例
     */
    default void putBroadcastManager(BroadcastManager broadcastManager) {
        putBroadcastManager(DefaultCacheConstant.DEFAULT_AREA, broadcastManager);
    }

    /**
     * 在指定的区域内放置一个广播管理器。
     *
     * @param area             广播管理器所在的区域
     * @param broadcastManager 广播管理器实例
     */
    void putBroadcastManager(String area, BroadcastManager broadcastManager);

}
