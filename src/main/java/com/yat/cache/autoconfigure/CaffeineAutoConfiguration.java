package com.yat.cache.autoconfigure;

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
        super("caffeine");
    }

    @Override
    protected CacheBuilder initCache(ConfigTree ct, String cacheAreaWithPrefix) {
        CaffeineCacheBuilder builder = CaffeineCacheBuilder.createCaffeineCacheBuilder();
        parseGeneralConfig(builder, ct);
        return builder;
    }

    public static class CaffeineCondition extends JetCacheCondition {
        public CaffeineCondition() {
            super("caffeine");
        }
    }
}
