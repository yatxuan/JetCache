package com.yat.cache.core.embedded;

import com.yat.cache.core.CacheResultCode;
import com.yat.cache.core.CacheValueHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ClassName LinkedHashMapCache
 * <p>Description 基于 LinkedHashMap 的缓存实现</p>
 *
 * @author Yat
 * Date 2024/8/22 11:09
 * version 1.0
 */
public class LinkedHashMapJetCache<K, V> extends AbstractEmbeddedJetCache<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(LinkedHashMapJetCache.class);

    public LinkedHashMapJetCache(EmbeddedCacheConfig<K, V> config) {
        super(config);
        addToCleaner();
    }

    protected void addToCleaner() {
        Cleaner.add(this);
    }

    @Override
    protected InnerMap createAreaCache() {
        return new LRUMap(config.getLimit());
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        if (clazz.equals(LinkedHashMap.class)) {
            return (T) innerMap;
        }
        throw new IllegalArgumentException(clazz.getName());
    }

    public void cleanExpiredEntry() {
        ((LRUMap) innerMap).cleanExpiredEntry();
    }

    final class LRUMap extends LinkedHashMap implements InnerMap {

        private final int max;
        private final ReentrantLock lock = new ReentrantLock();

        public LRUMap(int max) {
            super((int) (max * 1.4f), 0.75f, true);
            this.max = max;
//            this.lockObj = lockObj;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > max;
        }

        void cleanExpiredEntry() {
            lock.lock();
            try {
                for (Iterator it = entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry en = (Map.Entry) it.next();
                    Object value = en.getValue();
                    if (value instanceof CacheValueHolder h) {
                        if (System.currentTimeMillis() >= h.getExpireTime()) {
                            it.remove();
                        }
                    } else {
                        // assert false
                        if (value == null) {
                            logger.error("key {} is null", en.getKey());
                        } else {
                            logger.error("value of key {} is not a CacheValueHolder. type={}", en.getKey(),
                                    value.getClass());
                        }
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        @Override
        public Object getValue(Object key) {
            lock.lock();
            try {
                return get(key);
            } finally {
                lock.unlock();
            }
        }

        @Override
        public Map getAllValues(Collection keys) {
            lock.lock();
            Map values = new HashMap<>();
            try {
                for (Object key : keys) {
                    Object v = get(key);
                    if (v != null) {
                        values.put(key, v);
                    }
                }
            } finally {
                lock.unlock();
            }
            return values;
        }

        @Override
        public void putValue(Object key, Object value) {
            lock.lock();
            try {
                put(key, value);
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void putAllValues(Map map) {
            lock.lock();
            try {
                Set<Map.Entry> set = map.entrySet();
                for (Map.Entry en : set) {
                    put(en.getKey(), en.getValue());
                }
            } finally {
                lock.unlock();
            }
        }

        @Override
        public boolean removeValue(Object key) {
            lock.lock();
            try {
                return remove(key) != null;
            } finally {
                lock.unlock();
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean putIfAbsentValue(Object key, Object value) {
            lock.lock();
            try {
                CacheValueHolder h = (CacheValueHolder) get(key);
                if (h == null || parseHolderResult(h).getResultCode() == CacheResultCode.EXPIRED) {
                    put(key, value);
                    return true;
                } else {
                    return false;
                }
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void removeAllValues(Collection keys) {
            lock.lock();
            try {
                for (Object k : keys) {
                    remove(k);
                }
            } finally {
                lock.unlock();
            }
        }
    }


}

