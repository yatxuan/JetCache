package com.yat.cache.redis.springdata;

import com.yat.cache.core.CacheManager;
import com.yat.cache.core.external.ExternalCacheBuilder;
import com.yat.cache.core.support.BroadcastManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * Created on 2019/4/21.
 *
 * @author huangli
 */
public class RedisSpringDataCacheBuilder<T extends ExternalCacheBuilder<T>> extends ExternalCacheBuilder<T> {
    protected RedisSpringDataCacheBuilder() {
        buildFunc(config -> new RedisSpringDataCache((RedisSpringDataCacheConfig) config));
    }

    @Override
    public boolean supportBroadcast() {
        return true;
    }

    @Override
    public BroadcastManager createBroadcastManager(CacheManager cacheManager) {
        RedisSpringDataCacheConfig c = (RedisSpringDataCacheConfig) getConfig().clone();
        return new SpringDataBroadcastManager(cacheManager, c);
    }

    @Override
    public RedisSpringDataCacheConfig getConfig() {
        if (config == null) {
            config = new RedisSpringDataCacheConfig();
        }
        return (RedisSpringDataCacheConfig) config;
    }

    public T connectionFactory(RedisConnectionFactory connectionFactory) {
        getConfig().setConnectionFactory(connectionFactory);
        return self();
    }

    public void setConnectionFactory(RedisConnectionFactory connectionFactory) {
        getConfig().setConnectionFactory(connectionFactory);
    }

    public T listenerContainer(RedisMessageListenerContainer listenerContainer) {
        getConfig().setListenerContainer(listenerContainer);
        return self();
    }

    public void setListenerContainer(RedisMessageListenerContainer listenerContainer) {
        getConfig().setListenerContainer(listenerContainer);
    }

    public static RedisSpringDataCacheBuilderImpl createBuilder() {
        return new RedisSpringDataCacheBuilderImpl();
    }

    public static class RedisSpringDataCacheBuilderImpl extends RedisSpringDataCacheBuilder<RedisSpringDataCacheBuilderImpl> {
    }
}
