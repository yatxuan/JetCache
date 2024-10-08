package com.yat.cache.anno.method;

import com.yat.cache.anno.api.DefaultCacheConstant;
import com.yat.cache.anno.api.EnableJetCache;
import com.yat.cache.anno.api.JetCacheInvalidate;
import com.yat.cache.anno.api.JetCacheInvalidateContainer;
import com.yat.cache.anno.api.JetCachePenetrationProtect;
import com.yat.cache.anno.api.JetCacheRefresh;
import com.yat.cache.anno.api.JetCacheUpdate;
import com.yat.cache.anno.api.JetCached;
import com.yat.cache.anno.support.CacheInvalidateAnnoConfig;
import com.yat.cache.anno.support.CacheUpdateAnnoConfig;
import com.yat.cache.anno.support.CachedAnnoConfig;
import com.yat.cache.anno.support.PenetrationProtectConfig;
import com.yat.cache.core.RefreshPolicy;
import com.yat.cache.core.exception.CacheConfigException;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ClassName CacheConfigUtil
 * <p>Description 缓存配置工具</p>
 *
 * @author Yat
 * Date 2024/8/22 21:37
 * version 1.0
 */
public class CacheConfigUtil {

    /**
     * 综合解析方法上的缓存相关注解，并配置到 CacheInvokeConfig 对象中
     *
     * @param cac    CacheInvokeConfig 对象，用于存储解析后的配置
     * @param method 需要解析的方法对象
     * @return 如果方法上有缓存相关注解，则返回 true，否则返回 false
     */
    public static boolean parse(CacheInvokeConfig cac, Method method) {
        boolean hasAnnotation = false;

        // 解析 Cached 注解
        CachedAnnoConfig cachedConfig = parseCached(method);
        if (cachedConfig != null) {
            cac.setCachedAnnoConfig(cachedConfig);
            hasAnnotation = true;
        }

        // 解析 EnableCache 注解
        boolean enable = parseEnableCache(method);
        if (enable) {
            cac.setEnableCacheContext(true);
            hasAnnotation = true;
        }

        // 解析 CacheInvalidate 和 CacheInvalidateContainer 注解
        List<CacheInvalidateAnnoConfig> invalidateAnnoConfigs = parseCacheInvalidates(method);
        if (invalidateAnnoConfigs != null) {
            cac.setInvalidateAnnoConfigs(invalidateAnnoConfigs);
            hasAnnotation = true;
        }

        // 解析 CacheUpdate 注解
        CacheUpdateAnnoConfig updateAnnoConfig = parseCacheUpdate(method);
        if (updateAnnoConfig != null) {
            cac.setUpdateAnnoConfig(updateAnnoConfig);
            hasAnnotation = true;
        }

        // 检查是否存在冲突的注解组合
        if (cachedConfig != null && (invalidateAnnoConfigs != null || updateAnnoConfig != null)) {
            throw new CacheConfigException("@Cached can't coexists with @CacheInvalidate or @CacheUpdate: " + method);
        }

        return hasAnnotation;
    }

    private static CachedAnnoConfig parseCached(Method m) {
        JetCached anno = m.getAnnotation(JetCached.class);
        if (anno == null) {
            return null;
        }
        CachedAnnoConfig cc = new CachedAnnoConfig();
        cc.setArea(anno.area());
        cc.setName(anno.name());
        cc.setCacheType(anno.cacheType());
        cc.setSyncLocal(anno.syncLocal());
        cc.setEnabled(anno.enabled());
        cc.setTimeUnit(anno.timeUnit());
        cc.setExpire(anno.expire());
        cc.setLocalExpire(anno.localExpire());
        cc.setLocalLimit(anno.localLimit());
        cc.setCacheNullValue(anno.cacheNullValue());
        cc.setCondition(anno.condition());
        cc.setPostCondition(anno.postCondition());
        // 将缓存注释配置的序列化策略设置到缓存的配置中
        cc.setSerialPolicy(anno.serialPolicy());
        // 设置键转换器
        cc.setKeyConvertor(anno.keyConvertor());
        // 设置缓存键
        cc.setKey(anno.key());
        // 关联当前方法作为定义方法
        cc.setDefineMethod(m);

        JetCacheRefresh jetCacheRefresh = m.getAnnotation(JetCacheRefresh.class);
        if (jetCacheRefresh != null) {
            // 解析刷新策略并设置到缓存的配置中
            RefreshPolicy policy = parseRefreshPolicy(jetCacheRefresh);
            cc.setRefreshPolicy(policy);
        }

        JetCachePenetrationProtect protectAnno = m.getAnnotation(JetCachePenetrationProtect.class);
        if (protectAnno != null) {
            // 解析穿透保护配置并设置到缓存的配置中
            PenetrationProtectConfig protectConfig = parsePenetrationProtectConfig(protectAnno);
            cc.setPenetrationProtectConfig(protectConfig);
        }

        return cc;
    }

    /**
     * 解析方法上是否标注了 EnableCache 注解
     *
     * @param m 需要解析的方法对象
     * @return 如果存在 EnableCache 注解则返回 true，否则返回 false
     */
    private static boolean parseEnableCache(Method m) {
        EnableJetCache anno = m.getAnnotation(EnableJetCache.class);
        return anno != null;
    }

    /**
     * 解析方法上的 CacheInvalidate 注解配置
     * 该方法用于处理单个 CacheInvalidate 注解或 CacheInvalidateContainer 注解集合
     * 它首先尝试查找方法上的单个 CacheInvalidate 注解，如果存在，则处理该注解
     * 如果没有找到单个 CacheInvalidate 注解，则尝试查找 CacheInvalidateContainer 注解集合，并处理其中的每个注解
     *
     * @param m 要解析其缓存失效注解的Method对象
     * @return 包含解析出的 CacheInvalidateAnnoConfig 列表如果没有注解或解析失败，则返回 null
     */
    public static List<CacheInvalidateAnnoConfig> parseCacheInvalidates(Method m) {
        List<CacheInvalidateAnnoConfig> annoList = null;

        // 尝试获取单个 CacheInvalidate 注解
        JetCacheInvalidate ci = m.getAnnotation(JetCacheInvalidate.class);
        if (ci != null) {
            annoList = new ArrayList<>(1);
            annoList.add(createCacheInvalidateAnnoConfig(ci, m));
        } else {
            // 如果没有单个注解，则尝试获取 CacheInvalidateContainer 注解集合
            JetCacheInvalidateContainer cic = m.getAnnotation(JetCacheInvalidateContainer.class);
            if (cic != null) {
                JetCacheInvalidate[] jetCacheInvalidates = cic.value();
                annoList = new ArrayList<>(jetCacheInvalidates.length);
                for (JetCacheInvalidate jetCacheInvalidate : jetCacheInvalidates) {
                    annoList.add(createCacheInvalidateAnnoConfig(jetCacheInvalidate, m));
                }
            }
        }
        return annoList;
    }

    /**
     * 解析缓存更新注解配置
     * 本方法用于解析方法上的@CacheUpdate注解，提取并验证注解的相关信息
     * 如果方法上没有@CacheUpdate注解，或者注解的必要属性为空，则会返回null或抛出异常
     *
     * @param m 被解析的方法Method对象
     * @return CacheUpdateAnnoConfig对象，包含缓存更新注解的配置信息；如果方法上没有该注解或必要信息为空，则可能返回null
     * @throws CacheConfigException 如果@CacheUpdate注解的name或value属性为空，则抛出此异常
     */
    private static CacheUpdateAnnoConfig parseCacheUpdate(Method m) throws CacheConfigException {
        // 获取方法上的@CacheUpdate注解
        JetCacheUpdate anno = m.getAnnotation(JetCacheUpdate.class);
        // 如果方法上没有@CacheUpdate注解，则返回null
        if (anno == null) {
            return null;
        }

        // 创建并初始化CacheUpdateAnnoConfig对象以存储解析的注解信息
        CacheUpdateAnnoConfig cc = new CacheUpdateAnnoConfig();
        cc.setArea(anno.area());
        cc.setName(anno.name());

        // 验证@CacheUpdate注解的name属性是否为空，如果为空，则抛出异常
        if (cc.getName() == null || cc.getName().trim().isEmpty()) {
            throw new CacheConfigException("name is required for @CacheUpdate: " + m.getClass().getName() + "." + m.getName());
        }

        // 设置@CacheUpdate注解的key和value属性
        cc.setKey(anno.key());
        cc.setValue(anno.value());

        // 验证@CacheUpdate注解的value属性是否为空，如果为空，则抛出异常
        if (cc.getValue() == null || cc.getValue().trim().isEmpty()) {
            throw new CacheConfigException("value is required for @CacheUpdate: " + m.getClass().getName() + "." + m.getName());
        }

        // 设置@CacheUpdate注解的condition和multi属性，并关联到定义该注解的方法
        cc.setCondition(anno.condition());
        cc.setMulti(anno.multi());
        cc.setDefineMethod(m);

        // 返回包含注解配置信息的CacheUpdateAnnoConfig对象
        return cc;
    }

    /**
     * 解析缓存刷新策略
     *
     * @param jetCacheRefresh 缓存刷新配置对象，包含刷新相关的时间设置和单位
     * @return RefreshPolicy对象，表示解析后的刷新策略，包括刷新间隔、停止刷新时间等
     */
    public static RefreshPolicy parseRefreshPolicy(JetCacheRefresh jetCacheRefresh) {
        // 创建一个刷新策略对象来存储解析后的刷新策略
        RefreshPolicy policy = new RefreshPolicy();

        // 获取时间单位，用于时间值的转换
        TimeUnit t = jetCacheRefresh.timeUnit();

        // 设置刷新间隔时间，将根据时间单位转换刷新时间到毫秒
        policy.setRefreshMillis(t.toMillis(jetCacheRefresh.refresh()));

        // 如果停止刷新时间（自最后访问后）明确且不为默认的未定义值
        if (DefaultCacheConstant.isNotUndefined(jetCacheRefresh.stopRefreshAfterLastAccess())) {
            // 将停止刷新时间转换为毫秒并设置到策略对象中
            policy.setStopRefreshAfterLastAccessMillis(t.toMillis(jetCacheRefresh.stopRefreshAfterLastAccess()));
        }

        // 如果刷新锁超时时间明确且不为默认的未定义值
        if (DefaultCacheConstant.isNotUndefined(jetCacheRefresh.refreshLockTimeout())) {
            // 将刷新锁超时时间转换为毫秒并设置到策略对象中
            policy.setRefreshLockTimeoutMillis(t.toMillis(jetCacheRefresh.refreshLockTimeout()));
        }

        // 返回解析后的刷新策略对象
        return policy;
    }

    /**
     * 从缓存穿透保护注解中解析出穿透保护配置
     * <p>
     * 此方法负责将注解 {@link JetCachePenetrationProtect} 中的信息转换为 {@link PenetrationProtectConfig} 对象
     * 它提取了注解中的 value 和 timeout 信息，为穿透保护配置提供必要的参数
     *
     * @param protectAnno 缓存穿透保护注解，包含配置参数
     * @return PenetrationProtectConfig对象，用于在缓存操作中应用穿透保护
     */
    public static PenetrationProtectConfig parsePenetrationProtectConfig(JetCachePenetrationProtect protectAnno) {
        // 创建一个新的穿透保护配置实例
        PenetrationProtectConfig protectConfig = new PenetrationProtectConfig();
        // 设置是否启用穿透保护
        protectConfig.setPenetrationProtect(protectAnno.value());

        // 如果注解中的 timeout 不是未定义，则计算timeout的毫秒值
        if (DefaultCacheConstant.isNotUndefined(protectAnno.timeout())) {
            // 根据注解中指定的时间单位和时间值，转换为毫秒
            long timeout = protectAnno.timeUnit().toMillis(protectAnno.timeout());
            // 设置穿透保护的超时时间
            protectConfig.setPenetrationProtectTimeout(Duration.ofMillis(timeout));
        }
        // 返回构建好的穿透保护配置
        return protectConfig;
    }

    /**
     * 根据注解和方法信息创建缓存失效配置对象
     * 该方法负责解析方法上的@CacheInvalidate注解，并生成相应的配置对象
     *
     * @param anno 方法上的@CacheInvalidate注解
     * @param m    带有@CacheInvalidate注解的方法
     * @return 解析后的缓存失效配置对象
     * @throws CacheConfigException 如果注解的name属性为空或字符串为空，则抛出此异常
     */
    private static CacheInvalidateAnnoConfig createCacheInvalidateAnnoConfig(JetCacheInvalidate anno, Method m) {
        // 创建一个新的缓存失效配置对象
        CacheInvalidateAnnoConfig cc = new CacheInvalidateAnnoConfig();

        // 设置缓存的区域信息
        cc.setArea(anno.area());
        // 设置缓存的名称
        cc.setName(anno.name());

        // 检查缓存名称是否为空或空字符串，如果是，则抛出异常
        if (cc.getName() == null || cc.getName().trim().isEmpty()) {
            throw new CacheConfigException("name is required for @CacheInvalidate: " + m.getClass().getName() + "." + m.getName());
        }

        // 设置缓存的键
        cc.setKey(anno.key());
        // 设置缓存失效的条件
        cc.setCondition(anno.condition());
        // 设置是否支持多值失效
        cc.setMulti(anno.multi());
        // 设置定义该缓存失效行为的方法
        cc.setDefineMethod(m);

        // 返回解析后的缓存失效配置对象
        return cc;
    }
}
