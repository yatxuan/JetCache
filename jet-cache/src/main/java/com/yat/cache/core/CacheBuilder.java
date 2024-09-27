package com.yat.cache.core;

/**
 * ClassName CacheBuilder
 * <p>Description 缓存生成器</p>
 *
 * @author Yat
 * Date 2024/8/22 11:40
 * version 1.0
 */
public interface CacheBuilder {

    <K, V> JetCache<K, V> buildCache();
}
