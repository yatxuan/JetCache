package com.yat.cache.autoconfigure.init.embedded;

import com.yat.cache.autoconfigure.JetCacheCondition;
import com.yat.cache.autoconfigure.properties.BaseCacheProperties;
import com.yat.cache.autoconfigure.properties.enums.LocalCacheTypeEnum;
import com.yat.cache.core.CacheBuilder;
import com.yat.cache.core.embedded.LinkedHashMapCacheBuilder;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

/**
 * ClassName LinkedHashMapAutoConfiguration
 * <p>Description LinkedHashMap自动配置</p>
 *
 * @author Yat
 * Date 2024/8/22 22:11
 * version 1.0
 */
@Component
@Conditional(LinkedHashMapAutoConfiguration.LinkedHashMapCondition.class)
public class LinkedHashMapAutoConfiguration extends EmbeddedCacheAutoInit {

    public LinkedHashMapAutoConfiguration() {
        super(LocalCacheTypeEnum.LINKED_HASH_MAP.getUpperName());
    }

    @Override
    protected CacheBuilder initCache(BaseCacheProperties cacheProperties, String cacheAreaWithPrefix) {
        LinkedHashMapCacheBuilder<?> builder = LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder();
        parseGeneralConfig(builder, cacheProperties);
        return builder;
    }

    /**
     * ClassName LinkedHashMapCondition
     * <p>Description 是否注入 LinkedHashMapCondition 的条件判断</p>
     *
     * @author Yat
     * Date 2024/8/23 16:23
     * version 1.0
     */
    public static class LinkedHashMapCondition extends JetCacheCondition {
        public LinkedHashMapCondition() {
            super(LocalCacheTypeEnum.LINKED_HASH_MAP.name());
        }
    }
}
