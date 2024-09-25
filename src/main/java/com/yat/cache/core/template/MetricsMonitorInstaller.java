package com.yat.cache.core.template;

import com.yat.cache.core.Cache;
import com.yat.cache.core.CacheManager;
import com.yat.cache.core.CacheUtil;
import com.yat.cache.core.MultiLevelCache;
import com.yat.cache.core.support.AbstractLifecycle;
import com.yat.cache.core.support.DefaultCacheMonitor;
import com.yat.cache.core.support.DefaultMetricsManager;
import com.yat.cache.core.support.StatInfo;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * ClassName MetricsMonitorInstaller
 * <p>Description MetricsMonitorInstaller类用于在缓存管理系统中安装性能监控器</p>
 * 它负责根据给定的回调函数和间隔时间初始化和管理MetricsManager，
 * 并为指定的缓存添加监控器。
 *
 * @author Yat
 * Date 2024/8/22 20:15
 * version 1.0
 */
public class MetricsMonitorInstaller extends AbstractLifecycle implements CacheMonitorInstaller {

    /**
     * Metrics回调函数，用于处理收集到的统计信息。
     */
    private final Consumer<StatInfo> metricsCallback;
    /**
     * 监控数据收集的间隔时间。
     */
    private final Duration interval;
    /**
     * 实际负责监控管理的MetricsManager实例。
     */
    private DefaultMetricsManager metricsManager;

    /**
     * 初始化MetricsMonitorInstaller。
     *
     * @param metricsCallback 用于处理统计信息的回调函数。
     * @param interval        收集监控数据的间隔时间。
     */
    public MetricsMonitorInstaller(Consumer<StatInfo> metricsCallback, Duration interval) {
        this.metricsCallback = metricsCallback;
        this.interval = interval;
    }

    /**
     * 初始化过程，根据配置启动MetricsManager。
     */
    @Override
    protected void doInit() {
        if (metricsCallback != null && interval != null) {
            metricsManager = new DefaultMetricsManager(
                    (int) interval.toMinutes(), TimeUnit.MINUTES, metricsCallback
            );
            metricsManager.start();
        }
    }

    /**
     * 停止并清理MetricsManager。
     */
    @Override
    protected void doShutdown() {
        if (metricsManager != null) {
            // 停止并清理MetricsManager，断开与所有监控器的连接。
            metricsManager.stop();
            metricsManager.clear();
            metricsManager = null;
        }
    }

    @Override
    public void addMonitors(CacheManager cacheManager, Cache cache, QuickConfig quickConfig) {
        if (metricsManager == null) {
            return;
        }
        // 尝试获取抽象缓存实例，以便于监控器的添加。
        cache = CacheUtil.getAbstractCache(cache);
        if (cache instanceof MultiLevelCache) {
            MultiLevelCache mc = (MultiLevelCache) cache;
            // 处理多级缓存情况，目前仅支持两级缓存。
            if (mc.caches().length == 2) {
                Cache local = mc.caches()[0];
                Cache remote = mc.caches()[1];
                DefaultCacheMonitor localMonitor = new DefaultCacheMonitor(quickConfig.getName() + "_local");
                local.config().getMonitors().add(localMonitor);
                DefaultCacheMonitor remoteMonitor = new DefaultCacheMonitor(quickConfig.getName() + "_remote");
                remote.config().getMonitors().add(remoteMonitor);
                // 将本地和远程监控器添加到MetricsManager中。
                metricsManager.add(localMonitor, remoteMonitor);
            }
        }

        // 为当前缓存添加一个默认的监控器。
        DefaultCacheMonitor monitor = new DefaultCacheMonitor(quickConfig.getName());
        cache.config().getMonitors().add(monitor);
        // 将缓存监控器添加到MetricsManager中。
        metricsManager.add(monitor);
    }
}
