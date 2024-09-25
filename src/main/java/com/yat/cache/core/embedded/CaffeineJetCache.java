package com.yat.cache.core.embedded;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.yat.cache.core.CacheValueHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * ClassName CaffeineJetCache
 * <p>Description Caffeine 缓存实现类，继承自 AbstractEmbeddedCache 并使用 Caffeine 作为底层缓存存储。</p>
 *
 * @author Yat
 * Date 2024/8/22 10:35
 * version 1.0
 */
public class CaffeineJetCache<K, V> extends AbstractEmbeddedJetCache<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(CaffeineJetCache.class);

    private com.github.benmanes.caffeine.cache.Cache cache;

    public CaffeineJetCache(EmbeddedCacheConfig<K, V> config) {
        super(config);
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        if (clazz.equals(com.github.benmanes.caffeine.cache.Cache.class)) {
            return (T) cache;
        }
        throw new IllegalArgumentException(clazz.getName());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected InnerMap createAreaCache() {

        final boolean isExpireAfterAccess = config.isExpireAfterAccess();
        final long expireAfterAccess = config.getExpireAfterAccessInMillis();

        Caffeine<Object, Object> builder = Caffeine.newBuilder()
                // 软引用
                .softValues()
                // 缓存的最大条数
                .maximumSize(config.getLimit())
                // 移除缓存事件
                .removalListener(
                        (key, value, cause) ->
                                logger.info("缓存key【{}】，被移出缓存；{}", key, cause)
                );
        builder.expireAfter(new Expiry<Object, CacheValueHolder>() {
            @Override
            public long expireAfterCreate(Object key, CacheValueHolder value, long currentTime) {
                return getRestTimeInNanos(value);
            }

            /**
             * 计算给定缓存值持有者的剩余时间（以纳秒为单位）
             * 此方法用于确定缓存项何时到期它通过减去当前时间与缓存项的到期时间来计算剩余时间
             * 如果启用了访问后到期功能，它还会考虑最小到期时间
             *
             * @param value 缓存值持有者对象，包含到期时间和其他元数据
             * @return 返回剩余时间，以纳秒为单位
             */
            private long getRestTimeInNanos(CacheValueHolder value) {
                long now = System.currentTimeMillis();
                long ttl = value.getExpireTime() - now;
                if (isExpireAfterAccess) {
                    ttl = Math.min(ttl, expireAfterAccess);
                }
                return TimeUnit.MILLISECONDS.toNanos(ttl);
            }

            @Override
            public long expireAfterUpdate(Object key, CacheValueHolder value,
                                          long currentTime, long currentDuration) {
                return currentDuration;
            }

            @Override
            public long expireAfterRead(Object key, CacheValueHolder value,
                                        long currentTime, long currentDuration) {
                return getRestTimeInNanos(value);
            }
        });

        cache = builder.build();
        return new InnerMap() {
            @Override
            public Object getValue(Object key) {
                return cache.getIfPresent(key);
            }

            @Override
            public Map getAllValues(Collection keys) {
                return cache.getAllPresent(keys);
            }

            @Override
            public void putValue(Object key, Object value) {
                cache.put(key, value);
            }

            @Override
            public void putAllValues(Map map) {
                cache.putAll(map);
            }

            @Override
            public boolean removeValue(Object key) {
                return cache.asMap().remove(key) != null;
            }

            @Override
            public boolean putIfAbsentValue(Object key, Object value) {
                return cache.asMap().putIfAbsent(key, value) == null;
            }

            @Override
            public void removeAllValues(Collection keys) {
                cache.invalidateAll(keys);
            }
        };
    }
}
