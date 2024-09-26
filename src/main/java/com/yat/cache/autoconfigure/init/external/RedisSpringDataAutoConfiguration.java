package com.yat.cache.autoconfigure.init.external;

import com.yat.cache.autoconfigure.JetCacheCondition;
import com.yat.cache.autoconfigure.properties.RemoteCacheProperties;
import com.yat.cache.autoconfigure.properties.enums.RemoteCacheTypeEnum;
import com.yat.cache.core.exception.CacheConfigException;
import com.yat.cache.core.external.ExternalCacheBuilder;
import com.yat.cache.core.lang.Assert;
import com.yat.cache.redis.springdata.RedisSpringDataCacheBuilder;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.lang.NonNull;

import java.util.Map;

/**
 * ClassName RedisSpringDataAutoConfiguration
 * <p>Description 基于  SpringDataRedis 的远程缓存</p>
 *
 * @author Yat
 * Date 2024/8/22 22:11
 * version 1.0
 */
@Configuration
@Conditional(RedisSpringDataAutoConfiguration.SpringDataRedisCondition.class)
public class RedisSpringDataAutoConfiguration {

    @Bean
    public SpringDataRedisAutoInit springDataRedisAutoInit() {
        return new SpringDataRedisAutoInit();
    }

    public static class SpringDataRedisCondition extends JetCacheCondition {
        public SpringDataRedisCondition() {
            super(RemoteCacheTypeEnum.REDIS_SPRING_DATA.getUpperName());
        }
    }

    public static class SpringDataRedisAutoInit extends ExternalCacheAutoInit implements ApplicationContextAware {

        private ApplicationContext applicationContext;

        public SpringDataRedisAutoInit() {
            super(RemoteCacheTypeEnum.REDIS_SPRING_DATA.getUpperName());
        }

        @Override
        protected ExternalCacheBuilder<?> createExternalCacheBuilder(
                RemoteCacheProperties cacheProperties, String cacheAreaWithPrefix
        ) {
            Map<String, RedisConnectionFactory> beans = applicationContext.getBeansOfType(RedisConnectionFactory.class);
            Assert.notNull(beans, () -> new CacheConfigException("no RedisConnectionFactory in spring context"));
            if (beans.isEmpty()) {
                throw new CacheConfigException("no RedisConnectionFactory in spring context");
            }
            RedisConnectionFactory factory = beans.values().iterator().next();
            if (beans.size() > 1) {
                String connectionFactoryName = getConnectionFactoryName(cacheProperties, beans);
                factory = beans.get(connectionFactoryName);
            }
            return RedisSpringDataCacheBuilder.createBuilder().connectionFactory(factory);
        }

        private static String getConnectionFactoryName(
                RemoteCacheProperties remoteCacheProperties, Map<String, RedisConnectionFactory> beans
        ) {
            String connectionFactoryName = remoteCacheProperties.getRedisData().getConnectionFactory();

            if (connectionFactoryName == null) {
                throw new CacheConfigException(
                        "connectionFactory is required, because there is multiple RedisConnectionFactory in " +
                                "Spring context"
                );
            }
            if (!beans.containsKey(connectionFactoryName)) {
                throw new CacheConfigException(
                        "there is no RedisConnectionFactory named " + connectionFactoryName + " in Spring context"
                );
            }
            return connectionFactoryName;
        }

        @Override
        public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
            this.applicationContext = applicationContext;
        }
    }
}
