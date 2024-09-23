package com.yat.cache.anno.support;

import com.yat.cache.core.CacheBuilder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * ClassName GlobalCacheConfig
 * <p>Description 全局缓存配置</p>
 *
 * @author Yat
 * Date 2024/8/22 22:05
 * version 1.0
 */
@Setter
@Getter
public class GlobalCacheConfig {
    /**
     * {@link com.yat.cache.anno.api.Cached}  自动生成name的时候，
     * 为了不让name太长，hiddenPackages指定的包名前缀被截掉
     */
    private String[] hiddenPackages;
    /**
     * 是否添加区域名称 作为缓存key的前缀，默认为false
     * for compatible reason. This property controls whether add area as remote cache key prefix.
     * version<=2.4.3: add cache area in prefix, no config.
     * version>2.4.3 and version <2.7: default value is true, keep same as 2.4.3 if not set.
     * version>=2.7.0.RC: default value is false.
     * <p>
     * remove in the future.
     */
    @Deprecated
    private boolean areaInCacheName = false;
    /**
     * 是否启用缓存穿透保护，默认为false
     * 缓存穿透保护机制可以防止针对不存在的键的查询对后端系统的冲击
     */
    private boolean penetrationProtect = false;
    /**
     * 是否启用方法级别的缓存，默认为true
     * 如果启用，方法的返回值将被缓存，再次调用相同参数的方法时将从缓存中读取数据
     */
    private boolean enableMethodCache = true;
    /**
     * 本地缓存
     */
    private Map<String, CacheBuilder> localCacheBuilders;
    /**
     * 远程缓存
     */
    private Map<String, CacheBuilder> remoteCacheBuilders;
    /**
     * 统计信息间隔时间，单位为分钟，用于控制统计信息更新的频率
     */
    protected int statIntervalMinutes;

    public GlobalCacheConfig() {
    }

}
