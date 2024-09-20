package com.yat.cache.core;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * ClassName CacheValueHolder
 * <p>Description 缓存值持有者类，用于存储缓存数据及其过期时间。</p>
 * <p>该类不支持线程安全</p>
 *
 * @author Yat
 * Date 2024/9/20 21:05
 * version 1.0
 */
@Setter
@Getter
@NoArgsConstructor
public final class CacheValueHolder<V> implements Serializable {

    @Serial
    private static final long serialVersionUID = -7973743507831565203L;

    /**
     * 缓存的值
     */
    private V value;
    /**
     * 缓存项的过期时间
     */
    private long expireTime;
    /**
     * 最近一次访问缓存项的时间
     */
    private long accessTime;


    /**
     * 用于创建带有到期时间的缓存值持有者。
     *
     * @param value            缓存的值
     * @param expireAfterWrite 写入后到期的时间（毫秒）
     */
    public CacheValueHolder(V value, long expireAfterWrite) {
        this.value = value;
        // 设置当前时间为访问时间
        this.accessTime = System.currentTimeMillis();
        // 计算过期时间
        this.expireTime = accessTime + expireAfterWrite;
    }

}
