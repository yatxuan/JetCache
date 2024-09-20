package com.yat.cache.core;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

/**
 * ClassName RefreshPolicy
 * <p>Description 刷新策略类，用于管理缓存数据的刷新逻辑和策略</p>
 *
 * @author Yat
 * Date 2024/8/22 20:39
 * version 1.0
 */
@Setter
@Getter
@NoArgsConstructor
public class RefreshPolicy implements Cloneable {

    /**
     * 刷新时间间隔，单位为毫秒。
     */
    private long refreshMillis;

    /**
     * 最后一次访问后停止刷新的时间，单位为毫秒。
     */
    private long stopRefreshAfterLastAccessMillis;

    /**
     * 刷新锁超时时间，默认为60秒，单位为毫秒。
     */
    private long refreshLockTimeoutMillis = 60 * 1000;

    /**
     * 设置在最后一次访问后停止刷新的时间
     *
     * @param time     时间值
     * @param timeUnit 时间单位
     * @return 当前刷新策略实例
     */
    public RefreshPolicy stopRefreshAfterLastAccess(long time, TimeUnit timeUnit) {
        this.stopRefreshAfterLastAccessMillis = timeUnit.toMillis(time);
        return this;
    }

    /**
     * 设置刷新锁超时时间
     *
     * @param time     时间值
     * @param timeUnit 时间单位
     * @return 当前刷新策略实例
     */
    public RefreshPolicy refreshLockTimeout(long time, TimeUnit timeUnit) {
        this.refreshLockTimeoutMillis = timeUnit.toMillis(time);
        return this;
    }

    @Override
    public RefreshPolicy clone() {
        try {
            return (RefreshPolicy) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建一个新的 刷新策略实例
     *
     * @param time     时间值
     * @param timeUnit 时间单位
     * @return 新的 刷新策略实例
     */
    public static RefreshPolicy newPolicy(long time, TimeUnit timeUnit) {
        RefreshPolicy p = new RefreshPolicy();
        p.refreshMillis = timeUnit.toMillis(time);
        return p;
    }
}
