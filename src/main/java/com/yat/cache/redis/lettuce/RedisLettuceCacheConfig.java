package com.yat.cache.redis.lettuce;

import com.yat.cache.anno.api.DefaultCacheConstant;
import com.yat.cache.core.external.ExternalCacheConfig;
import io.lettuce.core.AbstractRedisClient;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import lombok.Getter;
import lombok.Setter;

/**
 * ClassName RedisLettuceCacheConfig
 * <p>Description RedisLettuce缓存配置</p>
 *
 * @author Yat
 * Date 2024/9/25 09:55
 * version 1.0
 */
@Setter
@Getter
public class RedisLettuceCacheConfig<K, V> extends ExternalCacheConfig<K, V> {
    /**
     * Redis客户端，用于操作Redis数据库
     */
    private AbstractRedisClient redisClient;

    /**
     * 到Redis服务器的连接，允许执行命令和接收响应
     */
    private StatefulConnection<byte[], byte[]> connection;

    /**
     * 到Redis服务器的发布/订阅连接，用于监听频道消息
     */
    private StatefulRedisPubSubConnection<byte[], byte[]> pubSubConnection;

    /**
     * 连接管理器，负责维护和管理Redis连接
     */
    private LettuceConnectionManager connectionManager = LettuceConnectionManager.defaultManager();

    /**
     * 异步操作结果的超时时间（毫秒），用于等待异步操作完成
     */
    private long asyncResultTimeoutInMillis = DefaultCacheConstant.ASYNC_RESULT_TIMEOUT.toMillis();
}
