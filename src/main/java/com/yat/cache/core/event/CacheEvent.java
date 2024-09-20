/**
 * Created on 2017/2/22.
 */
package com.yat.cache.core.event;

import com.yat.cache.core.Cache;

/**
 * The CacheEvent is used in single JVM while CacheMessage used for distributed message.
 *
 * @author huangli
 */
public class CacheEvent {

    protected Cache cache;

    public CacheEvent(Cache cache) {
        this.cache = cache;
    }

    public Cache getCache() {
        return cache;
    }

}
