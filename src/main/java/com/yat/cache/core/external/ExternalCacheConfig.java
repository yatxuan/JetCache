package com.yat.cache.core.external;

import com.yat.cache.core.CacheConfig;
import com.yat.cache.core.support.DecoderMap;
import com.yat.cache.core.support.encoders.JavaValueEncoder;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created on 16/9/9.
 *
 * @author huangli
 */
@Setter
@Getter
public class ExternalCacheConfig<K, V> extends CacheConfig<K, V> {

    private Supplier<String> keyPrefixSupplier;
    private Function<Object, byte[]> valueEncoder = JavaValueEncoder.INSTANCE;
    private Function<byte[], Object> valueDecoder = DecoderMap.defaultJavaValueDecoder();
    private String broadcastChannel;

    public String getKeyPrefix() {
        return keyPrefixSupplier == null ? null : keyPrefixSupplier.get();
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefixSupplier = () -> keyPrefix;
    }

}
