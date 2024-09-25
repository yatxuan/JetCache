package com.yat.cache.redis.lettuce;

import com.yat.cache.core.JetCacheManager;
import com.yat.cache.core.external.ExternalCacheBuilder;
import com.yat.cache.core.support.BroadcastManager;
import io.lettuce.core.AbstractRedisClient;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;

/**
 * ClassName RedisLettuceCacheBuilder
 * <p>Description 基于 RedisLettuce 的缓存构建器</p>
 *
 * @author Yat
 * Date 2024/9/25 09:44
 * version 1.0
 */
public class RedisLettuceCacheBuilder<T extends ExternalCacheBuilder<T>> extends ExternalCacheBuilder<T> {

    protected RedisLettuceCacheBuilder() {
        buildFunc(config -> new RedisLettuceJetCache((RedisLettuceCacheConfig) config));
    }

    @Override
    public boolean supportBroadcast() {
        return true;
    }

    @Override
    public BroadcastManager createBroadcastManager(JetCacheManager jetCacheManager) {
        RedisLettuceCacheConfig c = (RedisLettuceCacheConfig) getConfig().clone();
        return new LettuceBroadcastManager(jetCacheManager, c);
    }

    @Override
    public RedisLettuceCacheConfig getConfig() {
        if (config == null) {
            config = new RedisLettuceCacheConfig();
        }
        return (RedisLettuceCacheConfig) config;
    }

    public T redisClient(AbstractRedisClient redisClient) {
        getConfig().setRedisClient(redisClient);
        return self();
    }

    public void setRedisClient(AbstractRedisClient redisClient) {
        getConfig().setRedisClient(redisClient);
    }

    public T connection(StatefulConnection connection) {
        getConfig().setConnection(connection);
        return self();
    }

    public void setConnection(StatefulConnection connection) {
        getConfig().setConnection(connection);
    }

    public T pubSubConnection(StatefulRedisPubSubConnection pubSubConnection) {
        getConfig().setPubSubConnection(pubSubConnection);
        return self();
    }

    public void setPubSubConnection(StatefulRedisPubSubConnection pubSubConnection) {
        getConfig().setPubSubConnection(pubSubConnection);
    }

    public T connectionManager(LettuceConnectionManager connectionManager) {
        getConfig().setConnectionManager(connectionManager);
        return self();
    }

    public void setConnectionManager(LettuceConnectionManager connectionManager) {
        getConfig().setConnectionManager(connectionManager);
    }

    public T asyncResultTimeoutInMillis(long asyncResultTimeoutInMillis) {
        getConfig().setAsyncResultTimeoutInMillis(asyncResultTimeoutInMillis);
        return self();
    }

    public void setAsyncResultTimeoutInMillis(long asyncResultTimeoutInMillis) {
        getConfig().setAsyncResultTimeoutInMillis(asyncResultTimeoutInMillis);
    }

    public static RedisLettuceCacheBuilderImpl createRedisLettuceCacheBuilder() {
        return new RedisLettuceCacheBuilderImpl();
    }

    public static class RedisLettuceCacheBuilderImpl extends RedisLettuceCacheBuilder<RedisLettuceCacheBuilderImpl> {
    }
}
