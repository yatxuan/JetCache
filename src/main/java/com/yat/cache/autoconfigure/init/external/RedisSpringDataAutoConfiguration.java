package com.yat.cache.autoconfigure.init.external;

import com.yat.cache.autoconfigure.JetCacheCondition;
import com.yat.cache.autoconfigure.properties.BaseCacheProperties;
import com.yat.cache.autoconfigure.properties.RemoteCacheProperties;
import com.yat.cache.autoconfigure.properties.enums.RemoteCacheTypeEnum;
import com.yat.cache.core.CacheBuilder;
import com.yat.cache.core.exception.CacheConfigException;
import com.yat.cache.core.external.ExternalCacheBuilder;
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
 * Created on 2019/5/1.
 *
 * @author huangli
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
        protected CacheBuilder initCache(BaseCacheProperties cacheProperties, String cacheAreaWithPrefix) {
            Map<String, RedisConnectionFactory> beans = applicationContext.getBeansOfType(RedisConnectionFactory.class);
            if (beans == null || beans.isEmpty()) {
                throw new CacheConfigException("no RedisConnectionFactory in spring context");
            }
            RedisConnectionFactory factory = beans.values().iterator().next();
            if (beans.size() > 1) {
                RemoteCacheProperties remoteCacheProperties = (RemoteCacheProperties) cacheProperties;

                String connectionFactoryName = remoteCacheProperties.getConnectionFactory();

                if (connectionFactoryName == null) {
                    throw new CacheConfigException(
                            "connectionFactory is required, because there is multiple RedisConnectionFactory in " +
                                    "Spring context");
                }
                if (!beans.containsKey(connectionFactoryName)) {
                    throw new CacheConfigException("there is no RedisConnectionFactory named "
                            + connectionFactoryName + " in Spring context");
                }
                factory = beans.get(connectionFactoryName);
            }
            ExternalCacheBuilder builder = RedisSpringDataCacheBuilder.createBuilder().connectionFactory(factory);
            parseGeneralConfig(builder, cacheProperties);
            return builder;
        }

        @Override
        public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
            this.applicationContext = applicationContext;
        }
    }
}
