package com.yat.cache.autoconfigure;

import com.yat.cache.core.CacheBuilder;
import com.yat.cache.core.embedded.LinkedHashMapCacheBuilder;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

/**
 * Created on 2016/12/2.
 *
 * @author huangli
 */
@Component
@Conditional(LinkedHashMapAutoConfiguration.LinkedHashMapCondition.class)
public class LinkedHashMapAutoConfiguration extends EmbeddedCacheAutoInit {
    public LinkedHashMapAutoConfiguration() {
        super("linkedhashmap");
    }

    @Override
    protected CacheBuilder initCache(ConfigTree ct, String cacheAreaWithPrefix) {
        LinkedHashMapCacheBuilder builder = LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder();
        parseGeneralConfig(builder, ct);
        return builder;
    }

    public static class LinkedHashMapCondition extends JetCacheCondition {
        public LinkedHashMapCondition() {
            super("linkedhashmap");
        }
    }
}
