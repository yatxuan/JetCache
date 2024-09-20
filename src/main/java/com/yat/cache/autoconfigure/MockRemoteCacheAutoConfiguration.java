package com.yat.cache.autoconfigure;

import com.yat.cache.anno.api.CacheConsts;
import com.yat.cache.core.CacheBuilder;
import com.yat.cache.core.external.MockRemoteCacheBuilder;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

/**
 * Created on 2016/12/2.
 *
 * @author huangli
 */
@Component
@Conditional(MockRemoteCacheAutoConfiguration.MockRemoteCacheCondition.class)
public class MockRemoteCacheAutoConfiguration extends ExternalCacheAutoInit {
    public MockRemoteCacheAutoConfiguration() {
        super("mock");
    }

    @Override
    protected CacheBuilder initCache(ConfigTree ct, String cacheAreaWithPrefix) {
        MockRemoteCacheBuilder builder = MockRemoteCacheBuilder.createMockRemoteCacheBuilder();
        parseGeneralConfig(builder, ct);
        return builder;
    }

    @Override
    protected void parseGeneralConfig(CacheBuilder builder, ConfigTree ct) {
        super.parseGeneralConfig(builder, ct);
        MockRemoteCacheBuilder b = (MockRemoteCacheBuilder) builder;
        b.limit(Integer.parseInt(ct.getProperty("limit", String.valueOf(CacheConsts.DEFAULT_LOCAL_LIMIT))));
    }

    public static class MockRemoteCacheCondition extends JetCacheCondition {
        public MockRemoteCacheCondition() {
            super("mock");
        }
    }
}
