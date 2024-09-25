package com.yat.cache.core;

import com.yat.cache.anno.api.CacheType;
import com.yat.cache.core.embedded.EmbeddedCacheBuilder;
import com.yat.cache.core.exception.CacheConfigException;
import com.yat.cache.core.external.ExternalCacheBuilder;
import com.yat.cache.core.lang.Assert;
import com.yat.cache.core.support.BroadcastManager;
import com.yat.cache.core.template.CacheBuilderTemplate;
import com.yat.cache.core.template.CacheMonitorInstaller;
import com.yat.cache.core.template.QuickConfig;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * ClassName SimpleCacheManager
 * <p>Description 简单缓存管理器</p>
 *
 * @author Yat
 * Date 2024/8/22 11:49
 * version 1.0
 */
@NoArgsConstructor
public class SimpleJetCacheManager implements JetCacheManager, AutoCloseable {
    /**
     * 是否缓存空值,默认 false
     */
    private static final boolean DEFAULT_CACHE_NULL_VALUE = false;

    private static final Logger logger = LoggerFactory.getLogger(SimpleJetCacheManager.class);

    // area -> cacheName -> Cache
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, JetCache>> caches = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, BroadcastManager> broadcastManagers = new ConcurrentHashMap<>();

    @Getter
    @Setter
    private CacheBuilderTemplate cacheBuilderTemplate;

    @Override
    public void close() {
        broadcastManagers.forEach((area, bm) -> {
            try {
                bm.close();
            } catch (Exception e) {
                logger.error("error during close broadcast manager", e);
            }
        });
        broadcastManagers.clear();
        caches.forEach((area, areaMap) -> areaMap.forEach((cacheName, cache) -> {
            try {
                cache.close();
            } catch (Exception e) {
                logger.error("error during close Cache", e);
            }
        }));
        caches.clear();
    }

    @Override
    public BroadcastManager getBroadcastManager(String area) {
        return broadcastManagers.get(area);
    }

    @Override
    public JetCache getCache(String area, String cacheName) {
        ConcurrentHashMap<String, JetCache> areaMap = getCachesByArea(area);
        return areaMap.get(cacheName);
    }

    private ConcurrentHashMap<String, JetCache> getCachesByArea(String area) {
        return caches.computeIfAbsent(area, (key) -> new ConcurrentHashMap<>());
    }

    @Override
    public void putCache(String area, String cacheName, JetCache jetCache) {
        ConcurrentHashMap<String, JetCache> areaMap = getCachesByArea(area);
        areaMap.put(cacheName, jetCache);
    }

    /**
     * 根据给定的配置获取或创建缓存实例这个方法主要用于在缓存系统中根据特定配置动态创建缓存对象
     *
     * @param config 缓存配置对象，包含了创建缓存所需的所有信息，如缓存区域、缓存名等
     * @param <K>    缓存键的类型
     * @param <V>    缓存值的类型
     * @return 对应缓存区域和名称的缓存对象如果已经存在则返回已有的缓存对象，否则根据配置创建新的缓存对象
     * @throws IllegalStateException 如果cacheBuilderTemplate未设置，则抛出此异常，因为cacheBuilderTemplate是创建缓存对象所必需的
     */
    @Override
    public <K, V> JetCache<K, V> getOrCreateCache(QuickConfig config) {
        // 检查cacheBuilderTemplate是否已设置，这是创建缓存所必需的
        Assert.notNull(cacheBuilderTemplate, () -> new IllegalStateException("cacheBuilderTemplate not set"));

        // 确保配置中的缓存区域和名称不为null，因为它们是创建缓存所必需的
        Objects.requireNonNull(config.getArea());
        Objects.requireNonNull(config.getName());

        // 获取或初始化由指定缓存区域的所有缓存的映射
        ConcurrentHashMap<String, JetCache> m = getCachesByArea(config.getArea());

        // 尝试从缓存映射中获取缓存对象如果存在，直接返回
        JetCache c = m.get(config.getName());
        if (c != null) {
            return c;
        }

        // 如果缓存不存在，则根据配置创建新的缓存对象，并放入映射中
        return m.computeIfAbsent(config.getName(), n -> create(config));
    }


    @Override
    public void putBroadcastManager(String area, BroadcastManager broadcastManager) {
        broadcastManagers.put(area, broadcastManager);
    }

    /**
     * 根据配置创建缓存
     *
     * @param config 缓存的配置信息，包含缓存类型、过期时间等
     * @return 创建的缓存实例
     */
    private <K, V> JetCache<K, V> create(QuickConfig config) {
        JetCache<K, V> jetCache;
        // 当缓存类型为null或远程缓存时，构建远程缓存
        if (config.getCacheType() == null || config.getCacheType() == CacheType.REMOTE) {
            jetCache = buildRemote(config);
        } else if (config.getCacheType() == CacheType.LOCAL) {
            // 当缓存类型为本地缓存时，构建本地缓存
            jetCache = buildLocal(config);
        } else {
            // 当缓存类型既不是本地也不是远程时，构建多级缓存
            JetCache<K, V> local = buildLocal(config);
            JetCache<K, V> remote = buildRemote(config);

            // 设置子缓存的过期策略
            boolean useExpireOfSubCache = config.getLocalExpire() != null;
            jetCache = MultiLevelCacheBuilder.createMultiLevelCacheBuilder()
                    .expireAfterWrite(remote.config().getExpireAfterWriteInMillis(), TimeUnit.MILLISECONDS)
                    .addCache(local, remote)
                    .useExpireOfSubCache(useExpireOfSubCache)
                    .cacheNullValue(config.getCacheNullValue() != null ?
                            config.getCacheNullValue() : DEFAULT_CACHE_NULL_VALUE)
                    .buildCache();
        }
        // 根据配置的刷新策略，包装缓存实例
        if (config.getRefreshPolicy() != null) {
            jetCache = new RefreshJetCache<>(jetCache);
        } else if (config.getLoader() != null) {
            // 当有加载器配置时，包装缓存实例
            jetCache = new LoadingJetCache<>(jetCache);
        }
        // 设置刷新策略和加载器配置到缓存实例
        jetCache.config().setRefreshPolicy(config.getRefreshPolicy());
        jetCache.config().setLoader(config.getLoader());

        // 设置穿透保护
        boolean protect = config.getPenetrationProtect() != null ? config.getPenetrationProtect()
                : cacheBuilderTemplate.isPenetrationProtect();
        jetCache.config().setCachePenetrationProtect(protect);
        jetCache.config().setPenetrationProtectTimeout(config.getPenetrationProtectTimeout());

        // 为缓存实例添加监控器
        for (CacheMonitorInstaller i : cacheBuilderTemplate.getCacheMonitorInstallers()) {
            i.addMonitors(this, jetCache, config);
        }
        return jetCache;
    }

    /**
     * 根据QuickConfig配置构建远程缓存
     * 此方法主要用于初始化外部缓存实例，根据传入的配置信息选择合适的缓存构建器，
     * 设置缓存的过期时间、前缀、键值转换器等，并最终构建缓存实例
     *
     * @param config 缓存配置信息，包含了构建缓存所需的各种参数
     * @return 返回根据指定配置构建的缓存实例
     * @throws CacheConfigException 当找不到合适的缓存构建器时抛出此异常
     */
    private JetCache buildRemote(QuickConfig config) {
        // 根据配置中的区域信息获取对应的外部缓存构建器
        ExternalCacheBuilder cacheBuilder = (ExternalCacheBuilder) cacheBuilderTemplate
                .getCacheBuilder(1, config.getArea());
        // 如果缓存构建器为空，则抛出异常，表明没有找到指定区域的缓存构建器
        if (cacheBuilder == null) {
            throw new CacheConfigException("no remote cache builder: " + config.getArea());
        }

        // 如果配置了过期时间且大于0，则设置缓存写入后的过期时间
        if (config.getExpire() != null && config.getExpire().toMillis() > 0) {
            cacheBuilder.expireAfterWrite(config.getExpire().toMillis(), TimeUnit.MILLISECONDS);
        }

        // 根据配置决定缓存键的前缀
        String prefix;
        if (config.getUseAreaInPrefix() != null && config.getUseAreaInPrefix()) {
            // 如果配置中指定了需要在前缀中包含区域信息，则将区域和缓存名称作为前缀
            prefix = config.getArea() + "_" + config.getName();
        } else {
            // 否则，仅使用缓存名称作为前缀
            prefix = config.getName();
        }
        // 设置缓存键的前缀，如果缓存构建器的配置中已提供了前缀供应商，则进行特定处理
        if (cacheBuilder.getConfig().getKeyPrefixSupplier() != null) {
            Supplier<String> supplier = cacheBuilder.getConfig().getKeyPrefixSupplier();
            cacheBuilder.setKeyPrefixSupplier(() -> supplier.get() + prefix);
        } else {
            cacheBuilder.setKeyPrefix(prefix);
        }

        // 根据配置，设置缓存的键转换器、值编码器和值解码器
        if (config.getKeyConvertor() != null) {
            cacheBuilder.getConfig().setKeyConvertor(config.getKeyConvertor());
        }
        if (config.getValueEncoder() != null) {
            cacheBuilder.getConfig().setValueEncoder(config.getValueEncoder());
        }
        if (config.getValueDecoder() != null) {
            cacheBuilder.getConfig().setValueDecoder(config.getValueDecoder());
        }

        // 设置是否缓存空值，默认为true
        cacheBuilder.setCacheNullValue(config.getCacheNullValue() != null ?
                config.getCacheNullValue() : DEFAULT_CACHE_NULL_VALUE);
        // 使用当前配置构建缓存实例并返回
        return cacheBuilder.buildCache();
    }


    /**
     * 构建本地缓存
     *
     * @param config 快速配置对象，包含了缓存的配置信息
     * @return 返回构建的本地缓存实例
     * @throws CacheConfigException 如果找不到本地缓存构建器，则抛出此异常
     */
    private <K, V> JetCache<K, V> buildLocal(QuickConfig config) {
        // 根据配置的区域获取缓存构建器模板
        EmbeddedCacheBuilder cacheBuilder = (EmbeddedCacheBuilder) cacheBuilderTemplate.getCacheBuilder(
                0, config.getArea()
        );

        // 如果缓存构建器为null，抛出异常
        Assert.notNull(cacheBuilder, () -> new CacheConfigException("no local cache builder: " + config.getArea()));

        // 如果配置了本地缓存限制，则设置缓存限制
        if (config.getLocalLimit() != null && config.getLocalLimit() > 0) {
            cacheBuilder.setLimit(config.getLocalLimit());
        }
        // 如果缓存类型为 BOTH，并且配置了本地缓存过期时间，则设置写入后过期时间
        if (config.getCacheType() == CacheType.BOTH &&
                config.getLocalExpire() != null &&
                config.getLocalExpire().toMillis() > 0
        ) {
            cacheBuilder.expireAfterWrite(config.getLocalExpire().toMillis(), TimeUnit.MILLISECONDS);
        } else if (config.getExpire() != null && config.getExpire().toMillis() > 0) {
            // 否则，如果配置了全局缓存过期时间，则设置写入后过期时间
            cacheBuilder.expireAfterWrite(config.getExpire().toMillis(), TimeUnit.MILLISECONDS);
        }
        // 如果配置了键转换器，则设置键转换器
        if (config.getKeyConvertor() != null) {
            cacheBuilder.getConfig().setKeyConvertor(config.getKeyConvertor());
        }
        // 设置缓存空值，默认为 NULL_STRING
        cacheBuilder.setCacheNullValue(
                config.getCacheNullValue() != null ?
                        config.getCacheNullValue() : DEFAULT_CACHE_NULL_VALUE
        );
        // 构建并返回缓存实例
        return cacheBuilder.buildCache();
    }

}
