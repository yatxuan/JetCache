package com.yat.cache.core.support;

import com.yat.cache.core.exception.CacheException;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * ClassName CacheStat
 * <p>Description 代表缓存操作的统计信息</p>
 *
 * @author Yat
 * Date 2024/8/22 12:36
 * version 1.0
 */
@Setter
@Getter
public class CacheStat implements Serializable, Cloneable {

    @Serial
    private static final long serialVersionUID = -8802969946750554026L;

    /**
     * 缓存名称
     */
    protected String cacheName;

    /**
     * 统计收集周期的开始时间
     */
    protected long statStartTime;

    /**
     * 统计收集周期的结束时间
     */
    protected long statEndTime;

    // GET 操作统计
    /**
     * GET 操作总数
     */
    protected long getCount;

    /**
     * 成功的 GET 操作数（命中次数）
     */
    protected long getHitCount;

    /**
     * 失败的 GET 操作数（未命中次数）
     */
    protected long getMissCount;

    /**
     * GET 操作失败次数
     */
    protected long getFailCount;

    /**
     * 因过期而失败的 GET 操作数
     */
    protected long getExpireCount;

    /**
     * 所有 GET 操作总耗时
     */
    protected long getTimeSum;

    /**
     * 最短 GET 操作耗时
     */
    protected long minGetTime = Long.MAX_VALUE;

    /**
     * 最长 GET 操作耗时
     */
    protected long maxGetTime = 0;

    // PUT 操作统计
    /**
     * PUT 操作总数
     */
    protected long putCount;

    /**
     * 成功的 PUT 操作数
     */
    protected long putSuccessCount;

    /**
     * PUT 操作失败次数
     */
    protected long putFailCount;

    /**
     * 所有 PUT 操作总耗时
     */
    protected long putTimeSum;

    /**
     * 最短 PUT 操作耗时
     */
    protected long minPutTime = Long.MAX_VALUE;

    /**
     * 最长 PUT 操作耗时
     */
    protected long maxPutTime = 0;

    // REMOVE 操作统计
    /**
     * REMOVE 操作总数
     */
    protected long removeCount;

    /**
     * 成功的 REMOVE 操作数
     */
    protected long removeSuccessCount;

    /**
     * REMOVE 操作失败次数
     */
    protected long removeFailCount;

    /**
     * 所有 REMOVE 操作总耗时
     */
    protected long removeTimeSum;

    /**
     * 最短 REMOVE 操作耗时
     */
    protected long minRemoveTime = Long.MAX_VALUE;

    /**
     * 最长 REMOVE 操作耗时
     */
    protected long maxRemoveTime = 0;

    // LOAD 操作统计
    /**
     * LOAD 操作总数
     */
    protected long loadCount;

    /**
     * 成功的 LOAD 操作数
     */
    protected long loadSuccessCount;

    /**
     * LOAD 操作失败次数
     */
    protected long loadFailCount;

    /**
     * 所有 LOAD 操作总耗时
     */
    protected long loadTimeSum;

    /**
     * 最短 LOAD 操作耗时
     */
    protected long minLoadTime = Long.MAX_VALUE;

    /**
     * 最长 LOAD 操作耗时
     */
    protected long maxLoadTime = 0;

    @Override
    public CacheStat clone() {
        try {
            return (CacheStat) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new CacheException(e);
        }
    }

    /**
     * 计算 GET 操作的 QPS。
     *
     * @return GET 操作的 QPS 值
     */
    public double qps() {
        return tps(getCount);
    }

    /**
     * 计算每秒事务处理数 (TPS)。
     *
     * @param count 操作次数
     * @return TPS 值
     */
    private double tps(long count) {
        long t = statEndTime;
        if (t == 0) {
            t = System.currentTimeMillis();
        }
        t = t - statStartTime;
        if (t == 0) {
            return 0;
        } else {
            return 1000.0 * count / t;
        }
    }

    /**
     * 计算 PUT 操作的 TPS。
     *
     * @return PUT 操作的 TPS 值
     */
    public double putTps() {
        return tps(putCount);
    }

    /**
     * 计算 REMOVE 操作的 TPS。
     *
     * @return REMOVE 操作的 TPS 值
     */
    public double removeTps() {
        return tps(removeCount);
    }

    /**
     * 计算 LOAD 操作的 QPS。
     *
     * @return LOAD 操作的 QPS 值
     */
    public double loadQps() {
        return tps(loadCount);
    }

    /**
     * 计算 GET 操作的命中率。
     *
     * @return 命中率值
     */
    public double hitRate() {
        if (getCount == 0) {
            return 0;
        }
        return 1.0 * getHitCount / getCount;
    }

    /**
     * 计算 GET 操作的平均耗时。
     *
     * @return 平均耗时值
     */
    public double avgGetTime() {
        if (getCount == 0) {
            return 0;
        }
        return 1.0 * getTimeSum / getCount;
    }

    /**
     * 计算 PUT 操作的平均耗时。
     *
     * @return 平均耗时值
     */
    public double avgPutTime() {
        if (putCount == 0) {
            return 0;
        }
        return 1.0 * putTimeSum / putCount;
    }

    /**
     * 计算 REMOVE 操作的平均耗时。
     *
     * @return 平均耗时值
     */
    public double avgRemoveTime() {
        if (removeCount == 0) {
            return 0;
        }
        return 1.0 * removeTimeSum / removeCount;
    }

    /**
     * 计算 LOAD 操作的平均耗时。
     *
     * @return 平均耗时值
     */
    public double avgLoadTime() {
        if (loadCount == 0) {
            return 0;
        }
        return 1.0 * loadTimeSum / loadCount;
    }

    //---------------------------------------------------------------------


}
