package com.yat.cache.autoconfigure;

import com.yat.cache.anno.api.CacheConsts;
import com.yat.cache.anno.support.ParserFunction;
import com.yat.cache.core.CacheBuilder;
import com.yat.cache.core.external.ExternalCacheBuilder;

/**
 * Created on 2016/11/29.
 *
 * @author huangli
 */
public abstract class ExternalCacheAutoInit extends AbstractCacheAutoInit {
    public ExternalCacheAutoInit(String... cacheTypes) {
        super(cacheTypes);
    }

    @Override
    protected void parseGeneralConfig(CacheBuilder builder, ConfigTree ct) {
        super.parseGeneralConfig(builder, ct);
        ExternalCacheBuilder ecb = (ExternalCacheBuilder) builder;
        ecb.setKeyPrefix(ct.getProperty("keyPrefix"));
        ecb.setBroadcastChannel(parseBroadcastChannel(ct));
        ecb.setValueEncoder(new ParserFunction(ct.getProperty("valueEncoder", CacheConsts.DEFAULT_SERIAL_POLICY)));
        ecb.setValueDecoder(new ParserFunction(ct.getProperty("valueDecoder", CacheConsts.DEFAULT_SERIAL_POLICY)));
    }

    protected String parseBroadcastChannel(ConfigTree ct) {
        String broadcastChannel = ct.getProperty("broadcastChannel");
        if (broadcastChannel != null && !"".equals(broadcastChannel.trim())) {
            return broadcastChannel.trim();
        } else {
            return null;
        }
    }
}
