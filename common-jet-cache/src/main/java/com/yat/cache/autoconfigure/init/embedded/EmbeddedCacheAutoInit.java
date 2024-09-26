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
        EmbeddedCacheBuilder<?> builder = createEmbeddedCacheBuilder();
        parseEmbeddedGeneralConfig(builder, (LocalCacheProperties) cacheProperties);
        return builder;
    }

    /**
     * 创建一个嵌入式缓存构建器
     *
     * @return 返回一个嵌入式缓存构建器实例，具体类型视实现而定
     */
    protected abstract EmbeddedCacheBuilder<?> createEmbeddedCacheBuilder();

    /**
     * 解析嵌套式通用缓存配置
     * 此方法首先调用父类的通用配置解析方法，然后根据本地缓存的特性进行配置
     * 特别是设置缓存的限制大小，如果本地缓存属性中没有指定限制大小，
     * 则使用默认的本地缓存限制大小
     *
     * @param builder         缓存构建器，用于配置和构建缓存
     * @param cacheProperties 本地缓存属性，包含特定于本地缓存的配置属性
     */
    protected void parseEmbeddedGeneralConfig(EmbeddedCacheBuilder<?> builder, LocalCacheProperties cacheProperties) {
        super.parseGeneralConfig(builder, cacheProperties);

        // 从本地缓存属性中获取缓存限制大小
        Integer limit = cacheProperties.getLimit();
        // 如果没有指定限制大小，则使用默认的本地缓存限制大小
        if (limit == null) {
            limit = DefaultCacheConstant.DEFAULT_LOCAL_LIMIT;
        }
        // 设置缓存构建器的限制大小
        builder.limit(limit);
    }
}
