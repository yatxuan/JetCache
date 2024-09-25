package com.yat.cache.core.template;

import com.yat.cache.core.Cache;
import com.yat.cache.core.CacheManager;

/**
 * ClassName CacheMonitorInstaller
 * <p>Description 定义安装监控器到缓存实例的方法</p>
 *
 * @author Yat
 * Date 2024/8/22 11:55
 * version 1.0
 */
public interface CacheMonitorInstaller {

    /**
     * 添加监控器到缓存实例。
     *
     * @param cacheManager 缓存管理器实例
     * @param cache        缓存实例
     * @param quickConfig  快速配置对象
     */
    void addMonitors(CacheManager cacheManager, Cache cache, QuickConfig quickConfig);
}
