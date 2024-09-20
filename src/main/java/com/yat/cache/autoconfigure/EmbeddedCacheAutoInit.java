package com.yat.cache.autoconfigure;

import com.yat.cache.anno.api.CacheConsts;
import com.yat.cache.core.CacheBuilder;
import com.yat.cache.core.embedded.EmbeddedCacheBuilder;

/**
 * Created on 2016/12/2.
 *
 * @author huangli
 */
public abstract class EmbeddedCacheAutoInit extends AbstractCacheAutoInit {

    public EmbeddedCacheAutoInit(String... cacheTypes) {
        super(cacheTypes);
    }

    @Override
    protected void parseGeneralConfig(CacheBuilder builder, ConfigTree ct) {
        super.parseGeneralConfig(builder, ct);
        EmbeddedCacheBuilder ecb = (EmbeddedCacheBuilder) builder;

        ecb.limit(Integer.parseInt(ct.getProperty("limit", String.valueOf(CacheConsts.DEFAULT_LOCAL_LIMIT))));
    }
}
