package com.yat.cache.autoconfigure.init.external;

import com.yat.cache.anno.api.DefaultCacheConstant;
import com.yat.cache.autoconfigure.JetCacheCondition;
import com.yat.cache.autoconfigure.properties.RemoteCacheProperties;
import com.yat.cache.autoconfigure.properties.enums.RemoteCacheTypeEnum;
import com.yat.cache.core.CacheBuilder;
import com.yat.cache.core.external.ExternalCacheBuilder;
import com.yat.cache.core.external.MockRemoteCacheBuilder;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * ClassName MockRemoteCacheAutoConfiguration
 * <p>Description 基于 mock 的远程缓存</p>
 *
 * @author Yat
 * Date 2024/8/22 22:11
 * version 1.0
 */
@Component
@Conditional(MockRemoteCacheAutoConfiguration.MockRemoteCacheCondition.class)
public class MockRemoteCacheAutoConfiguration extends ExternalCacheAutoInit {

    public MockRemoteCacheAutoConfiguration() {
        super(RemoteCacheTypeEnum.MOCK.getUpperName());
    }

    @Override
    protected void parseExternalGeneralConfig(ExternalCacheBuilder<?> builder, RemoteCacheProperties properties) {
        super.parseGeneralConfig(builder, properties);
        MockRemoteCacheBuilder<?> b = (MockRemoteCacheBuilder<?>) builder;
        // 从配置中获取limit值，如果没有设置，则使用默认的本地限制值
        Integer limit = properties.getLimit();
        if (Objects.isNull(limit)) {
            limit = DefaultCacheConstant.DEFAULT_LOCAL_LIMIT;
        }
        b.limit(limit);
    }

    @Override
    protected CacheBuilder initExternalCache(RemoteCacheProperties cacheProperties, String cacheAreaWithPrefix) {
        MockRemoteCacheBuilder<?> builder = MockRemoteCacheBuilder.createMockRemoteCacheBuilder();
        parseGeneralConfig(builder, cacheProperties);
        return builder;
    }

    public static class MockRemoteCacheCondition extends JetCacheCondition {
        public MockRemoteCacheCondition() {
            super(RemoteCacheTypeEnum.MOCK.getUpperName());
        }
    }
}
