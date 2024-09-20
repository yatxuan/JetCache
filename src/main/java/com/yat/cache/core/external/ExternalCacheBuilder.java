package com.yat.cache.core.external;

import com.yat.cache.core.AbstractCacheBuilder;
import com.yat.cache.core.CacheManager;
import com.yat.cache.core.support.BroadcastManager;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created on 16/9/9.
 *
 * @author huangli
 */
public abstract class ExternalCacheBuilder<T extends ExternalCacheBuilder<T>> extends AbstractCacheBuilder<T> {

    public boolean supportBroadcast() {
        return false;
    }

    public BroadcastManager createBroadcastManager(CacheManager cacheManager) {
        return null;
    }

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

    public void setBroadcastChannel(String broadcastChannel) {
        getConfig().setBroadcastChannel(broadcastChannel);
    }

    public T keyPrefix(String keyPrefix) {
        getConfig().setKeyPrefixSupplier(() -> keyPrefix);
        return self();
    }

    public T keyPrefixSupplier(Supplier<String> keyPrefixSupplier) {
        getConfig().setKeyPrefixSupplier(keyPrefixSupplier);
        return self();
    }

    public T valueEncoder(Function<Object, byte[]> valueEncoder) {
        getConfig().setValueEncoder(valueEncoder);
        return self();
    }

    public T valueDecoder(Function<byte[], Object> valueDecoder) {
        getConfig().setValueDecoder(valueDecoder);
        return self();
    }

    public void setKeyPrefix(String keyPrefix) {
        if (keyPrefix != null) {
            getConfig().setKeyPrefixSupplier(() -> keyPrefix);
        } else {
            getConfig().setKeyPrefixSupplier(null);
        }
    }

    public void setKeyPrefixSupplier(Supplier<String> keyPrefixSupplier) {
        getConfig().setKeyPrefixSupplier(keyPrefixSupplier);
    }

    public void setValueEncoder(Function<Object, byte[]> valueEncoder) {
        getConfig().setValueEncoder(valueEncoder);
    }

    public void setValueDecoder(Function<byte[], Object> valueDecoder) {
        getConfig().setValueDecoder(valueDecoder);
    }
}
