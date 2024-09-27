package com.yat.cache.core.external;

import com.yat.cache.core.CacheConfig;
import com.yat.cache.core.support.DecoderMap;
import com.yat.cache.core.support.encoders.JavaValueEncoder;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * ClassName ExternalCacheConfig
 * <p>Description 远程缓存配置:封装了外部缓存所需的配置信息</p>
 *
 * @author Yat
 * Date 2024/8/22 13:39
 * version 1.0
 */
@Setter
@Getter
public class ExternalCacheConfig<K, V> extends CacheConfig<K, V> {

    /**
     * 键前缀。
     */
    private Supplier<String> keyPrefixSupplier;

    /**
     * 值编码器，默认使用 JavaValueEncoder 的实例。
     */
    private Function<Object, byte[]> valueEncoder = JavaValueEncoder.INSTANCE;

    /**
     * 值解码器，默认使用默认的 Java 值解码器。
     */
    private Function<byte[], Object> valueDecoder = DecoderMap.defaultJavaValueDecoder();

    /**
     * 广播通道名称。
     */
    private String broadcastChannel;

    /**
     * 获取键前缀。
     *
     * @return 键前缀。
     */
    public String getKeyPrefix() {
        return keyPrefixSupplier == null ? null : keyPrefixSupplier.get();
    }

    /**
     * 设置键前缀。
     *
     * @param keyPrefix 键前缀。
     */
    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefixSupplier = () -> keyPrefix;
    }

}
