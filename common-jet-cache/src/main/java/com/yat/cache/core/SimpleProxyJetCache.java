package com.yat.cache.core;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * SimpleProxyCache 类作为缓存代理，封装了底层缓存实现的细节。
 * 它提供了一组操作缓存的方法，并利用泛型K和V来指定键和值的类型。
 *
 * @param <K> 缓存键的类型
 * @param <V> 缓存值的类型
 */
public class SimpleProxyJetCache<K, V> implements ProxyJetCache<K, V> {

    /**
     * 底层缓存实现
     */
    protected JetCache<K, V> jetCache;

    /**
     * 初始化SimpleProxyCache。
     *
     * @param jetCache 底层缓存实现
     */
    public SimpleProxyJetCache(JetCache<K, V> jetCache) {
        this.jetCache = jetCache;
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        return jetCache.unwrap(clazz);
    }

    @Override
    public JetCache<K, V> getTargetCache() {
        return jetCache;
    }

    /**
     * 根据键获取缓存中的值。
     *
     * @param key 要获取的值对应的键
     * @return 键对应的值
     */
    @Override
    public V get(K key) {
        return jetCache.get(key);
    }

    @Override
    public CacheGetResult<V> GET(K key) {
        return jetCache.GET(key);
    }

    /**
     * 获取多个键对应的值。
     *
     * @param keys 要获取的值对应的键集合
     * @return 包含多键值对的Map
     */
    @Override
    public Map<K, V> getAll(Set<? extends K> keys) {
        return jetCache.getAll(keys);
    }

    @Override
    public MultiGetResult<K, V> GET_ALL(Set<? extends K> keys) {
        return jetCache.GET_ALL(keys);
    }

    /**
     * 向缓存中放入一个键值对。
     *
     * @param key   要放入的键
     * @param value 要放入的值
     */
    @Override
    public void put(K key, V value) {
        jetCache.put(key, value);
    }

    @Override
    public CacheResult PUT(K key, V value) {
        return jetCache.PUT(key, value);
    }

    @Override
    public CacheResult PUT(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        return jetCache.PUT(key, value, expireAfterWrite, timeUnit);
    }

    @Override
    public CacheConfig<K, V> config() {
        return jetCache.config();
    }

    /**
     * 向缓存中放入多个键值对。
     *
     * @param map 包含键值对的Map
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        jetCache.putAll(map);
    }

    @Override
    public CacheResult PUT_ALL(Map<? extends K, ? extends V> map) {
        return jetCache.PUT_ALL(map);
    }

    @Override
    public CacheResult PUT_ALL(Map<? extends K, ? extends V> map, long expireAfterWrite, TimeUnit timeUnit) {
        return jetCache.PUT_ALL(map, expireAfterWrite, timeUnit);
    }

    /**
     * 如果键不存在，则放入一个键值对。
     *
     * @param key   要放入的键
     * @param value 要放入的值
     * @return 如果键已存在，则返回false；否则返回true
     */
    @Override
    public boolean putIfAbsent(K key, V value) {
        return jetCache.putIfAbsent(key, value);
    }

    @Override
    public CacheResult PUT_IF_ABSENT(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        return jetCache.PUT_IF_ABSENT(key, value, expireAfterWrite, timeUnit);
    }

    /**
     * 从缓存中移除一个键。
     *
     * @param key 要移除的键
     * @return 如果键存在并被移除，则返回true；否则返回false
     */
    @Override
    public boolean remove(K key) {
        return jetCache.remove(key);
    }

    @Override
    public CacheResult REMOVE(K key) {
        return jetCache.REMOVE(key);
    }

    @Override
    public void removeAll(Set<? extends K> keys) {
        jetCache.removeAll(keys);
    }

    @Override
    public CacheResult REMOVE_ALL(Set<? extends K> keys) {
        return jetCache.REMOVE_ALL(keys);
    }

    @Override
    public void close() {
        jetCache.close();
    }

    @Override
    public boolean tryLockAndRun(K key, long expire, TimeUnit timeUnit, Runnable action) {
        return jetCache.tryLockAndRun(key, expire, timeUnit, action);
    }

    @Override
    public AutoReleaseLock tryLock(K key, long expire, TimeUnit timeUnit) {
        return jetCache.tryLock(key, expire, timeUnit);
    }

    @Override
    public V computeIfAbsent(K key, Function<K, V> loader) {
        return jetCache.computeIfAbsent(key, loader);
    }

    @Override
    public V computeIfAbsent(K key, Function<K, V> loader, boolean cacheNullWhenLoaderReturnNull) {
        return jetCache.computeIfAbsent(key, loader, cacheNullWhenLoaderReturnNull);
    }

    @Override
    public V computeIfAbsent(K key, Function<K, V> loader, boolean cacheNullWhenLoaderReturnNull,
                             long expireAfterWrite, TimeUnit timeUnit) {
        return jetCache.computeIfAbsent(key, loader, cacheNullWhenLoaderReturnNull, expireAfterWrite, timeUnit);
    }

    @Override
    public void put(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        jetCache.put(key, value, expireAfterWrite, timeUnit);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map, long expireAfterWrite, TimeUnit timeUnit) {
        jetCache.putAll(map, expireAfterWrite, timeUnit);
    }
}
