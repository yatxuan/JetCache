package com.yat.cache.redis.lettuce;

import com.yat.cache.anno.api.DefaultCacheConstant;
import com.yat.cache.core.external.ExternalCacheConfig;
import io.lettuce.core.AbstractRedisClient;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import lombok.Getter;
import lombok.Setter;

/**
 * Created on 2017/4/28.
 *
 * @author huangli
 */
@Setter
@Getter
public class RedisLettuceCacheConfig<K, V> extends ExternalCacheConfig<K, V> {

    private AbstractRedisClient redisClient;

    private StatefulConnection<byte[], byte[]> connection;

    private StatefulRedisPubSubConnection<byte[], byte[]> pubSubConnection;

    private LettuceConnectionManager connectionManager = LettuceConnectionManager.defaultManager();

    private long asyncResultTimeoutInMillis = DefaultCacheConstant.ASYNC_RESULT_TIMEOUT.toMillis();

}
