package com.yat.cache.core.support;

import com.yat.cache.core.Cache;
import com.yat.cache.core.CacheManager;
import com.yat.cache.core.CacheResult;
import com.yat.cache.core.CacheUtil;
import com.yat.cache.core.MultiLevelCache;
import com.yat.cache.core.embedded.AbstractEmbeddedCache;
import com.yat.cache.core.exception.CacheConfigException;
import com.yat.cache.core.external.ExternalCacheConfig;
import com.yat.cache.core.lang.Assert;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ClassName BroadcastManager
 * <p>Description 广播管理器抽象类，用于处理缓存通知和订阅。</p>
 *
 * @author Yat
 * Date 2024/8/22 17:44
 * version 1.0
 */
@Getter
public abstract class BroadcastManager implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(BroadcastManager.class);

    /**
     * 源ID，用于标识广播消息的来源
     */
    private final String sourceId = UUID.randomUUID().toString();
    /**
     * 缓存管理器
     */
    private final CacheManager cacheManager;

    /**
     * 初始化缓存管理器。
     *
     * @param cacheManager 缓存管理器实例
     */
    public BroadcastManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }
    /**
     * 检查配置项是否正确设置。
     *
     * @param config 外部缓存配置
     */
    protected void checkConfig(ExternalCacheConfig config) {
        Assert.notNull(config.getBroadcastChannel(), () -> new CacheConfigException("BroadcastChannel not set"));
        Assert.notNull(config.getValueEncoder(), () -> new CacheConfigException("no value encoder"));
        Assert.notNull(config.getValueDecoder(), () -> new CacheConfigException("no value decoder"));
    }
    /**
     * 发布缓存消息。
     *
     * @param cacheMessage 缓存消息
     * @return 发布结果
     */
    public abstract CacheResult publish(CacheMessage cacheMessage);

    /**
     * 开始订阅缓存通知。
     */
    public abstract void startSubscribe();

    /**
     * 关闭广播管理器资源。
     */
    @Override
    public void close() throws Exception {
    }
    /**
     * 处理接收到的通知消息。
     *
     * @param message 接收到的消息字节数组
     * @param decoder 解码器，用于将消息转换为对象
     */
    protected void processNotification(byte[] message, Function<byte[], Object> decoder) {
        try {
            if (message == null) {
                logger.error("notify message is null");
                return;
            }
            Object value = decoder.apply(message);
            if (value == null) {
                logger.error("notify message is null");
                return;
            }
            if (value instanceof CacheMessage) {
                processCacheMessage((CacheMessage) value);
            } else {
                logger.error("the message is not instance of CacheMessage, class={}", value.getClass());
            }
        } catch (Throwable e) {
            SquashedLogger.getLogger(logger).error("receive cache notify error", e);
        }
    }
    /**
     * 处理缓存消息。
     *
     * @param cacheMessage 缓存消息
     */
    private void processCacheMessage(CacheMessage cacheMessage) {
        if (sourceId.equals(cacheMessage.getSourceId())) {
            return;
        }
        Cache cache = cacheManager.getCache(cacheMessage.getArea(), cacheMessage.getCacheName());
        if (cache == null) {
            logger.warn("Cache instance not exists: {},{}", cacheMessage.getArea(), cacheMessage.getCacheName());
            return;
        }
        Cache absCache = CacheUtil.getAbstractCache(cache);
        if (!(absCache instanceof MultiLevelCache)) {
            logger.warn("Cache instance is not MultiLevelCache: {},{}", cacheMessage.getArea(),
                    cacheMessage.getCacheName());
            return;
        }
        Cache[] caches = ((MultiLevelCache) absCache).caches();
        Set<Object> keys = Stream.of(cacheMessage.getKeys()).collect(Collectors.toSet());
        for (Cache c : caches) {
            Cache localCache = CacheUtil.getAbstractCache(c);
            if (localCache instanceof AbstractEmbeddedCache) {
                ((AbstractEmbeddedCache) localCache).__removeAll(keys);
            } else {
                break;
            }
        }
    }

}
