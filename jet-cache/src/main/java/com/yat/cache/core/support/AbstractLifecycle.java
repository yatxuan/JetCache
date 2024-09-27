package com.yat.cache.core.support;

import java.util.concurrent.locks.ReentrantLock;

/**
 * ClassName AbstractLifecycle
 * <p>Description 抽象生命周期管理类，提供了初始化和关闭的基本框架</p>
 *
 * @author Yat
 * Date 2024/8/22 17:41
 * version 1.0
 */
public class AbstractLifecycle {
    final ReentrantLock reentrantLock = new ReentrantLock();
    /**
     * 标记是否已经初始化
     */
    private boolean init;
    /**
     * 标记是否已经关闭
     */
    private boolean shutdown;

    /**
     * 初始化方法，确保只被调用一次。
     */
    public final void init() {
        reentrantLock.lock();
        try {
            if (!init) {
                doInit();
                init = true;
            }
        } finally {
            reentrantLock.unlock();
        }
    }

    /**
     * 执行实际的初始化逻辑。
     */
    protected void doInit() {
    }

    /**
     * 关闭方法，确保在初始化后且未关闭的情况下只被调用一次。
     */
    public final void shutdown() {
        reentrantLock.lock();
        try {
            if (init && !shutdown) {
                doShutdown();
                init = false;
                shutdown = true;
            }
        } finally {
            reentrantLock.unlock();
        }
    }

    /**
     * 执行实际的关闭逻辑。
     */
    protected void doShutdown() {
    }
}
