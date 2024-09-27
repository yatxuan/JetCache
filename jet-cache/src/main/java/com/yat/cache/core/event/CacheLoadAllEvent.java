package com.yat.cache.core.event;

import com.yat.cache.core.JetCache;
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
    private final Set keys;
    /**
     * 缓存值
     */
    private final Map loadedValue;
    /**
     * 是否成功
     */
    private final boolean success;

    public CacheLoadAllEvent(JetCache jetCache, long millis, Set keys, Map loadedValue, boolean success) {
        super(jetCache);
        this.millis = millis;
        this.keys = keys;
        this.loadedValue = loadedValue;
        this.success = success;
    }

}
