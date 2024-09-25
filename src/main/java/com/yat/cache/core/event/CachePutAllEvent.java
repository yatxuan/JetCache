package com.yat.cache.core.event;

import com.yat.cache.core.Cache;
import com.yat.cache.core.CacheResult;
import lombok.Getter;

import java.util.Map;

/**
 * ClassName CachePutAllEvent
 * <p>Description 缓存批量插入事件:缓存批量插入操作完成时触发的事件</p>
 *
 * @author Yat
 * Date 2024/8/22 13:29
 * version 1.0
 */
@Getter
public class CachePutAllEvent extends CacheEvent {
    /**
     * 插入操作耗时
     */
    private final long millis;
    /**
     * 键值对映射.
     */
    private final Map map;
    /**
     * 插入操作结果
     */
    private final CacheResult result;

    public CachePutAllEvent(Cache cache, long millis, Map map, CacheResult result) {
        super(cache);
        this.millis = millis;
        this.map = map;
        this.result = result;
    }

}
