package com.yat.cache.core.event;

import com.yat.cache.core.Cache;
import lombok.Getter;

import java.util.Map;
import java.util.Set;

/**
 * ClassName CacheLoadAllEvent
 * <p>Description 缓存批量加载事件:缓存批量加载操作完成时触发的事件</p>
 *
 * @author Yat
 * Date 2024/8/22 13:27
 * version 1.0
 */
@Getter
public class CacheLoadAllEvent extends CacheEvent {

    /**
     * 缓存操作耗时
     */
    private final long millis;
    /**
     * 缓存键集合
     */
    private Set keys;
    /**
     * 缓存值
     */
    private Map loadedValue;
    /**
     * 是否成功
     */
    private boolean success;

    public CacheLoadAllEvent(Cache cache, long millis, Set keys, Map loadedValue, boolean success) {
        super(cache);
        this.millis = millis;
        this.keys = keys;
        this.loadedValue = loadedValue;
        this.success = success;
    }

}
