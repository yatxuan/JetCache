package com.yat.cache.redis.lettuce;

import com.yat.cache.core.CacheResult;
import com.yat.cache.core.CacheResultCode;
import com.yat.cache.core.JetCacheManager;
import com.yat.cache.core.ResultData;
import com.yat.cache.core.exception.CacheConfigException;
import com.yat.cache.core.support.BroadcastManager;
import com.yat.cache.core.support.CacheMessage;
import com.yat.cache.core.support.JetCacheExecutor;
import com.yat.cache.core.support.SquashedLogger;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.async.BaseRedisAsyncCommands;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ClassName LettuceBroadcastManager
 * <p>Description Lettuce实现的广播管理器，用于发布和订阅缓存事件 </p>
 *
 * @author Yat
 * Date 2024/9/25 09:24
 * version 1.0
 */
public class LettuceBroadcastManager extends BroadcastManager {

    private static final Logger logger = LoggerFactory.getLogger(LettuceBroadcastManager.class);
    /**
     * Redis缓存配置
     */
    private final RedisLettuceCacheConfig<Object, Object> config;
    /**
     * 广播通道的字节表示
     */
    private final byte[] channel;
    /**
     * Lettuce连接管理器
     */
    private final LettuceConnectionManager lettuceConnectionManager;
    /**
     * Redis异步命令执行器
     */
    private final BaseRedisAsyncCommands<byte[], byte[]> stringAsyncCommands;
    /**
     * 重入锁
     */
    private final ReentrantLock reentrantLock = new ReentrantLock();
    /**
     * 订阅线程启动状态
     */
    private volatile boolean subscribeThreadStart;
    /**
     * 发布/订阅的适配器
     */
    private volatile RedisPubSubAdapter<byte[], byte[]> pubSubAdapter;

    /**
     * 构造函数，初始化广播管理器。
     *
     * @param jetCacheManager 缓存管理器
     * @param config          Redis缓存配置
     * @throws CacheConfigException 如果PubSub连接未设置，抛出此异常
     */
    public LettuceBroadcastManager(JetCacheManager jetCacheManager, RedisLettuceCacheConfig<Object, Object> config) {
        super(jetCacheManager);
        checkConfig(config);
        if (config.getPubSubConnection() == null) {
            throw new CacheConfigException("PubSubConnection not set");
        }

        this.config = config;
        this.channel = config.getBroadcastChannel().getBytes(StandardCharsets.UTF_8);
        this.lettuceConnectionManager = config.getConnectionManager();
        this.lettuceConnectionManager.init(config.getRedisClient(), config.getConnection());
        this.stringAsyncCommands = (BaseRedisAsyncCommands<byte[], byte[]>) lettuceConnectionManager
                .asyncCommands(config.getRedisClient());
    }


    /**
     * 发布缓存事件到Redis频道。
     *
     * @param cacheMessage 缓存消息
     * @return 发布结果
     */
    @Override
    public CacheResult publish(CacheMessage cacheMessage) {
        try {
            byte[] value = config.getValueEncoder().apply(cacheMessage);
            RedisFuture<Long> future = stringAsyncCommands.publish(channel, value);
            return new CacheResult(future.handle((rt, ex) -> {
                if (ex != null) {
                    JetCacheExecutor.defaultExecutor().execute(() ->
                            SquashedLogger.getLogger(logger).error("JetCache publish error", ex));
                    return new ResultData(ex);
                } else {
                    return new ResultData(CacheResultCode.SUCCESS, null, null);
                }
            }));
        } catch (Exception ex) {
            SquashedLogger.getLogger(logger).error("JetCache publish error", ex);
            return new CacheResult(ex);
        }
    }

    /**
     * 启动订阅线程，监听Redis频道的缓存事件。
     */
    @Override
    public void startSubscribe() {
        reentrantLock.lock();
        try {
            if (subscribeThreadStart) {
                throw new IllegalStateException("startSubscribe has invoked");
            }
            this.pubSubAdapter = new RedisPubSubAdapter<>() {
                @Override
                public void message(byte[] channel, byte[] message) {
                    JetCacheExecutor.defaultExecutor().execute(
                            () -> processNotification(message, config.getValueDecoder())
                    );

                }
            };
            config.getPubSubConnection().addListener(this.pubSubAdapter);
            RedisPubSubAsyncCommands<byte[], byte[]> asyncCommands = config.getPubSubConnection().async();
            asyncCommands.subscribe(channel);
            this.subscribeThreadStart = true;
        } finally {
            reentrantLock.unlock();
        }
    }

    @Override
    public void close() {
        config.getPubSubConnection().removeListener(this.pubSubAdapter);
        config.getPubSubConnection().close();
    }
}
