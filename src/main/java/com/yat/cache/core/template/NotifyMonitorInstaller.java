/**
 * Created on 2022/08/01.
 */
package com.yat.cache.core.template;

import com.yat.cache.core.Cache;
import com.yat.cache.core.CacheBuilder;
import com.yat.cache.core.CacheManager;
import com.yat.cache.core.CacheMonitor;
import com.yat.cache.core.CacheUtil;
import com.yat.cache.core.MultiLevelCache;
import com.yat.cache.core.external.ExternalCacheBuilder;
import com.yat.cache.core.support.BroadcastManager;
import com.yat.cache.core.support.CacheNotifyMonitor;

import java.util.function.Function;

/**
 * @author huangli
 */
public class NotifyMonitorInstaller implements CacheMonitorInstaller {

    private final Function<String, CacheBuilder> remoteBuilderTemplate;

    public NotifyMonitorInstaller(Function<String, CacheBuilder> remoteBuilderTemplate) {
        this.remoteBuilderTemplate = remoteBuilderTemplate;
    }

    @Override
    public void addMonitors(CacheManager cacheManager, Cache cache, QuickConfig quickConfig) {
        if (quickConfig.getSyncLocal() == null || !quickConfig.getSyncLocal()) {
            return;
        }
        if (!(CacheUtil.getAbstractCache(cache) instanceof MultiLevelCache)) {
            return;
        }
        String area = quickConfig.getArea();
        final ExternalCacheBuilder cacheBuilder = (ExternalCacheBuilder) remoteBuilderTemplate.apply(area);
        if (cacheBuilder == null || !cacheBuilder.supportBroadcast()
                || cacheBuilder.getConfig().getBroadcastChannel() == null) {
            return;
        }

        if (cacheManager.getBroadcastManager(area) == null) {
            BroadcastManager cm = cacheBuilder.createBroadcastManager(cacheManager);
            if (cm != null) {
                cm.startSubscribe();
                cacheManager.putBroadcastManager(area, cm);
            }
        }

        CacheMonitor monitor = new CacheNotifyMonitor(cacheManager, area, quickConfig.getName());
        cache.config().getMonitors().add(monitor);
    }
}
