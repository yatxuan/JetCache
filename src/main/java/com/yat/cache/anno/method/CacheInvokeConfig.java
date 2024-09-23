package com.yat.cache.anno.method;

import com.yat.cache.anno.support.CacheInvalidateAnnoConfig;
import com.yat.cache.anno.support.CacheUpdateAnnoConfig;
import com.yat.cache.anno.support.CachedAnnoConfig;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 缓存调用配置类，用于存储和管理缓存相关的方法配置。
 * 包括缓存配置、缓存失效配置和缓存更新配置。
 *
 * @author Yat
 */
@Setter
@Getter
public class CacheInvokeConfig {
    /**
     * 默认的无缓存调用配置实例，用于表示未启用缓存或缓存配置为空的情况。
     */
    @Getter
    private static final CacheInvokeConfig noCacheInvokeConfigInstance = new CacheInvokeConfig();
    /**
     * 缓存配置，用于存储与{@code @Cached}注解相关的方法配置信息。
     */
    private CachedAnnoConfig cachedAnnoConfig;
    /**
     * 缓存失效配置列表，用于存储与{@code @CacheInvalidate}注解相关的方法配置信息。
     */
    private List<CacheInvalidateAnnoConfig> invalidateAnnoConfigs;
    /**
     * 缓存更新配置，用于存储与{@code @CacheUpdate}注解相关的方法配置信息。
     */
    private CacheUpdateAnnoConfig updateAnnoConfig;
    /**
     * 标识是否启用缓存上下文，用于在方法执行时决定是否需要进行缓存操作。
     */
    private boolean enableCacheContext;

}
