package com.yat.cache.core.event;

import com.yat.cache.core.Cache;
import com.yat.cache.core.CacheResult;

/**
 * Created on 2017/2/22.
 *
 * @author huangli
 */
public class CacheRemoveEvent extends CacheEvent {

    private long millis;
    private Object key;
    private CacheResult result;

    public CacheRemoveEvent(Cache cache, long millis, Object key, CacheResult result) {
        super(cache);
        this.millis = millis;
        this.key = key;
        this.result = result;
    }

    public long getMillis() {
        return millis;
    }

    public Object getKey() {
        return key;
    }

    public CacheResult getResult() {
        return result;
    }

}
