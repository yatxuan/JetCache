package com.yat.cache.autoconfigure.init.embedded;

import com.yat.cache.anno.api.DefaultCacheConstant;
import com.yat.cache.autoconfigure.init.AbstractCacheAutoInit;
import com.yat.cache.autoconfigure.properties.BaseCacheProperties;
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
        super.parseGeneralConfig(builder, cacheProperties);

        Integer limit = cacheProperties.getLimit();
        if (limit == null) {
            limit = DefaultCacheConstant.DEFAULT_LOCAL_LIMIT;
        }
        builder.limit(limit);
        return builder;
    }

    protected abstract EmbeddedCacheBuilder<?> createEmbeddedCacheBuilder();


}
