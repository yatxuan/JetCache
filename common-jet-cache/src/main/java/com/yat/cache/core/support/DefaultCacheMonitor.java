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
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ClassName DefaultCacheMonitor
 * <p>Description 默认的监控缓存操作的统计信息</p>
 *
 * @author Yat
 * Date 2024/8/22 11:56
 * version 1.0
 */
public class DefaultCacheMonitor implements CacheMonitor {

    private static final Logger logger = LoggerFactory.getLogger(DefaultCacheMonitor.class);

    private final ReentrantLock reentrantLock = new ReentrantLock();
    /**
     * 缓存的名称
     */
    @Getter
    private final String cacheName;
    private long epoch;
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
            Epoch.increment();
            epoch = Epoch.get();
        } finally {
            reentrantLock.unlock();
        }
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
            if (event.getEpoch() < epoch) {
                return;
            }
            if (event instanceof CacheGetEvent e) {
                afterGet(e.getMillis(), e.getKey(), e.getResult());
            } else if (event instanceof CachePutEvent e) {
                afterPut(e.getMillis(), e.getKey(), e.getValue(), e.getResult());
            } else if (event instanceof CacheRemoveEvent e) {
                afterRemove(e.getMillis(), e.getKey(), e.getResult());
            } else if (event instanceof CacheLoadEvent e) {
                afterLoad(e.getMillis(), e.getKey(), e.getLoadedValue(), e.isSuccess());
            } else if (event instanceof CacheGetAllEvent e) {
                afterGetAll(e.getMillis(), e.getKeys(), e.getResult());
            } else if (event instanceof CacheLoadAllEvent e) {
                afterLoadAll(e.getMillis(), e.getKeys(), e.getLoadedValue(), e.isSuccess());
            } else if (event instanceof CachePutAllEvent e) {
                afterPutAll(e.getMillis(), e.getMap(), e.getResult());
            } else if (event instanceof CacheRemoveAllEvent e) {
                afterRemoveAll(e.getMillis(), e.getKeys(), e.getResult());
            }
        } finally {
            reentrantLock.unlock();
        }
    }

    /**
     * Description: 处理单个获取操作
     * <p>
     * Date: 2024/8/22 12:30
     *
     * @param millis 耗时
     * @param key    键
     * @param result 结果
     */
    private void afterGet(long millis, Object key, CacheGetResult result) {
        cacheStat.minGetTime = Math.min(cacheStat.minGetTime, millis);
        cacheStat.maxGetTime = Math.max(cacheStat.maxGetTime, millis);
        cacheStat.getTimeSum += millis;
        cacheStat.getCount++;
        parseSingleGet(result);
    }

    /**
     * Description: 处理单个放入操作
     * <p>
     * Date: 2024/8/22 12:31
     *
     * @param millis 耗时
     * @param key    键
     * @param value  值
     * @param result 结果
     */
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

    /**
     * Description:  处理单个移除操作
     * <p>
     * Date: 2024/8/22 12:31
     *
     * @param millis 耗时
     * @param key    键
     * @param result 结果
     */
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

    /**
     * Description: 处理单个加载操作
     * <p>
     * Date: 2024/8/22 12:32
     *
     * @param millis      耗时
     * @param key         键
     * @param loadedValue 加载的值
     * @param success     是否成功
     */
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

    /**
     * Description: 处理批量获取操作
     * <p>
     * Date: 2024/8/22 12:33
     *
     * @param millis 耗时
     * @param keys   键
     * @param result 结果
     */
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

    /**
     * Description: 处理批量加载操作
     * <p>
     * Date: 2024/8/22 12:33
     *
     * @param millis      耗时
     * @param keys        键
     * @param loadedValue 加载的值
     * @param success     是否成功
     */
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

    /**
     * Description: 处理批量放入操作
     * <p>
     * Date: 2024/8/22 12:34
     *
     * @param millis 耗时
     * @param map    键
     * @param result 结果
     */
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

    /**
     * Description: 处理批量移除操作
     * <p>
     * Date: 2024/8/22 12:33
     *
     * @param millis 耗时
     * @param keys   键
     * @param result 结果
     */
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

    /**
     * Description: 解析单个获取的结果
     * <p>
     * Date: 2024/8/22 12:30
     *
     * @param result 结果
     */
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
