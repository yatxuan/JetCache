package com.yat.cache.anno.api;

/**
 * ClassName CacheType
 * <p>Description 缓存类型</p>
 *
 * @author Yat
 * Date 2024/8/22 09:27
 * version 1.0
 */
public enum CacheType {
    /**
     * 远程缓存
     */
    REMOTE,
    /**
     * 本地缓存
     */
    LOCAL,
    /**
     * 同时使用远程和本地缓存
     */
    BOTH
}
