package com.yat.cache.core;

import com.yat.cache.core.embedded.AbstractEmbeddedCache;
import com.yat.cache.core.event.CacheEvent;
import com.yat.cache.core.event.CacheGetAllEvent;
import com.yat.cache.core.event.CacheGetEvent;
import com.yat.cache.core.event.CachePutAllEvent;
import com.yat.cache.core.event.CachePutEvent;
import com.yat.cache.core.event.CacheRemoveAllEvent;
import com.yat.cache.core.event.CacheRemoveEvent;
import com.yat.cache.core.exception.CacheException;
import com.yat.cache.core.external.AbstractExternalCache;
import com.yat.cache.core.support.SquashedLogger;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * ClassName AbstractCache
 * <p>Description 抽象缓存</p>
 *
 * @author Yat
 * Date 2024/8/22 11:12
 * version 1.0
 */
public abstract class AbstractCache<K, V> implements Cache<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractCache.class);
    private static final ReentrantLock reentrantLock = new ReentrantLock();
    /**
     * 存储加载器锁对象
     */
    private volatile ConcurrentHashMap<Object, LoaderLock> loaderMap;
    @Getter
    protected volatile boolean closed;

    /**
     * 初始化或获取 加载锁映射
     *
     * @return 加载锁映射
     */
    ConcurrentHashMap<Object, LoaderLock> initOrGetLoaderMap() {
        if (loaderMap == null) {
            reentrantLock.lock();
            try {
                if (loaderMap == null) {
                    loaderMap = new ConcurrentHashMap<>();
                }
            } finally {
                reentrantLock.unlock();
            }
        }
        return loaderMap;
    }

    /**
     * 记录错误日志
     *
     * @param str 操作
     * @param key 键
     * @param e   异常
     */
    protected void logError(String str, Object key, Throwable e) {
        StringBuilder sb = new StringBuilder(256);
        sb.append("jetCache(")
                .append(this.getClass().getSimpleName()).append(") ")
                .append(str)
                .append(" error.");
        if (!(key instanceof byte[])) {
            try {
                sb.append(" key=[")
                        .append(config().getKeyConvertor().apply((K) key))
                        .append(']');
            } catch (Exception ex) {
                // ignore
            }
        }
        SquashedLogger.getLogger(logger).error(sb, e);
    }

    /**
     * 具体获取缓存值的实现
     *
     * @param key 键
     * @return 缓存获取结果
     */
    protected abstract CacheGetResult<V> do_GET(K key);

    @Override
    public final CacheGetResult<V> GET(K key) {
        long t = System.currentTimeMillis();
        CacheGetResult<V> result;
        if (key == null) {
            result = new CacheGetResult<>(CacheResultCode.FAIL, CacheResult.MSG_ILLEGAL_ARGUMENT, null);
        } else {
            result = do_GET(key);
        }
        result.future().thenRun(() -> {
            CacheGetEvent event = new CacheGetEvent(this, System.currentTimeMillis() - t, key, result);
            notify(event);
        });
        return result;
    }

    @Override
    public final MultiGetResult<K, V> GET_ALL(Set<? extends K> keys) {
        long t = System.currentTimeMillis();
        MultiGetResult<K, V> result;
        if (keys == null) {
            result = new MultiGetResult<>(CacheResultCode.FAIL, CacheResult.MSG_ILLEGAL_ARGUMENT, null);
        } else {
            result = do_GET_ALL(keys);
        }
        result.future().thenRun(() -> {
            CacheGetAllEvent event = new CacheGetAllEvent(this, System.currentTimeMillis() - t, keys, result);
            notify(event);
        });
        return result;
    }

    /**
     * 具体获取所有缓存值的实现
     *
     * @param keys 键集合
     * @return 多键获取结果
     */
    protected abstract MultiGetResult<K, V> do_GET_ALL(Set<? extends K> keys);

    /**
     * 通知缓存事件
     *
     * @param e 缓存事件
     */
    public void notify(CacheEvent e) {
        List<CacheMonitor> monitors = config().getMonitors();
        for (CacheMonitor m : monitors) {
            m.afterOperation(e);
        }
    }

    @Override
    public final CacheResult PUT(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        long t = System.currentTimeMillis();
        CacheResult result;
        if (key == null) {
            result = CacheResult.FAIL_ILLEGAL_ARGUMENT;
        } else {
            result = do_PUT(key, value, expireAfterWrite, timeUnit);
        }
        result.future().thenRun(() -> {
            CachePutEvent event = new CachePutEvent(this, System.currentTimeMillis() - t, key, value, result);
            notify(event);
        });
        return result;
    }

    /**
     * 具体放入缓存值的实现
     *
     * @param key              键
     * @param value            值
     * @param expireAfterWrite 写入后过期时间
     * @param timeUnit         时间单位
     * @return 缓存结果
     */
    protected abstract CacheResult do_PUT(K key, V value, long expireAfterWrite, TimeUnit timeUnit);

    @Override
    public final CacheResult PUT_ALL(Map<? extends K, ? extends V> map, long expireAfterWrite, TimeUnit timeUnit) {
        long t = System.currentTimeMillis();
        CacheResult result;
        if (map == null) {
            result = CacheResult.FAIL_ILLEGAL_ARGUMENT;
        } else {
            result = do_PUT_ALL(map, expireAfterWrite, timeUnit);
        }
        result.future().thenRun(() -> {
            CachePutAllEvent event = new CachePutAllEvent(this, System.currentTimeMillis() - t, map, result);
            notify(event);
        });
        return result;
    }

    /**
     * 具体放入所有缓存值的实现
     *
     * @param map              键值对映射
     * @param expireAfterWrite 写入后过期时间
     * @param timeUnit         时间单位
     * @return 缓存结果
     */
    protected abstract CacheResult do_PUT_ALL(
            Map<? extends K, ? extends V> map, long expireAfterWrite, TimeUnit timeUnit
    );

    @Override
    public final CacheResult PUT_IF_ABSENT(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        long t = System.currentTimeMillis();
        CacheResult result;
        if (key == null) {
            result = CacheResult.FAIL_ILLEGAL_ARGUMENT;
        } else {
            result = do_PUT_IF_ABSENT(key, value, expireAfterWrite, timeUnit);
        }
        result.future().thenRun(() -> {
            CachePutEvent event = new CachePutEvent(this, System.currentTimeMillis() - t, key, value, result);
            notify(event);
        });
        return result;
    }

    @Override
    public final CacheResult REMOVE(K key) {
        long t = System.currentTimeMillis();
        CacheResult result;
        if (key == null) {
            result = CacheResult.FAIL_ILLEGAL_ARGUMENT;
        } else {
            result = do_REMOVE(key);
        }
        result.future().thenRun(() -> {
            CacheRemoveEvent event = new CacheRemoveEvent(this, System.currentTimeMillis() - t, key, result);
            notify(event);
        });
        return result;
    }

    /**
     * 具体移除缓存值的实现
     *
     * @param key 键
     * @return 缓存结果
     */
    protected abstract CacheResult do_REMOVE(K key);

    /**
     * 移除指定键集合中的所有缓存项。
     * <p>
     * 此方法首先记录当前时间，然后检查传入的键集合是否为 null 以确定下一步操作。
     * 如果键为 null，则立即返回 {@link CacheResult#FAIL_ILLEGAL_ARGUMENT} 结果，表示非法参数。
     * 如果键不为 null，则调用 {@link #do_REMOVE_ALL} 方法执行实际的缓存移除操作。
     * 在移除操作完成后，根据操作结果和耗时生成一个 {@link CacheRemoveAllEvent} 事件，并通知事件监听器（如果存在）。
     * </p>
     * <p>
     * 此方法被标记为 {@code final}，防止子类覆盖。
     * </p>
     *
     * @param keys 要移除的缓存项的键集合
     * @return 移除操作的结果，为 {@link CacheResult} 的实例
     */
    @Override
    public final CacheResult REMOVE_ALL(Set<? extends K> keys) {
        // 记录操作开始时间
        long t = System.currentTimeMillis();
        CacheResult result;
        if (keys == null) {
            result = CacheResult.FAIL_ILLEGAL_ARGUMENT;
        } else {
            // 执行实际的移除操作
            result = do_REMOVE_ALL(keys);
        }
        // 在异步任务完成后执行
        result.future().thenRun(() -> {
            // 根据操作结果和耗时生成事件
            CacheRemoveAllEvent event = new CacheRemoveAllEvent(this, System.currentTimeMillis() - t, keys, result);
            // 通知事件
            notify(event);
        });
        return result;
    }

    @Override
    public void close() {
        this.closed = true;
    }

    @Override
    public final V computeIfAbsent(K key, Function<K, V> loader, boolean cacheNullWhenLoaderReturnNull) {
        return computeIfAbsentImpl(
                key, loader, cacheNullWhenLoaderReturnNull, 0, null, this
        );
    }

    @Override
    public final V computeIfAbsent(
            K key, Function<K, V> loader, boolean cacheNullWhenLoaderReturnNull,
            long expireAfterWrite, TimeUnit timeUnit
    ) {
        return computeIfAbsentImpl(
                key, loader, cacheNullWhenLoaderReturnNull, expireAfterWrite, timeUnit, this
        );
    }

    protected abstract CacheResult do_REMOVE_ALL(Set<? extends K> keys);

    protected abstract CacheResult do_PUT_IF_ABSENT(K key, V value, long expireAfterWrite, TimeUnit timeUnit);

    /**
     * 判断是否需要更新缓存
     *
     * @param loadedValue                   加载的值
     * @param cacheNullWhenLoaderReturnNull 当加载器返回null时是否缓存null
     * @param loader                        加载器函数
     * @return 是否需要更新缓存
     */
    private static <K, V> boolean needUpdate(
            V loadedValue, boolean cacheNullWhenLoaderReturnNull, Function<K, V> loader
    ) {
        if (loadedValue == null && !cacheNullWhenLoaderReturnNull) {
            return Boolean.FALSE;
        }
        if (loader instanceof CacheLoader && ((CacheLoader<K, V>) loader).vetoCacheUpdate()) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * 如果缓存中不存在则计算并放入缓存
     *
     * @param key                           键
     * @param loader                        加载器函数
     * @param cacheNullWhenLoaderReturnNull 当加载器返回null时是否缓存null
     * @param expireAfterWrite              写入后过期时间
     * @param timeUnit                      时间单位
     * @param cache                         缓存实例
     * @return 缓存值
     */
    static <K, V> V computeIfAbsentImpl(K key, Function<K, V> loader, boolean cacheNullWhenLoaderReturnNull,
                                        long expireAfterWrite, TimeUnit timeUnit, Cache<K, V> cache) {
        // 将缓存转换为抽象缓存对象，以便统一操作
        AbstractCache<K, V> abstractCache = CacheUtil.getAbstractCache(cache);
        // 创建一个代理加载器，用于在加载数据后通知缓存
        CacheLoader<K, V> newLoader = CacheUtil.createProxyLoader(cache, loader, abstractCache::notify);
        CacheGetResult<V> r;
        // 如果缓存支持刷新，则使用刷新缓存逻辑
        if (cache instanceof RefreshCache<K, V> refreshCache) {
            r = refreshCache.GET(key);
            refreshCache.addOrUpdateRefreshTask(key, newLoader);
        } else {
            r = cache.GET(key);
        }
        // 如果成功获取到值，则直接返回
        if (r.isSuccess()) {
            return r.getValue();
        } else {
            // 定义一个缓存更新器，根据条件更新缓存
            Consumer<V> cacheUpdater = (loadedValue) -> {
                if (needUpdate(loadedValue, cacheNullWhenLoaderReturnNull, newLoader)) {
                    if (timeUnit != null) {
                        cache.PUT(key, loadedValue, expireAfterWrite, timeUnit).waitForResult();
                    } else {
                        cache.PUT(key, loadedValue).waitForResult();
                    }
                }
            };

            V loadedValue;
            // 如果配置了缓存穿透保护，则使用同步加载逻辑
            if (cache.config().isCachePenetrationProtect()) {
                loadedValue = synchronizedLoad(cache.config(), abstractCache, key, newLoader, cacheUpdater);
            } else {
                loadedValue = newLoader.apply(key);
                cacheUpdater.accept(loadedValue);
            }

            return loadedValue;
        }
    }


    /**
     * 同步加载数据
     *
     * @param config        缓存配置
     * @param abstractCache 抽象缓存实例
     * @param key           键
     * @param newLoader     新的 加载器
     * @param cacheUpdater  缓存更新器
     * @return 加载的值
     */
    static <K, V> V synchronizedLoad(
            CacheConfig config, AbstractCache<K, V> abstractCache,
            K key, Function<K, V> newLoader, Consumer<V> cacheUpdater
    ) {
        // 初始化或获取加载器映射表
        ConcurrentHashMap<Object, LoaderLock> loaderMap = abstractCache.initOrGetLoaderMap();
        // 构建加载器锁的键
        Object lockKey = buildLoaderLockKey(abstractCache, key);
        while (true) {
            // 用于标记是否创建了新的 加载器锁
            boolean[] create = new boolean[1];
            // 计算并获取加载器锁，如果不存在则创建新的
            LoaderLock ll = loaderMap.computeIfAbsent(lockKey, (unusedKey) -> {
                create[0] = true;
                LoaderLock loaderLock = new LoaderLock();
                loaderLock.signal = new CountDownLatch(1);
                loaderLock.loaderThread = Thread.currentThread();
                return loaderLock;
            });
            // 如果当前线程创建了加载器锁或者已经是锁的持有者
            if (create[0] || ll.loaderThread == Thread.currentThread()) {
                try {
                    // 尝试从缓存中获取值
                    CacheGetResult<V> getResult = abstractCache.GET(key);
                    if (getResult.isSuccess()) {
                        // 如果获取成功，设置成功标志并返回值
                        ll.success = true;
                        ll.value = getResult.getValue();
                        return getResult.getValue();
                    } else {
                        // 如果获取不成功，使用新加载器加载值，并更新缓存
                        V loadedValue = newLoader.apply(key);
                        ll.success = true;
                        ll.value = loadedValue;
                        cacheUpdater.accept(loadedValue);
                        return loadedValue;
                    }
                } finally {
                    // 如果创建了新的 加载器锁，在完成后计数减一并移除锁
                    if (create[0]) {
                        ll.signal.countDown();
                        loaderMap.remove(lockKey);
                    }
                }
            } else {
                // 如果当前线程不是加载器锁的持有者，则等待其他线程完成加载
                try {
                    Duration timeout = config.getPenetrationProtectTimeout();
                    if (timeout == null) {
                        // 如果没有设置超时时间，则无限等待
                        ll.signal.await();
                    } else {
                        // 如果设置了超时时间，则有限等待
                        boolean ok = ll.signal.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
                        if (!ok) {
                            // 超时后仍未完成加载，则记录日志并使用新加载器加载值
                            logger.info("loader wait timeout:{}", timeout);
                            return newLoader.apply(key);
                        }
                    }
                } catch (InterruptedException e) {
                    // 等待过程中如果被中断，则记录日志并使用新加载器加载值
                    logger.warn("loader wait interrupted");
                    return newLoader.apply(key);
                }
                // 如果其他线程加载成功，则返回加载的值
                if (ll.success) {
                    return (V) ll.value;
                }
            }
        }
    }


    /**
     * 构建加载锁键
     *
     * @param c   缓存实例
     * @param key 键
     * @return 加载锁键
     */
    private static Object buildLoaderLockKey(Cache c, Object key) {
        if (c instanceof AbstractEmbeddedCache) {
            return ((AbstractEmbeddedCache) c).buildKey(key);
        } else if (c instanceof AbstractExternalCache) {
            byte[] bytes = ((AbstractExternalCache) c).buildKey(key);
            return ByteBuffer.wrap(bytes);
        } else if (c instanceof MultiLevelCache) {
            c = ((MultiLevelCache) c).caches()[0];
            return buildLoaderLockKey(c, key);
        } else if (c instanceof ProxyCache) {
            c = ((ProxyCache) c).getTargetCache();
            return buildLoaderLockKey(c, key);
        } else {
            throw new CacheException("impossible");
        }
    }

    /**
     * 加载锁类，用于同步加载操作和状态。
     * <p>
     * 该类主要用于配合懒加载模式下，多线程环境中的加载操作，
     * 通过使用计数器倒置信号和线程对象来确保线程安全和有序的加载过程。
     * </p>
     *
     * @see CountDownLatch
     */
    static class LoaderLock {
        /**
         * 计数器倒置信号，用于同步线程间的加载操作完成通知。
         */
        CountDownLatch signal;

        /**
         * 加载线程对象，用于标识执行加载操作的线程。
         */
        Thread loaderThread;
        /**
         * 是否成功的标志，用于标识加载操作是否成功完成。
         */
        volatile boolean success;

        /**
         * 加载的值对象，保存加载操作的结果。
         */
        volatile Object value;
    }
}
