package com.yat.cache.core.external;

import com.yat.cache.anno.api.DefaultCacheConstant;
import lombok.Getter;
import lombok.Setter;

/**
 * ClassName MockRemoteCacheConfig
 * <p>Description 远程缓存配置类:用于模拟远程缓存服务的配置</p>
 *
 * @author Yat
 * Date 2024/8/22 13:46
 * version 1.0
 */
@Getter
@Setter
public class MockRemoteCacheConfig<K, V> extends ExternalCacheConfig<K, V> {
    /**
     * 缓存限制，默认值来自 DefaultCacheConstant.DEFAULT_LOCAL_LIMIT。
     */
    private int limit = DefaultCacheConstant.DEFAULT_LOCAL_LIMIT;

}
