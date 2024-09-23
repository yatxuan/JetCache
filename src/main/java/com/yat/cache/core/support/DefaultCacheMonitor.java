package com.yat.cache.core.support;

import com.yat.cache.core.CacheGetResult;
import com.yat.cache.core.CacheMonitor;
import com.yat.cache.core.CacheResult;
import com.yat.cache.core.MultiGetResult;
import com.yat.cache.core.event.CacheEvent;
import com.yat.cache.core.event.CacheGetAllEvent;
import com.yat.cache.core.event.CacheGetEvent;
import com.yat.cache.core.event.CacheLoadAllEvent;
import com.yat.cache.core.event.CacheLoadEvent;
import com.yat.cache.core.event.CachePutAllEvent;
import com.yat.cache.core.event.CachePutEvent;
import com.yat.cache.core.event.CacheRemoveAllEvent;
import com.yat.cache.core.event.CacheRemoveEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created on 2016/10/27.
 *
 * @author huangli
 */
public class DefaultCacheMonitor implements CacheMonitor {

    private static final Logger logger = LoggerFactory.getLogger(DefaultCacheMonitor.class);

    private final ReentrantLock reentrantLock = new ReentrantLock();
    private String cacheName;
    protected CacheStat cacheStat;

    public DefaultCacheMonitor(String cacheName) {
        if (cacheName == null) {
            throw new NullPointerException();
        }
        this.cacheName = cacheName;
        resetStat();
    }

    public void resetStat() {
        reentrantLock.lock();
        try {
            cacheStat = new CacheStat();
            cacheStat.setStatStartTime(System.currentTimeMillis());
            cacheStat.setCacheName(cacheName);
        } finally {
            reentrantLock.unlock();
        }
    }

    public String getCacheName() {
        return cacheName;
    }

    public CacheStat getCacheStat() {
        reentrantLock.lock();
        try {
            CacheStat stat = cacheStat.clone();
            stat.setStatEndTime(System.currentTimeMillis());
            return stat;
        } finally {
            reentrantLock.unlock();
        }
    }

    @Override
    public void afterOperation(CacheEvent event) {
        reentrantLock.lock();
        try {
            if (event instanceof CacheGetEvent) {
                CacheGetEvent e = (CacheGetEvent) event;
                afterGet(e.getMillis(), e.getKey(), e.getResult());
            } else if (event instanceof CachePutEvent) {
                CachePutEvent e = (CachePutEvent) event;
                afterPut(e.getMillis(), e.getKey(), e.getValue(), e.getResult());
            } else if (event instanceof CacheRemoveEvent) {
                CacheRemoveEvent e = (CacheRemoveEvent) event;
                afterRemove(e.getMillis(), e.getKey(), e.getResult());
            } else if (event instanceof CacheLoadEvent) {
                CacheLoadEvent e = (CacheLoadEvent) event;
                afterLoad(e.getMillis(), e.getKey(), e.getLoadedValue(), e.isSuccess());
            } else if (event instanceof CacheGetAllEvent) {
                CacheGetAllEvent e = (CacheGetAllEvent) event;
                afterGetAll(e.getMillis(), e.getKeys(), e.getResult());
            } else if (event instanceof CacheLoadAllEvent) {
                CacheLoadAllEvent e = (CacheLoadAllEvent) event;
                afterLoadAll(e.getMillis(), e.getKeys(), e.getLoadedValue(), e.isSuccess());
            } else if (event instanceof CachePutAllEvent) {
                CachePutAllEvent e = (CachePutAllEvent) event;
                afterPutAll(e.getMillis(), e.getMap(), e.getResult());
            } else if (event instanceof CacheRemoveAllEvent) {
                CacheRemoveAllEvent e = (CacheRemoveAllEvent) event;
                afterRemoveAll(e.getMillis(), e.getKeys(), e.getResult());
            }
        } finally {
            reentrantLock.unlock();
        }
    }

    private void afterGet(long millis, Object key, CacheGetResult result) {
        cacheStat.minGetTime = Math.min(cacheStat.minGetTime, millis);
        cacheStat.maxGetTime = Math.max(cacheStat.maxGetTime, millis);
        cacheStat.getTimeSum += millis;
        cacheStat.getCount++;
        parseSingleGet(result);
    }

    private void afterPut(long millis, Object key, Object value, CacheResult result) {
        cacheStat.minPutTime = Math.min(cacheStat.minPutTime, millis);
        cacheStat.maxPutTime = Math.max(cacheStat.maxPutTime, millis);
        cacheStat.putTimeSum += millis;
        cacheStat.putCount++;
        switch (result.getResultCode()) {
            case SUCCESS:
                cacheStat.putSuccessCount++;
                break;
            case FAIL:
            case PART_SUCCESS:
                cacheStat.putFailCount++;
                break;
            case EXISTS:
                break;
            default:
                logger.warn("JetCache PUT return unexpected code: {}", result.getResultCode());
        }
    }

    private void afterRemove(long millis, Object key, CacheResult result) {
        cacheStat.minRemoveTime = Math.min(cacheStat.minRemoveTime, millis);
        cacheStat.maxRemoveTime = Math.max(cacheStat.maxRemoveTime, millis);
        cacheStat.removeTimeSum += millis;
        cacheStat.removeCount++;
        switch (result.getResultCode()) {
            case SUCCESS:
            case NOT_EXISTS:
                cacheStat.removeSuccessCount++;
                break;
            case FAIL:
            case PART_SUCCESS:
                cacheStat.removeFailCount++;
                break;
            default:
                logger.warn("JetCache REMOVE return unexpected code: {}", result.getResultCode());
        }
    }

    private void afterLoad(long millis, Object key, Object loadedValue, boolean success) {
        cacheStat.minLoadTime = Math.min(cacheStat.minLoadTime, millis);
        cacheStat.maxLoadTime = Math.max(cacheStat.maxLoadTime, millis);
        cacheStat.loadTimeSum += millis;
        cacheStat.loadCount++;
        if (success) {
            cacheStat.loadSuccessCount++;
        } else {
            cacheStat.loadFailCount++;
        }
    }

    private void afterGetAll(long millis, Set keys, MultiGetResult result) {
        if (keys == null) {
            return;
        }
        int keyCount = keys.size();
        cacheStat.minGetTime = Math.min(cacheStat.minGetTime, millis);
        cacheStat.maxGetTime = Math.max(cacheStat.maxGetTime, millis);
        cacheStat.getTimeSum += millis;
        cacheStat.getCount += keyCount;
        Map resultValues = result.getValues();
        if (resultValues == null) {
            cacheStat.getFailCount += keyCount;
        } else {
            for (Object singleResult : resultValues.values()) {
                CacheGetResult r = ((CacheGetResult) singleResult);
                parseSingleGet(r);
            }
        }
    }

    private void afterLoadAll(long millis, Set keys, Map loadedValue, boolean success) {
        if (keys == null) {
            return;
        }
        int count = keys.size();
        cacheStat.minLoadTime = Math.min(cacheStat.minLoadTime, millis);
        cacheStat.maxLoadTime = Math.max(cacheStat.maxLoadTime, millis);
        cacheStat.loadTimeSum += millis;
        cacheStat.loadCount += count;
        if (success) {
            cacheStat.loadSuccessCount += count;
        } else {
            cacheStat.loadFailCount += count;
        }
    }

    private void afterPutAll(long millis, Map map, CacheResult result) {
        if (map == null) {
            return;
        }
        int keyCount = map.size();
        cacheStat.minPutTime = Math.min(cacheStat.minPutTime, millis);
        cacheStat.maxPutTime = Math.max(cacheStat.maxPutTime, millis);
        cacheStat.putTimeSum += millis;
        cacheStat.putCount += keyCount;
        if (result.isSuccess()) {
            cacheStat.putSuccessCount += keyCount;
        } else {
            cacheStat.putFailCount += keyCount;
        }
    }

    private void afterRemoveAll(long millis, Set keys, CacheResult result) {
        if (keys == null) {
            return;
        }
        int keyCount = keys.size();
        cacheStat.minRemoveTime = Math.min(cacheStat.minRemoveTime, millis);
        cacheStat.maxRemoveTime = Math.max(cacheStat.maxRemoveTime, millis);
        cacheStat.removeTimeSum += millis;
        cacheStat.removeCount += keyCount;
        if (result.isSuccess()) {
            cacheStat.removeSuccessCount += keyCount;
        } else {
            cacheStat.removeFailCount += keyCount;
        }
    }

    private void parseSingleGet(CacheGetResult<?> result) {
        switch (result.getResultCode()) {
            case SUCCESS:
                cacheStat.getHitCount++;
                break;
            case NOT_EXISTS:
                cacheStat.getMissCount++;
                break;
            case EXPIRED:
                cacheStat.getExpireCount++;
                break;
            case FAIL:
                cacheStat.getFailCount++;
                break;
            default:
                logger.warn("JetCache get return unexpected code: {}", result.getResultCode());
        }
    }

}
