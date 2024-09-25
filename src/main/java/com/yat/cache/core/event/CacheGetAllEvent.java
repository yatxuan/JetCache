package com.yat.cache.core.event;

import com.yat.cache.core.JetCache;
import com.yat.cache.core.MultiGetResult;
import lombok.Getter;

import java.util.Set;

/**
 * ClassName CacheGetAllEvent
 * <p>Description 缓存中批量获取数据的事件细节</p>
 *
 * @author Yat
 * Date 2024/8/22 12:59
 * version 1.0
 */
@Getter
public class CacheGetAllEvent extends CacheEvent {
    /**
     * 事件发生的时间戳（毫秒）。
     */
    private final long millis;

    /**
     * 请求获取的所有键的集合。
     */
    private final Set<?> keys;

    /**
     * 从缓存中获取的结果。
     */
    private final MultiGetResult result;

    /**
     * 构造方法。
     *
     * @param jetCache  触发此事件的缓存实例。
     * @param millis 事件发生的时间戳（毫秒）。
     * @param keys   请求获取的所有键的集合。
     * @param result 从缓存中获取的结果。
     */
    public CacheGetAllEvent(JetCache jetCache, long millis, Set keys, MultiGetResult result) {
        super(jetCache);
        this.millis = millis;
        this.keys = keys;
        this.result = result;
    }

}
