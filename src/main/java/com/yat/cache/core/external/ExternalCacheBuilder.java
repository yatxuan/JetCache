package com.yat.cache.core.external;

import com.yat.cache.core.AbstractCacheBuilder;
import com.yat.cache.core.JetCacheManager;
import com.yat.cache.core.support.BroadcastManager;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * ClassName ExternalCacheBuilder
 * 抽象远程缓存构建器。
 * <p>
 * 此类提供了构建远程缓存配置的功能。
 * </p>
 *
 * @author Yat
 * Date 2024/8/22 13:38
 * version 1.0
 */
public abstract class ExternalCacheBuilder<T extends ExternalCacheBuilder<T>> extends AbstractCacheBuilder<T> {
    /**
     * 判断是否支持广播。
     *
     * @return 如果支持广播返回 true，否则返回 false。
     */
    public boolean supportBroadcast() {
        return false;
    }

    /**
     * 创建广播管理器。
     *
     * @param jetCacheManager 缓存管理器。
     * @return 广播管理器实例。
     */
    public BroadcastManager createBroadcastManager(JetCacheManager jetCacheManager) {
        return null;
    }

    /**
     * 设置广播通道。
     *
     * @param broadcastChannel 广播通道名称。
     * @return 当前构建器实例。
     */
    public T broadcastChannel(String broadcastChannel) {
        getConfig().setBroadcastChannel(broadcastChannel);
        return self();
    }

    @Override
    public ExternalCacheConfig getConfig() {
        if (config == null) {
            config = new ExternalCacheConfig();
        }
        return (ExternalCacheConfig) config;
    }

    /**
     * 设置广播通道。
     *
     * @param broadcastChannel 广播通道名称。
     */
    public void setBroadcastChannel(String broadcastChannel) {
        getConfig().setBroadcastChannel(broadcastChannel);
    }

    /**
     * 设置键前缀。
     *
     * @param keyPrefix 键前缀字符串。
     * @return 当前构建器实例。
     */
    public T keyPrefix(String keyPrefix) {
        getConfig().setKeyPrefixSupplier(() -> keyPrefix);
        return self();
    }

    /**
     * 设置键前缀。
     *
     * @param keyPrefixSupplier 键前缀。
     * @return 当前构建器实例。
     */
    public T keyPrefixSupplier(Supplier<String> keyPrefixSupplier) {
        getConfig().setKeyPrefixSupplier(keyPrefixSupplier);
        return self();
    }

    /**
     * 设置值编码器。
     *
     * @param valueEncoder 值编码器。
     * @return 当前构建器实例。
     */
    public T valueEncoder(Function<Object, byte[]> valueEncoder) {
        getConfig().setValueEncoder(valueEncoder);
        return self();
    }

    /**
     * 设置值解码器。
     *
     * @param valueDecoder 值解码器。
     * @return 当前构建器实例。
     */
    public T valueDecoder(Function<byte[], Object> valueDecoder) {
        getConfig().setValueDecoder(valueDecoder);
        return self();
    }

    /**
     * 设置键前缀。
     *
     * @param keyPrefix 键前缀字符串。
     */
    public void setKeyPrefix(String keyPrefix) {
        if (keyPrefix != null) {
            getConfig().setKeyPrefixSupplier(() -> keyPrefix);
        } else {
            getConfig().setKeyPrefixSupplier(null);
        }
    }

    /**
     * 设置键前缀
     *
     * @param keyPrefixSupplier 键 前缀
     */
    public void setKeyPrefixSupplier(Supplier<String> keyPrefixSupplier) {
        getConfig().setKeyPrefixSupplier(keyPrefixSupplier);
    }

    /**
     * 设置值编码器。
     *
     * @param valueEncoder 值编码器。
     */
    public void setValueEncoder(Function<Object, byte[]> valueEncoder) {
        getConfig().setValueEncoder(valueEncoder);
    }

    /**
     * 设置值解码器。
     *
     * @param valueDecoder 值解码器。
     */
    public void setValueDecoder(Function<byte[], Object> valueDecoder) {
        getConfig().setValueDecoder(valueDecoder);
    }
}
