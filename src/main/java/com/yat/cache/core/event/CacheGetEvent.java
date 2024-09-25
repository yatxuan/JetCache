package com.yat.cache.core.event;

import com.yat.cache.core.Cache;
import com.yat.cache.core.CacheGetResult;
import lombok.Getter;

/**
 * ClassName CacheGetEvent
 * <p>Description 表示从缓存中获取数据的事件</p>
 *
 * @author Yat
 * Date 2024/8/22 12:52
 * version 1.0
 */
@Getter
public class CacheGetEvent extends CacheEvent {

    /**
     * 事件发生的时间戳（毫秒）。
     */
    private final long millis;

    /**
     * 请求的缓存键。
     */
    private final Object key;

    /**
     * 获取操作的结果。
     */
    private final CacheGetResult result;

    /**
     * 构造一个新的 CacheGetEvent 实例。
     *
     * @param cache  发生事件的缓存实例。
     * @param millis 事件发生的时间戳（毫秒）。
     * @param key    请求的缓存键。
     * @param result 获取操作的结果。
     */
    public CacheGetEvent(Cache cache, long millis, Object key, CacheGetResult result) {
        super(cache);
        this.millis = millis;
        this.key = key;
        this.result = result;
    }

}
