package com.yat.cache.core;

/**
 * ClassName AutoReleaseLock
 * <p>Description 自动释放锁接口，用于在Java 7及以上的版本中通过try-with-resources语句自动管理锁的释放</p>
 *
 * @author Yat
 * Date 2024/8/22 20:19
 * version 1.0
 */
public interface AutoReleaseLock extends AutoCloseable {
    /**
     * 使用Java 7的try-with-resources语句来释放锁
     * 该方法详细说明了如何释放锁资源，具体实现见子类
     */
    @Override
    void close();
}
