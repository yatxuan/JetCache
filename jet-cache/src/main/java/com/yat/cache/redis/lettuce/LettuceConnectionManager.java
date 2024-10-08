package com.yat.cache.redis.lettuce;

import cn.hutool.core.lang.Assert;
import com.yat.cache.core.exception.CacheConfigException;
import com.yat.cache.core.exception.CacheException;
import io.lettuce.core.AbstractRedisClient;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * ClassName LettuceConnectionManager
 * <p>Description 基于 Lettuce 的连接管理器</p>
 *
 * @author Yat
 * Date 2024/9/25 09:32
 * version 1.0
 */
public class LettuceConnectionManager {

    /**
     * 单例的连接管理器，用于统一管理Redis连接
     */
    private static final LettuceConnectionManager defaultManager = new LettuceConnectionManager();

    /**
     * 线程安全的映射，用于存储Redis客户端及其相关对象
     * 使用WeakHashMap确保不会阻止垃圾回收
     */
    private final Map<AbstractRedisClient, LettuceObjects> map = Collections.synchronizedMap(new WeakHashMap<>());

    private LettuceConnectionManager() {
    }

    /**
     * 初始化Redis客户端连接
     * <p>
     * 此方法为给定的Redis客户端初始化连接对象它通过计算是否需要为当前Redis客户端
     * 创建一个新的连接对象来做到这一点如果需要，它会创建一个包含连接信息的新LettuceObjects
     * 实例，并将其与Redis客户端关联
     *
     * @param redisClient Redis客户端实例，是抽象Redis客户端的子类此参数标识了需要初始化连接的客户端
     * @param connection  状态连接实例，包含与Redis服务器的连接此参数是实际的连接对象，将被存储以供后续使用
     */
    public void init(AbstractRedisClient redisClient, StatefulConnection connection) {
        map.computeIfAbsent(redisClient, key -> {
            LettuceObjects lo = new LettuceObjects();
            lo.connection = connection;
            return lo;
        });
    }

    /**
     * 获取Redis命令执行器
     * 本方法旨在为给定的Redis客户端实例获取相应的命令执行器（sync）
     * 它首先建立与Redis服务器的连接（如果尚未建立），然后根据连接的类型
     * （州春Redis连接或州春Redis集群连接）来获取命令执行器如果连接类型不被支持，
     * 则抛出异常本方法返回的命令执行器可以用于执行Redis命令
     *
     * @param redisClient 抽象Redis客户端实例，用于获取连接和配置信息
     * @return Object 返回Redis命令执行器，类型取决于连接的类型
     * @throws CacheConfigException 如果连接类型不被支持，则抛出此异常
     */
    public Object commands(AbstractRedisClient redisClient) {
        // 建立与Redis服务器的连接
        connection(redisClient);
        // 从map中获取Lettuce对象，包括连接和命令执行器
        LettuceObjects lo = getLettuceObjectsFromMap(redisClient);
        // 如果命令执行器尚未初始化，则根据连接类型进行初始化
        if (lo.commands == null) {
            // 如果连接是州春Redis连接
            if (lo.connection instanceof StatefulRedisConnection) {
                lo.commands = ((StatefulRedisConnection) lo.connection).sync();
            } else if (lo.connection instanceof StatefulRedisClusterConnection) {
                // 如果连接是州春Redis集群连接
                lo.commands = ((StatefulRedisClusterConnection) lo.connection).sync();
            } else {
                // 如果连接类型不被支持，则抛出异常
                throw new CacheConfigException("type " + lo.connection.getClass() + " is not supported");
            }
        }
        // 返回初始化好的命令执行器
        return lo.commands;
    }


    /**
     * 获取与Redis客户端的连接
     * 此方法确保每个Redis客户端都有一个连接实例，如果之前没有创建连接，则会根据客户端类型创建一个新的连接
     *
     * @param redisClient Redis客户端实例，可以是RedisClient或RedisClusterClient类型的实例
     * @return StatefulConnection，这是与Redis客户端的连接对象
     * @throws CacheConfigException 如果redisClient不是支持的类型，则抛出此异常
     */
    public StatefulConnection connection(AbstractRedisClient redisClient) {
        // 从缓存中获取与redisClient相关的Lettuce对象
        LettuceObjects lo = getLettuceObjectsFromMap(redisClient);

        // 检查是否已经有一个连接存在，如果没有，则尝试创建一个
        if (lo.connection == null) {
            // 判断redisClient的具体类型，以确定是使用标准的RedisClient还是集群模式的RedisClusterClient
            if (redisClient instanceof RedisClient) {
                // 如果是普通的RedisClient，则使用JetCacheCodec建立一个新的连接
                lo.connection = ((RedisClient) redisClient).connect(new JetCacheCodec());
            } else if (redisClient instanceof RedisClusterClient) {
                // 如果是Redis集群客户端，则同样使用JetCacheCodec建立一个新的连接
                lo.connection = ((RedisClusterClient) redisClient).connect(new JetCacheCodec());
            } else {
                // 如果redisClient既不是RedisClient也不是RedisClusterClient，抛出异常
                throw new CacheConfigException("type " + redisClient.getClass() + " is not supported");
            }
        }
        // 返回现有的或新创建的连接
        return lo.connection;
    }

    /**
     * 从映射中获取LettuceObjects对象
     * <p>
     * 此方法通过给定的Redis客户端从映射中获取LettuceObjects实例如果映射中不存在对应的LettuceObjects实例，
     * 则抛出一个CacheException异常，表明LettuceObjects未初始化
     *
     * @param redisClient Redis客户端实例，用于识别所需的LettuceObjects实例
     * @return Redis客户端对应的LettuceObjects实例
     * @throws CacheException 如果LettuceObjects未初始化，抛出此异常
     */
    private LettuceObjects getLettuceObjectsFromMap(AbstractRedisClient redisClient) {
        // 从映射中尝试获取与redisClient关联的LettuceObjects实例
        LettuceObjects lo = map.get(redisClient);

        // 如果未找到LettuceObjects实例，抛出异常
        Assert.notNull(lo, () -> new CacheException("LettuceObjects is not initialized"));

        // 返回找到的LettuceObjects实例
        return lo;
    }

    /**
     * 根据传入的Redis客户端获取异步命令执行器
     * 此方法用于建立与Redis服务器的连接，并根据连接的类型返回相应的异步命令执行器
     * 通过异步命令执行器，可以执行非阻塞的Redis命令操作
     *
     * @param redisClient 抽象的Redis客户端，包含连接信息和基本操作
     * @return 返回异步命令执行器，具体类型取决于连接的类型
     * @throws CacheConfigException 如果连接类型不受支持，则抛出此异常
     */
    public Object asyncCommands(AbstractRedisClient redisClient) {
        // 建立或获取与Redis服务器的连接
        connection(redisClient);

        // 从缓存中获取与当前Redis客户端相关的连接对象
        LettuceObjects lo = getLettuceObjectsFromMap(redisClient);

        // 如果当前连接对象没有对应的异步命令执行器，则根据连接类型创建
        if (lo.asyncCommands == null) {
            if (lo.connection instanceof StatefulRedisConnection) {
                // 如果连接是StatefulRedisConnection类型，则获取其异步命令执行器
                lo.asyncCommands = ((StatefulRedisConnection) lo.connection).async();
            } else if (lo.connection instanceof StatefulRedisClusterConnection) {
                // 如果连接是StatefulRedisClusterConnection类型，则获取其异步命令执行器
                lo.asyncCommands = ((StatefulRedisClusterConnection) lo.connection).async();
            } else {
                // 如果连接类型不受支持，则抛出异常
                throw new CacheConfigException("type " + lo.connection.getClass() + " is not supported");
            }
        }

        // 返回对应的异步命令执行器
        return lo.asyncCommands;
    }

    /**
     * 根据传入的Redis客户端获取响应的Reactive命令接口
     * 此方法用于建立与Redis服务器的连接，并根据连接类型获取相应的Reactive命令接口
     * 如果连接类型为StatefulRedisConnection或StatefulRedisClusterConnection，将返回其Reactive命令接口
     * 否则，将抛出异常指示不支持的连接类型
     *
     * @param redisClient 抽象Redis客户端，用于建立连接和获取命令接口
     * @return 返回与redisClient关联的Reactive命令接口
     * @throws CacheConfigException 如果连接类型不是StatefulRedisConnection或StatefulRedisClusterConnection，抛出此异常
     */
    public Object reactiveCommands(AbstractRedisClient redisClient) {
        // 建立与Redis服务器的连接
        connection(redisClient);
        // 从缓存中获取与redisClient关联的Lettuce对象
        LettuceObjects lo = getLettuceObjectsFromMap(redisClient);
        // 如果尚未获取到Reactive命令接口，则根据连接类型进行获取
        if (lo.reactiveCommands == null) {
            // 判断连接是否为StatefulRedisConnection类型
            if (lo.connection instanceof StatefulRedisConnection) {
                lo.reactiveCommands = ((StatefulRedisConnection) lo.connection).reactive();
            } else if (lo.connection instanceof StatefulRedisClusterConnection) {
                // 否则判断连接是否为StatefulRedisClusterConnection类型
                lo.reactiveCommands = ((StatefulRedisClusterConnection) lo.connection).reactive();
            } else {
                // 如果连接类型既不是StatefulRedisConnection也不是StatefulRedisClusterConnection，抛出异常
                throw new CacheConfigException("type " + lo.connection.getClass() + " is not supported");
            }
        }
        // 返回获取到的Reactive命令接口
        return lo.reactiveCommands;
    }

    /**
     * 从连接池中移除并关闭与给定Redis客户端的连接
     *
     * @param redisClient 要关闭并移除的Redis客户端连接
     */
    public void removeAndClose(AbstractRedisClient redisClient) {
        // 从map中移除与redisClient关联的LettuceObjects对象
        LettuceObjects lo = map.remove(redisClient);

        // 如果LettuceObjects对象为空，则无需进行任何操作
        if (lo == null) {
            return;
        }

        // 关闭与LettuceObjects对象关联的连接
        if (lo.connection != null) {
            lo.connection.close();
        }

        // 关闭Redis客户端连接并释放资源
        redisClient.shutdown();
    }

    public static LettuceConnectionManager defaultManager() {
        return defaultManager;
    }

    /**
     * LettuceObjects类用于封装Lettuce连接和命令对象
     * 它提供了存储StatefulConnection和不同操作模式下的命令对象的机制
     */
    private static class LettuceObjects {
        /**
         * StatefulConnection用于管理与Redis服务器的连接
         */
        private StatefulConnection connection;

        /**
         * commands对象表示同步命令的集合
         */
        private Object commands;

        /**
         * asyncCommands对象表示异步命令的集合
         */
        private Object asyncCommands;

        /**
         * reactiveCommands对象表示响应式命令的集合
         */
        private Object reactiveCommands;
    }
}
