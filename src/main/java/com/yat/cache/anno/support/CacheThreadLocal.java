package com.yat.cache.anno.support;

import lombok.Getter;
import lombok.Setter;

/**
 * ClassName CacheThreadLocal
 * <p>Description 用于缓存ThreadLocal变量的类</p>
 *
 * @author Yat
 * Date 2024/9/23 11:39
 * version 1.0
 */
@Setter
@Getter
class CacheThreadLocal {
    /**
     * 存储当前线程中某个操作的启用计数
     */
    private int enabledCount = 0;

    /**
     * 获取当前线程中某个操作的启用计数
     *
     * @return 当前线程中某个操作的启用计数
     */
    int getEnabledCount() {
        return enabledCount;
    }

    /**
     * 设置当前线程中某个操作的启用计数
     *
     * @param enabledCount 要设置的启用计数值
     */
    void setEnabledCount(int enabledCount) {
        this.enabledCount = enabledCount;
    }
}
