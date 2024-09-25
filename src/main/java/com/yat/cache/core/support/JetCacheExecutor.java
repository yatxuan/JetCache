package com.yat.cache.core.support;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * JetCacheExecutor类提供了两个静态的ScheduledExecutorService实例，
 * 一个用于默认的缓存操作，另一个用于处理高IO负载的任务。
 * 这些线程池在类加载时创建，并在JVM关闭时优雅地关闭。
 * <p>
 *
 * @author Yat
 */
public class JetCacheExecutor {

    /**
     * 默认的缓存操作线程池
     */
    protected volatile static ScheduledExecutorService defaultExecutor;
    /**
     * 用于处理高IO负载任务的线程池
     */
    protected volatile static ScheduledExecutorService heavyIOExecutor;
    /**
     * 线程计数器，用于为高IO线程池中的线程命名
     */
    private static final AtomicInteger threadCount = new AtomicInteger(0);
    private static final ReentrantLock reentrantLock = new ReentrantLock();

    static {
        // JVM关闭时，优雅关闭所有线程池的钩子
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (defaultExecutor != null) {
                    defaultExecutor.shutdownNow();
                }
                if (heavyIOExecutor != null) {
                    heavyIOExecutor.shutdownNow();
                }
            }
        });
    }

    /**
     * 获取默认的缓存操作线程池实例。
     * 如果实例尚未创建，则创建一个新实例。
     *
     * @return 默认的ScheduledExecutorService实例
     */
    public static ScheduledExecutorService defaultExecutor() {
        if (defaultExecutor != null) {
            return defaultExecutor;
        }
        reentrantLock.lock();
        try {
            if (defaultExecutor == null) {
                ThreadFactory tf = r -> {
                    Thread t = new Thread(r, "JetCacheDefaultExecutor");
                    t.setDaemon(true);
                    return t;
                };
                defaultExecutor = new ScheduledThreadPoolExecutor(
                        1, tf, new ThreadPoolExecutor.DiscardPolicy());
            }
        } finally {
            reentrantLock.unlock();
        }
        return defaultExecutor;
    }

    /**
     * 获取用于处理高IO负载任务的线程池实例。
     * 如果实例尚未创建，则创建一个新实例。
     *
     * @return 用于高IO负载的ScheduledExecutorService实例
     */
    public static ScheduledExecutorService heavyIOExecutor() {
        if (heavyIOExecutor != null) {
            return heavyIOExecutor;
        }
        reentrantLock.lock();
        try {
            if (heavyIOExecutor == null) {
                ThreadFactory tf = r -> {
                    Thread t = new Thread(r, "JetCacheHeavyIOExecutor" + threadCount.getAndIncrement());
                    t.setDaemon(true);
                    return t;
                };
                heavyIOExecutor = new ScheduledThreadPoolExecutor(
                        10, tf, new ThreadPoolExecutor.DiscardPolicy());
            }
        } finally {
            reentrantLock.unlock();
        }
        return heavyIOExecutor;
    }

    /**
     * 外部设置默认的缓存操作线程池实例。
     *
     * @param executor 外部提供的ScheduledExecutorService实例
     */
    public static void setDefaultExecutor(ScheduledExecutorService executor) {
        JetCacheExecutor.defaultExecutor = executor;
    }

    /**
     * 外部设置用于处理高IO负载任务的线程池实例。
     *
     * @param heavyIOExecutor 外部提供的ScheduledExecutorService实例
     */
    public static void setHeavyIOExecutor(ScheduledExecutorService heavyIOExecutor) {
        JetCacheExecutor.heavyIOExecutor = heavyIOExecutor;
    }
}
