/**
 * Created on 2019/6/7.
 */
package com.yat.cache.core.template;

import com.yat.cache.core.Cache;
import com.yat.cache.core.CacheManager;

/**
 * @author huangli
 */
public interface CacheMonitorInstaller {
    void addMonitors(CacheManager cacheManager, Cache cache, QuickConfig quickConfig);
}
