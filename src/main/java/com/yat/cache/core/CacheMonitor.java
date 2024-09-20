package com.yat.cache.core;

import com.yat.cache.core.event.CacheEvent;

/**
 * Created on 2016/10/25.
 *
 * @author huangli
 */
@FunctionalInterface
public interface CacheMonitor {

    void afterOperation(CacheEvent event);

}
