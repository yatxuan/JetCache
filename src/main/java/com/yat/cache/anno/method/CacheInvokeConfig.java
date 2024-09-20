/**
 * Created on  13-09-21 23:04
 */
package com.yat.cache.anno.method;

import com.yat.cache.anno.support.CacheInvalidateAnnoConfig;
import com.yat.cache.anno.support.CacheUpdateAnnoConfig;
import com.yat.cache.anno.support.CachedAnnoConfig;

import java.util.List;

/**
 * @author huangli
 */
public class CacheInvokeConfig {
    private static final CacheInvokeConfig noCacheInvokeConfigInstance = new CacheInvokeConfig();
    private CachedAnnoConfig cachedAnnoConfig;
    private List<CacheInvalidateAnnoConfig> invalidateAnnoConfigs;
    private CacheUpdateAnnoConfig updateAnnoConfig;
    private boolean enableCacheContext;

    public CachedAnnoConfig getCachedAnnoConfig() {
        return cachedAnnoConfig;
    }

    public void setCachedAnnoConfig(CachedAnnoConfig cachedAnnoConfig) {
        this.cachedAnnoConfig = cachedAnnoConfig;
    }

    public boolean isEnableCacheContext() {
        return enableCacheContext;
    }

    public void setEnableCacheContext(boolean enableCacheContext) {
        this.enableCacheContext = enableCacheContext;
    }

    public List<CacheInvalidateAnnoConfig> getInvalidateAnnoConfigs() {
        return invalidateAnnoConfigs;
    }

    public void setInvalidateAnnoConfigs(List<CacheInvalidateAnnoConfig> invalidateAnnoConfigs) {
        this.invalidateAnnoConfigs = invalidateAnnoConfigs;
    }

    public CacheUpdateAnnoConfig getUpdateAnnoConfig() {
        return updateAnnoConfig;
    }

    public void setUpdateAnnoConfig(CacheUpdateAnnoConfig updateAnnoConfig) {
        this.updateAnnoConfig = updateAnnoConfig;
    }

    public static CacheInvokeConfig getNoCacheInvokeConfigInstance() {
        return noCacheInvokeConfigInstance;
    }
}
