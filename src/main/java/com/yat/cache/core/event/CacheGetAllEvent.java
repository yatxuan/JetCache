package com.yat.cache.core.event;

import com.yat.cache.core.Cache;
import com.yat.cache.core.MultiGetResult;

import java.util.Set;

/**
 * Created on 2017/2/22.
 *
 * @author huangli
 */
public class CacheGetAllEvent extends CacheEvent {
    private final long millis;
    private final Set keys;
    private final MultiGetResult result;

    public CacheGetAllEvent(Cache cache, long millis, Set keys, MultiGetResult result) {
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

    public MultiGetResult getResult() {
        return result;
    }
}
