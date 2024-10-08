package com.yat.cache.core.template;

import com.yat.cache.core.CacheBuilder;
import com.yat.cache.core.CacheMonitor;
import com.yat.cache.core.CacheUtil;
import com.yat.cache.core.JetCache;
import com.yat.cache.core.JetCacheManager;
import com.yat.cache.core.MultiLevelJetCache;
import com.yat.cache.core.external.ExternalCacheBuilder;
import com.yat.cache.core.support.BroadcastManager;
import com.yat.cache.core.support.CacheNotifyMonitor;

import java.util.function.Function;

/**
 * 通知监控安装器类，用于在缓存管理器中添加通知监控器
 *
 * @author Yat
 * Date 2024/8/22 11:54
 * version 1.0
 */
public class NotifyMonitorInstaller implements CacheMonitorInstaller {


    /**
     * 远程缓存构建器模板，用于根据区域名生成对应的缓存构建器
     */
    private final Function<String, CacheBuilder> remoteBuilderTemplate;

    /**
     * 初始化远程缓存构建器模板
     *
     * @param remoteBuilderTemplate 远程缓存构建器模板函数
     */
    public NotifyMonitorInstaller(Function<String, CacheBuilder> remoteBuilderTemplate) {
        this.remoteBuilderTemplate = remoteBuilderTemplate;
    }

    /**
     * 添加监控器到缓存管理器和缓存实例中
     *
     * @param jetCacheManager 缓存管理器
     * @param jetCache        缓存实例
     * @param quickConfig     快速配置对象，包含缓存的配置信息
     */
    @Override
    public void addMonitors(JetCacheManager jetCacheManager, JetCache jetCache, QuickConfig quickConfig) {
        // 如果未配置同步本地缓存或同步本地缓存设置为false，则直接返回
        if (quickConfig.getSyncLocal() == null || !quickConfig.getSyncLocal()) {
            return;
        }
        // 如果缓存不是多级缓存实例，则直接返回
        if (!(CacheUtil.getAbstractCache(jetCache) instanceof MultiLevelJetCache)) {
            return;
        }

        // 获取区域名
        String area = quickConfig.getArea();
        // 根据区域名获取远程缓存构建器实例
        final ExternalCacheBuilder cacheBuilder = (ExternalCacheBuilder) remoteBuilderTemplate.apply(area);
        if (cacheBuilder == null || !cacheBuilder.supportBroadcast()
                || cacheBuilder.getConfig().getBroadcastChannel() == null) {
            return;
        }

        // 如果缓存管理器中没有对应的广播管理器，则创建并启动订阅
        if (jetCacheManager.getBroadcastManager(area) == null) {
            BroadcastManager cm = cacheBuilder.createBroadcastManager(jetCacheManager);
            if (cm != null) {
                cm.startSubscribe();
                jetCacheManager.putBroadcastManager(area, cm);
            }
        }

        // 创建缓存通知监控器实例并添加到缓存配置的监控器列表中
        CacheMonitor monitor = createMonitor(jetCacheManager, quickConfig, area);

        jetCache.config().getMonitors().add(monitor);
    }

    protected CacheMonitor createMonitor(JetCacheManager cacheManager, QuickConfig quickConfig, String area) {
        return new CacheNotifyMonitor(cacheManager, area, quickConfig.getName());
    }

}
