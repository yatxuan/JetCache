package com.yat.cache.core;

/**
 * ClassName
 * <p>Description 缓存结果码枚举类</p>
 * <p> 该枚举类定义了一组缓存操作结果码，用于表示缓存操作的各种状态。</p>
 *
 * @author Yat
 * Date 2024/8/22 20:29
 * version 1.0
 */
public enum CacheResultCode {
    /**
     * 缓存操作成功。
     */
    SUCCESS,

    /**
     * 缓存操作部分成功。
     */
    PART_SUCCESS,

    /**
     * 缓存操作失败。
     */
    FAIL,

    /**
     * 缓存项不存在。
     */
    NOT_EXISTS,

    /**
     * 缓存项已存在。
     */
    EXISTS,

    /**
     * 缓存项已过期。
     */
    EXPIRED
}
