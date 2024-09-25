package com.yat.cache.core.external;

import com.yat.cache.anno.api.KeyConvertor;
import com.yat.cache.core.AbstractCache;
import com.yat.cache.core.RefreshCache;
import com.yat.cache.core.exception.CacheConfigException;
import com.yat.cache.core.exception.CacheException;
import com.yat.cache.core.lang.Assert;

import java.io.IOException;

/**
 * ClassName AbstractExternalCache
 * <p>Description 抽象远程缓存:提供外部缓存的基本功能，包括配置验证和键构建逻辑</p>
 *
 * @author Yat
 * Date 2024/8/22 13:31
 * version 1.0
 */
public abstract class AbstractExternalCache<K, V> extends AbstractCache<K, V> {

    private final ExternalCacheConfig<K, V> config;

    public AbstractExternalCache(ExternalCacheConfig<K, V> config) {
        this.config = config;
        checkConfig();
    }

    /**
     * 检查配置是否正确
     * <p>
     * 该方法旨在确保缓存配置的必要组件不为空收到的配置参数包括值编码器、值解码器和键前缀
     * 这些组件对于缓存的正常运行至关重要，因此必须在运行时进行验证
     */
    protected void checkConfig() {
        // 确保值编码器不为空如果为空，则抛出CacheConfigException，表明缺少值编码器
        Assert.notNull(config.getValueEncoder(), () -> new CacheConfigException("no value encoder"));

        // 确保值解码器不为空如果为空，则抛出CacheConfigException，表明缺少值解码器
        Assert.notNull(config.getValueDecoder(), () -> new CacheConfigException("no value decoder"));

        // 确保键前缀不为空如果为空，则抛出CacheConfigException，表明键前缀是必需的
        Assert.notNull(config.getKeyPrefix(), () -> new CacheConfigException("keyPrefix is required"));
    }

    /**
     * 根据给定的键和配置生成一个外部键
     * 该方法主要用于在缓存系统中生成统一的缓存键它考虑了键的转换器和键前缀
     *
     * @param key 原始的缓存键
     * @return 生成的外部键字节数组
     */
    public byte[] buildKey(K key) {
        try {
            // 根据条件判断，持有新的键值，初始为原始键
            Object newKey = key;

            // 如果配置了键转换器，则根据键转换器的类型进行处理
            if (config.getKeyConvertor() != null) {
                if (config.getKeyConvertor() instanceof KeyConvertor) {
                    // 对非保留键进行转换处理
                    if (!isPreservedKey(key)) {
                        newKey = config.getKeyConvertor().apply(key);
                    }
                } else {
                    // 处理旧版本的键转换逻辑
                    if (key instanceof byte[] || key instanceof String) {
                        newKey = key;
                    } else {
                        newKey = config.getKeyConvertor().apply(key);
                    }
                }
            }

            // 使用转换后的键和配置的键前缀生成最终的外部键
            return ExternalKeyUtil.buildKeyAfterConvert(newKey, config.getKeyPrefix());
        } catch (IOException e) {
            // 如果在生成键的过程中发生IO异常，则抛出缓存异常
            throw new CacheException(e);
        }
    }

    private boolean isPreservedKey(Object key) {
        if (key instanceof byte[] keyBytes) {
            return endWith(keyBytes, RefreshCache.LOCK_KEY_SUFFIX) ||
                    endWith(keyBytes, RefreshCache.TIMESTAMP_KEY_SUFFIX);
        }
        return false;
    }

    private boolean endWith(byte[] key, byte[] suffix) {
        int len = suffix.length;
        if (key.length < len) {
            return false;
        }
        int startPos = key.length - len;
        for (int i = 0; i < len; i++) {
            if (key[startPos + i] != suffix[i]) {
                return false;
            }
        }
        return true;
    }

}
