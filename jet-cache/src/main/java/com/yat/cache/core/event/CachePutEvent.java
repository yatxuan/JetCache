package com.yat.cache.core.event;

import com.yat.cache.core.CacheResult;
import com.yat.cache.core.JetCache;
import lombok.Getter;

/**
 * ClassName CachePutEvent
 * <p>Description 表示向缓存中放入数据的事件</p>
 *
 * @author Yat
 * Date 2024/8/22 12:55
 * version 1.0
 */
@Getter
public class CachePutEvent extends CacheEvent {

    /**
     * 事件发生的时间戳（毫秒）。
     */
    private final long millis;

    /**
     * 放入缓存的键。
     */
    private final Object key;

    /**
     * 放入缓存的值。
     */
    private final Object value;

    /**
     * 放入操作的结果。
     */
    private final CacheResult result;

    /**
     * 构造一个新的 CachePutEvent 实例。
     *
     * @param jetCache 发生事件的缓存实例。
     * @param millis   事件发生的时间戳（毫秒）。
     * @param key      放入缓存的键。
     * @param value    放入缓存的值。
     * @param result   放入操作的结果。
     */
    public CachePutEvent(JetCache jetCache, long millis, Object key, Object value, CacheResult result) {
        super(jetCache);
        this.millis = millis;
        this.key = key;
        this.value = value;
        this.result = result;
    }

}
