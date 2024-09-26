package com.yat.cache.core.embedded;

import com.yat.cache.anno.api.DefaultCacheConstant;
import com.yat.cache.core.CacheConfig;
import lombok.Getter;
import lombok.Setter;


/**
 * ClassName EmbeddedCacheConfig
 * <p>Description 缓存配置类，继承自 CacheConfig 并扩展了本地缓存限制的配置。</p>
 *
 * @author Yat
 * Date 2024/8/22 10:31
 * version 1.0
 */
@Setter
@Getter
public class EmbeddedCacheConfig<K, V> extends CacheConfig<K, V> {
    /**
     * 本地缓存的最大元素数量，默认值为 CacheConstant.DEFAULT_LOCAL_LIMIT。
     */
    private int limit = DefaultCacheConstant.DEFAULT_LOCAL_LIMIT;


}
