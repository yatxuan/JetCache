package com.yat.utils;

import cn.hutool.core.convert.Convert;
import cn.hutool.extra.spring.SpringUtil;
import com.yat.cache.anno.api.DefaultCacheConstant;
import com.yat.cache.core.JetCache;
import com.yat.cache.core.JetCacheManager;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * ClassName JetCacheUtil
 * Description 缓存工具类
 *
 * @author Yat
 * Date 2024/9/26 15:59
 * version 1.0
 */
@SuppressWarnings({"unused","unchecked"})
public class JetCacheUtil {

    private JetCacheUtil() {
    }

    public static final String cacheName = DefaultCacheConstant.DEFAULT_AREA + ":";

    private static final JetCacheManager cacheManager = SpringUtil.getBean(JetCacheManager.class);

    /**
     * Description: 获取默认缓存
     * <p>
     * Date: 2024/9/26 16:13
     */
    public static JetCache<Object, Object> getCache() {
        return getCache(cacheName);
    }

    /**
     * Description: 获取缓存
     * <p>
     * Date: 2024/9/26 16:13
     *
     * @param cacheName 缓存名称
     */
    public static JetCache<Object, Object> getCache(String cacheName) {
        return cacheManager.getCache(cacheName);
    }

    /**
     * Description: 获取缓存
     * <p>
     * Date: 2024/9/26 16:12
     *
     * @param area      区域名称
     * @param cacheName 缓存名称
     */
    public static JetCache<Object, Object> getCache(String area, String cacheName) {
        return cacheManager.getCache(area, cacheName);
    }

    public static <T> T get(String key, Class<T> returnClazz) {
        return get(cacheName, key, null);
    }

    public static <T> T get(String cacheName, String key, Class<T> returnClazz) {
        JetCache<Object, Object> cache = getCache(cacheName);
        return Optional.ofNullable(cache.get(key))
                .map(wrapper -> Convert.convert(returnClazz, wrapper))
                .orElse(null);
    }

    public static <T> T get(String key) {
        return get(cacheName, key);
    }

    public static <T> T get(String cacheName, String key) {
        JetCache<Object, Object> cache = getCache(cacheName);
        return (T) cache.get(key);
    }

    public static boolean exists(String key) {
        return exists(cacheName, key);
    }

    public static boolean exists(String cacheName, String key) {
        JetCache<Object, Object> cache = getCache(cacheName);
        return cache.get(key) != null;
    }

    public static void delete(String key) {
        delete(cacheName, key);
    }

    public static void delete(String cacheName, String key) {
        JetCache<Object, Object> cache = getCache(cacheName);
        cache.remove(key);
    }

    public static void put(String key, Object value, long expireAfterWrite, TimeUnit timeUnit) {
        put(cacheName, key, value, expireAfterWrite, timeUnit);
    }

    public static void put(String cacheName, String key, Object value, long expireAfterWrite, TimeUnit timeUnit) {
        JetCache<Object, Object> cache = getCache(cacheName);
        cache.put(key, value, expireAfterWrite, timeUnit);
    }

    public static void put(String key, Object value) {
        put(cacheName, key, value);
    }

    public static void put(String cacheName, String key, Object value) {
        JetCache<Object, Object> cache = getCache(cacheName);
        cache.put(key, value);
    }
}
