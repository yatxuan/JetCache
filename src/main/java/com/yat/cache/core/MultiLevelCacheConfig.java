package com.yat.cache.core;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName MultiLevelCacheConfig
 * <p>Description 多级缓存配置类</p>
 *
 * @author Yat
 * Date 2024/8/22 11:01
 * version 1.0
 */
@Setter
@Getter
public class MultiLevelCacheConfig<K, V> extends CacheConfig<K, V> {

    /**
     * 子缓存列表。
     */
    private List<Cache<K, V>> caches = new ArrayList<>();

    /**
     * 是否使用子缓存的过期时间
     */
    private boolean useExpireOfSubCache;

    @Override
    public MultiLevelCacheConfig clone() {
        MultiLevelCacheConfig copy = (MultiLevelCacheConfig) super.clone();
        if (caches != null) {
            copy.caches = new ArrayList(this.caches);
        }
        return copy;
    }

}
