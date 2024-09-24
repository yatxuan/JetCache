package com.yat.cache.anno.support;

import com.yat.cache.anno.api.DefaultCacheConstant;
import com.yat.cache.anno.method.CacheInvokeConfig;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理缓存相关配置的映射容器
 * 存储方法的缓存操作配置以及缓存名称配置，以支持快速检索
 *
 * @author Yat
 * Date 2024/8/22 22:03
 * version 1.0
 */
public class ConfigMap {
    /**
     * 存储方法信息与其缓存操作配置的映射
     */
    private final ConcurrentHashMap<String, CacheInvokeConfig> methodInfoMap = new ConcurrentHashMap<>();
    /**
     * 存储缓存名称与其配置的映射
     */
    private final ConcurrentHashMap<String, CachedAnnoConfig> cacheNameMap = new ConcurrentHashMap<>();

    /**
     * 根据方法信息存储缓存操作配置
     * 如果缓存操作配置中的缓存名称有效，也在缓存名称映射中存储相应的配置
     *
     * @param key    方法信息的唯一标识
     * @param config 方法的缓存操作配置
     */
    public void putByMethodInfo(String key, CacheInvokeConfig config) {
        methodInfoMap.put(key, config);
        CachedAnnoConfig cac = config.getCachedAnnoConfig();
        // 检查缓存操作配置中的缓存名称是否有效
        if (cac != null && !DefaultCacheConstant.isUndefined(cac.getName())) {
            cacheNameMap.put(cac.getArea() + "_" + cac.getName(), cac);
        }
    }

    /**
     * 根据方法信息获取缓存操作配置
     *
     * @param key 方法信息的唯一标识
     * @return 对应的缓存操作配置，如果不存在则返回null
     */
    public CacheInvokeConfig getByMethodInfo(String key) {
        return methodInfoMap.get(key);
    }

    /**
     * 根据缓存区域和名称获取缓存配置
     *
     * @param area      缓存区域
     * @param cacheName 缓存名称
     * @return 对应的缓存配置，如果不存在则返回null
     */
    public CachedAnnoConfig getByCacheName(String area, String cacheName) {
        return cacheNameMap.get(area + "_" + cacheName);
    }
}
