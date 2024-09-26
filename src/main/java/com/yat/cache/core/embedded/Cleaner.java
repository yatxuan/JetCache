package com.yat.cache.core.embedded;

import com.yat.cache.core.support.JetCacheExecutor;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ClassName Cleaner
 * <p>Description 清理器:负责定期清理已过期的缓存条目。</p>
 *
 * @author Yat
 * Date 2024/8/22 10:33
 * version 1.0
 */
class Cleaner {

    /**
     * 使用弱引用保存缓存实例的链表
     */
    // static LinkedList<WeakReference<LinkedHashMapJetCache>> linkedHashMapCaches = new LinkedList<>();
    //
    // private static final ReentrantLock reentrantLock = new ReentrantLock();

    static ConcurrentLinkedQueue<WeakReference<LinkedHashMapJetCache>> linkedHashMapCaches = new ConcurrentLinkedQueue<>();

    /*
      静态初始化块，启动定时任务
     */
    static {
        ScheduledExecutorService executorService = JetCacheExecutor.defaultExecutor();
        // 每隔60秒执行一次清理任务
        executorService.scheduleWithFixedDelay(Cleaner::run, 60, 60, TimeUnit.SECONDS);
    }

    /**
     * Description: 添加一个新的缓存实例到列表中
     * <p>
     * Date: 2024/8/22 13:20
     *
     * @param cache 缓存实例
     */
    static void add(LinkedHashMapJetCache cache) {
        linkedHashMapCaches.add(new WeakReference<>(cache));
    }

    /**
     * Description: 执行清理任务的方法
     * <p>
     * Date: 2024/8/22 13:21
     */
    static void run() {
        Iterator<WeakReference<LinkedHashMapJetCache>> it = linkedHashMapCaches.iterator();
        while (it.hasNext()) {
            WeakReference<LinkedHashMapJetCache> ref = it.next();
            LinkedHashMapJetCache c = ref.get();
            if (c == null) {
                it.remove();
            } else {
                c.cleanExpiredEntry();
            }
        }
    }

}
