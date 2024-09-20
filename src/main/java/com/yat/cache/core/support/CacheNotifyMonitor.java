/**
 * Created on 2022-05-04.
 */
package com.yat.cache.core.support;

import com.yat.cache.anno.api.CacheConsts;
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
 * @author huangli
 */
public class CacheNotifyMonitor implements CacheMonitor {
    private final BroadcastManager broadcastManager;
    private final String area;
    private final String cacheName;
    private final String sourceId;

    public CacheNotifyMonitor(CacheManager cacheManager, String cacheName) {
        this(cacheManager, CacheConsts.DEFAULT_AREA, cacheName);
    }

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
        if (event instanceof CachePutEvent) {
            CacheMessage m = new CacheMessage();
            m.setArea(area);
            m.setCacheName(cacheName);
            m.setSourceId(sourceId);
            CachePutEvent e = (CachePutEvent) event;
            m.setType(CacheMessage.TYPE_PUT);
            m.setKeys(new Object[]{convertKey(e.getKey(), localCache)});
            broadcastManager.publish(m);
        } else if (event instanceof CacheRemoveEvent) {
            CacheMessage m = new CacheMessage();
            m.setArea(area);
            m.setCacheName(cacheName);
            m.setSourceId(sourceId);
            CacheRemoveEvent e = (CacheRemoveEvent) event;
            m.setType(CacheMessage.TYPE_REMOVE);
            m.setKeys(new Object[]{convertKey(e.getKey(), localCache)});
            broadcastManager.publish(m);
        } else if (event instanceof CachePutAllEvent) {
            CacheMessage m = new CacheMessage();
            m.setArea(area);
            m.setCacheName(cacheName);
            m.setSourceId(sourceId);
            CachePutAllEvent e = (CachePutAllEvent) event;
            m.setType(CacheMessage.TYPE_PUT_ALL);
            if (e.getMap() != null) {
                m.setKeys(e.getMap().keySet().stream().map(k -> convertKey(k, localCache)).toArray());
            }
            broadcastManager.publish(m);
        } else if (event instanceof CacheRemoveAllEvent) {
            CacheMessage m = new CacheMessage();
            m.setArea(area);
            m.setCacheName(cacheName);
            m.setSourceId(sourceId);
            CacheRemoveAllEvent e = (CacheRemoveAllEvent) event;
            m.setType(CacheMessage.TYPE_REMOVE_ALL);
            if (e.getKeys() != null) {
                m.setKeys(e.getKeys().stream().map(k -> convertKey(k, localCache)).toArray());
            }
            broadcastManager.publish(m);
        }
    }

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

    private Object convertKey(Object key, AbstractEmbeddedCache localCache) {
        Function keyConvertor = localCache.config().getKeyConvertor();
        if (keyConvertor == null) {
            return key;
        } else {
            return keyConvertor.apply(key);
        }
    }
}
