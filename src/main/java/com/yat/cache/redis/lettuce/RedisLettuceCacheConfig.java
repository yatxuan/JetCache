package com.yat.cache.redis.lettuce;

import com.yat.cache.anno.api.CacheConsts;
import com.yat.cache.core.external.ExternalCacheConfig;
import io.lettuce.core.AbstractRedisClient;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;

/**
 * Created on 2017/4/28.
 *
 * @author huangli
 */
public class RedisLettuceCacheConfig<K, V> extends ExternalCacheConfig<K, V> {

    private AbstractRedisClient redisClient;

    private StatefulConnection<byte[], byte[]> connection;

    private StatefulRedisPubSubConnection<byte[], byte[]> pubSubConnection;

    private LettuceConnectionManager connectionManager = LettuceConnectionManager.defaultManager();

    private long asyncResultTimeoutInMillis = CacheConsts.ASYNC_RESULT_TIMEOUT.toMillis();

    public AbstractRedisClient getRedisClient() {
        return redisClient;
    }

    public void setRedisClient(AbstractRedisClient redisClient) {
        this.redisClient = redisClient;
    }

    public StatefulConnection<byte[], byte[]> getConnection() {
        return connection;
    }

    public void setConnection(StatefulConnection<byte[], byte[]> connection) {
        this.connection = connection;
    }

    public long getAsyncResultTimeoutInMillis() {
        return asyncResultTimeoutInMillis;
    }

    public void setAsyncResultTimeoutInMillis(long asyncResultTimeoutInMillis) {
        this.asyncResultTimeoutInMillis = asyncResultTimeoutInMillis;
    }

    public StatefulRedisPubSubConnection<byte[], byte[]> getPubSubConnection() {
        return pubSubConnection;
    }

    public void setPubSubConnection(StatefulRedisPubSubConnection<byte[], byte[]> pubSubConnection) {
        this.pubSubConnection = pubSubConnection;
    }

    public LettuceConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public void setConnectionManager(LettuceConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }
}
