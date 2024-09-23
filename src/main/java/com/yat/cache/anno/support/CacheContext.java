package com.yat.cache.anno.support;

import com.yat.cache.anno.api.CacheConsts;
import com.yat.cache.anno.api.EnableCache;
import com.yat.cache.anno.method.CacheInvokeContext;
import com.yat.cache.core.Cache;
import com.yat.cache.core.CacheManager;
import com.yat.cache.core.exception.CacheConfigException;
import com.yat.cache.core.template.QuickConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * ClassName CacheContext
 * <p>Description 缓存上下文</p>
 *
 * @author Yat
 * Date 2024/8/22 22:01
 * version 1.0
 */
public class CacheContext {

    private static final Logger logger = LoggerFactory.getLogger(CacheContext.class);

    private static final ThreadLocal<CacheThreadLocal> cacheThreadLocal =
            ThreadLocal.withInitial(CacheThreadLocal::new);
    /**
     * 配置提供者，用于获取缓存配置信息。
     */
    private final ConfigProvider configProvider;
    /**
     * 全局缓存配置，包含整个应用的缓存配置细节。
     */
    private final GlobalCacheConfig globalCacheConfig;
    /**
     * 缓存管理器，负责缓存的创建和管理。
     */
    private final CacheManager cacheManager;

    public CacheContext(CacheManager cacheManager, ConfigProvider configProvider, GlobalCacheConfig globalCacheConfig) {
        this.cacheManager = cacheManager;
        this.globalCacheConfig = globalCacheConfig;
        this.configProvider = configProvider;
    }

    /**
     * 创建一个缓存调用上下文对象
     * 此方法用于封装缓存的调用逻辑，提供一个上下文对象，该对象中包含了如何使用给定的配置映射来调用缓存函数的逻辑
     *
     * @param configMap 配置映射对象，包含调用缓存函数所需的配置信息
     * @return 返回一个 {@link CacheInvokeContext} 对象，该对象封装了特定配置映射下的缓存调用逻辑
     */
    public CacheInvokeContext createCacheInvokeContext(ConfigMap configMap) {
        // 创建一个新的缓存调用上下文实例
        CacheInvokeContext c = newCacheInvokeContext();

        // 设置缓存函数，定义如何使用配置映射来调用缓存
        c.setCacheFunction((cic, cac) -> createOrGetCache(cic, cac, configMap));

        // 返回配置好的缓存调用上下文对象
        return c;
    }

    protected CacheInvokeContext newCacheInvokeContext() {
        return new CacheInvokeContext();
    }

    /**
     * 创建或获取缓存实例
     *
     * @param invokeContext   调用上下文，用于在创建缓存时传递必要的上下文信息
     * @param cacheAnnoConfig 缓存注解配置，包含了缓存的配置信息
     * @param configMap       配置映射，用于查找缓存的定义
     * @return 返回缓存实例，如果找不到定义则返回null
     */
    private Cache createOrGetCache(
            CacheInvokeContext invokeContext, CacheAnnoConfig cacheAnnoConfig, ConfigMap configMap
    ) {
        // 从缓存注解配置中获取缓存实例
        Cache cache = cacheAnnoConfig.getCache();
        if (cache != null) {
            // 如果已经存在缓存实例，则直接返回
            return cache;
        }

        // 判断缓存注解配置的类型
        if (cacheAnnoConfig instanceof CachedAnnoConfig) {
            // 如果是 Cached 注解的配置，则根据 Cached 配置创建缓存实例
            cache = createCacheByCachedConfig((CachedAnnoConfig) cacheAnnoConfig, invokeContext);
        } else if (cacheAnnoConfig instanceof CacheInvalidateAnnoConfig || cacheAnnoConfig instanceof CacheUpdateAnnoConfig) {
            // 如果是 CacheInvalidated 或 CacheUpdate 注解的配置，则从缓存管理器中获取缓存实例
            cache = cacheManager.getCache(cacheAnnoConfig.getArea(), cacheAnnoConfig.getName());
            if (cache == null) {
                // 如果缓存管理器中没有找到缓存，则从配置映射中查找缓存定义
                CachedAnnoConfig cac = configMap.getByCacheName(cacheAnnoConfig.getArea(), cacheAnnoConfig.getName());
                if (cac == null) {
                    // 如果找不到缓存定义，则抛出异常
                    String message = "can't find cache definition with area=" + cacheAnnoConfig.getArea() +
                            " name=" + cacheAnnoConfig.getName() +
                            ", specified in " + cacheAnnoConfig.getDefineMethod();
                    CacheConfigException e = new CacheConfigException(message);
                    logger.error("Cache operation aborted because can't find cached definition", e);
                    return null;
                }
                // 找到缓存定义后，根据 Cached 配置创建缓存实例
                cache = createCacheByCachedConfig(cac, invokeContext);
            }
        }

        // 将创建的缓存实例设置到缓存注解配置中，以便后续使用
        cacheAnnoConfig.setCache(cache);
        return cache;
    }

    /**
     * 根据缓存配置对象创建或获取缓存实例
     * 当缓存名未定义时，会根据方法和目标对象生成缓存名
     *
     * @param ac            缓存配置对象，包含了缓存的区域和名称等信息
     * @param invokeContext 缓存调用上下文，包含了隐藏包和方法等信息
     * @return 返回创建或获取的缓存实例
     */
    private Cache createCacheByCachedConfig(CachedAnnoConfig ac, CacheInvokeContext invokeContext) {
        // 获取缓存区域信息
        String area = ac.getArea();
        // 获取缓存名，检查是否定义
        String cacheName = ac.getName();
        // 如果缓存名未定义，则生成缓存名
        if (CacheConsts.isUndefined(cacheName)) {
            // 根据配置提供者和调用上下文信息生成缓存名
            cacheName = configProvider.createCacheNameGenerator(invokeContext.getHiddenPackages())
                    .generateCacheName(invokeContext.getMethod(), invokeContext.getTargetObject());
        }
        // 创建或获取缓存实例并返回
        return __createOrGetCache(ac, area, cacheName);
    }

    /**
     * 创建或获取缓存对象
     * 根据缓存注解配置（cac）和指定的缓存区域及名称，构建并配置缓存
     *
     * @param cac       缓存注解配置，包含缓存的详细配置信息
     * @param area      缓存区域
     * @param cacheName 缓存名称
     * @return 配置好的缓存对象
     */
    public Cache __createOrGetCache(CachedAnnoConfig cac, String area, String cacheName) {
        // 新建缓存配置构建器，指定缓存区域和缓存名称
        QuickConfig.Builder b = QuickConfig.newBuilder(area, cacheName);

        // 设置缓存过期时间
        TimeUnit timeUnit = cac.getTimeUnit();
        if (cac.getExpire() > 0) {
            b.expire(Duration.ofMillis(timeUnit.toMillis(cac.getExpire())));
        }

        // 设置本地缓存过期时间
        if (cac.getLocalExpire() > 0) {
            b.localExpire(Duration.ofMillis(timeUnit.toMillis(cac.getLocalExpire())));
        }

        // 设置本地缓存容量限制
        if (cac.getLocalLimit() > 0) {
            b.localLimit(cac.getLocalLimit());
        }

        // 设置缓存类型
        b.cacheType(cac.getCacheType());

        // 设置是否同步到本地缓存
        b.syncLocal(cac.isSyncLocal());

        // 设置键转换器
        if (!CacheConsts.isUndefined(cac.getKeyConvertor())) {
            b.keyConvertor(configProvider.parseKeyConvertor(cac.getKeyConvertor()));
        }

        // 设置值的序列化策略
        if (!CacheConsts.isUndefined(cac.getSerialPolicy())) {
            b.valueEncoder(configProvider.parseValueEncoder(cac.getSerialPolicy()));
            b.valueDecoder(configProvider.parseValueDecoder(cac.getSerialPolicy()));
        }

        // 设置是否缓存空值
        b.cacheNullValue(cac.isCacheNullValue());

        // 设置是否在缓存名称中包含区域信息
        b.useAreaInPrefix(globalCacheConfig.getAreaInCacheName());

        // 配置穿透保护
        PenetrationProtectConfig ppc = cac.getPenetrationProtectConfig();
        if (ppc != null) {
            b.penetrationProtect(ppc.isPenetrationProtect());
            b.penetrationProtectTimeout(ppc.getPenetrationProtectTimeout());
        }

        // 设置刷新策略
        b.refreshPolicy(cac.getRefreshPolicy());

        // 根据配置构建并获取缓存
        return cacheManager.getOrCreateCache(b.build());
    }


    /**
     * 在当前线程中启用缓存，适用于@Cached(enabled=false)场景。
     * 此方法通过在线程局部变量中增加启用计数来临时允许缓存操作。
     * 一旦操作完成，通过finally块确保计数器被正确减少，从而避免影响其他代码块的缓存行为。
     *
     * @param callback 一个 Supplier 接口，用于执行需要临时启用缓存的代码。
     * @return 执行 Supplier 接口中的代码所返回的结果。
     * @see EnableCache
     */
    public static <T> T enableCache(Supplier<T> callback) {
        // 从线程局部变量中获取缓存状态对象
        CacheThreadLocal var = cacheThreadLocal.get();
        try {
            // 增加启用缓存计数，临时允许缓存操作
            var.setEnabledCount(var.getEnabledCount() + 1);
            // 执行用户提供的 Supplier 接口中的代码，并返回结果
            return callback.get();
        } finally {
            // 确保在执行结束后减少启用缓存计数，恢复之前的状态
            var.setEnabledCount(var.getEnabledCount() - 1);
        }
    }


    /**
     * 在当前线程中启用缓存
     * 此方法通过增加启用计数器，来跟踪当前线程中缓存的启用状态
     */
    protected static void enable() {
        // 获取当前线程的缓存上下文
        CacheThreadLocal var = cacheThreadLocal.get();
        // 将启用计数器加1，表示当前线程中缓存已被启用
        var.setEnabledCount(var.getEnabledCount() + 1);
    }


    /**
     * 禁用缓存功能
     * <p>
     * 说明：
     * 此方法用于禁用线程本地缓存的功能它通过减少启用计数器来实现禁用效果
     * 当启用计数器为零时，表示缓存功能已被完全禁用
     */
    protected static void disable() {
        // 获取当前线程的缓存对象
        CacheThreadLocal var = cacheThreadLocal.get();
        // 将启用计数器减一，以表示减少缓存的启用状态
        var.setEnabledCount(var.getEnabledCount() - 1);
    }


    /**
     * 判断缓存功能是否启用
     *
     * @return 如果缓存功能启用，则返回true；否则返回false
     * 通过检查线程本地存储中的缓存状态来确定缓存功能是否启用，
     * 具体来说，如果已启用的缓存计数大于0，则认为缓存功能启用
     */
    protected static boolean isEnabled() {
        return cacheThreadLocal.get().getEnabledCount() > 0;
    }


}
