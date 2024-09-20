package com.yat.cache.core;

/**
 * Created on 2016/12/13.
 *
 * @author huangli
 */
public interface ProxyCache<K, V> extends Cache<K, V> {
    @Override
    default <T> T unwrap(Class<T> clazz) {
        return getTargetCache().unwrap(clazz);
    }

    Cache<K, V> getTargetCache();

}
