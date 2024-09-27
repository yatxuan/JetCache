package com.yat.cache.core.embedded;

import com.yat.cache.core.AbstractJetCache;
import com.yat.cache.core.CacheConfig;
import com.yat.cache.core.CacheGetResult;
import com.yat.cache.core.CacheResult;
import com.yat.cache.core.CacheResultCode;
import com.yat.cache.core.CacheValueHolder;
import com.yat.cache.core.MultiGetResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ClassName AbstractEmbeddedCache
 * <p>Description
 * AbstractEmbeddedCache 是一个本地缓存抽象类，
 * 缓存实现提供了基础结构和一些基本的操作。
 * 该类继承自 AbstractCache 并实现了一些核心方法，
 * 如获取、放入和删除键值对。
 * </p>
 *
 * @author Yat
 * Date 2024/8/22 10:53
 * version 1.0
 */
public abstract class AbstractEmbeddedJetCache<K, V> extends AbstractJetCache<K, V> {

    private final ReentrantLock lock = new ReentrantLock();
    /**
     * 缓存配置对象，定义了缓存的行为和策略。
     */
    protected EmbeddedCacheConfig<K, V> config;
    /**
     * 内部映射接口，用于存储实际的缓存数据
     */
    protected InnerMap innerMap;

    public AbstractEmbeddedJetCache(EmbeddedCacheConfig<K, V> config) {
        this.config = config;
        innerMap = createAreaCache();
    }

    /**
     * Description: 由子类实现，返回一个 InnerMap 实例，用于存储缓存数据。
     * <p>
     * Date: 2024/8/22 11:18
     *
     * @return {@link InnerMap}
     */
    protected abstract InnerMap createAreaCache();

    /**
     * 返回当前缓存的配置信息
     */
    @Override
    public CacheConfig<K, V> config() {
        return config;
    }

    /**
     * 根据键获取对应的值。如果键不存在或已过期，则返回相应结果。
     */
    @Override
    protected CacheGetResult<V> do_GET(K key) {
        Object newKey = buildKey(key);
        CacheValueHolder<V> holder = (CacheValueHolder<V>) innerMap.getValue(newKey);
        return parseHolderResult(holder);
    }

    // ========================= 获取操作 ===========================

    /**
     * Description: 根据配置中的键转换函数，构建新的键。
     * 如果配置中没有指定转换函数，则直接返回原始键
     * <p>
     * Date: 2024/8/22 11:19
     *
     * @param key 键
     * @return {@link Object}
     */
    public Object buildKey(K key) {
        Object newKey = key;
        Function<K, Object> keyConvertor = config.getKeyConvertor();
        if (keyConvertor != null) {
            newKey = keyConvertor.apply(key);
        }
        return newKey;
    }

    /**
     * Description: 方法解析结果
     * <p>
     * Date: 2024/8/22 11:21
     *
     * @param holder 缓存值持有者
     * @return {@link CacheGetResult<V>}
     */
    protected CacheGetResult<V> parseHolderResult(CacheValueHolder<V> holder) {
        long now = System.currentTimeMillis();
        // 检查 CacheValueHolder 是否为空
        if (Objects.isNull(holder)) {
            return CacheGetResult.notExistsWithoutMsg();
        } else if (now >= holder.getExpireTime()) {
            return CacheGetResult.expiredWithoutMsg();
        } else {
            lock.lock();
            try {
                long accessTime = holder.getAccessTime();
                if (config.isExpireAfterAccess()) {
                    long expireAfterAccess = config.getExpireAfterAccessInMillis();
                    if (now >= accessTime + expireAfterAccess) {
                        return CacheGetResult.expiredWithoutMsg();
                    }
                }
                holder.setAccessTime(now);
            } finally {
                lock.unlock();
            }

            return new CacheGetResult<>(CacheResultCode.SUCCESS, null, holder);
        }
    }

    @Override
    protected MultiGetResult<K, V> do_GET_ALL(Set<? extends K> keys) {
        ArrayList<K> keyList = new ArrayList<>(keys.size());
        ArrayList<Object> newKeyList = new ArrayList<>(keys.size());
        keys.forEach((k) -> {
            Object newKey = buildKey(k);
            keyList.add(k);
            newKeyList.add(newKey);
        });
        Map<Object, CacheValueHolder<V>> innerResultMap = innerMap.getAllValues(newKeyList);
        Map<K, CacheGetResult<V>> resultMap = new HashMap<>();
        for (int i = 0; i < keyList.size(); i++) {
            K key = keyList.get(i);
            Object newKey = newKeyList.get(i);
            CacheValueHolder<V> holder = innerResultMap.get(newKey);
            resultMap.put(key, parseHolderResult(holder));
        }
        return new MultiGetResult<>(CacheResultCode.SUCCESS, null, resultMap);
    }

    @Override
    protected CacheResult do_PUT(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        CacheValueHolder<V> cacheObject = new CacheValueHolder<>(value, timeUnit.toMillis(expireAfterWrite));
        innerMap.putValue(buildKey(key), cacheObject);
        return CacheResult.SUCCESS_WITHOUT_MSG;
    }

    @Override
    protected CacheResult do_PUT_ALL(Map<? extends K, ? extends V> map, long expireAfterWrite, TimeUnit timeUnit) {
        HashMap<Object, CacheValueHolder<V>> newKeyMap = new HashMap<>();
        for (Map.Entry<? extends K, ? extends V> en : map.entrySet()) {
            CacheValueHolder<V> cacheObject = new CacheValueHolder<>(
                    en.getValue(), timeUnit.toMillis(expireAfterWrite)
            );
            newKeyMap.put(buildKey(en.getKey()), cacheObject);
        }
        innerMap.putAllValues(newKeyMap);
        return CacheResult.SUCCESS_WITHOUT_MSG;
    }

    @Override
    protected CacheResult do_REMOVE(K key) {
        innerMap.removeValue(buildKey(key));
        return CacheResult.SUCCESS_WITHOUT_MSG;
    }

    @Override
    protected CacheResult do_REMOVE_ALL(Set<? extends K> keys) {
        Set<Object> newKeys = keys.stream()
                .map(this::buildKey)
                .collect(Collectors.toSet());
        innerMap.removeAllValues(newKeys);

        return CacheResult.SUCCESS_WITHOUT_MSG;
    }

    @Override
    protected CacheResult do_PUT_IF_ABSENT(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        CacheValueHolder<V> cacheObject = new CacheValueHolder<>(
                value, timeUnit.toMillis(expireAfterWrite)
        );

        if (innerMap.putIfAbsentValue(buildKey(key), cacheObject)) {
            return CacheResult.SUCCESS_WITHOUT_MSG;
        } else {
            return CacheResult.EXISTS_WITHOUT_MSG;
        }
    }

    // internal method
    public void __removeAll(Set<? extends K> keys) {
        innerMap.removeAllValues(keys);
    }
}
