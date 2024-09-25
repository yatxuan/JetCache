package com.yat.cache.core;

import com.yat.cache.core.event.CacheEvent;
import com.yat.cache.core.event.CacheLoadAllEvent;
import com.yat.cache.core.event.CacheLoadEvent;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * CacheUtil工具类提供了一系列方法来操作缓存，包括创建缓存加载器代理、解析抽象缓存等。
 * 它旨在增强缓存操作的灵活性和可扩展性，同时确保事件的记录。
 *
 * @author Yat
 * @version 1.0
 */
public class CacheUtil {

    /**
     * 创建一个ProxyLoader用于加载缓存数据。该方法适用于传入的加载器已经是ProxyLoader的情况，
     * 或者需要创建一个新的ProxyLoader实例。
     *
     * @param cache         缓存实例，用于存储和加载数据。
     * @param loader        缓存加载器，用于加载数据。
     * @param eventConsumer 事件消费者，用于处理缓存加载事件。
     * @param <K>           缓存键的类型。
     * @param <V>           缓存值的类型。
     * @return 返回一个ProxyLoader实例。
     */
    public static <K, V> ProxyLoader<K, V> createProxyLoader(
            Cache<K, V> cache, Function<K, V> loader, Consumer<CacheEvent> eventConsumer
    ) {
        if (loader instanceof ProxyLoader) {
            return (ProxyLoader<K, V>) loader;
        }
        if (loader instanceof CacheLoader) {
            return createProxyLoader(cache, (CacheLoader) loader, eventConsumer);
        }
        return k -> {
            long t = System.currentTimeMillis();
            V v = null;
            boolean success = false;
            try {
                v = loader.apply(k);
                success = true;
            } finally {
                t = System.currentTimeMillis() - t;
                CacheLoadEvent event = new CacheLoadEvent(cache, t, k, v, success);
                eventConsumer.accept(event);
            }
            return v;
        };
    }

    /**
     * 创建一个ProxyLoader用于加载缓存数据。该方法适用于传入的加载器是一个Function的情况，
     * 或者需要根据Function创建一个新的ProxyLoader实例。
     *
     * @param cache         缓存实例，用于存储和加载数据。
     * @param loader        用于加载数据的函数。
     * @param eventConsumer 事件消费者，用于处理缓存加载事件。
     * @param <K>           缓存键的类型。
     * @param <V>           缓存值的类型。
     * @return 返回一个ProxyLoader实例。
     */
    public static <K, V> ProxyLoader<K, V> createProxyLoader(
            Cache<K, V> cache, CacheLoader<K, V> loader, Consumer<CacheEvent> eventConsumer
    ) {
        if (loader instanceof ProxyLoader) {
            return (ProxyLoader<K, V>) loader;
        }
        return new ProxyLoader<K, V>() {
            @Override
            public Map<K, V> loadAll(Set<K> keys) throws Throwable {
                long t = System.currentTimeMillis();
                boolean success = false;
                Map<K, V> kvMap = null;
                try {
                    kvMap = loader.loadAll(keys);
                    success = true;
                } finally {
                    t = System.currentTimeMillis() - t;
                    CacheLoadAllEvent event = new CacheLoadAllEvent(cache, t, keys, kvMap, success);
                    eventConsumer.accept(event);
                }
                return kvMap;
            }

            /**
             * 加载单个键对应的值，并记录加载事件。
             *
             * @param key 要加载的键。
             * @return 加载的值。
             * @throws Throwable 如果加载过程中发生错误。
             */
            @Override
            public V load(K key) throws Throwable {
                long t = System.currentTimeMillis();
                V v = null;
                boolean success = false;
                try {
                    v = loader.load(key);
                    success = true;
                } finally {
                    t = System.currentTimeMillis() - t;
                    CacheLoadEvent event = new CacheLoadEvent(cache, t, key, v, success);
                    eventConsumer.accept(event);
                }
                return v;
            }

            @Override
            public boolean vetoCacheUpdate() {
                return loader.vetoCacheUpdate();
            }
        };
    }

    /**
     * 获取缓存的抽象实现。该方法用于穿透代理缓存，获取最终的抽象缓存实现。
     *
     * @param c   缓存实例，可能是代理缓存或抽象缓存。
     * @param <K> 缓存键的类型。
     * @param <V> 缓存值的类型。
     * @return 返回抽象缓存实例。
     */
    public static <K, V> AbstractCache<K, V> getAbstractCache(Cache<K, V> c) {
        while (c instanceof ProxyCache) {
            c = ((ProxyCache) c).getTargetCache();
        }
        return (AbstractCache) c;
    }

    /**
     * ProxyLoader接口定义了缓存加载器的代理，用于扩展缓存加载行为并记录事件。
     *
     * @param <K> 缓存键的类型。
     * @param <V> 缓存值的类型。
     */
    private interface ProxyLoader<K, V> extends CacheLoader<K, V> {
    }

}
