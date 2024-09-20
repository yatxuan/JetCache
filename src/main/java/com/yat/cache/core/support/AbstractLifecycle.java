/**
 * Created on 2019/6/8.
 */
package com.yat.cache.core.support;


import java.util.concurrent.locks.ReentrantLock;

/**
 * @author huangli
 */
public class AbstractLifecycle {
    final ReentrantLock reentrantLock = new ReentrantLock();
    private boolean init;
    private boolean shutdown;

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

    protected void doInit() {
    }

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

    protected void doShutdown() {
    }
}
