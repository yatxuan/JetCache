package com.yat.cache.core;

import com.yat.cache.core.event.CacheEvent;


/**
 * ClassName CacheMonitor
 * <p>Description 函数式的接口:用于监控缓存操作</p>
 *
 * @author Yat
 * Date 2024/8/22 11:14
 * version 1.0
 */
@FunctionalInterface
public interface CacheMonitor {
    /**
     * Description: 在缓存操作完成后调用此方法
     * <p>
     * Date: 2024/8/22 11:15
     *
     * @param event 事件
     */
    void afterOperation(CacheEvent event);

}
