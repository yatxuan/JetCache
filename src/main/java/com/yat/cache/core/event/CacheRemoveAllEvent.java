package com.yat.cache.core.event;

import com.yat.cache.core.Cache;
import com.yat.cache.core.CacheResult;

import java.util.Set;

/**
 * Created on 2017/2/22.
 *
 * @author huangli
 */
public class CacheRemoveAllEvent extends CacheEvent {
    private final long millis;
    private final Set keys;
    private final CacheResult result;

    public CacheRemoveAllEvent(Cache cache, long millis, Set keys, CacheResult result) {
        super(cache);
        this.millis = millis;
        this.keys = keys;
        this.result = result;
    }

    public long getMillis() {
        return millis;
    }

    public Set getKeys() {
        return keys;
    }

    public CacheResult getResult() {
        return result;
    }
}
