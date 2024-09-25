package com.yat.cache.core.event;

import com.yat.cache.core.Cache;
import com.yat.cache.core.CacheResult;
import lombok.Getter;

/**
 * ClassName CacheRemoveEvent
 * <p>Description 缓存移除事件:缓存移除操作完成时触发的事件</p>
 *
 * @author Yat
 * Date 2024/8/22 13:31
 * version 1.0
 */
@Getter
public class CacheRemoveEvent extends CacheEvent {

    /**
     * 事件发生的时间戳（毫秒）。
     */
    private long millis;

    /**
     * 请求的缓存键。
     */
    private Object key;

    /**
     * 获取操作的结果。
     */
    private CacheResult result;

    /**
     * 构造一个新的 CacheGetEvent 实例。
     *
     * @param cache  发生事件的缓存实例。
     * @param millis 事件发生的时间戳（毫秒）。
     * @param key    请求的缓存键。
     * @param result 获取操作的结果。
     */
    public CacheRemoveEvent(Cache cache, long millis, Object key, CacheResult result) {
        super(cache);
        this.millis = millis;
        this.key = key;
        this.result = result;
    }

}
