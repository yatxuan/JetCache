package com.yat.cache.autoconfigure.init.embedded;

import com.yat.cache.autoconfigure.JetCacheCondition;
import com.yat.cache.autoconfigure.properties.LocalCacheProperties;
import com.yat.cache.autoconfigure.properties.enums.LocalCacheTypeEnum;
import com.yat.cache.core.CacheBuilder;
import com.yat.cache.core.embedded.CaffeineCacheBuilder;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

/**
 * ClassName CaffeineAutoConfiguration
 * <p>Description caffeine 本地缓存</p>
 *
 * @author Yat
 * Date 2024/8/22 22:09
 * version 1.0
 */
@Component
@Conditional(CaffeineAutoConfiguration.CaffeineCondition.class)
public class CaffeineAutoConfiguration extends EmbeddedCacheAutoInit {

    public CaffeineAutoConfiguration() {
        super(LocalCacheTypeEnum.CAFFEINE.getUpperName());
    }

    @Override
    protected CacheBuilder initEmbeddedCache(LocalCacheProperties cacheProperties, String cacheAreaWithPrefix) {
        CaffeineCacheBuilder<?> builder = CaffeineCacheBuilder.createCaffeineCacheBuilder();
        parseEmbeddedGeneralConfig(builder, cacheProperties);
        return builder;
    }

    public static class CaffeineCondition extends JetCacheCondition {
        public CaffeineCondition() {
            super(LocalCacheTypeEnum.CAFFEINE.getUpperName());
        }
    }
}
