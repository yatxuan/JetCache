package com.yat.cache.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Created on 2017/5/27.
 *
 * @author huangli
 */
@FunctionalInterface
public interface CacheLoader<K, V> extends Function<K, V> {
    default Map<K, V> loadAll(Set<K> keys) throws Throwable {
        Map<K, V> map = new HashMap<>();
        for (K k : keys) {
            map.put(k, load(k));
        }
        return map;
    }

    V load(K key) throws Throwable;

    @Override
    default V apply(K key) {
        try {
            return load(key);
        } catch (Throwable e) {
            throw new CacheInvokeException(e.getMessage(), e);
        }
    }

    default boolean vetoCacheUpdate() {
        return false;
    }

}
