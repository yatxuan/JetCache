package com.yat.cache.autoconfigure;

import io.lettuce.core.AbstractRedisClient;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;
import io.lettuce.core.cluster.api.reactive.RedisClusterReactiveCommands;
import io.lettuce.core.cluster.api.sync.RedisClusterCommands;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * ClassName LettuceFactory
 * <p>Description 基于 Lettuce 的连接工厂</p>
 *
 * @author Yat
 * Date 2024/9/25 09:41
 * version 1.0
 */
public class LettuceFactory implements FactoryBean<Object> {

    /**
     * 对象的类类型，用于反射或其他类型相关的操作。
     */
    private final Class<?> clazz;
    /**
     * 用于标识或查找的键值。
     */
    private final String key;
    /**
     * 用于自动配置的Bean实例，负责处理特定条件下的Bean配置。
     */
    @Autowired
    private AutoConfigureBeans autoConfigureBeans;
    /**
     * 标记是否已经初始化，用于避免重复初始化操作。
     */
    private boolean inited;
    /**
     * 用于存储任意对象的引用。
     */
    private Object obj;

    // for unit test
    LettuceFactory(AutoConfigureBeans autoConfigureBeans, String key, Class<?> clazz) {
        this(key, clazz);
        this.autoConfigureBeans = autoConfigureBeans;
    }


    /**
     * 创建一个LettuceFactory实例，用于生产不同类型的Redis客户端或连接对象
     *
     * @param key   用于标识此工厂实例的键，后续结合具体客户端或连接类型进行调整
     * @param clazz 需要创建的Redis客户端或连接的类类型，用于确定具体实现
     * @throws IllegalArgumentException 如果传入的类类型不匹配任何支持的Redis客户端或连接类型，则抛出此异常
     */
    public LettuceFactory(String key, Class<?> clazz) {
        this.clazz = clazz;

        // 根据传入的类类型，调整key的后缀，以适应不同类型的Redis客户端或连接
        if (AbstractRedisClient.class.isAssignableFrom(clazz)) {
            key += ".client";
        } else if (StatefulConnection.class.isAssignableFrom(clazz)) {
            key += ".connection";
        } else if (RedisClusterCommands.class.isAssignableFrom(clazz)) {
            // RedisCommands extends RedisClusterCommands
            key += ".commands";
        } else if (RedisClusterAsyncCommands.class.isAssignableFrom(clazz)) {
            // RedisAsyncCommands extends RedisClusterAsyncCommands
            key += ".asyncCommands";
        } else if (RedisClusterReactiveCommands.class.isAssignableFrom(clazz)) {
            // RedisReactiveCommands extends RedisClusterReactiveCommands
            key += ".reactiveCommands";
        } else {
            // 如果类类型不匹配任何支持的类型，抛出异常
            throw new IllegalArgumentException(clazz.getName());
        }
        this.key = key;
    }

    @Override
    public Object getObject() {
        init();
        return obj;
    }

    private void init() {
        if (!inited) {
            obj = autoConfigureBeans.getCustomContainer().get(key);
            inited = true;
        }
    }

    /**
     * 返回当前实例所持有的对象类型
     *
     * @return 当前实例所持有的对象类型
     */
    @Override
    public Class<?> getObjectType() {
        return clazz;
    }

    /**
     * 重写isSingleton方法，用于确认这是一个单例模式的实现
     *
     * @return 返回true，表示该对象是单例对象
     */
    @Override
    public boolean isSingleton() {
        return true;
    }

}
