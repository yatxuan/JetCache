package com.yat.cache.core;

/**
 * ClassName ProxyCache
 * <p>Description 代理缓存接口，用于实现缓存的代理模式</p>
 *
 * @author Yat
 * Date 2024/9/24 19:54
 * version 1.0
 */
public interface ProxyJetCache<K, V> extends JetCache<K, V> {
    /**
     * 获取目标缓存实例中指定类型的实例
     *
     * @param clazz 指定的类型
     * @param <T>   泛型标记
     * @return 返回指定类型的实例
     */
    @Override
    default <T> T unwrap(Class<T> clazz) {
        return getTargetCache().unwrap(clazz);
    }

    /**
     * 获取目标缓存实例
     *
     * @return 返回目标缓存实例
     */
    JetCache<K, V> getTargetCache();

}
