package com.yat.cache.redis.springdata;

import com.yat.cache.core.external.ExternalCacheConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;


/**
 * RedisSpringDataCacheConfig 类继承自 ExternalCacheConfig，用于配置和管理 Redis 缓存。
 * 它提供了与 Spring Data Redis 集成的功能，包括连接工厂和消息监听容器的配置。
 *
 * @author Yat
 * @version 1.0
 */
@Setter
@Getter
public class RedisSpringDataCacheConfig<K, V> extends ExternalCacheConfig<K, V> {

    /**
     * Redis 连接工厂，用于创建和管理 Redis 连接
     */
    private RedisConnectionFactory connectionFactory;

    /**
     * Redis 消息监听容器，用于处理 Redis 消息事件，是一个可选的属性
     */
    private RedisMessageListenerContainer listenerContainer;

}
