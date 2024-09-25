package com.yat.cache.redis.springdata;

import com.yat.cache.core.JetCacheManager;
import com.yat.cache.core.CacheResult;
import com.yat.cache.core.exception.CacheConfigException;
import com.yat.cache.core.support.BroadcastManager;
import com.yat.cache.core.support.CacheMessage;
import com.yat.cache.core.support.SquashedLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;


/**
 * 使用Spring Data Redis实现的BroadcastManager，用于管理和执行广播操作。
 * 广播操作主要用于缓存失效通知等场景。
 *
 * @author Yat
 * Date 2024/8/22 22:12
 * version 1.0
 */
public class SpringDataBroadcastManager extends BroadcastManager {

    private static final Logger logger = LoggerFactory.getLogger(SpringDataBroadcastManager.class);

    private final RedisSpringDataCacheConfig<Object, byte[]> config;
    private final MessageListener listener = this::onMessage;
    private final byte[] channel;
    private final ReentrantLock reentrantLock = new ReentrantLock();
    private volatile RedisMessageListenerContainer listenerContainer;

    /**
     * 初始化广播管理器。
     *
     * @param jetCacheManager 缓存管理器，用于管理不同的缓存。
     * @param config       配置信息，包含Redis连接工厂等必要设置。
     */
    public SpringDataBroadcastManager(JetCacheManager jetCacheManager, RedisSpringDataCacheConfig<Object, byte[]> config) {
        super(jetCacheManager);
        this.config = config;
        checkConfig(config);
        if (config.getConnectionFactory() == null) {
            throw new CacheConfigException("connectionFactory is required");
        }
        this.channel = config.getBroadcastChannel().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 发布消息到订阅者。
     *
     * @param cacheMessage 要发布的缓存消息。
     * @return 发布结果，成功或失败。
     */
    @Override
    public CacheResult publish(CacheMessage cacheMessage) {
        RedisConnection con = null;
        try {
            con = config.getConnectionFactory().getConnection();
            Function<Object, byte[]> valueEncoder = config.getValueEncoder();
            byte[] body = valueEncoder.apply(cacheMessage);
            con.publish(channel, body);
            return CacheResult.SUCCESS_WITHOUT_MSG;
        } catch (Exception ex) {
            SquashedLogger.getLogger(logger).error("JetCache publish error", ex);
            return new CacheResult(ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                    SquashedLogger.getLogger(logger).error("RedisConnection close fail", e);
                }
            }
        }
    }

    /**
     * 开始订阅消息。
     * 订阅特定主题的消息，接收广播通知。
     */
    @Override
    public void startSubscribe() {
        reentrantLock.lock();
        try {
            if (this.listenerContainer != null) {
                throw new IllegalStateException("subscribe thread is started");
            }
            Topic topic = new ChannelTopic(config.getBroadcastChannel());
            if (config.getListenerContainer() == null) {
                RedisMessageListenerContainer c = new RedisMessageListenerContainer();
                c.setConnectionFactory(config.getConnectionFactory());
                c.afterPropertiesSet();
                c.start();
                this.listenerContainer = c;
                logger.info("create RedisMessageListenerContainer instance");
            } else {
                this.listenerContainer = config.getListenerContainer();
            }
            this.listenerContainer.addMessageListener(listener, topic);
            logger.info("subscribe JetCache invalidate notification. channel={}", config.getBroadcastChannel());
        } finally {
            reentrantLock.unlock();
        }
    }

    /**
     * 关闭订阅服务。
     * 取消消息监听，并释放相关资源。
     *
     * @throws Exception 如果关闭过程中发生错误。
     */
    @Override
    public void close() throws Exception {
        reentrantLock.lock();
        try {
            if (this.listenerContainer != null) {
                this.listenerContainer.removeMessageListener(listener);
                if (this.config.getListenerContainer() == null) {
                    this.listenerContainer.destroy();
                }
            }
            this.listenerContainer = null;
        } finally {
            reentrantLock.unlock();
        }
    }

    /**
     * 接收消息的回调方法。
     *
     * @param message 接收到的消息。
     * @param pattern 匹配的模式，此处未使用。
     */
    private void onMessage(Message message, byte[] pattern) {
        processNotification(message.getBody(), config.getValueDecoder());
    }
}
