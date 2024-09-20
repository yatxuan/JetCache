package com.yat.cache.core.external;

import com.yat.cache.anno.api.CacheConsts;

public class MockRemoteCacheConfig<K, V> extends ExternalCacheConfig<K, V> {
    private int limit = CacheConsts.DEFAULT_LOCAL_LIMIT;

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
