package com.yat.cache.core.embedded;

import com.yat.cache.anno.api.DefaultCacheConstant;
import com.yat.cache.core.CacheConfig;

/**
 * Created on 16/9/7.
 *
 * @author huangli
 */
public class EmbeddedCacheConfig<K, V> extends CacheConfig<K, V> {
    private int limit = DefaultCacheConstant.DEFAULT_LOCAL_LIMIT;

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

}
