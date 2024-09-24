package com.yat.cache.core;

import com.yat.cache.core.embedded.AbstractEmbeddedCache;
import com.yat.cache.core.exception.CacheException;
import com.yat.cache.core.exception.CacheInvokeException;
import com.yat.cache.core.external.AbstractExternalCache;
import com.yat.cache.core.support.JetCacheExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * ClassName: RefreshCache
 * <p>
 * Description: 实现了一个可刷新的缓存类，继承自LoadingCache。
 * 提供了缓存项的刷新机制，并支持多级缓存。
 * </p>
 *
 * @author Yat
 * Date: 2024/8/22 20:39
 * version: 1.0
 */
public class RefreshCache<K, V> extends LoadingCache<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(RefreshCache.class);

    /**
     * 常量定义：锁后缀的字节数组形式。
     */
    public static final byte[] LOCK_KEY_SUFFIX = "_#RL#".getBytes();

    /**
     * 常量定义：时间戳后缀的字节数组形式。
     */
    public static final byte[] TIMESTAMP_KEY_SUFFIX = "_#TS#".getBytes();
    /**
     * 用于存储刷新任务的并发哈希映射。
     */
    private final ConcurrentHashMap<Object, RefreshTask> taskMap = new ConcurrentHashMap<>();
    /**
     * 标记是否为多级缓存。
     */
    private final boolean multiLevelCache;

    public RefreshCache(Cache cache) {
        super(cache);
        multiLevelCache = isMultiLevelCache();
    }

    /**
     * 检查当前缓存是否为多级缓存。
     *
     * @return 如果是多级缓存则返回true，否则返回false
     */
    private boolean isMultiLevelCache() {
        Cache c = getTargetCache();
        while (c instanceof ProxyCache) {
            c = ((ProxyCache) c).getTargetCache();
        }
        return c instanceof MultiLevelCache;
    }

    /**
     * 关闭缓存实例。
     */
    @Override
    public void close() {
        stopRefresh();
        super.close();
    }

    /**
     * 重写computeIfAbsent方法，根据指定的键和加载器计算值。
     *
     * @param key    键
     * @param loader 加载器
     * @return 计算得到的值
     */
    @Override
    public V computeIfAbsent(K key, Function<K, V> loader) {
        return computeIfAbsent(key, loader, config().isCacheNullValue());
    }

    /**
     * 重写computeIfAbsent方法，根据指定的键、加载器以及是否缓存null值来计算值。
     *
     * @param key                           键
     * @param loader                        加载器
     * @param cacheNullWhenLoaderReturnNull 是否在加载器返回null时缓存null值
     * @return 计算得到的值
     */
    @Override
    public V computeIfAbsent(K key, Function<K, V> loader, boolean cacheNullWhenLoaderReturnNull) {
        return AbstractCache.computeIfAbsentImpl(key, loader, cacheNullWhenLoaderReturnNull,
                0, null, this);
    }

    /**
     * 重写computeIfAbsent方法，根据指定的键、加载器、是否缓存null值、过期时间及单位来计算值。
     *
     * @param key                           键
     * @param loader                        加载器
     * @param cacheNullWhenLoaderReturnNull 是否在加载器返回null时缓存null值
     * @param expireAfterWrite              过期时间
     * @param timeUnit                      时间单位
     * @return 计算得到的值
     */
    @Override
    public V computeIfAbsent(K key, Function<K, V> loader, boolean cacheNullWhenLoaderReturnNull,
                             long expireAfterWrite, TimeUnit timeUnit) {
        return AbstractCache.computeIfAbsentImpl(key, loader, cacheNullWhenLoaderReturnNull,
                expireAfterWrite, timeUnit, this);
    }

    /**
     * 停止所有刷新任务。
     */
    protected void stopRefresh() {
        List<RefreshTask> tasks = new ArrayList<>();
        tasks.addAll(taskMap.values());
        tasks.forEach(task -> task.cancel());
    }

    @Override
    public V get(K key) throws CacheInvokeException {
        if (config.getRefreshPolicy() != null && hasLoader()) {
            addOrUpdateRefreshTask(key, null);
        }
        return super.get(key);
    }

    /**
     * 检查是否存在加载器。
     *
     * @return 如果配置了加载器则返回true，否则返回false
     */
    private boolean hasLoader() {
        return config.getLoader() != null;
    }

    /**
     * 添加或更新刷新任务。
     *
     * @param key    缓存键
     * @param loader 缓存加载器
     */
    protected void addOrUpdateRefreshTask(K key, CacheLoader<K, V> loader) {
        RefreshPolicy refreshPolicy = config.getRefreshPolicy();
        if (refreshPolicy == null) {
            return;
        }
        long refreshMillis = refreshPolicy.getRefreshMillis();
        if (refreshMillis > 0) {
            Object taskId = getTaskId(key);
            RefreshTask refreshTask = taskMap.computeIfAbsent(taskId, tid -> {
                logger.debug("add refresh task. interval={},  key={}", refreshMillis, key);
                RefreshTask task = new RefreshTask(taskId, key, loader);
                task.lastAccessTime = System.currentTimeMillis();
                task.future = JetCacheExecutor.heavyIOExecutor()
                        .scheduleWithFixedDelay(
                                task, refreshMillis, refreshMillis, TimeUnit.MILLISECONDS
                        );
                return task;
            });
            refreshTask.lastAccessTime = System.currentTimeMillis();
        }
    }

    /**
     * 获取任务ID。
     *
     * @param key 缓存键
     * @return 任务ID
     */
    private Object getTaskId(K key) {
        Cache c = concreteCache();
        if (c instanceof AbstractEmbeddedCache) {
            return ((AbstractEmbeddedCache) c).buildKey(key);
        } else if (c instanceof AbstractExternalCache) {
            byte[] bs = ((AbstractExternalCache) c).buildKey(key);
            return ByteBuffer.wrap(bs);
        } else {
            logger.error("can't getTaskId from " + c.getClass());
            return null;
        }
    }

    /**
     * 获取实际的缓存实例。
     *
     * @return 返回实际的缓存实例
     */
    protected Cache concreteCache() {
        Cache cache = getTargetCache();
        while (true) {
            if (cache instanceof ProxyCache) {
                cache = ((ProxyCache) cache).getTargetCache();
            } else if (cache instanceof MultiLevelCache) {
                Cache[] caches = ((MultiLevelCache) cache).caches();
                cache = caches[caches.length - 1];
            } else {
                return cache;
            }
        }
    }

    @Override
    public Map<K, V> getAll(Set<? extends K> keys) throws CacheInvokeException {
        if (config.getRefreshPolicy() != null && hasLoader()) {
            for (K key : keys) {
                addOrUpdateRefreshTask(key, null);
            }
        }
        return super.getAll(keys);
    }

    /**
     * 合并两个字节数组为一个新的字节数组。
     *
     * @param bs1 第一个字节数组
     * @param bs2 第二个字节数组
     * @return 新的字节数组
     */
    private byte[] combine(byte[] bs1, byte[] bs2) {
        byte[] newArray = Arrays.copyOf(bs1, bs1.length + bs2.length);
        System.arraycopy(bs2, 0, newArray, bs1.length, bs2.length);
        return newArray;
    }

    /**
     * ClassName RefreshTask
     * <p>Description 刷新任务类，实现Runnable接口</p>
     * 该类负责在后台刷新缓存中的数据，以确保数据的时效性。
     *
     * @author Yat
     * Date 2024/8/22 20:44
     * version 1.0
     */
    class RefreshTask implements Runnable {
        private final Object taskId;
        private final K key;
        private final CacheLoader<K, V> loader;

        private long lastAccessTime;
        private ScheduledFuture<?> future;

        RefreshTask(Object taskId, K key, CacheLoader<K, V> loader) {
            this.taskId = taskId;
            this.key = key;
            this.loader = loader;
        }

        @Override
        public void run() {
            try {
                if (config.getRefreshPolicy() == null || (loader == null && !hasLoader())) {
                    cancel();
                    return;
                }
                long now = System.currentTimeMillis();
                long stopRefreshAfterLastAccessMillis = config.getRefreshPolicy().getStopRefreshAfterLastAccessMillis();
                if (stopRefreshAfterLastAccessMillis > 0) {
                    if (lastAccessTime + stopRefreshAfterLastAccessMillis < now) {
                        logger.debug("cancel refresh: {}", key);
                        cancel();
                        return;
                    }
                }
                logger.debug("refresh key: {}", key);
                Cache concreteCache = concreteCache();
                if (concreteCache instanceof AbstractExternalCache) {
                    externalLoad(concreteCache, now);
                } else {
                    load();
                }
            } catch (Throwable e) {
                logger.error("refresh error: key=" + key, e);
            }
        }

        private void cancel() {
            logger.debug("cancel refresh: {}", key);
            future.cancel(false);
            taskMap.remove(taskId);
        }

        private void externalLoad(final Cache concreteCache, final long currentTime)
                throws Throwable {
            byte[] newKey = ((AbstractExternalCache) concreteCache).buildKey(key);
            byte[] lockKey = combine(newKey, LOCK_KEY_SUFFIX);
            long loadTimeOut = RefreshCache.this.config.getRefreshPolicy().getRefreshLockTimeoutMillis();
            long refreshMillis = config.getRefreshPolicy().getRefreshMillis();
            byte[] timestampKey = combine(newKey, TIMESTAMP_KEY_SUFFIX);

            // AbstractExternalCache buildKey method will not convert byte[]
            CacheGetResult refreshTimeResult = concreteCache.GET(timestampKey);
            boolean shouldLoad = false;
            if (refreshTimeResult.isSuccess()) {
                shouldLoad = currentTime >= Long.parseLong(refreshTimeResult.getValue().toString()) + refreshMillis;
            } else if (refreshTimeResult.getResultCode() == CacheResultCode.NOT_EXISTS) {
                shouldLoad = true;
            }

            if (!shouldLoad) {
                if (multiLevelCache) {
                    refreshUpperCaches(key);
                }
                return;
            }

            Runnable r = () -> {
                try {
                    load();
                    // AbstractExternalCache buildKey method will not convert byte[]
                    concreteCache.put(timestampKey, String.valueOf(System.currentTimeMillis()));
                } catch (Throwable e) {
                    throw new CacheException("refresh error", e);
                }
            };

            // AbstractExternalCache buildKey method will not convert byte[]
            boolean lockSuccess = concreteCache.tryLockAndRun(lockKey, loadTimeOut, TimeUnit.MILLISECONDS, r);
            if (!lockSuccess && multiLevelCache) {
                JetCacheExecutor.heavyIOExecutor().schedule(
                        () -> refreshUpperCaches(key), (long) (0.2 * refreshMillis), TimeUnit.MILLISECONDS);
            }
        }

        private void load() throws Throwable {
            CacheLoader<K, V> l = loader == null ? config.getLoader() : loader;
            if (l != null) {
                l = CacheUtil.createProxyLoader(cache, l, eventConsumer);
                V v = l.load(key);
                if (needUpdate(v, l)) {
                    cache.PUT(key, v);
                }
            }
        }

        private void refreshUpperCaches(K key) {
            MultiLevelCache<K, V> targetCache = (MultiLevelCache<K, V>) getTargetCache();
            Cache[] caches = targetCache.caches();
            int len = caches.length;

            CacheGetResult cacheGetResult = caches[len - 1].GET(key);
            if (!cacheGetResult.isSuccess()) {
                return;
            }
            for (int i = 0; i < len - 1; i++) {
                caches[i].PUT(key, cacheGetResult.getValue());
            }
        }
    }
}
