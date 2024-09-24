package com.yat.cache.anno.api;

import java.time.Duration;

/**
 * 默认缓存常量，定义了通用的缓存配置和标识常量。
 * 这些常量用于在缓存系统中提供统一的默认配置，包括缓存区域、缓存策略、以及异步操作的默认行为。
 *
 * @author Yat
 * Date 2024/8/22 09:28
 * version 1.0
 */
public interface DefaultCacheConstant {

    /**
     * 默认的缓存区域名称。
     */
    String DEFAULT_AREA = "default";
    /**
     * 默认启用方法缓存的状态。
     *
     * @value 默认启用状态 true
     */
    boolean DEFAULT_ENABLED = true;
    /**
     * 写入后的默认过期时间（以秒为单位）。
     * 设置为最大值表示永不过期。
     *
     * @value 默认过期时间 Integer.MAX_VALUE
     */
    int DEFAULT_EXPIRE = Integer.MAX_VALUE;
    /**
     * 默认的缓存类型。
     */
    CacheType DEFAULT_CACHE_TYPE = CacheType.REMOTE;
    /**
     * 本地缓存的默认大小限制。
     *
     * @value 默认大小限制 100
     */
    int DEFAULT_LOCAL_LIMIT = 100;
    /**
     * 默认情况下是否缓存空值。
     *
     * @value 默认不缓存空值 false
     */
    boolean DEFAULT_CACHE_NULL_VALUE = false;
    /**
     * 默认的序列化策略。
     */
    String DEFAULT_SERIAL_POLICY = SerialPolicy.JAVA;
    /**
     * 默认情况下是否启用批量更新。
     *
     * @value 默认不启用批量更新 false
     */
    boolean DEFAULT_MULTI = false;
    /**
     * 默认的异步操作等待时间。
     *
     * @value 默认等待时间 1000 毫秒
     */
    Duration ASYNC_RESULT_TIMEOUT = Duration.ofMillis(1000);
    /**
     * 未定义值的字符串表示。
     *
     * @value 未定义字符串 "$$undefined$$"
     */
    String UNDEFINED_STRING = "$$undefined$$";
    /**
     * 未定义的整型值。
     *
     * @value 未定义整型值 Integer.MIN_VALUE
     */
    int UNDEFINED_INT = Integer.MIN_VALUE;
    /**
     * 未定义的长整型值。
     *
     * @value 未定义长整型值 Long.MIN_VALUE
     */
    long UNDEFINED_LONG = Long.MIN_VALUE;

    /**
     * 判断给定的字符串是否为未定义值。
     *
     * @param value 待检查的字符串
     * @return 如果值为未定义字符串，则返回true
     */
    static boolean isUndefined(String value) {
        return UNDEFINED_STRING.equals(value);
    }

    /**
     * 判断给定的整型值是否为未定义值。
     *
     * @param value 待检查的整型值
     * @return 如果值为未定义整型值，则返回true
     */
    static boolean isUndefined(int value) {
        return UNDEFINED_INT == value;
    }
}
