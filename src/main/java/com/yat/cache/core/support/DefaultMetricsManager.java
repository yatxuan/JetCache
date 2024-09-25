package com.yat.cache.core.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * ClassName DefaultMetricsManager
 * <p>Description 默认的缓存监控管理器，负责定期收集和处理缓存统计信息。</p>
 *
 * @author Yat
 * Date 2024/8/22 18:04
 * version 1.0
 */
public class DefaultMetricsManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultMetricsManager.class);
    /**
     * 重置时间间隔
     */
    private final int resetTime;
    /**
     * 重置时间单位
     */
    private final TimeUnit resetTimeUnit;
    /**
     * 统计信息处理回调
     */
    private final Consumer<StatInfo> metricsCallback;
    private final ReentrantLock reentrantLock = new ReentrantLock();
    /**
     * 存储缓存监控器的列表，使用CopyOnWriteArrayList以支持并发修改
     */
    protected final CopyOnWriteArrayList<DefaultCacheMonitor> monitorList = new CopyOnWriteArrayList<>();
    /**
     * 定期执行的任务
     */
    final Runnable cmd = new Runnable() {

        /**
         * 上一次运行时间
         */
        private long time = System.currentTimeMillis();

        @Override
        public void run() {
            try {
                List<CacheStat> stats = monitorList.stream()
                        .map((m) -> {
                            CacheStat stat = m.getCacheStat();
                            m.resetStat();
                            return stat;
                        }).collect(Collectors.toList());

                long endTime = System.currentTimeMillis();
                StatInfo statInfo = new StatInfo();
                statInfo.setStartTime(time);
                statInfo.setEndTime(endTime);
                statInfo.setStats(stats);
                time = endTime;

                metricsCallback.accept(statInfo);
            } catch (Exception e) {
                logger.error("JetCache DefaultMetricsManager error", e);
            }
        }
    };
    /**
     * 定期任务的未来对象
     */
    private ScheduledFuture<?> future;

    /**
     * 初始化重置时间和单位以及统计信息处理回调。
     *
     * @param resetTime       重置时间间隔
     * @param resetTimeUnit   重置时间单位
     * @param metricsCallback 统计信息处理回调
     */
    public DefaultMetricsManager(int resetTime, TimeUnit resetTimeUnit, Consumer<StatInfo> metricsCallback) {
        this.resetTime = resetTime;
        this.resetTimeUnit = resetTimeUnit;
        this.metricsCallback = metricsCallback;
    }

    /**
     * 初始化重置时间和单位，并设置默认的统计信息处理回调。
     *
     * @param resetTime     重置时间间隔
     * @param resetTimeUnit 重置时间单位
     */
    public DefaultMetricsManager(int resetTime, TimeUnit resetTimeUnit) {
        this(resetTime, resetTimeUnit, false);
    }

    /**
     * 初始化重置时间和单位，并根据是否详细日志来设置统计信息处理回调。
     *
     * @param resetTime     重置时间间隔
     * @param resetTimeUnit 重置时间单位
     * @param verboseLog    是否输出详细日志
     */
    public DefaultMetricsManager(int resetTime, TimeUnit resetTimeUnit, boolean verboseLog) {
        this.resetTime = resetTime;
        this.resetTimeUnit = resetTimeUnit;
        this.metricsCallback = new StatInfoLogger(verboseLog);
    }
    /**
     * 启动定时任务。
     */
    public void start() {
        reentrantLock.lock();
        try {
            if (future != null) {
                return;
            }
            long delay = firstDelay(resetTime, resetTimeUnit);
            future = JetCacheExecutor.defaultExecutor().scheduleAtFixedRate(
                    cmd, delay, resetTimeUnit.toMillis(resetTime), TimeUnit.MILLISECONDS);
            logger.info("cache stat period at " + resetTime + " " + resetTimeUnit);
        } finally {
            reentrantLock.unlock();
        }
    }

    /**
     * 计算首次重置的时间延迟。
     *
     * @param resetTime     重置时间间隔
     * @param resetTimeUnit 重置时间单位
     * @return 首次重置的时间延迟（毫秒）
     */
    protected static long firstDelay(int resetTime, TimeUnit resetTimeUnit) {
        LocalDateTime firstResetTime = computeFirstResetTime(LocalDateTime.now(), resetTime, resetTimeUnit);
        return firstResetTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - System.currentTimeMillis();
    }

    /**
     * 根据当前时间计算下一次重置时间。
     *
     * @param baseTime 基准时间
     * @param time     重置时间间隔
     * @param unit     重置时间单位
     * @return 下一次重置时间
     */
    protected static LocalDateTime computeFirstResetTime(LocalDateTime baseTime, int time, TimeUnit unit) {
        if (unit != TimeUnit.SECONDS && unit != TimeUnit.MINUTES && unit != TimeUnit.HOURS && unit != TimeUnit.DAYS) {
            throw new IllegalArgumentException();
        }
        LocalDateTime t = baseTime;
        switch (unit) {
            case DAYS:
                t = t.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                break;
            case HOURS:
                if (24 % time == 0) {
                    t = t.plusHours(time - t.getHour() % time);
                } else {
                    t = t.plusHours(1);
                }
                t = t.withMinute(0).withSecond(0).withNano(0);
                break;
            case MINUTES:
                if (60 % time == 0) {
                    t = t.plusMinutes(time - t.getMinute() % time);
                } else {
                    t = t.plusMinutes(1);
                }
                t = t.withSecond(0).withNano(0);
                break;
            case SECONDS:
                if (60 % time == 0) {
                    t = t.plusSeconds(time - t.getSecond() % time);
                } else {
                    t = t.plusSeconds(1);
                }
                t = t.withNano(0);
                break;
        }
        return t;
    }
    /**
     * 停止定时任务。
     */
    public void stop() {
        reentrantLock.lock();
        try {
            future.cancel(false);
            logger.info("cache stat canceled");
            future = null;
        } finally {
            reentrantLock.unlock();
        }
    }

    /**
     * 添加缓存监控器。
     *
     * @param monitors 要添加的缓存监控器数组
     */
    public void add(DefaultCacheMonitor... monitors) {
        monitorList.addAll(Arrays.asList(monitors));
    }

    /**
     * 清空缓存监控器列表。
     */
    public void clear() {
        monitorList.clear();
    }


}
