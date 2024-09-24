package com.yat.cache.autoconfigure.init.external;

import com.yat.cache.anno.api.DefaultCacheConstant;
import com.yat.cache.autoconfigure.JetCacheCondition;
import com.yat.cache.autoconfigure.properties.BaseCacheProperties;
import com.yat.cache.autoconfigure.properties.enums.RemoteCacheTypeEnum;
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
        super(RemoteCacheTypeEnum.MOCK.getUpperName());
    }

    @Override
    protected CacheBuilder initCache(BaseCacheProperties cacheProperties, String cacheAreaWithPrefix) {
        MockRemoteCacheBuilder builder = MockRemoteCacheBuilder.createMockRemoteCacheBuilder();
        parseGeneralConfig(builder, cacheProperties);
        return builder;
    }

    @Override
    protected void parseGeneralConfig(CacheBuilder builder, BaseCacheProperties properties) {
        super.parseGeneralConfig(builder, properties);
        MockRemoteCacheBuilder b = (MockRemoteCacheBuilder) builder;
        Integer limit = properties.getLimit();
        if (limit == null) {
            limit = DefaultCacheConstant.DEFAULT_LOCAL_LIMIT;
        }
        b.limit(limit);
    }

    public static class MockRemoteCacheCondition extends JetCacheCondition {
        public MockRemoteCacheCondition() {
            super("mock");
        }
    }
}
