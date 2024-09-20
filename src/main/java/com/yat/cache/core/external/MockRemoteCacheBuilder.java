package com.yat.cache.core.external;

import com.yat.cache.core.CacheManager;
import com.yat.cache.core.CacheResult;
import com.yat.cache.core.support.BroadcastManager;
import com.yat.cache.core.support.CacheMessage;

/**
 * Created on 2016/10/20.
 *
 * @author huangli
 */
public class MockRemoteCacheBuilder<T extends ExternalCacheBuilder<T>> extends ExternalCacheBuilder<T> {

    private static boolean subscribeStart;
    private static CacheMessage lastPublishMessage;

    public MockRemoteCacheBuilder() {
        this.setKeyPrefix("DEFAULT_PREFIX");
        buildFunc((c) -> new MockRemoteCache((MockRemoteCacheConfig) c));
    }

    public T limit(int limit) {
        getConfig().setLimit(limit);
        return self();
    }

    @Override
    public boolean supportBroadcast() {
        return true;
    }

    @Override
    public MockRemoteCacheConfig getConfig() {
        if (config == null) {
            config = new MockRemoteCacheConfig();
        }
        return (MockRemoteCacheConfig) config;
    }

    @Override
    public BroadcastManager createBroadcastManager(CacheManager cacheManager) {
        return new BroadcastManager(cacheManager) {
            @Override
            public CacheResult publish(CacheMessage cacheMessage) {
                lastPublishMessage = cacheMessage;
                return CacheResult.SUCCESS_WITHOUT_MSG;
            }

            @Override
            public void startSubscribe() {
                subscribeStart = true;
            }
        };
    }

    public void setLimit(int limit) {
        getConfig().setLimit(limit);
    }

    public static MockRemoteCacheBuilderImpl createMockRemoteCacheBuilder() {
        return new MockRemoteCacheBuilderImpl();
    }

    public static boolean isSubscribeStart() {
        return subscribeStart;
    }

    public static CacheMessage getLastPublishMessage() {
        return lastPublishMessage;
    }

    public static void reset() {
        subscribeStart = false;
        lastPublishMessage = null;
    }

    public static class MockRemoteCacheBuilderImpl extends MockRemoteCacheBuilder<MockRemoteCacheBuilderImpl> {
    }
}
