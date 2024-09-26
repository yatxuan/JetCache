package com.yat.cache.core.support;

import java.util.concurrent.atomic.AtomicLong;

/**
 * ClassName Epoch
 * <p>Description Epoch类用于生成和获取epoch值 </p>
 * <p>Epoch值是一个递增的长整型数值，用于标识不同的时期或序列</p>
 */
public class Epoch {
    /**
     * 使用AtomicLong来保证线程安全的长整型原子变量
     */
    private static final AtomicLong V = new AtomicLong();

    /**
     * 以原子方式递增并获取当前的epoch值
     *
     * @return 当前的epoch值
     */
    public static long increment() {
        return V.incrementAndGet();
    }

    /**
     * 获取当前的epoch值
     *
     * @return 当前的epoch值
     */
    public static long get() {
        return V.get();
    }
}
