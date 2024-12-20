package com.yat.cache.autoconfigure.properties;

import com.yat.cache.anno.api.DefaultCacheConstant;
import com.yat.cache.anno.api.JetCached;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ClassName JetCacheProperties
 * Description 缓存配置
 *
 * @author Yat
 * Date 2024/8/21 13:26
 * version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = "jet-cache")
public class JetCacheProperties {

    /**
     * {@link JetCached}  自动生成name的时候，
     * 为了不让name太长，hiddenPackages指定的包名前缀被截掉
     * <p>与 GlobalCacheConfig 保持一致</p>
     */
    private String[] hiddenPackages;
    /**
     * 统计信息间隔时间，单位为分钟，用于控制统计信息更新的频率
     */
    private int statIntervalMinutes;
    /**
     * 是否添加区域名称 作为缓存key的前缀，默认为true
     */
    private Boolean areaInCacheName = true;
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
     * 本地缓存的信息 key-缓存区域标识-{@link DefaultCacheConstant#DEFAULT_AREA}
     */
    private Map<String, LocalCacheProperties> localCache = new LinkedHashMap<>();
    /**
     * 远程缓存的信息 key-缓存区域标识-{@link DefaultCacheConstant#DEFAULT_AREA}
     */
    private Map<String, RemoteCacheProperties> remoteCache = new LinkedHashMap<>();
}
