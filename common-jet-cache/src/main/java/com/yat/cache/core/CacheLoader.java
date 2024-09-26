package com.yat.cache.core;

import com.yat.cache.core.exception.CacheInvokeException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * ClassName CacheLoader
 * <p>Description CacheLoader是一个函数式接口，扩展了Function接口，用于定义如何加载缓存数据的策略。</p>
 * 它提供了加载单个键值对和批量加载多个键值对的方法，并允许自定义缓存更新的否决逻辑。
 *
 * @author Yat
 * Date 2024/8/22 20:22
 * version 1.0
 */
@FunctionalInterface
public interface CacheLoader<K, V> extends Function<K, V> {

    /**
     * 默认实现，用于批量加载与给定键集合关联的值集合。
     *
     * @param keys 要加载值的键集合
     * @return 包含键值对的映射
     * @throws Throwable 如果加载过程中出现异常
     */
    default Map<K, V> loadAll(Set<K> keys) throws Throwable {
        Map<K, V> map = new HashMap<>();
        for (K k : keys) {
            map.put(k, load(k));
        }
        return map;
    }

    /**
     * 加载缓存值
     * 如果缓存中不存在指定键的映射，则使用指定的加载函数计算该键的值
     *
     * @param key 要加载值的键
     * @return 与给定键关联的值
     * @throws Throwable 如果加载过程中出现异常
     */
    V load(K key) throws Throwable;

    /**
     * 覆盖Function接口的apply方法，用于加载单个键值对。
     * 如果加载过程中出现Throwable异常，会抛出CacheInvokeException。
     *
     * @param key 要加载值的键
     * @return 与给定键关联的值
     */
    @Override
    default V apply(K key) {
        try {
            return load(key);
        } catch (Throwable e) {
            throw new CacheInvokeException(e.getMessage(), e);
        }
    }

    /**
     * 允许自定义否决缓存更新的逻辑。
     * 默认返回false，表示允许更新。
     *
     * @return 如果后置条件不满足，则返回true阻止更新，否则返回false允许更新
     */
    default boolean vetoCacheUpdate() {
        return false;
    }

}
