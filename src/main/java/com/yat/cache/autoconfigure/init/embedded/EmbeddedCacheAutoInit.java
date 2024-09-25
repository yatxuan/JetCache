package com.yat.cache.autoconfigure.init.embedded;

import com.yat.cache.anno.api.DefaultCacheConstant;
import com.yat.cache.autoconfigure.init.AbstractCacheAutoInit;
import com.yat.cache.autoconfigure.properties.BaseCacheProperties;
import com.yat.cache.autoconfigure.properties.LocalCacheProperties;
import com.yat.cache.core.CacheBuilder;
import com.yat.cache.core.embedded.EmbeddedCacheBuilder;

/**
 * ClassName EmbeddedCacheAutoInit
 * <p>Description 本地缓存</p>
 *
 * @author Yat
 * Date 2024/8/22 22:09
 * version 1.0
 */
public abstract class EmbeddedCacheAutoInit extends AbstractCacheAutoInit {

    public EmbeddedCacheAutoInit(String... cacheTypes) {
        super(cacheTypes);
    }

    @Override
    protected CacheBuilder initCache(BaseCacheProperties cacheProperties, String cacheAreaWithPrefix) {
        return initEmbeddedCache((LocalCacheProperties) cacheProperties, cacheAreaWithPrefix);
    }

    /**
     * 初始化 本地 缓存的抽象方法
     *
     * @param cacheProperties     缓存属性，用于配置缓存的行为和特性
     * @param cacheAreaWithPrefix 带前缀的缓存区域，用于区分和组织不同的缓存空间
     * @return 返回一个初始化后的缓存构建器，用于进一步设置或操作缓存
     * <p>
     * 此方法为抽象方法，必须在子类中实现具体逻辑它提供了缓存系统初始化时的重要配置和定制选项，
     * 允许根据不同的缓存区域和属性灵活地设置缓存策略不同的缓存实现类可以根据给定的属性和区域，
     * 初始化最适合的缓存实例这个方法的存在使得缓存系统具有高度的 可配置性和灵活性，能够适应多种
     * 多样的缓存需求和场景
     */
    protected abstract CacheBuilder initEmbeddedCache(LocalCacheProperties cacheProperties, String cacheAreaWithPrefix);

    protected void parseEmbeddedGeneralConfig(EmbeddedCacheBuilder<?> builder, LocalCacheProperties cacheProperties) {
        super.parseGeneralConfig(builder, cacheProperties);

        Integer limit = cacheProperties.getLimit();
        if (limit == null) {
            limit = DefaultCacheConstant.DEFAULT_LOCAL_LIMIT;
        }
        builder.limit(limit);
    }

}
