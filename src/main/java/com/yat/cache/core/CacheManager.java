/**
 * Created on 2019/2/1.
 */
package com.yat.cache.core;

import com.yat.cache.anno.api.CacheConsts;
import com.yat.cache.core.support.BroadcastManager;
import com.yat.cache.core.template.QuickConfig;

/**
 * @author huangli
 */
public interface CacheManager {
    BroadcastManager getBroadcastManager(String area);

    default <K, V> Cache<K, V> getCache(String cacheName) {
        return getCache(CacheConsts.DEFAULT_AREA, cacheName);
    }

    <K, V> Cache<K, V> getCache(String area, String cacheName);

    default void putCache(String cacheName, Cache cache) {
        putCache(CacheConsts.DEFAULT_AREA, cacheName, cache);
    }

    void putCache(String area, String cacheName, Cache cache);

    /**
     * create or get Cache instance.
     *
     * @see QuickConfig#newBuilder(String)
     */
    <K, V> Cache<K, V> getOrCreateCache(QuickConfig config);

    default void putBroadcastManager(BroadcastManager broadcastManager) {
        putBroadcastManager(CacheConsts.DEFAULT_AREA, broadcastManager);
    }

    void putBroadcastManager(String area, BroadcastManager broadcastManager);

}
