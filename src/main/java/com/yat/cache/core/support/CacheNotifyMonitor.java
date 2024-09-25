package com.yat.cache.core.support;

import com.yat.cache.anno.api.DefaultCacheConstant;
import com.yat.cache.core.AbstractCache;
import com.yat.cache.core.Cache;
import com.yat.cache.core.CacheManager;
import com.yat.cache.core.CacheMonitor;
import com.yat.cache.core.CacheUtil;
import com.yat.cache.core.MultiLevelCache;
import com.yat.cache.core.embedded.AbstractEmbeddedCache;
import com.yat.cache.core.event.CacheEvent;
import com.yat.cache.core.event.CachePutAllEvent;
import com.yat.cache.core.event.CachePutEvent;
import com.yat.cache.core.event.CacheRemoveAllEvent;
import com.yat.cache.core.event.CacheRemoveEvent;

import java.util.function.Function;

/**
 * ClassName CacheNotifyMonitor
 * <p>Description 监听缓存操作事件并广播事件</p>
 *
 * @author Yat
 * Date 2024/8/22 12:57
 * version 1.0
 */
public class CacheNotifyMonitor implements CacheMonitor {

    /**
     * 广播管理器。
     */
    private final BroadcastManager broadcastManager;

    /**
     * 缓存区域名称。
     */
    private final String area;

    /**
     * 缓存名称。
     */
    private final String cacheName;

    /**
     * 广播源标识。
     */
    private final String sourceId;

    /**
     * 构造一个新的 CacheNotifyMonitor 实例，使用默认缓存区域。
     *
     * @param cacheManager 缓存管理器。
     * @param cacheName    缓存名称。
     */
    public CacheNotifyMonitor(CacheManager cacheManager, String cacheName) {
        this(cacheManager, DefaultCacheConstant.DEFAULT_AREA, cacheName);
    }

    /**
     * 构造一个新的 CacheNotifyMonitor 实例。
     *
     * @param cacheManager 缓存管理器。
     * @param area         缓存区域名称。
     * @param cacheName    缓存名称。
     */
    public CacheNotifyMonitor(CacheManager cacheManager, String area, String cacheName) {
        this.broadcastManager = cacheManager.getBroadcastManager(area);
        this.area = area;
        this.cacheName = cacheName;
        if (broadcastManager != null) {
            this.sourceId = broadcastManager.getSourceId();
        } else {
            this.sourceId = null;
        }
    }

    /**
     * 在缓存操作后执行。
     *
     * @param event 缓存事件。
     */
    @Override
    public void afterOperation(CacheEvent event) {
        if (this.broadcastManager == null) {
            return;
        }
        AbstractCache absCache = CacheUtil.getAbstractCache(event.getCache());
        if (absCache.isClosed()) {
            return;
        }
        AbstractEmbeddedCache localCache = getLocalCache(absCache);
        if (localCache == null) {
            return;
        }

        // 根据不同的事件类型创建 CacheMessage 并广播
        if (event instanceof CachePutEvent e) {
            CacheMessage m = new CacheMessage();
            m.setArea(area);
            m.setCacheName(cacheName);
            m.setSourceId(sourceId);
            m.setType(CacheMessage.TYPE_PUT);
            m.setKeys(new Object[]{convertKey(e.getKey(), localCache)});
            broadcastManager.publish(m);
        } else if (event instanceof CacheRemoveEvent e) {
            CacheMessage m = new CacheMessage();
            m.setArea(area);
            m.setCacheName(cacheName);
            m.setSourceId(sourceId);
            m.setType(CacheMessage.TYPE_REMOVE);
            m.setKeys(new Object[]{convertKey(e.getKey(), localCache)});
            broadcastManager.publish(m);
        } else if (event instanceof CachePutAllEvent e) {
            CacheMessage m = new CacheMessage();
            m.setArea(area);
            m.setCacheName(cacheName);
            m.setSourceId(sourceId);
            m.setType(CacheMessage.TYPE_PUT_ALL);
            if (e.getMap() != null) {
                m.setKeys(e.getMap().keySet().stream().map(k -> convertKey(k, localCache)).toArray());
            }
            broadcastManager.publish(m);
        } else if (event instanceof CacheRemoveAllEvent e) {
            CacheMessage m = new CacheMessage();
            m.setArea(area);
            m.setCacheName(cacheName);
            m.setSourceId(sourceId);
            m.setType(CacheMessage.TYPE_REMOVE_ALL);
            if (e.getKeys() != null) {
                m.setKeys(e.getKeys().stream().map(k -> convertKey(k, localCache)).toArray());
            }
            broadcastManager.publish(m);
        }
    }

    /**
     * 获取本地缓存实例。
     *
     * @param absCache 抽象缓存实例。
     * @return 本地缓存实例。
     */
    private AbstractEmbeddedCache getLocalCache(AbstractCache absCache) {
        if (!(absCache instanceof MultiLevelCache)) {
            return null;
        }
        for (Cache c : ((MultiLevelCache) absCache).caches()) {
            if (c instanceof AbstractEmbeddedCache) {
                return (AbstractEmbeddedCache) c;
            }
        }
        return null;
    }

    /**
     * 转换缓存键。
     *
     * @param key        键。
     * @param localCache 本地缓存配置。
     * @return 转换后的键。
     */
    private Object convertKey(Object key, AbstractEmbeddedCache localCache) {
        Function<Object, Object> keyConvertor = localCache.config().getKeyConvertor();
        if (keyConvertor == null) {
            return key;
        } else {
            return keyConvertor.apply(key);
        }
    }
}
