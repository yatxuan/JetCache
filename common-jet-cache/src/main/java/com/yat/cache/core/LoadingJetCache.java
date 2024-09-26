package com.yat.cache.core;

import com.yat.cache.core.event.CacheEvent;
import com.yat.cache.core.exception.CacheInvokeException;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * ClassName LoadingCache
 * <p>Description LoadingCache类是SimpleProxyCache的扩展，它提供了一种加载缓存的机制</p>
 * 通过使用CacheLoader，它可以在缓存未命中时动态加载数据。
 *
 * @author Yat
 * Date 2024/8/22 20:31
 * version 1.0
 */
public class LoadingJetCache<K, V> extends SimpleProxyJetCache<K, V> {

    /**
     * 用于处理缓存事件的消费者
     */
    protected Consumer<CacheEvent> eventConsumer;

    /**
     * 缓存配置
     */
    protected CacheConfig<K, V> config;

    /**
     * 初始化缓存实例。
     *
     * @param jetCache 实际的缓存实例
     */
    public LoadingJetCache(JetCache<K, V> jetCache) {
        super(jetCache);
        this.config = config();
        eventConsumer = CacheUtil.getAbstractCache(jetCache)::notify;
    }

    /**
     * 获取键对应的值，如果缓存中不存在，则使用CacheLoader加载数据。
     *
     * @param key 缓存键
     * @return 缓存值
     * @throws CacheInvokeException 如果加载数据时发生错误
     */
    @Override
    public V get(K key) throws CacheInvokeException {
        CacheLoader<K, V> loader = config.getLoader();
        if (loader != null) {
            return AbstractJetCache.computeIfAbsentImpl(
                    key, loader, config.isCacheNullValue(), 0, null, this
            );
        } else {
            return jetCache.get(key);
        }
    }

    /**
     * 批量获取一组键对应的值，如果缓存中不存在，则使用CacheLoader加载数据。
     *
     * @param keys 缓存键集合
     * @return 包含缓存值的映射
     * @throws CacheInvokeException 如果加载数据时发生错误
     */
    @Override
    public Map<K, V> getAll(Set<? extends K> keys) throws CacheInvokeException {
        CacheLoader<K, V> loader = config.getLoader();
        if (loader != null) {
            MultiGetResult<K, V> r = GET_ALL(keys);
            Map<K, V> kvMap;
            if (r.isSuccess() || r.getResultCode() == CacheResultCode.PART_SUCCESS) {
                kvMap = r.unwrapValues();
            } else {
                kvMap = new HashMap<>();
            }
            Set<K> keysNeedLoad = new LinkedHashSet<>();
            keys.forEach((k) -> {
                if (!kvMap.containsKey(k)) {
                    keysNeedLoad.add(k);
                }
            });
            if (!config.isCachePenetrationProtect()) {
                if (eventConsumer != null) {
                    loader = CacheUtil.createProxyLoader(jetCache, loader, eventConsumer);
                }
                Map<K, V> loadResult;
                try {
                    loadResult = loader.loadAll(keysNeedLoad);

                    CacheLoader<K, V> theLoader = loader;
                    Map<K, V> updateValues = new HashMap<>();
                    loadResult.forEach((k, v) -> {
                        if (needUpdate(v, theLoader)) {
                            updateValues.put(k, v);
                        }
                    });
                    // batch put
                    if (!updateValues.isEmpty()) {
                        PUT_ALL(updateValues);
                    }
                } catch (Throwable e) {
                    throw new CacheInvokeException(e);
                }
                kvMap.putAll(loadResult);
            } else {
                AbstractJetCache<K, V> abstractCache = CacheUtil.getAbstractCache(jetCache);
                loader = CacheUtil.createProxyLoader(jetCache, loader, eventConsumer);
                for (K key : keysNeedLoad) {
                    Consumer<V> cacheUpdater = (v) -> {
                        if (needUpdate(v, config.getLoader())) {
                            PUT(key, v);
                        }
                    };
                    V v = AbstractJetCache.synchronizedLoad(config, abstractCache, key, loader, cacheUpdater);
                    kvMap.put(key, v);
                }
            }
            return kvMap;
        } else {
            return jetCache.getAll(keys);
        }
    }

    /**
     * 判断加载的数据是否需要更新到缓存中。
     *
     * @param loadedValue 加载的数据值
     * @param loader      CacheLoader实例
     * @return true 如果需要更新，否则返回false
     */
    protected boolean needUpdate(V loadedValue, CacheLoader<K, V> loader) {
        if (loadedValue == null && !config.isCacheNullValue()) {
            return Boolean.FALSE;
        }
        if (loader.vetoCacheUpdate()) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}
