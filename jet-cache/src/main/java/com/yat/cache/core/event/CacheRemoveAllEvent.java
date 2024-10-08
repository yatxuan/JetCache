package com.yat.cache.core.event;

import com.yat.cache.core.CacheResult;
import com.yat.cache.core.JetCache;
import lombok.Getter;

import java.util.Set;

/**
 * ClassName CacheRemoveAllEvent
 * <p>Description 表示从缓存中移除所有指定键的事件</p>
 *
 * @author Yat
 * Date 2024/8/22 12:47
 * version 1.0
 */
@Getter
public class CacheRemoveAllEvent extends CacheEvent {
    /**
     * 事件发生的时间戳（毫秒）
     */
    private final long millis;
    /**
     * 要从缓存中移除的键集合
     */
    private final Set<?> keys;
    /**
     * 移除操作的结果。
     */
    private final CacheResult result;

    /**
     * 构造一个新的 CacheRemoveAllEvent 实例。
     *
     * @param jetCache 发生事件的缓存实例。
     * @param millis   事件发生的时间戳（毫秒）。
     * @param keys     要从缓存中移除的键集合。
     * @param result   移除操作的结果。
     */
    public CacheRemoveAllEvent(JetCache jetCache, long millis, Set keys, CacheResult result) {
        super(jetCache);
        this.millis = millis;
        this.keys = keys;
        this.result = result;
    }

}
