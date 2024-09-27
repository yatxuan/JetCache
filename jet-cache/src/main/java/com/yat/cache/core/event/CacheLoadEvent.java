package com.yat.cache.core.event;

import com.yat.cache.core.JetCache;
import lombok.Getter;

/**
 * ClassName CacheLoadEvent
 * <p>Description 缓存加载事件:缓存单个条目加载操作完成时触发的事件</p>
 *
 * @author Yat
 * Date 2024/8/22 13:28
 * version 1.0
 */
@Getter
public class CacheLoadEvent extends CacheEvent {

    /**
     * 缓存操作耗时
     */
    private final long millis;
    /**
     * 缓存键
     */
    private final Object key;
    /**
     * 缓存值
     */
    private final Object loadedValue;
    /**
     * 是否成功
     */
    private final boolean success;

    public CacheLoadEvent(JetCache jetCache, long millis, Object key, Object loadedValue, boolean success) {
        super(jetCache);
        this.millis = millis;
        this.key = key;
        this.loadedValue = loadedValue;
        this.success = success;
    }

}
