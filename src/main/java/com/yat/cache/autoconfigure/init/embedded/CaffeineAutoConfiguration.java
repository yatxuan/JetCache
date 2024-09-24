package com.yat.cache.autoconfigure.init.embedded;

import com.yat.cache.autoconfigure.JetCacheCondition;
import com.yat.cache.autoconfigure.properties.BaseCacheProperties;
import com.yat.cache.autoconfigure.properties.enums.LocalCacheTypeEnum;
import com.yat.cache.core.CacheBuilder;
import com.yat.cache.core.embedded.CaffeineCacheBuilder;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

/**
 * Created on 2016/12/2.
 *
 * @author huangli
 */
@Component
@Conditional(CaffeineAutoConfiguration.CaffeineCondition.class)
public class CaffeineAutoConfiguration extends EmbeddedCacheAutoInit {
    public CaffeineAutoConfiguration() {
        super(LocalCacheTypeEnum.CAFFEINE.getUpperName());
    }

    @Override
    protected CacheBuilder initCache(BaseCacheProperties cacheProperties, String cacheAreaWithPrefix) {
        CaffeineCacheBuilder builder = CaffeineCacheBuilder.createCaffeineCacheBuilder();
        parseGeneralConfig(builder, cacheProperties);
        return builder;
    }

    public static class CaffeineCondition extends JetCacheCondition {
        public CaffeineCondition() {
            super("caffeine");
        }
    }
}
