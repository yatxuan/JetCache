package com.yat.cache.core.event;

import com.yat.cache.core.Cache;
import com.yat.cache.core.CacheGetResult;

/**
 * Created on 2017/2/22.
 *
 * @author huangli
 */
public class CacheGetEvent extends CacheEvent {

    private long millis;
    private Object key;
    private CacheGetResult result;

    public CacheGetEvent(Cache cache, long millis, Object key, CacheGetResult result) {
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

    public CacheGetResult getResult() {
        return result;
    }

}
